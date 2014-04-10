package service

import fixture._
import infrastructure.command.UserCommands._
import infrastructure.event.UserEvents._
import domain._

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.ask
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

import scalaz._
import scalaz.Scalaz._

/**
 * Note: to run from Eclipse uncomment the @RunWith line. To run from SBT the line should be
 * commented out.
 *
 */
//@RunWith(classOf[JUnitRunner])
class UserProcessorSpec extends UserProcessorFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  override val nameGenerator = new NameGenerator(this.getClass.getName)

  "A user processor" should {

    "add a user" in {
      val name = nameGenerator.next[User]
      val email = java.util.UUID.randomUUID.toString.toUpperCase + "@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = AddUserCommand(name, email, password, hasher, salt, avatarUrl)
      val future = ask(userProcessor, cmd).mapTo[DomainValidation[UserAddedEvent]]

      waitNonBlocking(future) { r =>
        r match {
          case Success(event) =>
            event.name should be(name)
            event.email should be(email)
            event.password should be(password)
            event.hasher should be(hasher)
            event.salt should be(salt)
            event.avatarUrl should be(avatarUrl)

            userRepository.userWithId(UserId(event.id)).map { u =>
              u.version should be(0L)
            }

          case Failure(msg) =>
            val errors = msg.list.mkString(", ")
            fail(s"Error: $errors")
        }
      }
    }

    "not add a user with an already registered email address" in {
      val name = nameGenerator.next[User]
      val email = java.util.UUID.randomUUID.toString.toUpperCase + "@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = AddUserCommand(name, email, password, hasher, salt, avatarUrl)
      val future = ask(userProcessor, cmd).mapTo[DomainValidation[UserAddedEvent]]

      waitBlocking(future) match {
        case Failure(msg) =>
          fail(msg.list.mkString(", "))
        case Success(event) =>
      }

      val future2 = ask(userProcessor, cmd).mapTo[DomainValidation[UserAddedEvent]]
      waitBlocking(future2) match {
        case Success(event) =>
          fail

        case Failure(msg) =>
          msg.list.mkString(",") should startWith("user already exists")
      }
    }
  }
}
