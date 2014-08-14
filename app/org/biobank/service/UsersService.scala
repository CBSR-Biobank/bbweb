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
        notLocked <- UserHelper.isUserNotLocked(user)
        validPwd <- {
          if (passwordHasher.valid(user.password, user.salt, enteredPwd)) {
            user.success
          } else {
            DomainError("invalid password").failNel
          }
        }
      } yield user
    }

    def resetPassword(cmd: ResetUserPasswordCmd): Future[DomainValidation[UserPasswordResetEvent]] = {
      usersProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserPasswordResetEvent]])
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

      case event: UserPasswordResetEvent => recoverEvent(event)

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

      case cmd: ResetUserPasswordCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case "snap" =>
        saveSnapshot(SnapshotState(userRepository.allUsers))
        stash()

      case _ =>
        throw new IllegalStateException("message not handled")
    }

    def validateCmd(cmd: RegisterUserCmd): DomainValidation[UserRegisteredEvent] = {
      val salt = passwordHasher.generateSalt
      val encryptedPwd = passwordHasher.encrypt(cmd.password, salt)
      for {
        emailAvailable <- userRepository.emailAvailable(cmd.email)
        user <- RegisteredUser.create(
          UserId(cmd.email), -1L, DateTime.now, cmd.name, cmd.email, encryptedPwd, salt, cmd.avatarUrl)
        event <- UserRegisteredEvent(
          user.id.id, DateTime.now, user.name, user.email, encryptedPwd, salt, user.avatarUrl).success
      } yield {
        event
      }
    }

    def validateCmd(cmd: ActivateUserCmd): DomainValidation[UserActivatedEvent] = {
      val timeNow = DateTime.now
      for {
        user <- userRepository.getByKey(UserId(cmd.email))
        registeredUser <- UserHelper.isUserRegistered(user)
        activatedUser <- registeredUser.activate(Some(cmd.expectedVersion), timeNow)
        event <- UserActivatedEvent(
          activatedUser.id.id, activatedUser.version, timeNow).success
      } yield {
        event
      }
    }

    def validateCmd(cmd: UpdateUserCmd): DomainValidation[UserUpdatedEvent] = {
      val timeNow = DateTime.now

      def getPassword(user: ActiveUser, newPlainPassword: Option[String]): (String, String) = {
        newPlainPassword.fold {
          (user.password, user.salt)
        } { plainPwd =>
          val newSalt = passwordHasher.generateSalt
          val newPwd = passwordHasher.encrypt(plainPwd, newSalt)
          (newPwd, newSalt)
        }
      }

      for {
        user <- userRepository.getByKey(UserId(cmd.email))
        activeUser <- UserHelper.isUserActive(user)
        passwordInfo <- getPassword(activeUser, cmd.password).success
        updatedUser <- activeUser.update(
          Some(cmd.expectedVersion), timeNow, cmd.name, cmd.email, passwordInfo._1, passwordInfo._2,
          cmd.avatarUrl)
        event <- UserUpdatedEvent(updatedUser.id.id, updatedUser.version, timeNow, updatedUser.name,
          updatedUser.email, updatedUser.password, updatedUser.avatarUrl).success
      } yield event
    }

    def validateCmd(cmd: LockUserCmd): DomainValidation[UserLockedEvent] = {
      val timeNow = DateTime.now
      for {
        user <- userRepository.getByKey(UserId(cmd.email))
        activeUser <- UserHelper.isUserActive(user)
        lockedUser <- activeUser.lock(Some(cmd.expectedVersion), timeNow)
        event <- UserLockedEvent(lockedUser.id.id, lockedUser.version, timeNow).success
      } yield {
        event
      }
    }

    def validateCmd(cmd: UnlockUserCmd): DomainValidation[UserUnlockedEvent] = {
      val timeNow = DateTime.now
      for {
        user <- userRepository.getByKey(UserId(cmd.email))
        lockedUser <- UserHelper.isUserLocked(user)
        unlockedUser <- lockedUser.unlock(Some(cmd.expectedVersion), timeNow)
        event <- UserUnlockedEvent(lockedUser.id.id, lockedUser.version, timeNow).success
      } yield {
        event
      }
    }

    // only active users can request a password reset
    def validateCmd(cmd: ResetUserPasswordCmd): DomainValidation[UserPasswordResetEvent] = {
      val salt = passwordHasher.generateSalt
      val plainPassword = Utils.randomString(8)
      val encryptedPwd = passwordHasher.encrypt(plainPassword, salt)
      val timeNow = DateTime.now
      for {
        user <- userRepository.getByKey(UserId(cmd.email))
        activeUser <- UserHelper.isUserActive(user)
        event <- UserPasswordResetEvent(user.id.id, salt, encryptedPwd, timeNow).success
        email <- EmailService.passwordResetEmail(user.email, plainPassword).success
      } yield event
    }

    def recoverEvent(event: UserRegisteredEvent) = {
      log.debug(s"recoverEvent: $event")
      val validation = for {
        registeredUser <- RegisteredUser.create(UserId(event.email), -1L, event.dateTime, event.name,
          event.email, event.password, event.salt, event.avatarUrl)
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
        registeredUser <- UserHelper.isUserRegistered(user)
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
        activeUser <- UserHelper.isUserActive(user)
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
        activeUser <- UserHelper.isUserActive(user)
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
        lockedUser <- UserHelper.isUserLocked(user)
        unlockedUser <- lockedUser.unlock(Some(lockedUser.version), event.dateTime)
        savedUser <- userRepository.put(unlockedUser).success
      } yield savedUser

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("unlocking user from event failed")
      }
    }

    def recoverEvent(event: UserPasswordResetEvent): Unit = {
      val validation = for {
        user <- userRepository.getByKey(UserId(event.id))
        activeUser <- UserHelper.isUserActive(user)
        updatedUser <- activeUser.resetPassword(event.salt, event.password, event.dateTime)
        savedUser <- userRepository.put(updatedUser).success
      } yield savedUser

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        validation.swap.map { err =>
          throw new IllegalStateException("resetting password from event failed: " + err)
        }
        ()
      }
    }
  }
}
