package org.biobank.service.users

import akka.actor._
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import org.biobank.TestData
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

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
  import org.biobank.CommonValidations._

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
      case et: EventType.Registered       => applyRegisteredEvent(event)
      case et: EventType.Activated        => applyActivatedEvent(event)
      case et: EventType.NameUpdated      => applyNameUpdatedEvent(event)
      case et: EventType.EmailUpdated     => applyEmailUpdatedEvent(event)
      case et: EventType.PasswordUpdated  => applyPasswordUpdatedEvent(event)
      case et: EventType.AvatarUrlUpdated => applyAvatarUrlUpdatedEvent(event)
      case et: EventType.Locked           => applyLockedEvent(event)
      case et: EventType.Unlocked         => applyUnlockedEvent(event)
      case et: EventType.PasswordReset    => applyPasswordResetEvent(event)

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
        user <- RegisteredUser.create(newUserId,
                                      0L,
                                      cmd.name,
                                      cmd.email,
                                      encryptedPwd,
                                      salt,
                                      cmd.avatarUrl)
      } yield UserEvent(user.id.id).update(
        _.optionalUserId               := cmd.userId,
        _.time                         := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.registered.name              := cmd.name,
        _.registered.email             := cmd.email,
        _.registered.password          := encryptedPwd,
        _.registered.salt              := salt,
        _.registered.optionalAvatarUrl := cmd.avatarUrl)

    process(event) { applyRegisteredEvent(_) }
  }

  def processActivateUserCmd(cmd: ActivateUserCmd): Unit = {
    val v = updateRegistered(cmd) {
        _.activate.map { user =>
          UserEvent(user.id.id).update(
            _.optionalUserId    := cmd.userId,
            _.time              := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.activated.version := cmd.expectedVersion)
        }
      }

    process(v) { applyActivatedEvent(_) }
  }

  def processUpdateUserNameCmd(cmd: UpdateUserNameCmd): Unit = {
    val v = updateActive(cmd) {
        _.withName(cmd.name).map { user =>
          UserEvent(user.id.id).update(
            _.optionalUserId      := cmd.userId,
            _.time                := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.nameUpdated.version := cmd.expectedVersion,
            _.nameUpdated.name    := cmd.name)
        }
      }

    process(v) { applyNameUpdatedEvent(_) }
  }

  def processUpdateUserEmailCmd(cmd: UpdateUserEmailCmd): Unit = {
    val v = updateActive(cmd) { user =>
      for {
        emailAvailable <- emailAvailable(cmd.email, user.id)
        updatedUser    <- user.withEmail(cmd.email)
      } yield UserEvent(user.id.id).update(
        _.optionalUserId       := cmd.userId,
        _.time                 := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.emailUpdated.version := cmd.expectedVersion,
        _.emailUpdated.email   := cmd.email)

    }

    process(v) { applyEmailUpdatedEvent(_) }
  }

  def processUpdateUserPasswordCmd(cmd: UpdateUserPasswordCmd): Unit = {
    val v = updateActive(cmd) { u =>
        if (passwordHasher.valid(u.password, u.salt, cmd.currentPassword)) {
          val passwordInfo = encryptPassword(u, cmd.newPassword)
          u.withPassword(passwordInfo.password, passwordInfo.salt).map { user =>
            UserEvent(user.id.id).update(
              _.optionalUserId           := cmd.userId,
              _.time                     := ISODateTimeFormat.dateTime.print(DateTime.now),
              _.passwordUpdated.version  := cmd.expectedVersion,
              _.passwordUpdated.password := passwordInfo.password,
              _.passwordUpdated.salt     := passwordInfo.salt)
          }
        } else {
          InvalidPassword.failureNel
        }
      }

    process(v) { applyPasswordUpdatedEvent(_) }
  }

  def processUpdateUserAvatarUrlCmd(cmd: UpdateUserAvatarUrlCmd): Unit = {
    val v = updateActive(cmd) {
        _.withAvatarUrl(cmd.avatarUrl).map { user =>
          UserEvent(user.id.id).update(
            _.optionalUserId                     := cmd.userId,
            _.time                               := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.avatarUrlUpdated.version           := cmd.expectedVersion,
            _.avatarUrlUpdated.optionalAvatarUrl := cmd.avatarUrl)
        }
      }

    process(v) { applyAvatarUrlUpdatedEvent(_) }
  }

  // only active users can request a password reset
  def processResetUserPasswordCmd(cmd: ResetUserPasswordCmd): Unit = {
    val event = userRepository.getByEmail(cmd.email) match {
        case Success(user: ActiveUser) => {
          val plainPassword = Utils.randomString(8)
          val passwordInfo = encryptPassword(user, plainPassword)

          user.withPassword(passwordInfo.password, passwordInfo.salt).fold(
            err => err.failure[UserEvent],
            updatedUser => {
              emailService.passwordResetEmail(user.email, plainPassword)
              UserEvent(user.id.id).update(
                _.optionalUserId         := cmd.userId,
                _.time                   := ISODateTimeFormat.dateTime.print(DateTime.now),
                _.passwordReset.version  := cmd.expectedVersion,
                _.passwordReset.password := passwordInfo.password,
                _.passwordReset.salt     := passwordInfo.salt).success
            }
          )
        }
        case Success(user) =>
          InvalidStatus(s"password reset user is not active: $cmd").failureNel[UserEvent]

        case Failure(err) => err.failure[UserEvent]
      }

    process(event) { applyPasswordResetEvent(_) }
  }

  def processLockUserCmd(cmd: LockUserCmd): Unit = {
    val v = updateActive(cmd) {
        _.lock.map { user =>
          UserEvent(user.id.id).update(
            _.optionalUserId := cmd.userId,
            _.time           := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.locked.version := cmd.expectedVersion)
        }
      }

    process(v) { applyLockedEvent(_) }
  }

  def processUnlockUserCmd(cmd: UnlockUserCmd): Unit = {
    val v = updateLocked(cmd) {
        _.unlock.map { user =>
          UserEvent(user.id.id).update(
            _.optionalUserId := cmd.userId,
            _.time           := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.unlocked.version := cmd.expectedVersion)
        }
    }

    process(v) { applyUnlockedEvent(_) }
  }

  def updateUser[T <: User](cmd: UserModifyCommand)
                (fn: User => DomainValidation[UserEvent])
      : DomainValidation[UserEvent] = {
    for {
      user         <- userRepository.getByKey(UserId(cmd.id))
      validVersion <- user.requireVersion(cmd.expectedVersion)
      updatedUser  <- fn(user)
    } yield updatedUser
  }

  def updateRegistered[T <: User](cmd: UserModifyCommand)
                      (fn: RegisteredUser => DomainValidation[UserEvent])
      : DomainValidation[UserEvent] = {
    updateUser(cmd) {
      case user: RegisteredUser => fn(user)
      case user => InvalidStatus(s"user not registered: ${cmd.id}").failureNel
    }
  }

  def updateActive[T <: User](cmd: UserModifyCommand)
                  (fn: ActiveUser => DomainValidation[UserEvent])
      : DomainValidation[UserEvent] = {
    updateUser(cmd) {
      case user: ActiveUser => fn(user)
      case user => InvalidStatus(s"user not active: ${cmd.id}").failureNel
    }
  }

  def updateLocked[T <: User]
    (cmd: UserModifyCommand)(fn: LockedUser => DomainValidation[UserEvent])
      : DomainValidation[UserEvent] = {
    updateUser(cmd) {
      case user: LockedUser => fn(user)
      case user => InvalidStatus(s"user not locked: ${cmd.id}").failureNel
    }
  }

  def applyRegisteredEvent(event: UserEvent): Unit = {
    if (!event.eventType.isRegistered) {
      log.error(s"invalid event type: $event")
    } else {
      val registeredEvent = event.getRegistered

      RegisteredUser.create(id           = UserId(event.id),
                            version      = 0L,
                            name         = registeredEvent.getName,
                            email        = registeredEvent.getEmail,
                            password     = registeredEvent.getPassword,
                            salt         = registeredEvent.getSalt,
                            avatarUrl    = registeredEvent.avatarUrl
      ).fold (
        err => log.error(s"could not add study from event: $event"),
        u   => {
          userRepository.put(
            u.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  def onValidEventUserAndVersion(event: UserEvent,
                                 eventType: Boolean,
                                 eventVersion: Long)
                                (fn: User => Unit): Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      userRepository.getByKey(UserId(event.id)).fold(
        err => log.error(s"user from event does not exist: $err"),
        user => {
          if (user.version != eventVersion) {
            log.error(s"event version check failed: user version: ${user.version}, event: $event")
          } else {
            fn(user)
          }
        }
      )
    }
  }

  def onValidEventActiveUserAndVersion(event: UserEvent,
                                         eventType: Boolean,
                                         eventVersion: Long)
                                        (fn: ActiveUser => Unit): Unit = {
    onValidEventUserAndVersion(event, eventType, eventVersion) {
      case user: ActiveUser => fn(user)
      case user => log.error(s"$user for $event is not disabled")
    }
  }

  def onValidEventRegisteredUserAndVersion(event: UserEvent,
                                           eventType: Boolean,
                                           eventVersion: Long)
                                          (fn: RegisteredUser => Unit): Unit = {
    onValidEventUserAndVersion(event, eventType, eventVersion) {
      case user: RegisteredUser => fn(user)
      case user => log.error(s"$user for $event is not disabled")
    }
  }

  def onValidEventLockedUserAndVersion(event: UserEvent,
                                           eventType: Boolean,
                                           eventVersion: Long)
                                          (fn: LockedUser => Unit): Unit = {
    onValidEventUserAndVersion(event, eventType, eventVersion) {
      case user: LockedUser => fn(user)
      case user => log.error(s"$user for $event is not disabled")
    }
  }

  def applyActivatedEvent(event: UserEvent): Unit = {
    onValidEventRegisteredUserAndVersion(event,
                                         event.eventType.isActivated,
                                         event.getActivated.getVersion) { user =>
      user.activate.fold(
        err => log.error(s"could not activate user from event: $event"),
        u => {
          userRepository.put(
            u.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  def applyNameUpdatedEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isNameUpdated,
                                     event.getNameUpdated.getVersion) { user =>
      user.withName(event.getNameUpdated.getName).fold(
        err => log.error(s"could not update user from event: $event"),
        u => {
          userRepository.put(
            u.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  def applyEmailUpdatedEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isEmailUpdated,
                                     event.getEmailUpdated.getVersion) { user =>
      user.withEmail(event.getEmailUpdated.getEmail).fold(
        err => log.error(s"could not update user from event: $event"),
        u => {
          userRepository.put(
            u.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  def applyPasswordUpdatedEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isPasswordUpdated,
                                     event.getPasswordUpdated.getVersion) { user =>
      val updatedEvent = event.getPasswordUpdated

      user.withPassword(updatedEvent.getPassword, updatedEvent.getSalt).fold(
        err => log.error(s"could not update user from event: $event"),
        u => {
          userRepository.put(
            u.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  def applyAvatarUrlUpdatedEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isAvatarUrlUpdated,
                                     event.getAvatarUrlUpdated.getVersion) { user =>
      user.withAvatarUrl(event.getAvatarUrlUpdated.avatarUrl).fold(
        err => log.error(s"could not update user from event: $event"),
        u => {
          userRepository.put(
            u.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  def applyPasswordResetEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isPasswordReset,
                                     event.getPasswordReset.getVersion) { user =>
      val updatedEvent = event.getPasswordReset

      user.withPassword(updatedEvent.getPassword, updatedEvent.getSalt).fold(
        err => log.error(s"could not update user from event: $event"),
        u => {
          userRepository.put(
            u.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  def applyLockedEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isLocked,
                                     event.getLocked.getVersion) { user =>
      user.lock.fold(
        err => log.error(s"could not lock user from event: $event"),
        u => {
          userRepository.put(
            u.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  def applyUnlockedEvent(event: UserEvent): Unit = {
    onValidEventLockedUserAndVersion(event,
                                     event.eventType.isUnlocked,
                                     event.getUnlocked.getVersion) { user =>
      user.unlock.fold(
        err => log.error(s"could not unlock user from event: $event"),
        u => {
          userRepository.put(
            u.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  /**
   *  Searches the repository for a matching item.
   */
  protected def emailAvailableMatcher(email: String)(matcher: User => Boolean)
      : DomainValidation[Boolean] = {
    val exists = userRepository.getValues.exists { item =>
        matcher(item)
      }

    if (exists)  EmailNotAvailable(s"user with email already exists: $email").failureNel
    else true.success
  }

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
