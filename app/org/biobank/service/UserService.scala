package org.biobank.service

import org.biobank.domain._
import org.biobank.domain.validation.UserValidationHelper
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service.Messages._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import play.api.Logger
import securesocial.core.{ Identity, SocialUser, PasswordInfo, AuthenticationMethod }
import securesocial.core.providers.utils.PasswordHasher
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import akka.actor.ActorLogging
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._
import scalaz.Scalaz._

trait UserServiceComponent {

  val userService: UserService

  trait UserService extends ApplicationService {

    def find(id: securesocial.core.IdentityId): Option[securesocial.core.Identity]

    def findByEmailAndProvider(
      email: String, providerId: String): Option[securesocial.core.Identity]

    def getByEmail(email: String): DomainValidation[User]

    def add(cmd: AddUserCommand): Future[DomainValidation[UserAddedEvent]]

  }
}

trait UserServiceComponentImpl extends UserServiceComponent {
  self: RepositoryComponent =>

  class UserServiceImpl(userProcessor: ActorRef)(implicit system: ActorSystem) extends UserService {

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

    def add(cmd: AddUserCommand): Future[DomainValidation[UserAddedEvent]] = {
      userProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserAddedEvent]])
    }

  }
}

trait UserProcessorComponent {

  trait UserProcessor extends Processor[UserId, User]

}

case class SnapshotState(users: Set[User])

trait UserProcessorComponentImpl extends UserProcessorComponent {
  self: RepositoryComponent =>

  /**
    * Handles the commands to configure users.
    */
  class UserProcessorImpl extends UserProcessor {

    override val repository = userRepository

    val receiveRecover: Receive = {
      case event: UserAddedEvent =>
        recoverUser(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.users.foreach(i => userRepository.put(i))

      case _ =>
	throw new IllegalStateException("message not handled")
    }

    val receiveCommand: Receive = {
      case cmd: AddUserCommand => process(validateCmd(cmd)){ event => recoverUser(event) }

      case cmd: ActivateUserCommand => process(validateCmd(cmd)){ event => recoverUser(event) }

      case cmd: LockUserCommand => process(validateCmd(cmd)){ event => recoverUser(event) }

      case cmd: UnlockUserCommand => process(validateCmd(cmd)){ event => recoverUser(event) }

      case "snap" =>
        saveSnapshot(SnapshotState(userRepository.allUsers))
        stash()

      case _ =>
	throw new IllegalStateException("message not handled")
    }

    def validateCmd(cmd: AddUserCommand): DomainValidation[UserAddedEvent] = {
      for {
        emailAvailable <- repository.emailAvailable(cmd.email)
        user <- RegisteredUser.create(UserId(cmd.email), -1L, cmd.name, cmd.email,
          cmd.password, cmd.hasher, cmd.salt, cmd.avatarUrl)
        event <- UserAddedEvent(user.id.toString, user.name, user.email,
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

    def unlockUser(cmd: UnlockUserCommand) = {
    }

    def recoverUser(event: UserAddedEvent) = {
      log.debug(s"recoverUser: $event")
      val validation = RegisteredUser.create(UserId(event.email), -1L, event.name, event.email,
        event.password, event.hasher, event.salt, event.avatarUrl)

      if (validation.isFailure) {
          // this should never happen because the only way to get here is that the
          // command passed validation
          throw new IllegalStateException("creating user from event failed")
      } else {
	repository.put(validation.getOrElse(null))
      }
    }

    def recoverUser(event: UserActivatedEvent) = {
      log.debug(s"recoverUser: $event")
      val validation = repository.getByKey(UserId(event.id))

      if (validation.isFailure) {
          // this should never happen because the only way to get here is that the
          // command passed validation
          throw new IllegalStateException("creating user from event failed")
      } else {
	val user = validation.getOrElse(null)
	repository.put(user.activate)
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
  }
}
