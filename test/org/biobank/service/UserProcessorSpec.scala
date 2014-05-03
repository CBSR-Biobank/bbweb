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
      val user = factory.createRegisteredUser

      val cmd = RegisterUserCommand(user.name, user.email, user.password, user.hasher,
        user.salt, user.avatarUrl)
      val validation = ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]]
        .futureValue

      validation should be ('success)
      validation map { event =>
        event shouldBe a [UserRegisterdEvent]
        event should have (
          'id (user.email),
          'name (user.name),
          'email (user.email),
          'password (user.password),
          'hasher (user.hasher),
          'salt (user.salt),
          'avatarUrl (user.avatarUrl)
        )

        userRepository.getByKey(UserId(event.id)) map { user =>
          user shouldBe a[RegisteredUser]
        }
      }
    }

    "not add a user with an already registered email address" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val cmd = RegisterUserCommand(user.name, user.email, user.password, user.hasher,
        user.salt, user.avatarUrl)
      val validation2 = ask(userProcessor, cmd).mapTo[DomainValidation[UserRegisterdEvent]]
        .futureValue
      validation2 should be ('failure)

      validation2.swap.map { err =>
        err.list should have length 1
        err.list.head should include ("user already exists")
      }
    }

    "activate a user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val validation2 = ask(userProcessor, ActivateUserCommand(user.email, Some(0L)))
        .mapTo[DomainValidation[UserActivatedEvent]]
        .futureValue

      validation2 should be ('success)
      validation2 map { event =>
        event shouldBe a[UserActivatedEvent]
        event.id should be(user.email)
      }
    }

    "lock an activated a user" in {
      val activeUser = factory.createActiveUser
      userRepository.put(activeUser)

      val validation = ask(userProcessor, LockUserCommand(activeUser.email, Some(1L)))
        .mapTo[DomainValidation[UserLockedEvent]]
        .futureValue

      validation should be ('success)
      validation map { event =>
        event shouldBe a[UserLockedEvent]
        event.id should be(activeUser.email)
      }
    }

    "not lock a registered user" in {
      val user = factory.createRegisteredUser
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

    "not unlock a registered user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val validation2 = ask(userProcessor, UnlockUserCommand(user.email, Some(0L)))
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
