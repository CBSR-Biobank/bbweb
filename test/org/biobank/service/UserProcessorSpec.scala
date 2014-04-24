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
      val validation = ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]]
	.futureValue
      validation should be ('success)

      validation map { event =>
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
      }
    }

    "not add a user with an already registered email address" in {
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val user = RegisteredUser.create(UserId(email), -1L, name, email, password, hasher, salt,
	avatarUrl) | fail
      userRepository.put(user)

      val cmd = RegisterUserCommand(name, email, password, hasher, salt, avatarUrl)
      val validation2 = ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]]
	.futureValue
      validation2 should be ('failure)

      validation2.swap.map { err =>
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

      val user = RegisteredUser.create(UserId(email), -1L, name, email, password, hasher, salt,
	avatarUrl) | fail
      userRepository.put(user)

      val validation2 = ask(userProcessor, ActivateUserCommand(user.email, Some(0L)))
        .mapTo[DomainValidation[UserActivatedEvent]]
	.futureValue

      validation2 should be ('success)
      validation2 map { event =>
	event shouldBe a[UserActivatedEvent]
	event.id should be(email)
      }
    }

    "lock an activated a user" in {
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val registeredUser = RegisteredUser.create(UserId(email), -1L, name, email, password, hasher, salt,
	avatarUrl) | fail
      val activeUser = registeredUser.activate(Some(0L)) | fail
      userRepository.put(activeUser)

      val validation = ask(userProcessor, LockUserCommand(activeUser.email, Some(1L)))
        .mapTo[DomainValidation[UserLockedEvent]]
	.futureValue

      validation should be ('success)
      validation map { event =>
	event shouldBe a[UserLockedEvent]
	event.id should be(email)
      }
    }

    "not lock a registered user" in {
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val user = RegisteredUser.create(UserId(email), -1L, name, email, password, hasher, salt,
	avatarUrl) | fail
      userRepository.put(user)

      val validation2 = ask(userProcessor, LockUserCommand(user.email, Some(0L)))
        .mapTo[DomainValidation[UserLockedEvent]]
	.futureValue
      validation2 should be ('failure)

      validation2.swap map { err =>
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

      val user = RegisteredUser.create(UserId(email), -1L, name, email, password, hasher, salt,
	avatarUrl) | fail
      userRepository.put(user)

      val validation2 = ask(userProcessor, UnlockUserCommand(email, Some(0L)))
        .mapTo[DomainValidation[UserLockedEvent]]
	.futureValue
      validation2 should be ('failure)

      validation2.swap map { err =>
        err.list should have length 1
        err.list.head should include ("the user is not active")
      }
    }

  }

}
