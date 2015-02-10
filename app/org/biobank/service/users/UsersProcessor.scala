package org.biobank.service.users

import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._

import akka.actor.{ ActorSystem, ActorRef }
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import com.trueaccord.scalapb.GeneratedMessage

import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  * Handles the commands to configure users.
  */
class UsersProcessor(implicit inj: Injector) extends Processor with Injectable {

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
    case wevent: WrappedEvent[_] => {
      wevent.event match {
        case event: UserRegisteredEvent       => recoverUserRegisteredEvent(event, wevent.userId, wevent.dateTime)
        case event: UserActivatedEvent        => recoverUserActivatedEvent(event, wevent.userId, wevent.dateTime)
        case event: UserNameUpdatedEvent      => recoverUserNameUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: UserEmailUpdatedEvent     => recoverUserEmailUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: UserPasswordUpdatedEvent  => recoverUserPasswordUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: UserAvatarUrlUpdatedEvent => recoverUserAvatarUrlUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: UserLockedEvent           => recoverUserLockedEvent(event, wevent.userId, wevent.dateTime)
        case event: UserUnlockedEvent         => recoverUserUnlockedEvent(event, wevent.userId, wevent.dateTime)
        case event: UserPasswordResetEvent    => recoverUserPasswordResetEvent(event, wevent.userId, wevent.dateTime)

        case event => log.error(s"wrapped event not handled: $event")
      }
    }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.users.foreach(i => userRepository.put(i))

    case event: RecoveryCompleted =>

    case event => log.error(s"event not handled: $event")
  }

  val receiveCommand: Receive = {
    case procCmd: WrappedCommand =>
      implicit val userId = procCmd.userId

      procCmd.command match {
        case cmd: RegisterUserCmd        => processRegisterUserCmd(cmd)
        case cmd: ActivateUserCmd        => processActivateUserCmd(cmd)
        case cmd: UpdateUserNameCmd      => processUpdateUserNameCmd(cmd)
        case cmd: UpdateUserEmailCmd     => processUpdateUserEmailCmd(cmd)
        case cmd: UpdateUserPasswordCmd  => processUpdateUserPasswordCmd(cmd)
        case cmd: UpdateUserAvatarUrlCmd => processUpdateUserAvatarUrlCmd(cmd)
        case cmd: ResetUserPasswordCmd   => processResetUserPasswordCmd(cmd)
        case cmd: LockUserCmd            => processLockUserCmd(cmd)
        case cmd: UnlockUserCmd          => processUnlockUserCmd(cmd)

        case cmd => log.error(s"wrapped command not handled: $cmd")
      }

    case "snap" =>
      saveSnapshot(SnapshotState(userRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"UsersProcessor: message not handled: $cmd")
  }

  def processRegisterUserCmd(cmd: RegisterUserCmd)(implicit userId: Option[UserId]): Unit = {
    val newUserId = userRepository.nextIdentity

    if (userRepository.getByKey(newUserId).isSuccess) {
      log.error(s"user with id already exsits: $userId")
    }

    val salt = passwordHasher.generateSalt
    val encryptedPwd = passwordHasher.encrypt(cmd.password, salt)

    val event = for {
      emailAvailable <- emailAvailable(cmd.email)
      user <- RegisteredUser.create(
        newUserId, -1L, DateTime.now, cmd.name, cmd.email, encryptedPwd, salt, cmd.avatarUrl)
      event <- UserRegisteredEvent(
        id = user.id.id,
        name = Some(user.name),
        email = Some(user.email),
        password = Some(encryptedPwd),
        salt =  Some(salt),
        avatarUrl = user.avatarUrl).success
    } yield event

    process(event){ wevent =>
      recoverUserRegisteredEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  def processActivateUserCmd(cmd: ActivateUserCmd)(implicit userId: Option[UserId]): Unit = {
    val timeNow = DateTime.now
    val v = updateRegistered(cmd) { u => u.activate }
    val event = v.fold(
      err => err.failure,
      user => UserActivatedEvent(id = user.id.id, version = Some(user.version)).success
    )

    process(event){ wevent =>
      recoverUserActivatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  def processUpdateUserNameCmd(cmd: UpdateUserNameCmd)(implicit userId: Option[UserId]): Unit = {
    val timeNow = DateTime.now
    val v = updateActive(cmd) { _.updateName(cmd.name) }
    val event = v.fold(
      err => err.failure,
      user => UserNameUpdatedEvent(
        id = user.id.id,
        version = Some(user.version),
        name = Some(user.name)).success
    )

    process(event){ wevent =>
      recoverUserNameUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  def processUpdateUserEmailCmd(cmd: UpdateUserEmailCmd)(implicit userId: Option[UserId]): Unit = {
    val timeNow = DateTime.now

    val v = updateActive(cmd) { user =>
      for {
        emailAvailable <- emailAvailable(cmd.email, user.id)
        updatedUser <- user.updateEmail(cmd.email)
      } yield updatedUser
    }

    val event = v.fold(
      err => err.failure,
      user => UserEmailUpdatedEvent(
        id = user.id.id,
        version = Some(user.version),
        email = Some(user.email)).success
    )

    process(event){ wevent =>
      recoverUserEmailUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  def processUpdateUserPasswordCmd(cmd: UpdateUserPasswordCmd)(implicit userId: Option[UserId]): Unit = {
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
      err => err.failure[UserPasswordUpdatedEvent],
      user => UserPasswordUpdatedEvent(
        id = user.id.id,
        version = Some(user.version),
        password = Some(user.password),
        salt = Some(user.salt)).success
    )

    process(event){ wevent =>
      recoverUserPasswordUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  def processUpdateUserAvatarUrlCmd(cmd: UpdateUserAvatarUrlCmd)(implicit userId: Option[UserId]): Unit = {
    val timeNow = DateTime.now

    val v = updateActive(cmd) { user =>
      user.updateAvatarUrl(cmd.avatarUrl)
    }

    val event = v.fold(
      err => err.failure,
      user => UserAvatarUrlUpdatedEvent(
        id = user.id.id,
        version = Some(user.version),
        avatarUrl = user.avatarUrl).success
    )

    process(event){ wevent =>
      recoverUserAvatarUrlUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  // only active users can request a password reset
  def processResetUserPasswordCmd(cmd: ResetUserPasswordCmd)(implicit userId: Option[UserId]): Unit = {
    val event = userRepository.getByEmail(cmd.email).fold(
      err => err.failure[UserPasswordResetEvent],
      user => {
        user match {
          case activeUser: ActiveUser => {
            val plainPassword = Utils.randomString(8)
            val passwordInfo = encryptPassword(activeUser, plainPassword)

            activeUser.updatePassword(passwordInfo.password, passwordInfo.salt).fold(
              err => err.failure[UserPasswordResetEvent],
              updatedUser => {
                EmailService.passwordResetEmail(user.email, plainPassword)
                UserPasswordResetEvent(
                  id = updatedUser.id.id,
                  version = Some(updatedUser.version),
                  password = Some(updatedUser.password),
                  salt = Some(updatedUser.salt)
                ).success
              }
            )
          }

          case user =>
            DomainError(s"user is not active: $cmd").failureNel[UserPasswordResetEvent]
        }
      }
    )

    process(event){ wevent =>
      recoverUserPasswordResetEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  def processLockUserCmd(cmd: LockUserCmd)(implicit userId: Option[UserId]): Unit = {
    val timeNow = DateTime.now
    val v = updateActive(cmd) { u => u.lock }
    val event = v.fold(
      err => err.failure,
      user => UserLockedEvent(id = user.id.id, version = Some(user.version)).success
    )

    process(event){ wevent =>
      recoverUserLockedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  def processUnlockUserCmd(cmd: UnlockUserCmd)(implicit userId: Option[UserId]): Unit = {
    val timeNow = DateTime.now
    val v = updateLocked(cmd) { u => u.unlock }
    val event = v.fold(
      err => err.failure,
      user => UserUnlockedEvent(id = user.id.id, version = Some(user.version)).success
    )

    process(event){ wevent =>
      recoverUserUnlockedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  def updateUser[T <: User]
    (cmd: UserModifyCommand)
    (fn: User => DomainValidation[T])
      : DomainValidation[T] = {
    for {
      user         <- userRepository.getByKey(UserId(cmd.id))
      validVersion <- user.requireVersion(cmd.expectedVersion)
      updatedUser  <- fn(user)
    } yield updatedUser
  }

  def updateRegistered[T <: User]
    (cmd: UserModifyCommand)
    (fn: RegisteredUser => DomainValidation[T])
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

  def recoverUserRegisteredEvent
    (event: UserRegisteredEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    log.debug(s"recoverEvent: $event")

    userRepository.put(RegisteredUser(
      id           = UserId(event.id),
      version      = 0L,
      timeAdded    = dateTime,
      timeModified = None,
      name         = event.getName,
      email        = event.getEmail,
      password     = event.getPassword,
      salt         = event.getSalt,
      avatarUrl    = event.avatarUrl))
    ()
  }

  def recoverUserActivatedEvent
    (event: UserActivatedEvent, userId: Option[UserId], dateTime: DateTime) = {
    log.debug(s"recoverEvent: $event")

    userRepository.getRegistered(UserId(event.id)).fold(
      err => log.error(s"activating user from event failed: $err"),
      u => {
        userRepository.put(ActiveUser(
          id           = u.id,
          version      = event.getVersion,
          timeAdded    = u.timeAdded,
          timeModified = Some(dateTime),
          name         = u.name,
          email        = u.email,
          password     = u.password,
          salt         = u.salt,
          avatarUrl    = u.avatarUrl))
        ()
      }
    )
  }

  def recoverUserNameUpdatedEvent
    (event: UserNameUpdatedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    log.debug(s"recoverEvent: $event")

    userRepository.getActive(UserId(event.id)).fold(
      err => log.error(s"updating name on user from event failed: $err"),
      u => {
        userRepository.put(u.copy(
          version      = event.getVersion,
          name         = event.getName,
          timeModified = Some(dateTime)))
        ()
      }
    )
  }

  def recoverUserEmailUpdatedEvent
    (event: UserEmailUpdatedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    log.debug(s"recoverEvent: $event")

    userRepository.getActive(UserId(event.id)).fold(
      err => log.error(s"updating email on user from event failed: $err"),
      u => {
        userRepository.put(u.copy(
          version      = event.getVersion,
          email        = event.getEmail,
          timeModified = Some(dateTime)))
        ()
      }
    )
  }

  def recoverUserPasswordUpdatedEvent
    (event: UserPasswordUpdatedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    log.debug(s"recoverEvent: $event")
    updatePassword(UserId(event.id), event.getVersion, event.getPassword, event.getSalt, dateTime).fold(
      err => log.error(s"updating password on user from event failed: $err"),
      user => ()
    )
  }

  def recoverUserAvatarUrlUpdatedEvent
    (event: UserAvatarUrlUpdatedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    log.debug(s"recoverEvent: $event")

    userRepository.getActive(UserId(event.id)).fold(
      err => log.error(s"updating avatar URL on user from event failed: $err"),
      u => {
        userRepository.put(u.copy(
          version      = event.getVersion,
          avatarUrl    = event.avatarUrl,
          timeModified = Some(dateTime)))
        ()
      }
    )
  }

  def recoverUserPasswordResetEvent
    (event: UserPasswordResetEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    log.debug(s"recoverEvent: $event")
    updatePassword(UserId(event.id), event.getVersion, event.getPassword, event.getSalt, dateTime).fold(
      err => log.error(s"resetting password on user from event failed: $err"),
      user => ()
    )
  }

  def recoverUserLockedEvent
    (event: UserLockedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    log.debug(s"recoverEvent: $event")

    userRepository.getActive(UserId(event.id)).fold(
      err => log.error(s"locking user from event failed: $err"),
      u => {
        userRepository.put(LockedUser(
          id           = u.id,
          version      = event.getVersion,
          timeAdded    = u.timeAdded,
          timeModified = Some(dateTime),
          name         = u.name,
          email        = u.email,
          password     = u.password,
          salt         = u.salt,
          avatarUrl    = u.avatarUrl))
        ()
      }
    )
  }

  def recoverUserUnlockedEvent
    (event: UserUnlockedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    log.debug(s"recoverEvent: $event")

    userRepository.getLocked(UserId(event.id)).fold(
      err => log.error(s"unlocking user from event failed: $err"),
      u => {
        userRepository.put(ActiveUser(
          id           = u.id,
          version      = event.getVersion,
          timeAdded    = u.timeAdded,
          timeModified = Some(dateTime),
          name         = u.name,
          email        = u.email,
          password     = u.password,
          salt         = u.salt,
          avatarUrl    = u.avatarUrl))
        ()
      }
    )
  }

  /** Common code for events  UserPasswordUpdatedEvent and UserPasswordResetEvent */
  private def updatePassword(
    id: UserId,
    version: Long,
    password: String,
    salt: String,
    dateTime: DateTime): DomainValidation[User] = {

    userRepository.getActive(id).fold(
      err => DomainError(s"user with id not found: $id").failureNel,
      u => {
        userRepository.put(u.copy(
          version      = version,
          password     = password,
          salt         = salt,
          timeModified = Some(dateTime)))
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

}
