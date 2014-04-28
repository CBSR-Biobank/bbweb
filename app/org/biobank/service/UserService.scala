package org.biobank.service

import org.biobank.domain._
import org.biobank.domain.validation.UserValidationHelper
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service.Messages._

import akka.actor.{ ActorSystem, ActorRef }
import scala.concurrent.Future
import akka.pattern.ask
import securesocial.core.{ Identity, SocialUser, PasswordInfo, AuthenticationMethod }
import securesocial.core.providers.utils.PasswordHasher
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._
import scalaz.Scalaz._

trait UserServiceComponent {
  self: RepositoryComponent =>

  class UserService(userProcessor: ActorRef)(implicit system: ActorSystem) extends ApplicationService {

    val log = LoggerFactory.getLogger(this.getClass)

    def find(id: securesocial.core.IdentityId): Option[securesocial.core.Identity] = {
      userRepository.userWithId(UserId(id.userId)) match {
        case Success(user) => Some(toSecureSocialIdentity(user))
        case Failure(err) =>
          log.error(err.list.mkString(","))
          none
      }
    }

    def findByEmailAndProvider(
      email: String, providerId: String): Option[securesocial.core.Identity] = {
      userRepository.userWithId(UserId(email)) match {
        case Success(user) => some(toSecureSocialIdentity(user))
        case Failure(err) =>
          log.error(err.list.mkString(","))
          none
      }
    }

    def getByEmail(email: String): DomainValidation[User] = {
      userRepository.userWithId(UserId(email))
    }

    private def toSecureSocialIdentity(user: User): securesocial.core.Identity = {
      SocialUser(securesocial.core.IdentityId(user.email, "userpass"),
        user.email, user.email, user.email,
        some(user.email), None, AuthenticationMethod.UserPassword, None, None,
        some(PasswordInfo(PasswordHasher.BCryptHasher, user.password, None)))
    }

    def add(cmd: RegisterUserCommand): Future[DomainValidation[UserRegisterdEvent]] = {
      userProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserRegisterdEvent]])
    }

  }
}

trait UserProcessorComponent {
  self: RepositoryComponent =>

  case class SnapshotState(users: Set[User])

  /**
    * Handles the commands to configure users.
    */
  class UserProcessor extends Processor {

    val receiveRecover: Receive = {
      case event: UserRegisterdEvent => recoverEvent(event)

      case event: UserActivatedEvent => recoverEvent(event)

      case event: UserLockedEvent => recoverEvent(event)

      case event: UserUnlockedEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.users.foreach(i => userRepository.put(i))

      case _ =>
	throw new IllegalStateException("message not handled")
    }

    val receiveCommand: Receive = {
      case cmd: RegisterUserCommand => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: ActivateUserCommand => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: LockUserCommand => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: UnlockUserCommand => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case "snap" =>
        saveSnapshot(SnapshotState(userRepository.allUsers))
        stash()

      case _ =>
	throw new IllegalStateException("message not handled")
    }

    def validateCmd(cmd: RegisterUserCommand): DomainValidation[UserRegisterdEvent] = {
      for {
        emailAvailable <- userRepository.emailAvailable(cmd.email)
        user <- RegisteredUser.create(UserId(cmd.email), -1L, cmd.name, cmd.email,
          cmd.password, cmd.hasher, cmd.salt, cmd.avatarUrl)
        event <- UserRegisterdEvent(user.id.toString, user.name, user.email,
          user.password, user.hasher, user.salt, user.avatarUrl).success
      } yield {
        event
      }
    }

    def validateCmd(cmd: ActivateUserCommand): DomainValidation[UserActivatedEvent] = {
      for {
        user <- userRepository.userWithId(UserId(cmd.email))
        registeredUser <- isUserRegistered(user)
        activatedUser <- registeredUser.activate(cmd.expectedVersion)
        event <- UserActivatedEvent(activatedUser.id.toString, activatedUser.version).success
      } yield {
        event
      }
    }

    def validateCmd(cmd: LockUserCommand): DomainValidation[UserLockedEvent] = {
      for {
        user <- userRepository.userWithId(UserId(cmd.email))
        activeUser <- isUserActive(user)
        lockedUser <- activeUser.lock(cmd.expectedVersion)
        event <- UserLockedEvent(lockedUser.id.toString, lockedUser.version).success
      } yield {
        event
      }
    }

    def validateCmd(cmd: UnlockUserCommand): DomainValidation[UserUnlockedEvent] = {
      for {
        user <- userRepository.userWithId(UserId(cmd.email))
        lockedUser <- isUserLocked(user)
        unlockedUser <- lockedUser.unlock(cmd.expectedVersion)
        event <- UserUnlockedEvent(lockedUser.id.toString, lockedUser.version).success
      } yield {
        event
      }
    }

    def recoverEvent(event: UserRegisterdEvent) = {
      log.debug(s"recoverEvent: $event")
      val validation = for {
	registeredUser <- RegisteredUser.create(UserId(event.email), -1L, event.name, event.email,
          event.password, event.hasher, event.salt, event.avatarUrl)
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
	activatedUser <- registeredUser.activate(Some(registeredUser.version))
	savedUser <- userRepository.put(activatedUser).success
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
	lockedUser <- activeUser.lock(Some(activeUser.version))
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
	unlockedUser <- lockedUser.unlock(Some(lockedUser.version))
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
