package service

import domain._
import service.commands._
import service.events._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import play.api.Logger
import securesocial.core.{ Identity, SocialUser, PasswordInfo, AuthenticationMethod }
import securesocial.core.providers.utils.PasswordHasher
import org.eligosource.eventsourced.core._
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

trait UserServiceComponent {

  val userService: UserService

  trait UserService extends ApplicationService {

    def find(id: securesocial.core.IdentityId): Option[securesocial.core.Identity]

    def findByEmailAndProvider(
      email: String, providerId: String): Option[securesocial.core.Identity]

    def getByEmail(email: String): Option[User]

    def add(user: securesocial.core.Identity): securesocial.core.Identity

  }
}

trait UserServiceComponentImpl extends UserServiceComponent {
  self: RepositoryComponent =>

  class UserServiceImpl(commandBus: ActorRef)(implicit system: ActorSystem) extends UserService {
    import system.dispatcher

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

    def add(user: securesocial.core.Identity): securesocial.core.Identity = {
      user.passwordInfo match {
        case Some(passwordInfo) =>
          val cmd = AddUserCmd(user.fullName, user.email.getOrElse(""),
            passwordInfo.password, passwordInfo.hasher, passwordInfo.salt,
            user.avatarUrl)
          commandBus ? Message(ServiceMsg(cmd, null)) map (_.asInstanceOf[DomainValidation[User]])
          user
        case None => null
      }
    }

  }
}

trait UserProcessorComponent {

  trait UserProcessor extends Processor

}

trait UserProcessorComponentImpl extends UserProcessorComponent {
  self: RepositoryComponent =>

  class UserProcessorImpl extends UserProcessor {
    self: Emitter =>

    def receive = {
      case msg: ServiceMsg =>
        msg.cmd match {
          case cmd: AddUserCmd =>
            process(addUser(cmd, emitter("eventBus")))

          case other => // must be for another command handler
        }

      case msg =>
        log.info("invalid message received: " + msg)
    }

    def addUser(cmd: AddUserCmd, listeners: MessageEmitter): DomainValidation[User] = {
      val item = for {
        newItem <- userRepository.add(RegisteredUser(UserId(cmd.email), 0L, cmd.name, cmd.email,
          cmd.password, cmd.hasher, cmd.salt, cmd.avatarUrl))
        event <- (listeners sendEvent UserAddedEvent(
          newItem.id, newItem.name, newItem.email)).success
      } yield newItem
      item
    }
  }
}
