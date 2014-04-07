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

  class UserServiceImpl() extends UserService {

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
          // FIXME: send command to aggregate root
          user
        case None => null
      }
    }

  }
}

trait UserAggregateComponent {

  trait UserAggregate extends Processor

}

trait UserAggregateComponentImpl extends UserAggregateComponent {
  self: RepositoryComponent =>

  class UserAggregateImpl extends UserAggregate {

    def receive = {
      case msg: ServiceMsg =>
        msg.cmd match {
          case cmd: AddUserCmd =>
            // FIXME: what if this user already exists?
            process(msg, addUser(cmd))

          case other => // must be for another command handler
        }

      case msg =>
        log.info("invalid message received: " + msg)
    }

    def addUser(cmd: AddUserCmd): DomainValidation[User] = {
      val item = for {
        newItem <- userRepository.add(RegisteredUser(UserId(cmd.email), 0L, cmd.name, cmd.email,
          cmd.password, cmd.hasher, cmd.salt, cmd.avatarUrl))
        event <- UserAddedEvent(newItem.id, newItem.name, newItem.email).success
      } yield newItem
      item
    }
  }
}
