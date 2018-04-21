package org.biobank.domain.users

import java.time.OffsetDateTime
import org.biobank.domain.{DomainSpec, DomainValidation}
import org.biobank.fixture.NameGenerator
import org.slf4j.LoggerFactory

/**
 *
 */
class UserSpec extends DomainSpec {
  import org.biobank.TestUtils._
  import org.biobank.matchers.EntityMatchers._

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

  describe("A registered user") {

    it("be created") {
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

        u must beEntityWithTimeStamps(OffsetDateTime.now, None, 5L)
      }
    }

    it("be activated") {
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

        u must beEntityWithTimeStamps(user.timeAdded, Some(OffsetDateTime.now), 5L)
      }
    }

    it("pass authentication") {
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]

      val user = factory.createRegisteredUser.copy(email = email, password = password)
      createFrom(user) mustSucceed { user =>
        user.authenticate(password) mustSucceed { authenticatedUser =>
          authenticatedUser mustBe(user)
          ()
        }
      }
    }

  }

  describe("An active user") {

    it("have it's name changed") {
      val user = factory.createActiveUser
      val newName = faker.Name.name

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

        u must beEntityWithTimeStamps(user.timeAdded, Some(OffsetDateTime.now), 5L)
      }
    }

    it("have it's email changed") {
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

        u must beEntityWithTimeStamps(user.timeAdded, Some(OffsetDateTime.now), 5L)
      }
    }

    it("have it's password changed") {
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

        u must beEntityWithTimeStamps(user.timeAdded, Some(OffsetDateTime.now), 5L)
      }
    }

    it("have it's avatar URL changed") {
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

        u must beEntityWithTimeStamps(user.timeAdded, Some(OffsetDateTime.now), 5L)
      }
    }

    it("can be locked") {
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

        u must beEntityWithTimeStamps(user.timeAdded, Some(OffsetDateTime.now), 5L)
      }
    }
  }

  describe("An locked user") {

    it("be unlocked") {
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

        u must beEntityWithTimeStamps(user.timeAdded, Some(OffsetDateTime.now), 5L)
      }
    }
  }

  describe("A user") {

    it("not be created with an empty id") {
      val user = factory.createRegisteredUser.copy(id = UserId(""))
      createFrom(user) mustFail "IdRequired"
    }

    it("not be created with an invalid version") {
      val user = factory.createRegisteredUser.copy(version = -2L)
      createFrom(user) mustFail "InvalidVersion"
    }

    it("not be created with an empty name") {
      val user = factory.createRegisteredUser.copy(name = "")
      createFrom(user) mustFail "InvalidName"
    }

    it("not be created with an empty email") {
      val user = factory.createRegisteredUser.copy(email = "")
      createFrom(user) mustFail "InvalidEmail"
    }

    it("not be created with an invalid email") {
      val user = factory.createRegisteredUser.copy(email = nameGenerator.next[User])
      createFrom(user) mustFail "InvalidEmail"
    }

    it("not be created with an empty password") {
      val user = factory.createRegisteredUser.copy(password = "")
      createFrom(user) mustFail "PasswordRequired"
    }

    it("not be created with an empty salt option") {
      val user = factory.createRegisteredUser.copy(salt = "")
      createFrom(user) mustFail "SaltRequired"
    }

    it("not be created with an invalid avatar url") {
      val user = factory.createRegisteredUser.copy(avatarUrl = Some(nameGenerator.next[User]))
      createFrom(user) mustFail "InvalidUrl"
    }

    it("fail authentication for bad password") {
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val badPassword = nameGenerator.next[User]

      val user = factory.createRegisteredUser.copy(email = email, password = password)
      createFrom(user) mustSucceed { user =>
        user.authenticate(badPassword) mustFail "authentication failure"
      }
    }

    it("have more than one validation fail") {
      val user = factory.createRegisteredUser.copy(version = -1L, name = "")
      createFrom(user) mustFail ("InvalidVersion", "InvalidName")
    }

  }

}
