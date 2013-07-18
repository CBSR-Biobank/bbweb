package service

import domain.{ DomainValidation, DomainError }
import service.commands._
import service.events._
import domain._

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

import scalaz._
import Scalaz._

class UserService(userProcessor: ActorRef)(implicit system: ActorSystem)
  extends ApplicationService {
  import system.dispatcher

  //implicit val timeout = Timeout(5.seconds)

  def find(id: securesocial.core.UserId): Option[securesocial.core.Identity] = {
    UserRepository.userWithId(UserId(id.id)) match {
      case Success(user) => some(toSecureSocialIdentity(user))
      case Failure(x) => none
    }
  }

  def findByEmailAndProvider(
    email: String, providerId: String): Option[securesocial.core.Identity] = {
    UserRepository.userWithId(UserId(email)) match {
      case Success(user) => some(toSecureSocialIdentity(user))
      case Failure(x) => none
    }
  }

  def getByEmail(email: String): Option[User] = {
    UserRepository.userWithId(UserId(email)).toOption
  }

  private def toSecureSocialIdentity(user: User): securesocial.core.Identity = {
    SocialUser(securesocial.core.UserId(user.email, "userpass"),
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
        userProcessor ? Message(ServiceMsg(cmd, null)) map (_.asInstanceOf[DomainValidation[User]])
        user
      case None => null
    }
  }

}

class UserProcessor() extends Processor {
  this: Emitter =>

  def receive = {
    case msg: ServiceMsg =>
      msg.cmd match {
        case cmd: AddUserCmd =>
          process(addUser(cmd, emitter("listenter")))

        case other => // must be for another command handler
      }

    case x =>
      throw new Error("invalid message received: " + x)
  }

  def addUser(cmd: AddUserCmd, listeners: MessageEmitter): DomainValidation[User] = {
    UserRepository.add(RegisteredUser(UserId(cmd.email), 0L, cmd.name, cmd.email,
      cmd.password, cmd.hasher, cmd.salt, cmd.avatarUrl))
  }
}
