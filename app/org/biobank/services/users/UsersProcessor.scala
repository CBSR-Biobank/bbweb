package org.biobank.services.users

import akka.actor._
import akka.persistence.{RecoveryCompleted, SaveSnapshotSuccess, SaveSnapshotFailure, SnapshotOffer}
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import org.biobank.domain.users._
import org.biobank.infrastructure.commands.UserCommands._
import org.biobank.infrastructure.events.UserEvents._
import org.biobank.services._
import play.api.{Configuration, Environment}
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object UsersProcessor {
  import org.biobank.ValidationKey

  def props: Props = Props[UsersProcessor]

  final case class SnapshotState(users: Set[User])

  final case class PasswordInfo(password: String, salt: String)

  implicit val snapshotStateFormat: Format[SnapshotState] = Json.format[SnapshotState]

  case object InvalidNewPassword extends ValidationKey

}

/**
 * Handles the commands to configure users.
 */
class UsersProcessor @Inject() (val config:         Configuration,
                                val userRepository: UserRepository,
                                val passwordHasher: PasswordHasher,
                                val emailService:   EmailService,
                                val env:            Environment,
                                val snapshotWriter: SnapshotWriter)
    extends Processor {

  import UsersProcessor._
  import org.biobank.CommonValidations._
  import UserEvent.EventType

  override def persistenceId: String = "user-processor-id"

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var replyTo: Option[ActorRef] = None

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val receiveRecover: Receive = {
    case event: UserEvent =>
      event.eventType match {
        case _: EventType.Registered => applyRegisteredEvent(event)
        case _: EventType.Activated  => applyActivatedEvent(event)
        case _: EventType.Unlocked   => applyUnlockedEvent(event)
        case _: EventType.Locked     => applyLockedEvent(event)

        case _: EventType.WhenActive       =>
          event.getWhenActive.eventType match {
            case _: UserEvent.WhenActive.EventType.NameUpdated      => applyNameUpdatedEvent(event)
            case _: UserEvent.WhenActive.EventType.EmailUpdated     => applyEmailUpdatedEvent(event)
            case _: UserEvent.WhenActive.EventType.PasswordUpdated  => applyPasswordUpdatedEvent(event)
            case _: UserEvent.WhenActive.EventType.AvatarUrlUpdated => applyAvatarUrlUpdatedEvent(event)
            case _: UserEvent.WhenActive.EventType.PasswordReset    => applyPasswordResetEvent(event)
            case _ => log.error(s"user active event not handled: $event")
          }

        case _ => log.error(s"user event not handled: $event")
      }

    case SnapshotOffer(_, snapshotFilename: String) =>
      applySnapshot(snapshotFilename)

    case RecoveryCompleted =>
      log.debug("UsersProcessor: recovery completed")

    case event => log.error(s"event not handled: $event")
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Throw"))
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
      processUpdateCmd(cmd, lockUserCmdToEvent, applyLockedEvent)

    case cmd: UnlockUserCmd =>
      processUpdateCmdOnLockedUser(cmd, unlockUserCmdToEvent, applyUnlockedEvent)

    case "snap" =>
     mySaveSnapshot
     replyTo = Some(sender())

    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"snapshot saved successfully: ${metadata}")
      replyTo.foreach(_ ! akka.actor.Status.Success(s"snapshot saved: $metadata"))
      replyTo = None

    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"snapshot save error: ${metadata}")
      replyTo.foreach(_ ! akka.actor.Status.Failure(reason))
      replyTo = None

    case "persistence_restart" =>
      throw new Exception(
        "UsersProcessor: Intentionally throwing exception to test persistence by restarting the actor")

    case cmd => log.error(s"UsersProcessor: message not handled: $cmd")
  }

  private def mySaveSnapshot(): Unit = {
    val snapshotState = SnapshotState(userRepository.getValues.toSet)
    val filename = snapshotWriter.save(persistenceId, Json.toJson(snapshotState).toString)
    log.debug(s"saved snapshot to: $filename")
    saveSnapshot(filename)
  }

  private def applySnapshot(filename: String): Unit = {
    log.debug(s"snapshot recovery file: $filename")
    val fileContents = snapshotWriter.load(filename);
    Json.parse(fileContents).validate[SnapshotState].fold(
      errors => log.error(s"could not apply snapshot: $filename: $errors"),
      snapshot =>  {
        log.debug(s"snapshot contains ${snapshot.users.size} users")
        snapshot.users.foreach(userRepository.put)
      }
    )
  }

  private def registerUserCmdToEvent(cmd: RegisterUserCmd): ServiceValidation[UserEvent] = {
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
    } yield {
      emailService.userRegisteredEmail(cmd.name, cmd.email)

      UserEvent(user.id.id).update(
        _.time                         := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.registered.name              := cmd.name,
        _.registered.email             := cmd.email,
        _.registered.password          := encryptedPwd,
        _.registered.salt              := salt,
        _.registered.optionalAvatarUrl := cmd.avatarUrl)
    }
  }

  private def activateCmdToEvent(cmd:  ActivateUserCmd,
                                 user: RegisteredUser): ServiceValidation[UserEvent] = {
    user.activate.map { u =>
      emailService.userActivatedEmail(u.email)

      UserEvent(user.id.id).update(
        _.time                     := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.whenActive.sessionUserId := cmd.sessionUserId,
        _.activated.version        := cmd.expectedVersion)
    }
  }

  private def updateNameCmdToEvent(cmd:  UpdateUserNameCmd,
                                   user: ActiveUser): ServiceValidation[UserEvent] = {
    user.withName(cmd.name).map { _ =>
      UserEvent(user.id.id).update(
        _.time                        := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.whenActive.sessionUserId    := cmd.sessionUserId,
        _.whenActive.version          := cmd.expectedVersion,
        _.whenActive.nameUpdated.name := cmd.name)
    }
  }

  private def updateEmailCmdToEvent(cmd:  UpdateUserEmailCmd,
                                    user: ActiveUser): ServiceValidation[UserEvent] = {
    for {
      emailAvailable <- emailAvailable(cmd.email, user.id)
      updatedUser    <- user.withEmail(cmd.email)
    } yield UserEvent(user.id.id).update(
      _.time                          := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.whenActive.sessionUserId      := cmd.sessionUserId,
      _.whenActive.version            := cmd.expectedVersion,
      _.whenActive.emailUpdated.email := cmd.email)
  }

  private def updatePasswordCmdToEvent(cmd:  UpdateUserPasswordCmd,
                                       user: ActiveUser): ServiceValidation[UserEvent] = {
    if (passwordHasher.valid(user.password, user.salt, cmd.currentPassword)) {
      for {
        validNewPassword <- validateNonEmptyString(cmd.newPassword, InvalidNewPassword)
        passwordInfo     <- encryptPassword(cmd.newPassword).successNel[String]
        updated          <- user.withPassword(passwordInfo.password, passwordInfo.salt)
      } yield UserEvent(user.id.id).update(
        _.time                                := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.whenActive.sessionUserId            := cmd.sessionUserId,
        _.whenActive.version                  := cmd.expectedVersion,
        _.whenActive.passwordUpdated.password := passwordInfo.password,
        _.whenActive.passwordUpdated.salt     := passwordInfo.salt)
    } else {
      InvalidPassword.failureNel[UserEvent]
    }
  }

  private def updateAvatarUrlCmdToEvent(cmd:  UpdateUserAvatarUrlCmd,
                                        user: ActiveUser): ServiceValidation[UserEvent] = {
    user.withAvatarUrl(cmd.avatarUrl).map { _ =>
      UserEvent(user.id.id).update(
        _.time                                          := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.whenActive.sessionUserId                      := cmd.sessionUserId,
        _.whenActive.version                            := cmd.expectedVersion,
        _.whenActive.avatarUrlUpdated.optionalAvatarUrl := cmd.avatarUrl)
    }
  }

  /**
   * only active users can request a password reset
   */
  private def resetUserPasswordCmdToEvent(cmd: ResetUserPasswordCmd): ServiceValidation[UserEvent] = {
    val plainPassword = Utils.randomString(8)
    for {
      user <- userRepository.getByEmail(cmd.email)
      activeUser <- {
        user match {
          case u: ActiveUser => u.successNel[String]
          case u => InvalidStatus(s"user for password reset is not active: $cmd").failureNel[ActiveUser]
        }
      }
      passwordInfo <- encryptPassword(plainPassword).successNel[String]
      updatedUser  <- activeUser.withPassword(passwordInfo.password, passwordInfo.salt)
    } yield {
      emailService.passwordResetEmail(updatedUser.email, plainPassword)

      UserEvent(user.id.id).update(
        _.time                              := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.whenActive.optionalSessionUserId  := None,
        _.whenActive.version                := user.version,
        _.whenActive.passwordReset.password := passwordInfo.password,
        _.whenActive.passwordReset.salt     := passwordInfo.salt)
    }
  }

  private def lockUserCmdToEvent(cmd:  LockUserCmd, user: User): ServiceValidation[UserEvent] = {
    val v = user match {
        case au: ActiveUser => au.lock
        case ru: RegisteredUser => ru.lock
        case _ => InvalidStatus(s"user not registered or active: ${user.id.id}").failureNel[LockedUser]
      }

    v.map { _ =>
      UserEvent(user.id.id).update(
        _.time           := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.locked.sessionUserId := cmd.sessionUserId,
        _.locked.version := cmd.expectedVersion)
    }
  }

  private def unlockUserCmdToEvent(cmd: UnlockUserCmd,
                                   user: LockedUser): ServiceValidation[UserEvent] = {
    user.unlock.map { _ =>
      UserEvent(user.id.id).update(
        _.time           := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.unlocked.sessionUserId := cmd.sessionUserId,
        _.unlocked.version := cmd.expectedVersion)
    }
  }

  private def processUpdateCmd[T <: UserModifyCommand]
    (cmd:           T,
     validateCmd:   (T, User) => ServiceValidation[UserEvent],
     applyEvent:    UserEvent => Unit): Unit = {
    val event = for {
        user         <- userRepository.getByKey(UserId(cmd.id))
        validVersion <- user.requireVersion(cmd.expectedVersion)
        event        <- validateCmd(cmd, user)
      } yield event

    process(event)(applyEvent)
  }

  private def processUpdateCmdOnRegisteredUser[T <: UserModifyCommand]
    (cmd:           T,
     validateCmd:   (T, RegisteredUser) => ServiceValidation[UserEvent],
     applyEvent:    UserEvent => Unit): Unit = {

    def udpateOnRegisteredUser(cmd: T, user: User): ServiceValidation[UserEvent] = {
      user match {
        case u: RegisteredUser => validateCmd(cmd, u)
        case _ => InvalidStatus(s"user not registered: ${cmd.id}").failureNel[UserEvent]
      }
    }

    processUpdateCmd(cmd, udpateOnRegisteredUser, applyEvent)
  }

  private def processUpdateCmdOnActiveUser[T <: UserModifyCommand]
    (cmd:           T,
     validateCmd:   (T, ActiveUser) => ServiceValidation[UserEvent],
     applyEvent:    UserEvent => Unit): Unit = {

    def udpateOnActiveUser(cmd: T, user: User): ServiceValidation[UserEvent] = {
      user match {
        case u: ActiveUser => validateCmd(cmd, u)
        case _ => InvalidStatus(s"user not active: ${cmd.id}").failureNel[UserEvent]
      }
    }

    processUpdateCmd(cmd, udpateOnActiveUser, applyEvent)
  }

  private def processUpdateCmdOnLockedUser[T <: UserModifyCommand]
    (cmd:           T,
     validateCmd:   (T, LockedUser) => ServiceValidation[UserEvent],
     applyEvent:    UserEvent => Unit): Unit = {

    def udpateOnLockedUser(cmd: T, user: User): ServiceValidation[UserEvent] = {
      user match {
        case u: LockedUser => validateCmd(cmd, u)
        case _ => InvalidStatus(s"user not locked: ${cmd.id}").failureNel[UserEvent]
      }
    }

    processUpdateCmd(cmd, udpateOnLockedUser, applyEvent)
  }

  private def applyRegisteredEvent(event: UserEvent): Unit = {
    if (!event.eventType.isRegistered) {
      log.error(s"invalid event type: $event")
    } else {
      val registeredEvent = event.getRegistered

      val v = RegisteredUser.create(id           = UserId(event.id),
                                    version      = 0L,
                                    name         = registeredEvent.getName,
                                    email        = registeredEvent.getEmail,
                                    password     = registeredEvent.getPassword,
                                    salt         = registeredEvent.getSalt,
                                    avatarUrl    = registeredEvent.avatarUrl)

      if (v.isFailure) {
        log.error(s"could not add user from event: $v")
      }

      v.foreach { u =>
        userRepository.put(u.copy(slug      = userRepository.slug(registeredEvent.getName),
                                  timeAdded = OffsetDateTime.parse(event.getTime)))
      }
    }
  }

  private def onValidEventUserAndVersion(event: UserEvent,
                                         eventType: Boolean,
                                         eventVersion: Long)
                                        (applyEvent: (User, OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      userRepository.getByKey(UserId(event.id)).fold(
        err => log.error(s"user from event does not exist: $err"),
        user => {
          if (user.version != eventVersion) {
            log.error(s"event version check failed: user version: ${user.version}, event: $event")
          } else {
            val eventTime = OffsetDateTime.parse(event.getTime)
            val update = applyEvent(user, eventTime)

            if (update.isFailure) {
              log.error(s"user update from event failed: $update")
            }
          }
        }
      )
    }
  }

  private def onValidEventRegisteredUserAndVersion(event: UserEvent,
                                                   eventType: Boolean,
                                                   eventVersion: Long)
                                                  (applyEvent: (RegisteredUser,
                                                                OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    onValidEventUserAndVersion(event, eventType, eventVersion) { (user, eventTime) =>
      user match {
        case user: RegisteredUser => applyEvent(user, eventTime)
        case user => ServiceError(s"user not registered: $event").failureNel[Boolean]
      }
    }
  }

  private def onValidUserActiveEvent(event: UserEvent)
                                    (applyEvent: (ActiveUser, OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    onValidEventUserAndVersion(event,
                               event.eventType.isWhenActive,
                               event.getWhenActive.getVersion) { (user, eventTime) =>
      user match {
        case user: ActiveUser => applyEvent(user, eventTime)
        case user => ServiceError(s"user not active: $event").failureNel[Boolean]
      }
    }
  }

  private def onValidEventLockedUserAndVersion(event: UserEvent,
                                               eventType: Boolean,
                                               eventVersion: Long)
                                              (applyEvent: (LockedUser,
                                                            OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    onValidEventUserAndVersion(event, eventType, eventVersion) { (user, eventTime) =>
      user match {
        case user: LockedUser => applyEvent(user, eventTime)
        case user => ServiceError(s"user not locked: $event").failureNel[Boolean]
      }
    }
  }

  private def applyActivatedEvent(event: UserEvent): Unit = {
    onValidEventRegisteredUserAndVersion(event,
                                         event.eventType.isActivated,
                                         event.getActivated.getVersion) { (user, eventTime) =>
      val v = user.activate
      v.foreach { u => userRepository.put(u.copy(timeAdded = eventTime)) }
      v.map(_ => true)
    }
  }

  private def applyNameUpdatedEvent(event: UserEvent): Unit = {
    onValidUserActiveEvent(event) { (user, eventTime) =>
      val v = user.withName(event.getWhenActive.getNameUpdated.getName).map { u =>
          u.copy(slug         = userRepository.slug(u.name),
                 timeModified = Some(eventTime))
        }
      v.foreach(userRepository.put)
      v.map(_ => true)
    }
  }

  private def applyEmailUpdatedEvent(event: UserEvent): Unit = {
    onValidUserActiveEvent(event) { (user, eventTime) =>
      val v = user.withEmail(event.getWhenActive.getEmailUpdated.getEmail)
      v.foreach(u => userRepository.put(u.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  private def applyPasswordUpdatedEvent(event: UserEvent): Unit = {
    onValidUserActiveEvent(event) { (user, eventTime) =>
      val v = user.withPassword(event.getWhenActive.getPasswordUpdated.getPassword,
                                event.getWhenActive.getPasswordUpdated.getSalt)
      v.foreach(u => userRepository.put(u.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  private def applyAvatarUrlUpdatedEvent(event: UserEvent): Unit = {
    onValidUserActiveEvent(event) { (user, eventTime) =>
      val v = user.withAvatarUrl(event.getWhenActive.getAvatarUrlUpdated.avatarUrl)
      v.foreach(u => userRepository.put(u.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  private def applyPasswordResetEvent(event: UserEvent): Unit = {
    onValidUserActiveEvent(event) { (user, eventTime) =>
      val v = user.withPassword(event.getWhenActive.getPasswordReset.getPassword,
                                event.getWhenActive.getPasswordReset.getSalt)
      v.foreach(u => userRepository.put(u.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  /* registered and active users can be locked */
  private def applyLockedEvent(event: UserEvent): Unit = {
    onValidEventUserAndVersion(event,
                               event.eventType.isLocked,
                               event.getLocked.getVersion) { (user, eventTime) =>
      val v = user match {
          case au: ActiveUser => au.lock
          case ru: RegisteredUser => ru.lock
          case _ => InvalidStatus(s"user not registered or active: $event").failureNel[LockedUser]
        }
      v.foreach(u => userRepository.put(u.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  private def applyUnlockedEvent(event: UserEvent): Unit = {
    onValidEventLockedUserAndVersion(event,
                                     event.eventType.isUnlocked,
                                     event.getUnlocked.getVersion) { (user, eventTime) =>
      val v = user.unlock
      v.foreach(u => userRepository.put(u.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  /**
   *  Searches the repository for a matching item.
   */
  private def emailAvailableMatcher(email: String)(matcher: User => Boolean)
      : ServiceValidation[Boolean] = {
    val exists = userRepository.getValues.exists { item =>
        matcher(item)
      }

    if (exists) EmailNotAvailable(s"user with email already exists: $email").failureNel[Boolean]
    else true.successNel[String]
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def emailAvailable(email: String): ServiceValidation[Boolean] = {
    emailAvailableMatcher(email){ item =>
      item.email == email
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def emailAvailable(email: String, excludeUserId: UserId): ServiceValidation[Boolean] = {
    emailAvailableMatcher(email){ item =>
      (item.email == email) && (item.id != excludeUserId)
    }
  }

  private def encryptPassword(newPlainPassword: String): PasswordInfo = {
    val newSalt = passwordHasher.generateSalt
    val newPwd = passwordHasher.encrypt(newPlainPassword, newSalt)
    PasswordInfo(newPwd, newSalt)
  }

  private def init(): Unit = {
    userRepository.init
  }

  init

}
