package service

import infrastructure.{ DomainValidation, DomainError, ReadWriteRepository }
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
import securesocial.core._

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
      Logger.debug("find user id: %s".format(id))
    }
    userRepo.getByKey(new domain.UserId(id.id)) match {
      case Success(user) => some(toSecureSocialUser(user))
      case Failure(x) => none
    }
  }

  def findByEmailAndProvider(
    email: String, providerId: String): Option[securesocial.core.Identity] = {
    if (Logger.isDebugEnabled) {
      Logger.debug("find user { email: %s, providerId: %s }".format(email, providerId))
    }
    userRepo.getValues.find {
      // FIXME: add provider
      user => user.email.equals(email)
    } match {
      case Some(user) => some(toSecureSocialUser(user))
      case None => none
    }
  }

  def getByEmail(email: String): Option[User] =
    userRepo.getMap.values.find(u => u.email.equals(email))

  def toSecureSocialUser(user: User): securesocial.core.SocialUser = {
    SocialUser(securesocial.core.UserId(user.id.id, user.id.id),
      "", "", user.name, some(user.email), None, AuthenticationMethod(""), None, None)
  }

  def add(user: securesocial.core.Identity): securesocial.core.Identity = {
    // FIXME: add password
    userProcessor ? Message(AddUserCmd(user.fullName, user.email.getOrElse(""), "")) map {
      _.asInstanceOf[DomainValidation[User]]
    }
    user
  }

}

class UserProcessor(
  userRepo: ReadWriteRepository[domain.UserId, User]) extends Processor {
  this: Emitter =>

  def receive = {
    case cmd: AddUserCmd =>
      process(addUser(cmd, emitter("listenter")))
    case cmd: AuthenticateUserCmd =>
      process(authenticateUser(cmd, emitter("listenter")))
  }

  def addUser(cmd: AddUserCmd, listeners: MessageEmitter): DomainValidation[User] = {
    userRepo.getMap.values.find(u => u.email.equals(cmd.email)) match {
      case Some(user) =>
        DomainError("user with email already exists: %s" format cmd.email).fail
      case None =>
        val newUser = User.add(cmd.name, cmd.email, cmd.password)
        userRepo.updateMap(newUser)
        listeners sendEvent UserAddedEvent(newUser.id, newUser.name, newUser.email)
        newUser.success
    }
  }

  def authenticateUser(cmd: AuthenticateUserCmd, listeners: MessageEmitter): DomainValidation[User] = {
    userRepo.getMap.values.find(u => u.email.equals(cmd.email)) match {
      case Some(user) =>
        val v = user.authenticate(cmd.email, cmd.password)
        v match {
          case Success(u) => listeners sendEvent UserAuthenticatedEvent(u.id, u.name, u.email)
          case Failure(_) => // do nothing
        }
        v
      case None => DomainError("authentication failure").fail
    }
  }
}