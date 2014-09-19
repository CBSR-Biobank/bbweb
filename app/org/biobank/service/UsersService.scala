package org.biobank.service

import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._

import akka.actor.{ ActorSystem, ActorRef }
import scala.concurrent.Future
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import scala.concurrent.ExecutionContext.Implicits.global
import org.joda.time.DateTime

import scalaz._
import scalaz.Scalaz._

trait UsersServiceComponent {
  self: RepositoriesComponent with PasswordHasherComponent =>

  class UsersService(usersProcessor: ActorRef) extends ApplicationService {

    val log = LoggerFactory.getLogger(this.getClass)

    def getAll: Set[User] = {
      userRepository.allUsers
    }

    def getUser(id: String): DomainValidation[User] = {
      userRepository.getByKey(UserId(id))
    }

    def getByEmail(email: String): DomainValidation[User] = {
      userRepository.getByEmail(email)
    }

    def register(cmd: RegisterUserCmd): Future[DomainValidation[UserRegisteredEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserRegisteredEvent]])
    }

    def updateName(cmd: UpdateUserNameCmd): Future[DomainValidation[UserNameUpdatedEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserNameUpdatedEvent]])
    }

    def updateEmail(cmd: UpdateUserEmailCmd): Future[DomainValidation[UserEmailUpdatedEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserEmailUpdatedEvent]])
    }

    def updatePassword(cmd: UpdateUserPasswordCmd): Future[DomainValidation[UserPasswordUpdatedEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserPasswordUpdatedEvent]])
    }

    def resetPassword(cmd: ResetUserPasswordCmd): Future[DomainValidation[UserPasswordResetEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserPasswordResetEvent]])
    }

    def activate(cmd: ActivateUserCmd): Future[DomainValidation[UserActivatedEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserActivatedEvent]])
    }

    def lock(cmd: LockUserCmd): Future[DomainValidation[UserLockedEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserLockedEvent]])
    }

    def unlock(cmd: UnlockUserCmd): Future[DomainValidation[UserUnlockedEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserUnlockedEvent]])
    }

    def remove(cmd: RemoveUserCmd): Future[DomainValidation[UserRemovedEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserRemovedEvent]])
    }

    def validatePassword(email: String, enteredPwd: String): DomainValidation[User] = {
      for {
        user <- userRepository.getByEmail(email)
        validPwd <- {
          if (passwordHasher.valid(user.password, user.salt, enteredPwd)) {
            user.success
          } else {
            DomainError("invalid password").failNel
          }
        }
        notLocked <- UserHelper.isUserNotLocked(user)
      } yield user
    }

  }
}

trait UsersProcessorComponent {
  self: RepositoriesComponent with PasswordHasherComponent =>

  case class SnapshotState(users: Set[User])

  /**
    * Handles the commands to configure users.
    */
  class UsersProcessor extends Processor {

    override def persistenceId = "user-processor-id"

    case class PasswordInfo(password: String, salt: String)

    def encryptPassword(user: ActiveUser, newPlainPassword: String): PasswordInfo = {
      val newSalt = passwordHasher.generateSalt
      val newPwd = passwordHasher.encrypt(newPlainPassword, newSalt)
      PasswordInfo(newPwd, newSalt)
    }

    val receiveRecover: Receive = {
      case event: UserRegisteredEvent => recoverEvent(event)
      case event: UserActivatedEvent => recoverEvent(event)
      case event: UserNameUpdatedEvent => recoverEvent(event)
      case event: UserEmailUpdatedEvent => recoverEvent(event)
      case event: UserPasswordUpdatedEvent => recoverEvent(event)
      case event: UserLockedEvent => recoverEvent(event)
      case event: UserUnlockedEvent => recoverEvent(event)
      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.users.foreach(i => userRepository.put(i))

      case event: RecoveryCompleted =>

      case event => throw new IllegalStateException(s"event not handled: $event")
    }

    val receiveCommand: Receive = {
      case cmd: RegisterUserCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: ActivateUserCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: UpdateUserNameCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: UpdateUserEmailCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: UpdateUserPasswordCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: ResetUserPasswordCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: LockUserCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: UnlockUserCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case "snap" =>
        saveSnapshot(SnapshotState(userRepository.allUsers))
        stash()

      case cmd => throw new IllegalStateException(s"message not handled: $cmd")
    }

    def validateCmd(cmd: RegisterUserCmd): DomainValidation[UserRegisteredEvent] = {
      val userId = userRepository.nextIdentity

      if (userRepository.getByKey(userId).isSuccess) {
        throw new IllegalStateException(s"user with id already exsits: $userId")
      }

      val salt = passwordHasher.generateSalt
      val encryptedPwd = passwordHasher.encrypt(cmd.password, salt)
      for {
        emailAvailable <- emailAvailable(cmd.email)
        user <- RegisteredUser.create(
          userId, -1L, DateTime.now, cmd.name, cmd.email, encryptedPwd, salt, cmd.avatarUrl)
        event <- UserRegisteredEvent(
          user.id.id, DateTime.now, user.name, user.email, encryptedPwd, salt, user.avatarUrl).success
      } yield event
    }

    def validateCmd(cmd: ActivateUserCmd): DomainValidation[UserActivatedEvent] = {
      val timeNow = DateTime.now
      val v = updateRegistered(cmd) { u => u.activate }
      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        user => UserActivatedEvent(user.id.id, user.version, timeNow).success
      )
    }

    def validateCmd(cmd: UpdateUserNameCmd): DomainValidation[UserNameUpdatedEvent] = {
      val timeNow = DateTime.now
      val v = updateActive(cmd) { u => u.updateName(cmd.name) }
      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        user => UserNameUpdatedEvent(user.id.id, user.version, timeNow, user.name).success
      )
    }

    def validateCmd(cmd: UpdateUserEmailCmd): DomainValidation[UserEmailUpdatedEvent] = {
      val timeNow = DateTime.now

      val v = updateActive(cmd) { user =>
        for {
          emailAvailable <- emailAvailable(cmd.email, user.id)
          updatedUser <- user.updateEmail(cmd.email)
        } yield updatedUser
      }

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        user => UserEmailUpdatedEvent(user.id.id, user.version, timeNow, user.email).success
      )
    }

    def validateCmd(cmd: UpdateUserPasswordCmd): DomainValidation[UserPasswordUpdatedEvent] = {
      val timeNow = DateTime.now

      val v = updateActive(cmd) { user =>
        if (passwordHasher.valid(user.password, user.salt, cmd.oldPassword)) {
          val passwordInfo = encryptPassword(user, cmd.newPassword)
          user.updatePassword(passwordInfo.password, passwordInfo.salt)
        } else {
          DomainError("invalid password").failNel
        }
      }

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        user => UserPasswordUpdatedEvent(user.id.id, user.version, timeNow, user.password, user.salt).success
      )
    }

    // only active users can request a password reset
    def validateCmd(cmd: ResetUserPasswordCmd): DomainValidation[UserPasswordResetEvent] = {
      val timeNow = DateTime.now

      val v = updateActive(cmd) { user =>
        val plainPassword = Utils.randomString(8)
        val passwordInfo = encryptPassword(user, plainPassword)
        EmailService.passwordResetEmail(user.email, plainPassword)
        user.updatePassword(passwordInfo.password, passwordInfo.salt)
      }

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        user => UserPasswordResetEvent(user.id.id, user.version, user.password, user.salt, timeNow).success
      )
    }

    def validateCmd(cmd: LockUserCmd): DomainValidation[UserLockedEvent] = {
      val timeNow = DateTime.now
      val v = updateActive(cmd) { u => u.lock }
      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        user => UserLockedEvent(user.id.id, user.version, timeNow).success
      )
    }

    def validateCmd(cmd: UnlockUserCmd): DomainValidation[UserUnlockedEvent] = {
      val timeNow = DateTime.now
      val v = updateLocked(cmd) { u => u.unlock }
      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        user => UserUnlockedEvent(user.id.id, user.version, timeNow).success
      )
    }

    def updateUser[T <: User]
      (cmd: UserCommand)
      (fn: User => DomainValidation[T])
        : DomainValidation[T] = {
      for {
        user         <- userRepository.getByKey(UserId(cmd.id))
        validVersion <- user.requireVersion(cmd.expectedVersion)
        updatedUser  <- fn(user)
      } yield updatedUser
    }

    def updateRegistered[T <: User]
      (cmd: UserCommand)
      (fn: RegisteredUser => DomainValidation[T])
        : DomainValidation[T] = {
      updateUser(cmd) {
        case user: RegisteredUser => fn(user)
        case user => s"$user for $cmd is not registered".failNel
      }
    }

    def updateActive[T <: User]
      (cmd: UserCommand)
      (fn: ActiveUser => DomainValidation[T])
        : DomainValidation[T] = {
      updateUser(cmd) {
        case user: ActiveUser => fn(user)
        case user => s"$user for $cmd is not active".failNel
      }
    }

    def updateLocked[T <: User]
      (cmd: UserCommand)
      (fn: LockedUser => DomainValidation[T])
        : DomainValidation[T] = {
      updateUser(cmd) {
        case user: LockedUser => fn(user)
        case user => s"$user for $cmd is not locked".failNel
      }
    }

    def recoverEvent(event: UserRegisteredEvent): Unit = {
      log.debug(s"recoverEvent: $event")
      userRepository.put(RegisteredUser(UserId(event.id), 0L, event.dateTime, None, event.name,
        event.email, event.password, event.salt, event.avatarUrl))
      ()
    }

    def recoverEvent(event: UserActivatedEvent): Unit = {
      log.debug(s"recoverEvent: $event")
      userRepository.getRegistered(UserId(event.id)).fold(
        err => throw new IllegalStateException(s"activating user from event failed: $err"),
        u =>
        userRepository.put(ActiveUser(u.id, event.version, u.addedDate, Some(event.dateTime),
          u.name, u.email, u.password, u.salt, u.avatarUrl))
      )
      ()
    }

    def recoverEvent(event: UserNameUpdatedEvent): Unit = {
      log.debug(s"recoverEvent: $event")
      userRepository.getActive(UserId(event.id)).fold(
        err => throw new IllegalStateException(s"updating name on user from event failed: $err"),
        u => userRepository.put(u.copy(
          version = event.version, name = event.name, lastUpdateDate = Some(event.dateTime)))
      )
      ()
    }

    def recoverEvent(event: UserEmailUpdatedEvent): Unit = {
      log.debug(s"recoverEvent: $event")
      userRepository.getActive(UserId(event.id)).fold(
        err => throw new IllegalStateException(s"updating email on user from event failed: $err"),
        u => userRepository.put(u.copy(
          version = event.version, email = event.email, lastUpdateDate = Some(event.dateTime)))
      )
      ()
    }

    def recoverEvent(event: UserPasswordUpdatedEvent): Unit = {
      log.debug(s"recoverEvent: $event")
      userRepository.getActive(UserId(event.id)).fold(
        err => throw new IllegalStateException(s"updating password on user from event failed: $err"),
        u => userRepository.put(u.copy(
          version = event.version, password = event.password, salt = event.salt,
          lastUpdateDate = Some(event.dateTime)))
      )
      ()
    }

    def recoverEvent(event: UserPasswordResetEvent): Unit = {
      log.debug(s"recoverEvent: $event")
      userRepository.getActive(UserId(event.id)).fold(
        err => throw new IllegalStateException(s"resetting password on user from event failed: $err"),
        u => userRepository.put(u.copy(
          version = event.version, password = event.password, salt = event.salt,
          lastUpdateDate = Some(event.dateTime)))
      )
      ()
    }

    def recoverEvent(event: UserLockedEvent): Unit = {
      log.debug(s"recoverEvent: $event")
      userRepository.getActive(UserId(event.id)).fold(
        err => throw new IllegalStateException(s"locking user from event failed: $err"),
        u => userRepository.put(LockedUser(
          u.id, event.version, u.addedDate, Some(event.dateTime), u.name, u.email, u.password, u.salt,
          u.avatarUrl))
      )
      ()
    }

    def recoverEvent(event: UserUnlockedEvent): Unit = {
      log.debug(s"recoverEvent: $event")
      userRepository.getLocked(UserId(event.id)).fold(
        err => throw new IllegalStateException(s"unlocking user from event failed: $err"),
        u => userRepository.put(ActiveUser(
          u.id, event.version, u.addedDate, Some(event.dateTime), u.name, u.email, u.password, u.salt,
          u.avatarUrl))
      )
      ()
    }

    /** Searches the repository for a matching item.
      */
    protected def emailAvailableMatcher(
      email: String)(matcher: User => Boolean): DomainValidation[Boolean] = {
      val exists = userRepository.getValues.exists { item =>
        matcher(item)
      }
      if (exists) {
        DomainError(s"user with email already exists: $email").failNel
      } else {
        true.success
      }
    }

    val errMsgNameExists = "user with email already exists"

    private def emailAvailable(email: String): DomainValidation[Boolean] = {
      emailAvailableMatcher(email){ item =>
        item.email.equals(email)
      }
    }

    private def emailAvailable(email: String, excludeUserId: UserId): DomainValidation[Boolean] = {
      emailAvailableMatcher(email){ item =>
        item.email.equals(email) && (item.id != excludeUserId)
      }
    }

  }
}
