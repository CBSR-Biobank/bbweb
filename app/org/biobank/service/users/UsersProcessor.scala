package org.biobank.service.users

import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._

import akka.actor.{ ActorSystem, ActorRef }
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import com.trueaccord.scalapb.GeneratedMessage
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Handles the commands to configure users.
 */
class UsersProcessor(implicit inj: Injector) extends Processor with Injectable {
  import UserEvent.EventType

  case class PasswordInfo(password: String, salt: String)

  case class SnapshotState(users: Set[User])

  val userRepository = inject [UserRepository]

  val passwordHasher = inject [PasswordHasher]

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
      user <- RegisteredUser.create(newUserId, -1L, DateTime.now, cmd.name, cmd.email,
                                    encryptedPwd, salt, cmd.avatarUrl)
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
    val timeNow = DateTime.now
    val v = updateRegistered(cmd) { u => u.activate }
    val event = v.fold(
      err => err.failure,
      user => createUserEvent(user.id, cmd).withActivated(
        UserActivatedEvent(version = Some(user.version))).success
    )

    process(event) { applyUserActivatedEvent(_) }
  }

  def processUpdateUserNameCmd(cmd: UpdateUserNameCmd): Unit = {
    val timeNow = DateTime.now
    val v = updateActive(cmd) { _.updateName(cmd.name) }
    val event = v.fold(
      err => err.failure,
      user => createUserEvent(user.id, cmd).withNameUpdated(
        UserNameUpdatedEvent(
          version = Some(user.version),
          name    = Some(user.name))).success
    )

    process(event) { applyUserNameUpdatedEvent(_) }
  }

  def processUpdateUserEmailCmd(cmd: UpdateUserEmailCmd): Unit = {
    val timeNow = DateTime.now

    val v = updateActive(cmd) { user =>
      for {
        emailAvailable <- emailAvailable(cmd.email, user.id)
        updatedUser <- user.updateEmail(cmd.email)
      } yield updatedUser
    }

    val event = v.fold(
      err => err.failure,
      user => createUserEvent(user.id, cmd).withEmailUpdated(
        UserEmailUpdatedEvent(version = Some(user.version),
                              email   = Some(user.email))).success
    )

    process(event) { applyUserEmailUpdatedEvent(_) }
  }

  def processUpdateUserPasswordCmd(cmd: UpdateUserPasswordCmd): Unit = {
    val timeNow = DateTime.now

    val v = updateActive(cmd) { user =>
      if (passwordHasher.valid(user.password, user.salt, cmd.currentPassword)) {
        val passwordInfo = encryptPassword(user, cmd.newPassword)
        user.updatePassword(passwordInfo.password, passwordInfo.salt)
      } else {
        DomainError("invalid password").failureNel
      }
    }

    val event = v.fold(
      err => err.failure[UserEvent],
      user => createUserEvent(user.id, cmd).withPasswordUpdated(
        UserPasswordUpdatedEvent(version  = Some(user.version),
                                 password = Some(user.password),
                                 salt     = Some(user.salt))).success
    )

    process(event) { applyUserPasswordUpdatedEvent(_) }
  }

  def processUpdateUserAvatarUrlCmd(cmd: UpdateUserAvatarUrlCmd): Unit = {
    val timeNow = DateTime.now

    val v = updateActive(cmd) { user =>
      user.updateAvatarUrl(cmd.avatarUrl)
    }

    val event = v.fold(
      err => err.failure,
      user => createUserEvent(user.id, cmd).withAvatarUrlUpdated(
        UserAvatarUrlUpdatedEvent(
          version   = Some(user.version),
          avatarUrl = user.avatarUrl)).success
    )

    process(event) { applyUserAvatarUrlUpdatedEvent(_) }
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

            activeUser.updatePassword(passwordInfo.password, passwordInfo.salt).fold(
              err => err.failure[UserEvent],
              updatedUser => {
                EmailService.passwordResetEmail(user.email, plainPassword)
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
    val timeNow = DateTime.now
    val v = updateActive(cmd) { u => u.lock }
    log.debug(s"command: $cmd")
    val event = v.fold(
      err => err.failure,
      user => createUserEvent(user.id, cmd).withLocked(
        UserLockedEvent(version = Some(user.version))).success
    )

    process(event) { applyUserLockedEvent(_) }
  }

  def processUnlockUserCmd(cmd: UnlockUserCmd): Unit = {
    val timeNow = DateTime.now
    val v = updateLocked(cmd) { u => u.unlock }
    val event = v.fold(
      err => err.failure,
      user => createUserEvent(user.id, cmd).withUnlocked(
        UserUnlockedEvent(version = Some(user.version))).success
    )

    process(event) { applyUserUnlockedEvent(_) }
  }

  def updateUser[T <: User](cmd: UserModifyCommand)(fn: User => DomainValidation[T])
      : DomainValidation[T] = {
    for {
      user         <- userRepository.getByKey(UserId(cmd.id))
      validVersion <- user.requireVersion(cmd.expectedVersion)
      updatedUser  <- fn(user)
    } yield updatedUser
  }

  def updateRegistered[T <: User](cmd: UserModifyCommand)(fn: RegisteredUser => DomainValidation[T])
      : DomainValidation[T] = {
    updateUser(cmd) {
      case user: RegisteredUser => fn(user)
      case user => s"user is not registered: ${cmd.id}".failureNel
    }
  }

  def updateActive[T <: User]
    (cmd: UserModifyCommand)
                  (fn: ActiveUser => DomainValidation[T])
      : DomainValidation[T] = {
    updateUser(cmd) {
      case user: ActiveUser => fn(user)
      case user => s"user is not active: ${cmd.id}".failureNel
    }
  }

  def updateLocked[T <: User]
    (cmd: UserModifyCommand)
                  (fn: LockedUser => DomainValidation[T])
      : DomainValidation[T] = {
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

  val errMsgNameExists = "user with email already exists"

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

}
