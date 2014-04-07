package service

import domain._
import service.commands.UserCommands._
import service.events.UserEvents._
import service.Messages._

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
import ExecutionContext.Implicits.global

import scalaz._
import scalaz.Scalaz._

trait UserServiceComponent {

  val userService: UserService

  trait UserService extends ApplicationService {

    def find(id: securesocial.core.IdentityId): Option[securesocial.core.Identity]

    def findByEmailAndProvider(
      email: String, providerId: String): Option[securesocial.core.Identity]

    def getByEmail(email: String): Option[User]

    def add(cmd: AddUserCommand): Future[DomainValidation[UserAddedEvent]]

  }
}

trait UserServiceComponentImpl extends UserServiceComponent {
  self: RepositoryComponent =>

  class UserServiceImpl(userProcessor: ActorRef)(implicit system: ActorSystem) extends UserService {

    val log = LoggerFactory.getLogger(this.getClass)

    def find(id: securesocial.core.IdentityId): Option[securesocial.core.Identity] = {
      userRepository.userWithId(UserId(id.userId)) match {
        case Success(user) =>
          some(toSecureSocialIdentity(user))
        case Failure(x) => none
      }
    }

    def findByEmailAndProvider(
      email: String, providerId: String): Option[securesocial.core.Identity] = {
      userRepository.userWithId(UserId(email)) match {
        case Success(user) => some(toSecureSocialIdentity(user))
        case Failure(x) => none
      }
    }

    def getByEmail(email: String): Option[User] = {
      userRepository.userWithId(UserId(email)).toOption
    }

    private def toSecureSocialIdentity(user: User): securesocial.core.Identity = {
      SocialUser(securesocial.core.IdentityId(user.email, "userpass"),
        user.email, user.email, user.email,
        some(user.email), None, AuthenticationMethod.UserPassword, None, None,
        some(PasswordInfo(PasswordHasher.BCryptHasher, user.password, None)))
    }

    def add(cmd: AddUserCommand): Future[DomainValidation[UserAddedEvent]] = {
      log.info(s"add: $cmd")
      userProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserAddedEvent]])
    }

  }
}

trait UserProcessorComponent {

  trait UserProcessor extends Processor

}

case class SnapshotState(users: Set[User])

trait UserProcessorComponentImpl extends UserProcessorComponent {
  self: RepositoryComponent =>

  class UserProcessorImpl extends UserProcessor {

    def updateState(event: UserEvent) {
      event match {
        case event: UserAddedEvent =>
          userRepository.add(RegisteredUser(UserId(event.email), 0L, event.name, event.email,
            event.password, event.hasher, event.salt, event.avatarUrl))
        case event: UserAuthenticatedEvent =>
        // do nothing
      }
    }

    val receiveRecover: Receive = {
      case event: UserEvent => updateState(event)
      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.users.foreach(i => userRepository.update(i))
    }

    val receiveCommand: Receive = {
      case cmd: AddUserCommand =>
        addUser(cmd)

      case "snap" =>
        saveSnapshot(SnapshotState(userRepository.allUsers))
        stash()
    }

    def addUser(cmd: AddUserCommand) = {
      val evt = for {
        available <- userRepository.emailAvailable(cmd.email)
        event <- UserAddedEvent(new UserId(cmd.email), cmd.name, cmd.email, cmd.password,
          cmd.hasher, cmd.salt, cmd.avatarUrl).success
        save <- persist(event)(e => updateState(e)).success
      } yield event
      logEvent(evt)
      sender ! evt
    }
  }
}
