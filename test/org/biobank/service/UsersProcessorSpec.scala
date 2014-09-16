package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.domain._
import org.biobank.domain.user._

import akka.pattern.ask
import org.scalatest.Tag
import org.scalatest.OptionValues._
import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._

import scalaz._
import scalaz.Scalaz._

class UsersProcessorSpec extends UsersProcessorFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  val TimeCoparisonMillis = 600L

  def checkTimeStamps(user: User, expectedAddedTime: DateTime, expectedLastUpdateTime: Option[DateTime]) = {
    (user.addedDate to expectedAddedTime).millis should be < TimeCoparisonMillis
    expectedLastUpdateTime.fold {
      user.lastUpdateDate should be (None)
    } {
      dateTime => (user.lastUpdateDate.value to dateTime).millis should be < TimeCoparisonMillis
    }

  }

  def checkTimeStamps(user: User, expectedAddedTime: DateTime, expectedLastUpdateTime: DateTime) = {
    (user.addedDate to expectedAddedTime).millis should be < 200L
    (user.lastUpdateDate.value to expectedLastUpdateTime).millis should be < TimeCoparisonMillis
  }

  "A user processor" should {

    "add a user" in {
      val user = factory.createRegisteredUser

      val cmd = RegisterUserCmd(user.name, user.email, user.password, user.avatarUrl)
      ask(usersProcessor, cmd).mapTo[DomainValidation[UserRegisteredEvent]].futureValue.fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a [UserRegisteredEvent]
          event should have (
            'name (user.name),
            'email (user.email),
            'avatarUrl (user.avatarUrl)
          )

          // password should be encrypted
          event.password should not be(user.password)

          // salt should not be empty
          event.salt.size should be > 0

          userRepository.getByKey(UserId(event.id)) map { user =>
            user shouldBe a[RegisteredUser]
            checkTimeStamps(user, DateTime.now, None)
          }
        }
      )
    }

    "not add a user with an already registered email address" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val cmd = RegisterUserCmd(user.name, user.email, user.password, user.avatarUrl)
      ask(usersProcessor, cmd).mapTo[DomainValidation[UserRegisteredEvent]].futureValue.fold(
        err => {
          err.list should have length 1
          err.list.head should include ("user with email already exists")
        },
        user => fail("command should fail")
      )
    }

    "activate a user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, ActivateUserCmd(user.email, user.version))
        .mapTo[DomainValidation[UserActivatedEvent]]
        .futureValue

      v.fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a[UserActivatedEvent]
          userRepository.getByKey(UserId(event.id)) map { repoUser =>
            repoUser shouldBe a[ActiveUser]
            checkTimeStamps(repoUser, user.addedDate, DateTime.now)
          }
        }
      )

    }

    "update a user's name" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newName = nameGenerator.next[User]

      val cmd = UpdateUserNameCmd(user.id.id, user.version, newName)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserNameUpdatedEvent]].futureValue

      v.fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a [UserNameUpdatedEvent]
          event should have (
            'id        (user.email),
            'version   (user.version + 1),
            'name      (newName)
          )

          userRepository.getByKey(UserId(event.id)) map { repoUser =>
            repoUser shouldBe a[ActiveUser]
            checkTimeStamps(repoUser, user.addedDate, DateTime.now)
          }
        }
      )
    }

    "update a user's email" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newEmail = nameGenerator.nextEmail[User]

      val cmd = UpdateUserEmailCmd(user.id.id, user.version, newEmail)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEmailUpdatedEvent]]
        .futureValue

      v.fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a [UserEmailUpdatedEvent]
          event should have (
            'id        (user.email),
            'version   (user.version + 1),
            'email     (newEmail)
          )

          userRepository.getByKey(UserId(event.id)) map { repoUser =>
            repoUser shouldBe a[ActiveUser]
            checkTimeStamps(repoUser, user.addedDate, DateTime.now)
          }
        }
      )
    }

    "update a user's password" in {
      val plainPassword = nameGenerator.next[User]
      val salt = passwordHasher.generateSalt
      val encryptedPassword = passwordHasher.encrypt(plainPassword, salt)
      val user = factory.createActiveUser.copy(password = encryptedPassword, salt = salt)
      userRepository.put(user)

      val newPassword = nameGenerator.nextEmail[User]

      val cmd = UpdateUserPasswordCmd(user.id.id, user.version, plainPassword, newPassword)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserPasswordUpdatedEvent]].futureValue

      v.fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a [UserPasswordUpdatedEvent]
          event should have (
            'id        (user.email),
            'version   (user.version + 1)
          )

          // password should be encrypted
          event.password should not be(newPassword)
          event.salt.length should be > 0

          userRepository.getByKey(UserId(event.id)) map { repoUser =>
            user shouldBe a[ActiveUser]
            checkTimeStamps(repoUser, user.addedDate, DateTime.now)
          }
        }
      )
    }

    "reset a user's password" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newPassword = nameGenerator.nextEmail[User]

      val cmd = ResetUserPasswordCmd(user.id.id, user.version)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserPasswordResetEvent]].futureValue

      v.fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a [UserPasswordResetEvent]
          event should have (
            'id        (user.email),
            'version   (user.version + 1)
          )

          // password should be encrypted
          event.password.length should be> 0
          event.salt.length should be > 0

          userRepository.getByKey(UserId(event.id)) map { repoUser =>
            user shouldBe a[ActiveUser]
            checkTimeStamps(repoUser, user.addedDate, DateTime.now)
          }
        }
      )
    }

    "lock an activated a user" in {
      val activeUser = factory.createActiveUser
      userRepository.put(activeUser)

      val v = ask(usersProcessor, LockUserCmd(activeUser.email, 1L))
        .mapTo[DomainValidation[UserLockedEvent]]
        .futureValue

      v.fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a[UserLockedEvent]
          event.id should be(activeUser.email)

          userRepository.getByKey(UserId(event.id)) map { repoUser =>
            repoUser shouldBe a[LockedUser]
            checkTimeStamps(repoUser, activeUser.addedDate, DateTime.now)
          }
        }
      )
    }

    "not lock a registered user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, LockUserCmd(user.email, 0L))
        .mapTo[DomainValidation[UserLockedEvent]]
        .futureValue

      v.fold(
        err => {
          err.list should have length 1
          err.list.head should include ("not active")
        },
        event => fail("should not be able to lock a registered user")
      )
    }

    "not unlock a registered user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, UnlockUserCmd(user.email, user.version))
        .mapTo[DomainValidation[UserLockedEvent]]
        .futureValue

      v.fold(
        err => {
          err.list should have length 1
          err.list.head should include ("not locked")
        },
        event => fail("should not be able to unlock a registered user")
      )
    }

    "not unlock an active user" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val v = ask(usersProcessor, UnlockUserCmd(user.email, user.version))
        .mapTo[DomainValidation[UserLockedEvent]]
        .futureValue

      v.fold(
        err => {
          err.list should have length 1
          err.list.head should include ("not locked")
        },
        event => fail("should not be able to unlock an active user")
      )
    }

  }

}
