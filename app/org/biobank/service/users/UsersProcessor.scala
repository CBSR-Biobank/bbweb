package org.biobank.service.users

import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.TestData

import akka.actor._
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object UsersProcessor {

  def props = Props[UsersProcessor]

}

/**
 * Handles the commands to configure users.
 */
class UsersProcessor @javax.inject.Inject() (val userRepository: UserRepository,
                                             val passwordHasher: PasswordHasher,
                                             val emailService:   EmailService,
                                             val testData:       TestData)
    extends Processor {

  import UserEvent.EventType

  case class PasswordInfo(password: String, salt: String)

  case class SnapshotState(users: Set[User])

  override def persistenceId = "user-processor-id"

  def encryptPassword(user: ActiveUser, newPlainPassword: String): PasswordInfo = {
    val newSalt = passwordHasher.generateSalt
    val newPwd = passwordHasher.encrypt(newPlainPassword, newSalt)
    PasswordInfo(newPwd, newSalt)
  }

  val receiveRecover: Receive = {
    case event: UserEvent => event.eventType match {
      case et: EventType.Registered       => applyUserRegisteredEvent(event)
      case et: EventType.Activated        => applyUserActivatedEvent(event)
      case et: EventType.NameUpdated      => applyUserNameUpdatedEvent(event)
      case et: EventType.EmailUpdated     => applyUserEmailUpdatedEvent(event)
      case et: EventType.PasswordUpdated  => applyUserPasswordUpdatedEvent(event)
      case et: EventType.AvatarUrlUpdated => applyUserAvatarUrlUpdatedEvent(event)
      case et: EventType.Locked           => applyUserLockedEvent(event)
      case et: EventType.Unlocked         => applyUserUnlockedEvent(event)
      case et: EventType.PasswordReset    => applyUserPasswordResetEvent(event)

      case _ => log.error(s"user event not handled: $event")
    }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.users.foreach(i => userRepository.put(i))

    case event: RecoveryCompleted =>

    case event => log.error(s"event not handled: $event")
  }

  val receiveCommand: Receive = {
    case cmd: RegisterUserCmd        => processRegisterUserCmd(cmd)
    case cmd: ActivateUserCmd        => processActivateUserCmd(cmd)
    case cmd: UpdateUserNameCmd      => processUpdateUserNameCmd(cmd)
    case cmd: UpdateUserEmailCmd     => processUpdateUserEmailCmd(cmd)
    case cmd: UpdateUserPasswordCmd  => processUpdateUserPasswordCmd(cmd)
    case cmd: UpdateUserAvatarUrlCmd => processUpdateUserAvatarUrlCmd(cmd)
    case cmd: ResetUserPasswordCmd   => processResetUserPasswordCmd(cmd)
    case cmd: LockUserCmd            => processLockUserCmd(cmd)
    case cmd: UnlockUserCmd          => processUnlockUserCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(userRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"UsersProcessor: message not handled: $cmd")
  }

  def processRegisterUserCmd(cmd: RegisterUserCmd): Unit = {
    val newUserId = userRepository.nextIdentity

    if (userRepository.getByKey(newUserId).isSuccess) {
      log.error(s"user with id already exsits: $newUserId")
    }

    val salt = passwordHasher.generateSalt
    val encryptedPwd = passwordHasher.encrypt(cmd.password, salt)

    val event = for {
      emailAvailable <- emailAvailable(cmd.email)
      user <- RegisteredUser.create(newUserId, -1L,
                                    cmd.name,
                                    cmd.email,
                                    encryptedPwd,
                                    salt,
                                    cmd.avatarUrl)
      event <- createUserEvent(user.id, cmd).withRegistered(
        UserRegisteredEvent(name      = Some(user.name),
                            email     = Some(user.email),
                            password  = Some(encryptedPwd),
                            salt      = Some(salt),
                            avatarUrl = user.avatarUrl)).success
    } yield event

    process(event) { applyUserRegisteredEvent(_) }
  }

  def processActivateUserCmd(cmd: ActivateUserCmd): Unit = {
    val v = updateRegistered(cmd) { u =>
      for {
        user <- u.activate
        event <- createUserEvent(user.id, cmd).withActivated(
          UserActivatedEvent(version = Some(user.version))).success
      } yield event
    }

    process(v) { applyUserActivatedEvent(_) }
  }

  def processUpdateUserNameCmd(cmd: UpdateUserNameCmd): Unit = {
    val v = updateActive(cmd) { u =>
      for {
        user  <- u.withName(cmd.name)
        event <- createUserEvent(user.id, cmd).withNameUpdated(
          UserNameUpdatedEvent(
            version = Some(user.version),
            name    = Some(user.name))).success
      } yield event
    }

    process(v) { applyUserNameUpdatedEvent(_) }
  }

  def processUpdateUserEmailCmd(cmd: UpdateUserEmailCmd): Unit = {
    val v = updateActive(cmd) { user =>
      for {
        emailAvailable <- emailAvailable(cmd.email, user.id)
        updatedUser    <- user.withEmail(cmd.email)
        event          <- createUserEvent(user.id, cmd).withEmailUpdated(
        UserEmailUpdatedEvent(version = Some(updatedUser.version),
                              email   = Some(updatedUser.email))).success
      } yield event
    }

    process(v) { applyUserEmailUpdatedEvent(_) }
  }

  def processUpdateUserPasswordCmd(cmd: UpdateUserPasswordCmd): Unit = {
    val v = updateActive(cmd) { u =>
      for {
        user <- {
          if (passwordHasher.valid(u.password, u.salt, cmd.currentPassword)) {
            val passwordInfo = encryptPassword(u, cmd.newPassword)
            u.withPassword(passwordInfo.password, passwordInfo.salt)
          } else {
            DomainError("invalid password").failureNel
          }
        }
        event <- createUserEvent(user.id, cmd).withPasswordUpdated(
        UserPasswordUpdatedEvent(version  = Some(user.version),
                                 password = Some(user.password),
                                 salt     = Some(user.salt))).success
      } yield event
    }

    process(v) { applyUserPasswordUpdatedEvent(_) }
  }

  def processUpdateUserAvatarUrlCmd(cmd: UpdateUserAvatarUrlCmd): Unit = {
    val v = updateActive(cmd) { u =>
      for {
        user  <- u.withAvatarUrl(cmd.avatarUrl)
        event <- createUserEvent(user.id, cmd).withAvatarUrlUpdated(
        UserAvatarUrlUpdatedEvent(
          version   = Some(user.version),
          avatarUrl = user.avatarUrl)).success
      } yield event
    }

    process(v) { applyUserAvatarUrlUpdatedEvent(_) }
  }

  // only active users can request a password reset
  def processResetUserPasswordCmd(cmd: ResetUserPasswordCmd): Unit = {
    val event = userRepository.getByEmail(cmd.email).fold(
      err => err.failure[UserEvent],
      user => {
        user match {
          case activeUser: ActiveUser => {
            val plainPassword = Utils.randomString(8)
            val passwordInfo = encryptPassword(activeUser, plainPassword)

            activeUser.withPassword(passwordInfo.password, passwordInfo.salt).fold(
              err => err.failure[UserEvent],
              updatedUser => {
                emailService.passwordResetEmail(user.email, plainPassword)
                createUserEvent(updatedUser.id, cmd).withPasswordReset(
                  UserPasswordResetEvent(version  = Some(updatedUser.version),
                                         password = Some(updatedUser.password),
                                         salt     = Some(updatedUser.salt))).success
              }
            )
          }

          case user =>
            DomainError(s"user is not active: $cmd").failureNel[UserEvent]
        }
      }
    )

    process(event) { applyUserPasswordResetEvent(_) }
  }

  def processLockUserCmd(cmd: LockUserCmd): Unit = {
    val v = updateActive(cmd) { u =>
      for {
        user  <- u.lock()
        event <- createUserEvent(user.id, cmd).withLocked(
          UserLockedEvent(version = Some(user.version))).success
      } yield event
    }

    process(v) { applyUserLockedEvent(_) }
  }

  def processUnlockUserCmd(cmd: UnlockUserCmd): Unit = {
    val v = updateLocked(cmd) { u =>
      for {
        user  <- u.unlock
        event <- createUserEvent(user.id, cmd).withUnlocked(
          UserUnlockedEvent(version = Some(user.version))).success
      } yield event
    }

    process(v) { applyUserUnlockedEvent(_) }
  }

  def updateUser[T <: User]
    (cmd: UserModifyCommand)(fn: User => DomainValidation[UserEvent])
      : DomainValidation[UserEvent] = {
    for {
      user         <- userRepository.getByKey(UserId(cmd.id))
      validVersion <- user.requireVersion(cmd.expectedVersion)
      updatedUser  <- fn(user)
    } yield updatedUser
  }

  def updateRegistered[T <: User]
    (cmd: UserModifyCommand)(fn: RegisteredUser => DomainValidation[UserEvent])
      : DomainValidation[UserEvent] = {
    updateUser(cmd) {
      case user: RegisteredUser => fn(user)
      case user => s"user is not registered: ${cmd.id}".failureNel
    }
  }

  def updateActive[T <: User]
    (cmd: UserModifyCommand)(fn: ActiveUser => DomainValidation[UserEvent])
      : DomainValidation[UserEvent] = {
    updateUser(cmd) {
      case user: ActiveUser => fn(user)
      case user => s"user is not active: ${cmd.id}".failureNel
    }
  }

  def updateLocked[T <: User]
    (cmd: UserModifyCommand)(fn: LockedUser => DomainValidation[UserEvent])
      : DomainValidation[UserEvent] = {
    updateUser(cmd) {
      case user: LockedUser => fn(user)
      case user => s"user is not locked: ${cmd.id}".failureNel
    }
  }

  def applyUserRegisteredEvent(event: UserEvent): Unit = {
    log.debug(s"recoverEvent: $event")

    if (event.eventType.isRegistered) {
      val registeredEvent = event.getRegistered

      userRepository.put(
        RegisteredUser(id           = UserId(event.id),
                       version      = 0L,
                       timeAdded    = ISODateTimeFormat.dateTime.parseDateTime(event.getTime),
                       timeModified = None,
                       name         = registeredEvent.getName,
                       email        = registeredEvent.getEmail,
                       password     = registeredEvent.getPassword,
                       salt         = registeredEvent.getSalt,
                       avatarUrl    = registeredEvent.avatarUrl))
      ()
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  def applyUserActivatedEvent(event: UserEvent): Unit = {
    log.debug(s"recoverEvent: $event")

    if (event.eventType.isActivated) {
      userRepository.getRegistered(UserId(event.id)).fold(
        err => log.error(s"activating user from event failed: $err"),
        u => {
          userRepository.put(ActiveUser(
                               id           = u.id,
                               version      = event.getActivated.getVersion,
                               timeAdded    = u.timeAdded,
                               timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
                               name         = u.name,
                               email        = u.email,
                               password     = u.password,
                               salt         = u.salt,
                               avatarUrl    = u.avatarUrl))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  def applyUserNameUpdatedEvent(event: UserEvent): Unit = {
    log.debug(s"recoverEvent: $event")

    if (event.eventType.isNameUpdated) {
      val nameUpdatedEvent = event.getNameUpdated

      userRepository.getActive(UserId(event.id)).fold(
        err => log.error(s"updating user name from event failed: $err"),
        u => {
          userRepository.put(u.copy(
                               version      = nameUpdatedEvent.getVersion,
                               name         = nameUpdatedEvent.getName,
                               timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  def applyUserEmailUpdatedEvent(event: UserEvent): Unit = {
    log.debug(s"recoverEvent: $event")

    if (event.eventType.isEmailUpdated) {
      val emailUpdatedEvent = event.getEmailUpdated

      userRepository.getActive(UserId(event.id)).fold(
        err => log.error(s"updating user email from event failed: $err"),
        u => {
          userRepository.put(u.copy(
                               version      = emailUpdatedEvent.getVersion,
                               email        = emailUpdatedEvent.getEmail,
                               timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  def applyUserPasswordUpdatedEvent(event: UserEvent): Unit = {
    log.debug(s"recoverEvent: $event")

    if (event.eventType.isPasswordUpdated) {
      val passwordUpdatedEvent = event.getPasswordUpdated

      updatePassword(UserId(event.id),
                     passwordUpdatedEvent.getVersion,
                     passwordUpdatedEvent.getPassword,
                     passwordUpdatedEvent.getSalt,
                     event.getTime).fold(
        err => log.error(s"updating password on user from event failed: $err"),
        user => ()
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  def applyUserAvatarUrlUpdatedEvent(event: UserEvent): Unit = {
    log.debug(s"recoverEvent: $event")

    if (event.eventType.isAvatarUrlUpdated) {
      userRepository.getActive(UserId(event.id)).fold(
        err => log.error(s"updating avatar URL on user from event failed: $err"),
        u => {
          val avatarUrlUpdatedEvent = event.getAvatarUrlUpdated
          userRepository.put(u.copy(
                               version      = avatarUrlUpdatedEvent.getVersion,
                               avatarUrl    = avatarUrlUpdatedEvent.avatarUrl,
                               timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  def applyUserPasswordResetEvent(event: UserEvent): Unit = {
    log.debug(s"recoverEvent: $event")

    if (event.eventType.isPasswordReset) {
      val passwordResetEvent = event.getPasswordReset

      updatePassword(UserId(event.id),
                     passwordResetEvent.getVersion,
                     passwordResetEvent.getPassword,
                     passwordResetEvent.getSalt,
                     event.getTime).fold(
        err => log.error(s"resetting password on user from event failed: $err"),
        user => ()
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  def applyUserLockedEvent(event: UserEvent): Unit = {
    log.debug(s"recoverEvent: $event")

    if (event.eventType.isLocked) {
      userRepository.getActive(UserId(event.id)).fold(
        err => log.error(s"locking user from event failed: $err"),
        u => {
          userRepository.put(
            LockedUser(id           = u.id,
                       version      = event.getLocked.getVersion,
                       timeAdded    = u.timeAdded,
                       timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
                       name         = u.name,
                       email        = u.email,
                       password     = u.password,
                       salt         = u.salt,
                       avatarUrl    = u.avatarUrl))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  def applyUserUnlockedEvent(event: UserEvent): Unit = {
    log.debug(s"recoverEvent: $event")

    if (event.eventType.isUnlocked) {

      userRepository.getLocked(UserId(event.id)).fold(
        err => log.error(s"unlocking user from event failed: $err"),
        u => {
          userRepository.put(
            ActiveUser(id           = u.id,
                       version      = event.getUnlocked.getVersion,
                       timeAdded    = u.timeAdded,
                       timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
                       name         = u.name,
                       email        = u.email,
                       password     = u.password,
                       salt         = u.salt,
                       avatarUrl    = u.avatarUrl))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  /** Common code for events  UserPasswordUpdatedEvent and UserPasswordResetEvent */
  private def updatePassword(id:       UserId,
                             version:  Long,
                             password: String,
                             salt:     String,
                             dateTime: String): DomainValidation[User] = {

    userRepository.getActive(id).fold(
      err => DomainError(s"user with id not found: $id").failureNel,
      u => {
        userRepository.put(u.copy(version      = version,
                                  password     = password,
                                  salt         = salt,
                                  timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(dateTime))))
        u.successNel
      }
    )
  }

  /** Searches the repository for a matching item.
   */
  protected def emailAvailableMatcher(
    email: String)(matcher: User => Boolean): DomainValidation[Boolean] = {
    val exists = userRepository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) {
      DomainError(s"user with email already exists: $email").failureNel
    } else {
      true.success
    }
  }

  val ErrMsgNameExists = "user with email already exists"

  private def emailAvailable(email: String): DomainValidation[Boolean] = {
    emailAvailableMatcher(email){ item =>
      item.email == email
    }
  }

  private def emailAvailable(email: String, excludeUserId: UserId): DomainValidation[Boolean] = {
    emailAvailableMatcher(email){ item =>
      (item.email == email) && (item.id != excludeUserId)
    }
  }


  /**
   * Creates a user event with the userId for the user that issued the command, and the current date and time.
   */
  private def createUserEvent(id: UserId, command: UserCommand) =
    UserEvent(id     = id.id,
              userId = command.userId,
              time   = Some(ISODateTimeFormat.dateTime.print(DateTime.now)))


  /**
   * For debug only in development mode - password is "testuser"
   */
  private def createDefaultUser(): Unit = {
    if (context.system.settings.config.hasPath(TestData.configPath)
      && context.system.settings.config.getBoolean(TestData.configPath)) {

      log.debug("createDefaultUser")

      userRepository.put(
        ActiveUser(id           = org.biobank.Global.DefaultUserId,
                   version      = 0L,
                   timeAdded    = DateTime.now,
                   timeModified = None,
                   name         = "Administrator",
                   email        = org.biobank.Global.DefaultUserEmail,
                   password     = "$2a$10$Kvl/h8KVhreNDiiOd0XiB.0nut7rysaLcKpbalteFuDN8uIwaojCa",
                   salt         =  "$2a$10$Kvl/h8KVhreNDiiOd0XiB.",
                   avatarUrl    = None))
      ()
    }
  }

  createDefaultUser
  testData.addMultipleUsers
}
