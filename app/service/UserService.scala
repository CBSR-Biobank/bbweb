package service

import infrastructure.{ DomainValidation, DomainError, ReadWriteRepository }
import infrastructure._
import infrastructure.commands._
import infrastructure.events._
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

class UserService(
  userRepo: ReadWriteRepository[domain.UserId, User],
  userProcessor: ActorRef)(implicit system: ActorSystem) {
  import system.dispatcher

  implicit val timeout = Timeout(5.seconds)

  def find(id: securesocial.core.UserId): Option[securesocial.core.Identity] = {
    if (Logger.isDebugEnabled) {
      Logger.debug("find { id: %s }".format(id.id))
    }
    userRepo.getValues.find(u => u.email.equals(id.id)) match {
      case Some(user) => some(toSecureSocialIdentity(user))
      case None => none
    }
  }

  def findByEmailAndProvider(
    email: String, providerId: String): Option[securesocial.core.Identity] = {
    if (Logger.isDebugEnabled) {
      Logger.debug("findByEmailAndProvider { email: %s, providerId: %s }".format(email, providerId))
    }
    userRepo.getValues.find {
      // FIXME: add provider
      user => user.email.equals(email)
    } match {
      case Some(user) => some(toSecureSocialIdentity(user))
      case None => none
    }
  }

  def getByEmail(email: String): Option[User] =
    userRepo.getMap.values.find(u => u.email.equals(email))

  def toSecureSocialIdentity(user: User): securesocial.core.Identity = {
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
        userProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[User]])
        user
      case None => null
    }
  }

}

class UserProcessor(
  userRepo: ReadWriteRepository[domain.UserId, User]) extends Processor {
  this: Emitter =>

  def receive = {
    case msg: BiobankMsg =>
      msg.cmd match {
        case cmd: AddUserCmd =>
          process(addUser(cmd, emitter("listenter")))

        case _ =>
          throw new Error("invalid command received: ")

      }
  }

  def addUser(cmd: AddUserCmd, listeners: MessageEmitter): DomainValidation[User] = {
    def userIdExists(id: Option[String]): DomainValidation[UserId] = {
      id match {
        case Some(id) => new UserId(id).success
        case None => DomainError("missing ID value").fail
      }
    }

    def userExists(email: String): DomainValidation[Boolean] = {
      if (userRepo.getValues.exists(u => u.email.equals(cmd.email)))
        DomainError("user with email already exists: %s" format cmd.email).fail
      else
        true.success
    }

    def addItem(item: User) {
      userRepo.updateMap(item)
      listeners sendEvent UserAddedEvent(item.id, item.name, item.email)
    }

    for {
      userId <- userIdExists(cmd.userId)
      emailCheck <- userExists(cmd.email)
      newUser <- User.add(userId, cmd.name, cmd.email, cmd.password, cmd.hasher, cmd.salt, cmd.salt)
      addedItem <- addItem(newUser).success
    } yield newUser
  }
}
