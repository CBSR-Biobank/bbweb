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
    case cmd: RegisterUserCmd =>
      process(registerUserCmdToEvent(cmd))(applyRegisteredEvent)

    case cmd: ActivateUserCmd =>
      processUpdateCmdOnRegisteredUser(cmd, activateCmdToEvent, applyActivatedEvent)

    case cmd: UpdateUserNameCmd =>
      processUpdateCmdOnActiveUser(cmd, updateNameCmdToEvent, applyNameUpdatedEvent)

    case cmd: UpdateUserEmailCmd =>
      processUpdateCmdOnActiveUser(cmd, updateEmailCmdToEvent, applyEmailUpdatedEvent)

    case cmd: UpdateUserPasswordCmd =>
      processUpdateCmdOnActiveUser(cmd, updatePasswordCmdToEvent, applyPasswordUpdatedEvent)

    case cmd: UpdateUserAvatarUrlCmd =>
      processUpdateCmdOnActiveUser(cmd, updateAvatarUrlCmdToEvent, applyAvatarUrlUpdatedEvent)

    case cmd: ResetUserPasswordCmd =>
      process(resetUserPasswordCmdToEvent(cmd))(applyPasswordResetEvent)

    case cmd: LockUserCmd =>
      processUpdateCmdOnActiveUser(cmd, lockUserCmdToEvent, applyLockedEvent)

    case cmd: UnlockUserCmd =>
      processUpdateCmdOnLockedUser(cmd, unlockUserCmdToEvent, applyUnlockedEvent)

    case "snap" =>
      saveSnapshot(SnapshotState(userRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"UsersProcessor: message not handled: $cmd")
  }

  private def registerUserCmdToEvent(cmd: RegisterUserCmd): DomainValidation[UserEvent] = {
    val salt = passwordHasher.generateSalt
    val encryptedPwd = passwordHasher.encrypt(cmd.password, salt)

    for {
      emailAvailable <- emailAvailable(cmd.email)
      userId         <- validNewIdentity(userRepository.nextIdentity, userRepository)
      user           <- RegisteredUser.create(userId,
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
  }

  private def activateCmdToEvent(cmd:  ActivateUserCmd,
                                  user: RegisteredUser): DomainValidation[UserEvent] = {
    user.activate.map { _ =>
      UserEvent(user.id.id).update(
        _.optionalUserId    := cmd.userId,
        _.time              := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.activated.version := cmd.expectedVersion)
    }
  }

  private def updateNameCmdToEvent(cmd:  UpdateUserNameCmd,
                                    user: ActiveUser): DomainValidation[UserEvent] = {
    user.withName(cmd.name).map { _ =>
      UserEvent(user.id.id).update(
        _.optionalUserId      := cmd.userId,
        _.time                := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.nameUpdated.version := cmd.expectedVersion,
        _.nameUpdated.name    := cmd.name)
    }
  }

  private def updateEmailCmdToEvent(cmd:  UpdateUserEmailCmd,
                                     user: ActiveUser): DomainValidation[UserEvent] = {
    for {
      emailAvailable <- emailAvailable(cmd.email, user.id)
      updatedUser    <- user.withEmail(cmd.email)
    } yield UserEvent(user.id.id).update(
      _.optionalUserId       := cmd.userId,
      _.time                 := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.emailUpdated.version := cmd.expectedVersion,
      _.emailUpdated.email   := cmd.email)
  }

  private def updatePasswordCmdToEvent(cmd:  UpdateUserPasswordCmd,
                                        user: ActiveUser): DomainValidation[UserEvent] = {
    if (passwordHasher.valid(user.password, user.salt, cmd.currentPassword)) {
      val passwordInfo = encryptPassword(user, cmd.newPassword)
      user.withPassword(passwordInfo.password, passwordInfo.salt).map { user =>
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

  private def updateAvatarUrlCmdToEvent(cmd:  UpdateUserAvatarUrlCmd,
                                         user: ActiveUser): DomainValidation[UserEvent] = {
    user.withAvatarUrl(cmd.avatarUrl).map { _ =>
      UserEvent(user.id.id).update(
        _.optionalUserId                     := cmd.userId,
        _.time                               := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.avatarUrlUpdated.version           := cmd.expectedVersion,
        _.avatarUrlUpdated.optionalAvatarUrl := cmd.avatarUrl)
    }
  }

  /**
   * only active users can request a password reset
   */
  private def resetUserPasswordCmdToEvent(cmd: ResetUserPasswordCmd): DomainValidation[UserEvent] = {
    userRepository.getByEmail(cmd.email) match {
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
  }

  private def lockUserCmdToEvent(cmd:  LockUserCmd,
                                  user: ActiveUser): DomainValidation[UserEvent] = {
    user.lock.map { _ =>
      UserEvent(user.id.id).update(
        _.optionalUserId := cmd.userId,
        _.time           := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.locked.version := cmd.expectedVersion)
    }
  }

  private def unlockUserCmdToEvent(cmd: UnlockUserCmd,
                                    user: LockedUser): DomainValidation[UserEvent] = {
    user.unlock.map { _ =>
      UserEvent(user.id.id).update(
        _.optionalUserId := cmd.userId,
        _.time           := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.unlocked.version := cmd.expectedVersion)
    }
  }

  private def processUpdateCmd[T <: UserModifyCommand]
    (cmd:           T,
     validateCmd:   (T, User) => DomainValidation[UserEvent],
     applyEvent:    UserEvent => Unit): Unit = {
    var event = for {
        user         <- userRepository.getByKey(UserId(cmd.id))
        validVersion <- user.requireVersion(cmd.expectedVersion)
        event        <- validateCmd(cmd, user)
      } yield event

    process(event)(applyEvent)
  }

  private def processUpdateCmdOnRegisteredUser[T <: UserModifyCommand]
    (cmd:           T,
     validateCmd:   (T, RegisteredUser) => DomainValidation[UserEvent],
     applyEvent:    UserEvent => Unit): Unit = {

    def udpateOnRegisteredUser(cmd: T, user: User): DomainValidation[UserEvent] = {
      user match {
        case u: RegisteredUser => validateCmd(cmd, u)
        case _ => InvalidStatus(s"user not registered: ${cmd.id}").failureNel
      }
    }

    processUpdateCmd(cmd, udpateOnRegisteredUser, applyEvent)
  }

  private def processUpdateCmdOnActiveUser[T <: UserModifyCommand]
    (cmd:           T,
     validateCmd:   (T, ActiveUser) => DomainValidation[UserEvent],
     applyEvent:    UserEvent => Unit): Unit = {

    def udpateOnRegisteredUser(cmd: T, user: User): DomainValidation[UserEvent] = {
      user match {
        case u: ActiveUser => validateCmd(cmd, u)
        case _ => InvalidStatus(s"user not active: ${cmd.id}").failureNel
      }
    }

    processUpdateCmd(cmd, udpateOnRegisteredUser, applyEvent)
  }

  private def processUpdateCmdOnLockedUser[T <: UserModifyCommand]
    (cmd:           T,
     validateCmd:   (T, LockedUser) => DomainValidation[UserEvent],
     applyEvent:    UserEvent => Unit): Unit = {

    def udpateOnRegisteredUser(cmd: T, user: User): DomainValidation[UserEvent] = {
      user match {
        case u: LockedUser => validateCmd(cmd, u)
        case _ => InvalidStatus(s"user not locked: ${cmd.id}").failureNel
      }
    }

    processUpdateCmd(cmd, udpateOnRegisteredUser, applyEvent)
  }

  private def applyRegisteredEvent(event: UserEvent): Unit = {
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

  private def onValidEventUserAndVersion(event: UserEvent,
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

  private def onValidEventActiveUserAndVersion(event: UserEvent,
                                               eventType: Boolean,
                                               eventVersion: Long)
                                              (fn: ActiveUser => Unit): Unit = {
    onValidEventUserAndVersion(event, eventType, eventVersion) {
      case user: ActiveUser => fn(user)
      case user => log.error(s"$user for $event is not disabled")
    }
  }

  private def onValidEventRegisteredUserAndVersion(event: UserEvent,
                                                   eventType: Boolean,
                                                   eventVersion: Long)
                                                  (fn: RegisteredUser => Unit): Unit = {
    onValidEventUserAndVersion(event, eventType, eventVersion) {
      case user: RegisteredUser => fn(user)
      case user => log.error(s"$user for $event is not disabled")
    }
  }

  private def onValidEventLockedUserAndVersion(event: UserEvent,
                                               eventType: Boolean,
                                               eventVersion: Long)
                                              (fn: LockedUser => Unit): Unit = {
    onValidEventUserAndVersion(event, eventType, eventVersion) {
      case user: LockedUser => fn(user)
      case user => log.error(s"$user for $event is not disabled")
    }
  }

  private def applyActivatedEvent(event: UserEvent): Unit = {
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

  private def applyNameUpdatedEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isNameUpdated,
                                     event.getNameUpdated.getVersion) { user =>
      user.withName(event.getNameUpdated.getName).fold(
        err => log.error(s"could not update user from event: $event"),
        u => storeUpdated(u, event.getTime)
      )
    }
  }

  private def applyEmailUpdatedEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isEmailUpdated,
                                     event.getEmailUpdated.getVersion) { user =>
      user.withEmail(event.getEmailUpdated.getEmail).fold(
        err => log.error(s"could not update user from event: $event"),
        u => storeUpdated(u, event.getTime)
      )
    }
  }

  private def applyPasswordUpdatedEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isPasswordUpdated,
                                     event.getPasswordUpdated.getVersion) { user =>
      val updatedEvent = event.getPasswordUpdated

      user.withPassword(updatedEvent.getPassword, updatedEvent.getSalt).fold(
        err => log.error(s"could not update user from event: $event"),
        u => storeUpdated(u, event.getTime)
      )
    }
  }

  private def applyAvatarUrlUpdatedEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isAvatarUrlUpdated,
                                     event.getAvatarUrlUpdated.getVersion) { user =>
      user.withAvatarUrl(event.getAvatarUrlUpdated.avatarUrl).fold(
        err => log.error(s"could not update user from event: $event"),
        u => storeUpdated(u, event.getTime)
      )
    }
  }

  private def applyPasswordResetEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isPasswordReset,
                                     event.getPasswordReset.getVersion) { user =>
      val updatedEvent = event.getPasswordReset

      user.withPassword(updatedEvent.getPassword, updatedEvent.getSalt).fold(
        err => log.error(s"could not update user from event: $event"),
        u => storeUpdated(u, event.getTime)
      )
    }
  }

  private def applyLockedEvent(event: UserEvent): Unit = {
    onValidEventActiveUserAndVersion(event,
                                     event.eventType.isLocked,
                                     event.getLocked.getVersion) { user =>
      user.lock.fold(
        err => log.error(s"could not lock user from event: $event"),
        u => storeUpdated(u, event.getTime)
      )
    }
  }

  private def applyUnlockedEvent(event: UserEvent): Unit = {
    onValidEventLockedUserAndVersion(event,
                                     event.eventType.isUnlocked,
                                     event.getUnlocked.getVersion) { user =>
      user.unlock.fold(
        err => log.error(s"could not unlock user from event: $event"),
        u => storeUpdated(u, event.getTime)
      )
    }
  }

  /**
   * This can be improved once
   *
   */
  private def storeUpdated(user: User, time: String): Unit = {
    val timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(time))
    val updatedUser = user match {
        case u: ActiveUser     => u.copy(timeModified = timeModified)
        case u: RegisteredUser => u.copy(timeModified = timeModified)
        case u: LockedUser     => u.copy(timeModified = timeModified)
      }
    userRepository.put(updatedUser)
    ()
  }

  /**
   *  Searches the repository for a matching item.
   */
  private def emailAvailableMatcher(email: String)(matcher: User => Boolean)
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

  private def encryptPassword(user: ActiveUser, newPlainPassword: String): PasswordInfo = {
    val newSalt = passwordHasher.generateSalt
    val newPwd = passwordHasher.encrypt(newPlainPassword, newSalt)
    PasswordInfo(newPwd, newSalt)
  }

  /**
   * For debug only in development mode - password is "testuser"
   */
  private  def createDefaultUser(): Unit = {
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
