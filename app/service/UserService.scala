package service

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import org.eligosource.eventsourced.core._

import domain._
import service.commands._
import service.events._

import scalaz._
import Scalaz._

class UserService(
  usersRef: Ref[Map[UserId, User]], userProcessor: ActorRef)(implicit system: ActorSystem) {
  import system.dispatcher

  //
  // Consistent reads
  //

  def getUsersMap = usersRef.single.get
  def getByEmail(email: String): Option[User] =
    getUsersMap.find(u => u._2.email.equals(email)) match {
      case Some(v) => Some(v._2)
      case None => None
    }

  //
  // Updates  
  //

  implicit val timeout = Timeout(5.seconds)

  def authenticate(email: String, password: String): Future[DomainValidation[User]] =
    userProcessor ? Message(AuthenticateUserCmd(email, password)) map (_.asInstanceOf[DomainValidation[User]])

}

class UserProcessor(usersRef: Ref[Map[UserId, User]]) extends Actor { this: Emitter =>

  def receive = {
    case addUserCmd: AddUserCmd =>
      process(addUser(addUserCmd)) { user =>
        emitter("listenter") sendEvent UserAddedEvent(user.id, user.name, user.email)
      }
    case authenticateUserCmd: AuthenticateUserCmd =>
      process(authenticateUser(authenticateUserCmd)) { user =>
        emitter("listenter") sendEvent UserAddedEvent(user.id, user.name, user.email)
      }
  }

  //def process(validation: DomainValidation[User])(onSuccess: User => Unit) = {
  def process(validation: DomainValidation[User])(onSuccess: User => Unit) = {
    validation.foreach { user =>
      updateUsers(user)
      onSuccess(user)
    }
    sender ! validation
  }

  def addUser(cmd: AddUserCmd): DomainValidation[User] = {
    readUsers.find(u => u._2.email.equals(cmd.email)) match {
      case Some(user) => DomainError("user with email already exists: %s" format cmd.email).fail
      case None => User.add(User.nextIdentity, cmd.name, cmd.email, cmd.password)
    }
  }

  def authenticateUser(cmd: AuthenticateUserCmd): DomainValidation[User] = {
    readUsers.find(u => u._2.email.equals(cmd.email)) match {
      case Some(entry) => entry._2.authenticate(cmd.email, cmd.password)
      case None => DomainError("authentication failure").fail
    }

  }

  private def readUsers =
    usersRef.single.get

  private def updateUsers(user: User) =
    usersRef.single.transform(users => users + (user.id -> user))
}