package org.biobank.service

import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.domain.validation.UserValidationHelper
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

    def getByEmail(email: String): DomainValidation[User] = {
      userRepository.getByKey(UserId(email))
    }

    def register(cmd: RegisterUserCmd): Future[DomainValidation[UserRegisteredEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserRegisteredEvent]])
    }

    def activate(cmd: ActivateUserCmd): Future[DomainValidation[UserActivatedEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserActivatedEvent]])
    }

    def update(cmd: UpdateUserCmd): Future[DomainValidation[UserUpdatedEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserUpdatedEvent]])
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
        user <- userRepository.getByKey(UserId(email))
        validPwd <- {
          if (passwordHasher.valid(user.password, user.salt, enteredPwd)) {
            user.success
          } else {
            DomainError("invalid password").failNel
          }
        }
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

    val receiveRecover: Receive = {
      case event: UserRegisteredEvent => recoverEvent(event)

      case event: UserActivatedEvent => recoverEvent(event)

      case event: UserUpdatedEvent => recoverEvent(event)

      case event: UserLockedEvent => recoverEvent(event)

      case event: UserUnlockedEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.users.foreach(i => userRepository.put(i))

      case event: RecoveryCompleted =>

      case msg =>
        throw new IllegalStateException(s"message not handled: $msg")
    }

    val receiveCommand: Receive = {
      case cmd: RegisterUserCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: ActivateUserCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: UpdateUserCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: LockUserCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: UnlockUserCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case "snap" =>
        saveSnapshot(SnapshotState(userRepository.allUsers))
        stash()

      case _ =>
        throw new IllegalStateException("message not handled")
    }

    def validateCmd(cmd: RegisterUserCmd): DomainValidation[UserRegisteredEvent] = {
      val salt = passwordHasher.generateSalt
      for {
        emailAvailable <- userRepository.emailAvailable(cmd.email)
        user <- RegisteredUser.create(
          UserId(cmd.email), -1L, DateTime.now, cmd.name, cmd.email, cmd.password, salt, cmd.avatarUrl)
        event <- UserRegisteredEvent(user.id.toString, DateTime.now, user.name, user.email,
          user.password, user.avatarUrl).success
      } yield {
        event
      }
    }

    def validateCmd(cmd: ActivateUserCmd): DomainValidation[UserActivatedEvent] = {
      val timeNow = DateTime.now
      for {
        user <- userRepository.getByKey(UserId(cmd.email))
        registeredUser <- isUserRegistered(user)
        activatedUser <- registeredUser.activate(Some(cmd.expectedVersion), timeNow)
        event <- UserActivatedEvent(
          activatedUser.id.toString, activatedUser.version, timeNow).success
      } yield {
        event
      }
    }

    def validateCmd(cmd: UpdateUserCmd): DomainValidation[UserUpdatedEvent] = {
      val timeNow = DateTime.now
      for {
        user <- userRepository.getByKey(UserId(cmd.email))
        activeUser <- isUserActive(user)
        updatedUser <- activeUser.update(
          Some(cmd.expectedVersion), timeNow, cmd.name, cmd.email, cmd.password,
          activeUser.salt, cmd.avatarUrl)
        event <- UserUpdatedEvent(updatedUser.id.id, updatedUser.version, timeNow, updatedUser.name,
          updatedUser.email, updatedUser.password, updatedUser.avatarUrl).success
      } yield {
        event
      }
    }

    def validateCmd(cmd: LockUserCmd): DomainValidation[UserLockedEvent] = {
      val timeNow = DateTime.now
      for {
        user <- userRepository.getByKey(UserId(cmd.email))
        activeUser <- isUserActive(user)
        lockedUser <- activeUser.lock(Some(cmd.expectedVersion), timeNow)
        event <- UserLockedEvent(lockedUser.id.toString, lockedUser.version, timeNow).success
      } yield {
        event
      }
    }

    def validateCmd(cmd: UnlockUserCmd): DomainValidation[UserUnlockedEvent] = {
      val timeNow = DateTime.now
      for {
        user <- userRepository.getByKey(UserId(cmd.email))
        lockedUser <- isUserLocked(user)
        unlockedUser <- lockedUser.unlock(Some(cmd.expectedVersion), timeNow)
        event <- UserUnlockedEvent(lockedUser.id.toString, lockedUser.version, timeNow).success
      } yield {
        event
      }
    }

    def recoverEvent(event: UserRegisteredEvent) = {
      log.debug(s"recoverEvent: $event")
      val validation = for {
        // FIXME: add hasher and salt to user
        registeredUser <- RegisteredUser.create(UserId(event.email), -1L, event.dateTime, event.name,
          event.email, event.password, "some-salt", event.avatarUrl)
        savedUser <- userRepository.put(registeredUser).success
      } yield savedUser

      if (validation.isFailure) {
          // this should never happen because the only way to get here is when the
          // command passed validation
          throw new IllegalStateException("creating user from event failed")
      }
    }

    def recoverEvent(event: UserActivatedEvent): Unit = {
      log.debug(s"recoverEvent: $event")

      val validation = for {
        user <- userRepository.getByKey(UserId(event.id))
        registeredUser <- isUserRegistered(user)
        activatedUser <- registeredUser.activate(Some(registeredUser.version), event.dateTime)
        savedUser <- userRepository.put(activatedUser).success
      } yield savedUser

      if (validation.isFailure) {
          // this should never happen because the only way to get here is when the
          // command passed validation
          throw new IllegalStateException("activating user from event failed")
      }
    }

    def recoverEvent(event: UserUpdatedEvent): Unit = {
      log.debug(s"recoverEvent: $event")

      val validation = for {
        user <- userRepository.getByKey(UserId(event.id))
        activeUser <- isUserActive(user)
        updatedUser <- activeUser.update(
          Some(activeUser.version), event.dateTime, event.name, event.email,
          event.password, activeUser.salt, event.avatarUrl)
        savedUser <- userRepository.put(updatedUser).success
      } yield savedUser

      if (validation.isFailure) {
          // this should never happen because the only way to get here is when the
          // command passed validation
          throw new IllegalStateException("activating user from event failed")
      }
    }

    def recoverEvent(event: UserLockedEvent): Unit = {
      log.debug(s"recoverEvent: $event")

      val validation = for {
        user <- userRepository.getByKey(UserId(event.id))
        activeUser <- isUserActive(user)
        lockedUser <- activeUser.lock(Some(activeUser.version), event.dateTime)
        savedUser <- userRepository.put(lockedUser).success
      } yield savedUser

      if (validation.isFailure) {
          // this should never happen because the only way to get here is when the
          // command passed validation
          throw new IllegalStateException("locking user from event failed")
      }
    }

    def recoverEvent(event: UserUnlockedEvent): Unit = {
      log.debug(s"recoverEvent: $event")

      val validation = for {
        user <- userRepository.getByKey(UserId(event.id))
        lockedUser <- isUserLocked(user)
        unlockedUser <- lockedUser.unlock(Some(lockedUser.version), event.dateTime)
        savedUser <- userRepository.put(unlockedUser).success
      } yield savedUser

      if (validation.isFailure) {
          // this should never happen because the only way to get here is when the
          // command passed validation
          throw new IllegalStateException("unlocking user from event failed")
      }
    }

    private def isUserRegistered(user: User): DomainValidation[RegisteredUser] = {
      user match {
        case registeredUser: RegisteredUser => registeredUser.success
        case _ => DomainError(s"the user is not registered").failNel
      }
    }

    private def isUserActive(user: User): DomainValidation[ActiveUser] = {
      user match {
        case activeUser: ActiveUser => activeUser.success
        case _ => DomainError(s"the user is not active").failNel
      }
    }

    private def isUserLocked(user: User): DomainValidation[LockedUser] = {
      user match {
        case lockedUser: LockedUser => lockedUser.success
        case _ => DomainError(s"the user is not active").failNel
      }
    }
  }
}
