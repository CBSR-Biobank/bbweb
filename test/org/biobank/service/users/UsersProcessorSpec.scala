package org.biobank.service.users

import org.biobank.fixture._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.domain._
import org.biobank.domain.user._

import akka.pattern._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import scalaz.Scalaz._
import akka.testkit.TestKit

class UsersProcessorSpec extends TestFixture {

  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A user processor" must {

    "add a user" in {
      val user = factory.createRegisteredUser

      val cmd = RegisterUserCmd(None, user.name, user.email, user.password, user.avatarUrl)
      val v = ask(usersProcessor, cmd)
        .mapTo[DomainValidation[UserEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a [UserEvent]
        event.eventType.isRegistered mustBe true
        event.getRegistered must have (
          'name (Some(user.name)),
          'email (Some(user.email)),
          'avatarUrl (user.avatarUrl)
        )

        event.getRegistered.getPassword must not be(user.password)    // password mustBe encrypted
        event.getRegistered.getSalt.size must be > 0                  // salt must not be empty

        userRepository.getRegistered(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, DateTime.now, None)
        }
      }
    }

    "not add a user with an already registered email address" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val cmd = RegisterUserCmd(None, user.name, user.email, user.password, user.avatarUrl)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]].futureValue
      v mustFailContains "user with email already exists"
    }

    "activate a user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, ActivateUserCmd(None, user.id.id, user.version))
        .mapTo[DomainValidation[UserEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[UserEvent]
        userRepository.getActive(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, user.timeAdded, DateTime.now)
        }
      }
    }

    "not activate a user with a bad version" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, ActivateUserCmd(None, user.id.id, user.version - 1))
        .mapTo[DomainValidation[UserEvent]]
        .futureValue
      v mustFailContains "expected version doesn't match current version"
    }

    "update a user's name" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newName = nameGenerator.next[User]

      val cmd = UpdateUserNameCmd(None, user.id.id, user.version, newName)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]].futureValue

      v mustSucceed { event =>
        event mustBe a [UserEvent]
        event.id mustBe user.id.id
        event.eventType.isNameUpdated mustBe true
        event.getNameUpdated must have (
          'version   (Some(user.version)),
          'name      (Some(newName))
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

      val cmd = UpdateUserNameCmd(None, user.id.id, user.version - 1, newName)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]].futureValue
      v mustFailContains "expected version doesn't match current version"
    }

    "update a user's email" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val newEmail = nameGenerator.nextEmail[User]

      val cmd = UpdateUserEmailCmd(None, user.id.id, user.version, newEmail)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a [UserEvent]
        event.id mustBe user.id.id
        event.eventType.isEmailUpdated mustBe true
        event.getEmailUpdated must have (
          'version   (Some(user.version)),
          'email     (Some(newEmail))
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

      val cmd = UpdateUserEmailCmd(None, user.id.id, user.version - 1, newEmail)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]].futureValue
      v mustFailContains "expected version doesn't match current version"
    }

    "update a user's password" in {
      val plainPassword = nameGenerator.next[User]
      val salt = passwordHasher.generateSalt
      val encryptedPassword = passwordHasher.encrypt(plainPassword, salt)
      val user = factory.createActiveUser.copy(password = encryptedPassword, salt = salt)
      userRepository.put(user)

      val newPassword = nameGenerator.nextEmail[User]

      val cmd = UpdateUserPasswordCmd(None, user.id.id, user.version, plainPassword, newPassword)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]].futureValue

      v mustSucceed { event =>
        event mustBe a [UserEvent]
        event.id mustBe user.id.id
        event.eventType.isPasswordUpdated mustBe true
        event.getPasswordUpdated.version mustBe (Some(user.version))

        // password mustBe encrypted
        event.getPasswordUpdated.password.value must not be(newPassword)
        event.getPasswordUpdated.salt.value.length must be > 0

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

      val cmd = UpdateUserPasswordCmd(None, user.id.id, user.version - 1, plainPassword, newPassword)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]].futureValue
      v mustFailContains "expected version doesn't match current version"
    }

    "reset a user's password" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val cmd = ResetUserPasswordCmd(user.email)
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]].futureValue

      v mustSucceed { event =>
        event mustBe a [UserEvent]
        event.id mustBe user.id.id
        event.eventType.isPasswordReset mustBe true
        event.getPasswordReset.version mustBe (Some(user.version))

        // password mustBe encrypted
        event.getPasswordReset.password.value.length must be > 0
        event.getPasswordReset.salt.value.length must be > 0

        userRepository.getActive(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, user.timeAdded, DateTime.now)
        }
      }
    }

    "not reset a password with an invalid email" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val cmd = ResetUserPasswordCmd(nameGenerator.nextEmail[User])
      val v = ask(usersProcessor, cmd).mapTo[DomainValidation[UserEvent]].futureValue
      v mustFailContains "EmailNotFound.*user email not found.*"
    }

    "lock an activated a user" in {
      val activeUser = factory.createActiveUser
      userRepository.put(activeUser)

      val v = ask(usersProcessor, LockUserCmd(None, activeUser.id.id, activeUser.version))
        .mapTo[DomainValidation[UserEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[UserEvent]
        event.id mustBe(activeUser.id.id)

        userRepository.getLocked(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, activeUser.timeAdded, DateTime.now)
        }
      }
    }

    "not lock an activated a user with a bad version" in {
      val activeUser = factory.createActiveUser
      userRepository.put(activeUser)

      val v = ask(usersProcessor, LockUserCmd(None, activeUser.id.id, activeUser.version - 1))
        .mapTo[DomainValidation[UserEvent]]
        .futureValue
      v mustFailContains "expected version doesn't match current version"
    }

    "unlock a locked a user" in {
      val lockedUser = factory.createLockedUser
      userRepository.put(lockedUser)

      val v = ask(usersProcessor, UnlockUserCmd(None, lockedUser.id.id, lockedUser.version))
        .mapTo[DomainValidation[UserEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[UserEvent]
        event.id mustBe(lockedUser.id.id)

        userRepository.getActive(UserId(event.id)) mustSucceed { repoUser =>
          checkTimeStamps(repoUser, lockedUser.timeAdded, DateTime.now)
        }
      }
    }

    "not unlock a locked a user with a bad version" in {
      val lockedUser = factory.createLockedUser
      userRepository.put(lockedUser)

      val v = ask(usersProcessor, UnlockUserCmd(None, lockedUser.id.id, lockedUser.version - 1))
        .mapTo[DomainValidation[UserEvent]]
        .futureValue
      v mustFailContains "expected version doesn't match current version"
    }

    "not lock a registered user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, LockUserCmd(None, user.id.id, user.version))
        .mapTo[DomainValidation[UserEvent]]
        .futureValue
      v mustFailContains "not active"
    }

    "not unlock a registered user" in {
      val user = factory.createRegisteredUser
      userRepository.put(user)

      val v = ask(usersProcessor, UnlockUserCmd(None, user.id.id, user.version))
        .mapTo[DomainValidation[UserEvent]]
        .futureValue
      v mustFailContains "not locked"
    }

    "not unlock an active user" in {
      val user = factory.createActiveUser
      userRepository.put(user)

      val v = ask(usersProcessor, UnlockUserCmd(None, user.id.id, user.version))
        .mapTo[DomainValidation[UserEvent]]
        .futureValue

      v mustFailContains "not locked"
    }

  }

}
