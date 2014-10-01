package org.biobank.service.users

import org.biobank.service.PasswordHasher
import org.biobank.fixture._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.domain._
import org.biobank.domain.user._

import akka.pattern.ask
import org.joda.time.DateTime
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import scaldi.MutableInjectorAggregation
import scalaz._
import scalaz.Scalaz._

class UsersProcessorSpec extends TestFixture {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val userRepository = inject [UserRepository]

  val passwordHasher = inject [PasswordHasher]

  val usersProcessor = injectActorRef [UsersProcessor]

  val nameGenerator = new NameGenerator(this.getClass)

  "A user processor" must {

    "add a user" in {
      val user = factory.createRegisteredUser

      val cmd = RegisterUserCmd(user.name, user.email, user.password, user.avatarUrl)
      val v = ask(usersProcessor, cmd, None)
        .mapTo[DomainValidation[UserRegisteredEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a [UserRegisteredEvent]
        event must have (
          'name (user.name),
          'email (user.email),
          'avatarUrl (user.avatarUrl)
        )

        event.password must not be(user.password)    // password mustBe encrypted
        event.salt.size must be > 0                  // salt must not be empty

        userRepository.getRegistered(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, DateTime.now, None)
        }
      }
    }

    "not add a user with an already registered email address" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val cmd = RegisterUserCmd(user.name, user.email, user.password, user.avatarUrl)
      val v = ask(usersProcessor, cmd, None).mapTo[DomainValidation[UserRegisteredEvent]].futureValue
      v mustFail "user with email already exists"
    }

    "activate a user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, ActivateUserCmd(user.id.id, user.version))
        .mapTo[DomainValidation[UserActivatedEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[UserActivatedEvent]
        userRepository.getActive(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, user.timeAdded, DateTime.now)
        }
      }
    }

    "not activate a user with a bad version" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, ActivateUserCmd(user.id.id, user.version - 1))
        .mapTo[DomainValidation[UserActivatedEvent]]
        .futureValue
      v mustFail "expected version doesn't match current version"
    }

    "update a user's name" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newName = nameGenerator.next[User]

      val cmd = UpdateUserNameCmd(user.id.id, user.version, newName)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserNameUpdatedEvent]].futureValue

      v mustSucceed { event =>
        event mustBe a [UserNameUpdatedEvent]
        event must have (
          'id        (user.id.id),
          'version   (user.version + 1),
          'name      (newName)
        )

        userRepository.getActive(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, user.timeAdded, DateTime.now)
        }
      }
    }

    "not update a user's name with an invalid version" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newName = nameGenerator.next[User]

      val cmd = UpdateUserNameCmd(user.id.id, user.version - 1, newName)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserNameUpdatedEvent]].futureValue
      v mustFail "expected version doesn't match current version"
    }

    "update a user's email" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newEmail = nameGenerator.nextEmail[User]

      val cmd = UpdateUserEmailCmd(user.id.id, user.version, newEmail)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEmailUpdatedEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a [UserEmailUpdatedEvent]
        event must have (
          'id        (user.id.id),
          'version   (user.version + 1),
          'email     (newEmail)
        )

        userRepository.getActive(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, user.timeAdded, DateTime.now)
        }
      }
    }

    "not update a user's email with an invalid version" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newEmail = nameGenerator.next[User]

      val cmd = UpdateUserEmailCmd(user.id.id, user.version - 1, newEmail)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEmailUpdatedEvent]].futureValue
      v mustFail "expected version doesn't match current version"
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

      v mustSucceed { event =>
        event mustBe a [UserPasswordUpdatedEvent]
        event must have (
          'id        (user.id.id),
          'version   (user.version + 1)
        )

        // password mustBe encrypted
        event.password must not be(newPassword)
        event.salt.length must be > 0

        userRepository.getActive(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, user.timeAdded, DateTime.now)
        }
      }
    }

    "not update a user's password with an invalid version" in {
      val plainPassword = nameGenerator.next[User]
      val salt = passwordHasher.generateSalt
      val encryptedPassword = passwordHasher.encrypt(plainPassword, salt)
      val user = factory.createActiveUser.copy(password = encryptedPassword, salt = salt)
      userRepository.put(user)

      val newPassword = nameGenerator.nextEmail[User]

      val cmd = UpdateUserPasswordCmd(user.id.id, user.version - 1, plainPassword, newPassword)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserPasswordUpdatedEvent]].futureValue
      v mustFail "expected version doesn't match current version"
    }

    "reset a user's password" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newPassword = nameGenerator.nextEmail[User]

      val cmd = ResetUserPasswordCmd(user.email)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserPasswordResetEvent]].futureValue

      v mustSucceed { event =>
        event mustBe a [UserPasswordResetEvent]
        event must have (
          'id        (user.id.id),
          'version   (user.version + 1)
        )

        // password mustBe encrypted
        event.password.length must be > 0
        event.salt.length must be > 0

        userRepository.getActive(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, user.timeAdded, DateTime.now)
        }
      }
    }

    "not reset a password with an invalid email" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newPassword = nameGenerator.nextEmail[User]

      val cmd = ResetUserPasswordCmd(nameGenerator.nextEmail[User])
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserPasswordResetEvent]].futureValue
      v mustFail "user with email not found"
    }

    "lock an activated a user" in {
      val activeUser = factory.createActiveUser
      userRepository.put(activeUser)

      val v = ask(usersProcessor, LockUserCmd(activeUser.id.id, activeUser.version))
        .mapTo[DomainValidation[UserLockedEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[UserLockedEvent]
        event.id mustBe(activeUser.id.id)

        userRepository.getLocked(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, activeUser.timeAdded, DateTime.now)
        }
      }
    }

    "not lock an activated a user with a bad version" in {
      val activeUser = factory.createActiveUser
      userRepository.put(activeUser)

      val v = ask(usersProcessor, LockUserCmd(activeUser.id.id, activeUser.version - 1))
        .mapTo[DomainValidation[UserLockedEvent]]
        .futureValue
      v mustFail "expected version doesn't match current version"
    }

    "unlock a locked a user" in {
      val lockedUser = factory.createLockedUser
      userRepository.put(lockedUser)

      val v = ask(usersProcessor, UnlockUserCmd(lockedUser.id.id, lockedUser.version))
        .mapTo[DomainValidation[UserUnlockedEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[UserUnlockedEvent]
        event.id mustBe(lockedUser.id.id)

        userRepository.getActive(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, lockedUser.timeAdded, DateTime.now)
        }
      }
    }

    "not unlock a locked a user with a bad version" in {
      val lockedUser = factory.createLockedUser
      userRepository.put(lockedUser)

      val v = ask(usersProcessor, UnlockUserCmd(lockedUser.id.id, lockedUser.version - 1))
        .mapTo[DomainValidation[UserUnlockedEvent]]
        .futureValue
      v mustFail "expected version doesn't match current version"
    }

    "not lock a registered user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, LockUserCmd(user.id.id, user.version))
        .mapTo[DomainValidation[UserLockedEvent]]
        .futureValue
      v mustFail "not active"
    }

    "not unlock a registered user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, UnlockUserCmd(user.id.id, user.version))
        .mapTo[DomainValidation[UserLockedEvent]]
        .futureValue
      v mustFail "not locked"
    }

    "not unlock an active user" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val v = ask(usersProcessor, UnlockUserCmd(user.id.id, user.version))
        .mapTo[DomainValidation[UserLockedEvent]]
        .futureValue

      v mustFail "not locked"
    }

  }

}
