package org.biobank.service

import fixture._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.domain._

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

  override def beforeAll: Unit = {
    super.beforeAll
  }

  "A user processor" should {

    "add a user" in {
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = AddUserCommand(name, email, password, hasher, salt, avatarUrl)
      val future = ask(userProcessor, cmd).mapTo[DomainValidation[UserAddedEvent]]

      waitNonBlocking(future) { r =>
        r match {
          case Success(event) =>
            event.id.toString should be(email)
            event.name should be(name)
            event.email should be(email)
            event.password should be(password)
            event.hasher should be(hasher)
            event.salt should be(salt)
            event.avatarUrl should be(avatarUrl)

            userRepository.userWithId(UserId(event.id)).map { user =>
              user.email should be(email)
            }

          case Failure(msg) =>
            val errors = msg.list.mkString(", ")
            fail(s"Error: $errors")
        }
      }
    }

    "not add a user with an already registered email address" in {
      val name = nameGenerator.next[User]
      val email = "user2@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = AddUserCommand(name, email, password, hasher, salt, avatarUrl)
      val r = waitBlocking(ask(userProcessor, cmd).mapTo[DomainValidation[UserAddedEvent]])

      val user = r.getOrElse(fail("failure response from processor"))
      user.email should be(email)

      waitBlocking(ask(userProcessor, cmd).mapTo[DomainValidation[UserAddedEvent]]) match {
        case Success(event) => fail

        case Failure(msg) =>
          msg.list.mkString(",") should startWith("user already exists")
      }
    }

    "activate a user" in {
      val name = nameGenerator.next[User]
      val email = "user3@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = AddUserCommand(name, email, password, hasher, salt, avatarUrl)
      val r = waitBlocking(ask(userProcessor, cmd).mapTo[DomainValidation[UserAddedEvent]])

      val event = r.getOrElse(fail("failure response from processor"))

      waitBlocking(ask(userProcessor, ActivateUserCommand(event.email, Some(event.version)))
        .mapTo[DomainValidation[UserActivatedEvent]]) match {
        case Success(event) =>
          event.id should be(email)

        case Failure(msg) =>
          fail(msg.list.mkString(","))
      }

    }
  }
}
