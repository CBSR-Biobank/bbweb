package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.domain._

import akka.pattern.ask
import org.scalatest.Tag
import org.slf4j.LoggerFactory

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

  val nameGenerator = new NameGenerator(this.getClass)

  "A user processor" should {

    "add a user" in {
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = RegisterUserCommand(name, email, password, hasher, salt, avatarUrl)
      ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]].futureValue match {
        case Success(event) =>
          event shouldBe a [UserRegisterdEvent]
          event.id.toString should be(email)
          event.name should be(name)
          event.email should be(email)
          event.password should be(password)
          event.hasher should be(hasher)
          event.salt should be(salt)
          event.avatarUrl should be(avatarUrl)

          val user = userRepository.userWithId(UserId(event.id)).getOrElse(fail)
          user shouldBe a[RegisteredUser]

        case Failure(msg) =>
          fail(msg.list.mkString(", "))
      }
    }

    "not add a user with an already registered email address" in {
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = RegisterUserCommand(name, email, password, hasher, salt, avatarUrl)
      val event = ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]]
        .futureValue.getOrElse(fail)
      event.email should be(email)

      ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]].futureValue match {
        case Success(event) => fail
        case Failure(err) =>
          err.list should have length 1
          err.list.head should include ("user already exists")
      }
    }

    "activate a user" in {
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = RegisterUserCommand(name, email, password, hasher, salt, avatarUrl)
      val event = ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]]
        .futureValue.getOrElse(fail)

      ask(userProcessor, ActivateUserCommand(event.email, Some(0L)))
        .mapTo[DomainValidation[UserActivatedEvent]].futureValue match {
        case Success(event) => event.id should be(email)
        case Failure(err) => fail(err.list.mkString(","))
      }
    }

    "lock an activated a user" in {
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = RegisterUserCommand(name, email, password, hasher, salt, avatarUrl)
      ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]].futureValue.getOrElse(fail)

      ask(userProcessor, ActivateUserCommand(email, Some(0L)))
        .mapTo[DomainValidation[UserActivatedEvent]].futureValue.getOrElse(fail)

      ask(userProcessor, LockUserCommand(email, Some(1L)))
        .mapTo[DomainValidation[UserLockedEvent]].futureValue match {
        case Success(event) => event.id should be(email)
        case Failure(err) => fail(err.list.mkString(","))
      }
    }
  }

  "A user processor" can {

    "not lock a registered user" in {
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = RegisterUserCommand(name, email, password, hasher, salt, avatarUrl)
      ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]].futureValue.getOrElse(fail)

      ask(userProcessor, LockUserCommand(email, Some(0L)))
        .mapTo[DomainValidation[UserLockedEvent]].futureValue match {
        case Success(event) => fail
        case Failure(err) =>
          err.list should have length 1
          err.list.head should include ("the user is not active")
      }
    }

    "not unlock a registered user" taggedAs(Tag("SingleTest")) in {
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val cmd = RegisterUserCommand(name, email, password, hasher, salt, avatarUrl)
      ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]].futureValue.getOrElse(fail)

      ask(userProcessor, UnlockUserCommand(email, Some(0L)))
        .mapTo[DomainValidation[UserLockedEvent]].futureValue match {
        case Success(event) => fail
        case Failure(err) =>
          err.list should have length 1
          err.list.head should include ("the user is not active")
      }
    }

  }

}
