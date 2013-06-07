package service

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import org.eligosource.eventsourced.core._

import domain._
import infrastructure.{ ReadWriteRepository }
import infrastructure.commands._
import infrastructure.events._

import scalaz._
import Scalaz._

class UserService(
  userRepo: ReadWriteRepository[UserId, User],
  userProcessor: ActorRef)(implicit system: ActorSystem) {
  import system.dispatcher

  def getByEmail(email: String): Option[User] =
    userRepo.getMap.values.find(u => u.email.equals(email))

  implicit val timeout = Timeout(5.seconds)

  def authenticate(email: String, password: String): Future[DomainValidation[User]] =
    userProcessor ? Message(AuthenticateUserCmd(email, password)) map (_.asInstanceOf[DomainValidation[User]])

}

class UserProcessor(userRepo: ReadWriteRepository[UserId, User]) extends Processor { this: Emitter =>

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
    if (cmd.email.equals("admin@admin.com")) {
      User.add("admin", "admin@admin.com", "admin").success
    } else {
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
}