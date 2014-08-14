package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.domain._
import org.biobank.domain.user._

import akka.pattern.ask
import org.scalatest.Tag
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

class UsersProcessorSpec extends UsersProcessorFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A user processor" should {

    "add a user" in {
      val user = factory.createRegisteredUser

      val cmd = RegisterUserCmd(user.name, user.email, user.password, user.avatarUrl)
      val validation = ask(usersProcessor, cmd).mapTo[DomainValidation[UserRegisteredEvent]]
        .futureValue

      validation should be ('success)
      validation map { event =>
        event shouldBe a [UserRegisteredEvent]
        event should have (
          'id (user.email),
          'name (user.name),
          'email (user.email),
          'avatarUrl (user.avatarUrl)
        )

        // password should be encrypted
        event.password should not be(user.password)

        userRepository.getByKey(UserId(event.id)) map { user =>
          user shouldBe a[RegisteredUser]
        }
      }
    }

    "update a user" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val user2 = factory.createActiveUser

      val cmd = UpdateUserCmd(
        user.version, user2.name, user2.email, Some(user2.password), user2.avatarUrl)
      val validation = ask(usersProcessor, cmd).mapTo[DomainValidation[UserUpdatedEvent]]
        .futureValue

      validation should be ('success)
      validation map { event =>
        event shouldBe a [UserUpdatedEvent]
        event should have (
          'id        (user.email),
          'version   (user.version + 1),
          'name      (user2.name),
          'email     (user2.email),
          'avatarUrl (user2.avatarUrl)
        )

        // password should be encrypted
        event.password should not be(user2.password)
        userRepository.getByKey(UserId(event.id)) map { user =>
          user shouldBe a[ActiveUser]
        }
      }
    }

    "not add a user with an already registered email address" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val cmd = RegisterUserCmd(user.name, user.email, user.password, user.avatarUrl)
      val validation2 = ask(usersProcessor, cmd).mapTo[DomainValidation[UserRegisteredEvent]]
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

      val validation2 = ask(usersProcessor, ActivateUserCmd(user.email, 0L))
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

      val validation = ask(usersProcessor, LockUserCmd(activeUser.email, 1L))
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

      val validation2 = ask(usersProcessor, LockUserCmd(user.email, 0L))
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

      val validation2 = ask(usersProcessor, UnlockUserCmd(user.email, 0L))
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
