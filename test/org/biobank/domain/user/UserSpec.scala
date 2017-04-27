package org.biobank.domain.user

import org.biobank.domain.{DomainSpec, DomainValidation}
import org.biobank.fixture.NameGenerator
import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory

/**
 *
 */
class UserSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(user: RegisteredUser): DomainValidation[RegisteredUser] =
    RegisteredUser.create(id          = user.id,
                          version     = user.version,
                          name        = user.name,
                          email       = user.email,
                          password    = user.password,
                          salt        = user.salt,
                          avatarUrl   = user.avatarUrl)

  "A registered user" can {

    "be created" in {
      val user = factory.createRegisteredUser
      createFrom(user) mustSucceed { u =>
        u mustBe a[RegisteredUser]
        u must have (
          'id        (user.id),
          'version   (0),
          'name      (user.name),
          'email     (user.email),
          'password  (user.password),
          'salt      (user.salt),
          'avatarUrl (user.avatarUrl),
          'state     (User.registeredState.id)
        )

        checkTimeStamps(u, DateTime.now, None)
      }
    }

    "be activated" in {
      val user = factory.createRegisteredUser

      user.activate.mustSucceed { u =>
        u mustBe a[ActiveUser]

        u must have (
          'id        (user.id),
          'version   (user.version + 1),
          'name      (user.name),
          'email     (user.email),
          'password  (user.password),
          'salt      (user.salt),
          'avatarUrl (user.avatarUrl),
          'state     (User.activeState.id)
        )

        checkTimeStamps(u, user.timeAdded, DateTime.now)
      }
    }

    "pass authentication" in {
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]

      val user = factory.createRegisteredUser.copy(email = email, password = password)
      createFrom(user) mustSucceed { user =>
        user.authenticate(email, password) mustSucceed { authenticatedUser =>
          authenticatedUser mustBe(user)
          ()
        }
      }
    }

  }

  "An active user" can {

    "have it's name changed" in {
      val user = factory.createActiveUser
      val newName = nameGenerator.next[String]

      user.withName(newName) mustSucceed { u =>
        u mustBe a[ActiveUser]

        u must have (
          'id        (user.id),
          'version   (user.version + 1),
          'name      (newName),
          'email     (user.email),
          'password  (user.password),
          'salt      (user.salt),
          'avatarUrl (user.avatarUrl),
          'state     (User.activeState.id)
        )

        checkTimeStamps(u, user.timeAdded, DateTime.now)
      }
    }

    "have it's email changed" in {
      val user = factory.createActiveUser
      val newEmail = nameGenerator.nextEmail

      user.withEmail(newEmail) mustSucceed { u =>
        u mustBe a[ActiveUser]

        u must have (
          'id        (user.id),
          'version   (user.version + 1),
          'name      (user.name),
          'email     (newEmail),
          'password  (user.password),
          'salt      (user.salt),
          'avatarUrl (user.avatarUrl),
          'state     (User.activeState.id)
        )

        checkTimeStamps(u, user.timeAdded, DateTime.now)
      }
    }

    "have it's password changed" in {
      val user = factory.createActiveUser
      val newPassword = nameGenerator.next[String]
      val newSalt = nameGenerator.next[String]

      user.withPassword(newPassword, newSalt) mustSucceed { u =>
        u mustBe a[ActiveUser]

        u must have (
          'id        (user.id),
          'version   (user.version + 1),
          'name      (user.name),
          'email     (user.email),
          'password  (newPassword),
          'salt      (newSalt),
          'avatarUrl (user.avatarUrl),
          'state     (User.activeState.id)
        )

        checkTimeStamps(u, user.timeAdded, DateTime.now)
      }
    }

    "have it's avatar URL changed" in {
      val user = factory.createActiveUser
      val newUrl = Some(nameGenerator.nextUrl[ActiveUser])

      user.withAvatarUrl(newUrl) mustSucceed { u =>
        u mustBe a[ActiveUser]

        u must have (
          'id        (user.id),
          'version   (user.version + 1),
          'name      (user.name),
          'email     (user.email),
          'password  (user.password),
          'salt      (user.salt),
          'avatarUrl (newUrl),
          'state     (User.activeState.id)
        )

        checkTimeStamps(u, user.timeAdded, DateTime.now)
      }
    }

    "can be locked" in {
      val user = factory.createActiveUser

      user.lock.mustSucceed { u =>
        u mustBe a[LockedUser]

        u must have (
          'id        (user.id),
          'version   (user.version + 1),
          'name      (user.name),
          'email     (user.email),
          'password  (user.password),
          'salt      (user.salt),
          'avatarUrl (user.avatarUrl),
          'state     (User.lockedState.id)
        )

        checkTimeStamps(u, user.timeAdded, DateTime.now)
      }
    }
  }

  "An locked user" can {

    "be unlocked" in {
      val user = factory.createLockedUser

      user.unlock.mustSucceed { u =>
        u mustBe a[ActiveUser]

        u must have (
          'id        (user.id),
          'version   (user.version + 1),
          'name      (user.name),
          'email     (user.email),
          'password  (user.password),
          'salt      (user.salt),
          'avatarUrl (user.avatarUrl),
          'state     (User.activeState.id)
        )

        checkTimeStamps(u, user.timeAdded, DateTime.now)
      }
    }
  }

  "A user" must {

    "not be created with an empty id" in {
      val user = factory.createRegisteredUser.copy(id = UserId(""))
      createFrom(user) mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val user = factory.createRegisteredUser.copy(version = -2L)
      createFrom(user) mustFail "InvalidVersion"
    }

    "not be created with an empty name" in {
      val user = factory.createRegisteredUser.copy(name = "")
      createFrom(user) mustFail "InvalidName"
    }

    "not be created with an empty email" in {
      val user = factory.createRegisteredUser.copy(email = "")
      createFrom(user) mustFail "InvalidEmail"
    }

    "not be created with an invalid email" in {
      val user = factory.createRegisteredUser.copy(email = nameGenerator.next[User])
      createFrom(user) mustFail "InvalidEmail"
    }

    "not be created with an empty password" in {
      val user = factory.createRegisteredUser.copy(password = "")
      createFrom(user) mustFail "PasswordRequired"
    }

    "not be created with an empty salt option" in {
      val user = factory.createRegisteredUser.copy(salt = "")
      createFrom(user) mustFail "SaltRequired"
    }

    "not be created with an invalid avatar url" in {
      val user = factory.createRegisteredUser.copy(avatarUrl = Some(nameGenerator.next[User]))
      createFrom(user) mustFail "InvalidUrl"
    }

    "fail authentication for bad password" in {
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val badPassword = nameGenerator.next[User]

      val user = factory.createRegisteredUser.copy(email = email, password = password)
      createFrom(user) mustSucceed { user =>
        user.authenticate(email, badPassword) mustFail "authentication failure"
      }
    }

    "have more than one validation fail" in {
      val user = factory.createRegisteredUser.copy(version = -1L, name = "")
      createFrom(user) mustFail ("InvalidVersion", "InvalidName")
    }

  }

}
