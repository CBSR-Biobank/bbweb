package org.biobank.domain.user

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import org.scalatest.Tag
import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory

/**
  * Note: to run from Eclipse uncomment the @RunWith line. To run from SBT the line mustBe
  * commented out.
  *
  */
//@RunWith(classOf[JUnitRunner])
class UserSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A user" can {

    "be created" in {
      val version = -1L
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val id = UserId(email)
      val password = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some("http://test.com/")

      val timeNow = DateTime.now

      val validation = RegisteredUser.create(
        id, version, timeNow, name, email, password, salt, avatarUrl)
      validation mustSucceed  { user =>
        user mustBe a[RegisteredUser]
        user must have (
          'id (id),
          'version (0L),
          'name (name),
          'email (email),
          'password (password),
          'salt (salt),
          'avatarUrl (avatarUrl)
        )

        user.timeAdded mustBe (timeNow)
        user.timeModified mustBe (None)
      }
    }

    "can be activated, locked, and unlocked" in {
      val user = factory.createRegisteredUser

      user.activate.mustSucceed { activeUser =>
        activeUser mustBe a[ActiveUser]
        activeUser.version mustBe(user.version + 1)
        activeUser.timeAdded mustBe (user.timeAdded)

        activeUser.lock.mustSucceed { lockedUser =>
          lockedUser mustBe a[LockedUser]
          lockedUser.version mustBe(activeUser.version + 1)
          lockedUser.timeAdded mustBe (user.timeAdded)

          lockedUser.unlock.mustSucceed { unlockedUser =>
            unlockedUser mustBe a[ActiveUser]
            unlockedUser.version mustBe(lockedUser.version + 1)
            unlockedUser.timeAdded mustBe (user.timeAdded)
          }
        }
      }
    }
  }

  "A user" must {

    "not be created with an empty id" in {
      val id = UserId("")
      val version = -1L
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(
        id, version, DateTime.now, name, email, password, salt, avatarUrl).fold(
        err => err.list must (have length 1 and contain("IdRequired")),
        user => fail("id validation failed")
      )
    }

    "not be created with an invalid version" in {
      val id = UserId(nameGenerator.next[User])
      val version = -2L
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(
        id, version, DateTime.now, name, email, password, salt, avatarUrl).fold(
        err => err.list must (have length 1 and contain("InvalidVersion")),
        user => fail("version validation failed")
      )
    }

    "not be created with an empty name" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = ""
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(
        id, version, DateTime.now, name, email, password, salt, avatarUrl).fold(
        err => err.list must (have length 1 and contain("InvalidName")),
        user => fail("name validation failed")
      )
    }

    "not be created with an empty email" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.nextEmail[User]
      val email = ""
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(
        id, version, DateTime.now, name, email, password, salt, avatarUrl).fold(
        err => {
          err.list must have length 1
          err.list.head must include("InvalidEmail")
        },
        user => fail("name validation failed")
      )
    }

    "not be created with an invalid email" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.nextEmail[User]
      val email = "abcdef"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(
        id, version, DateTime.now, name, email, password, salt, avatarUrl).fold(
        err => {
          err.list must have length 1
          err.list.head must include("InvalidEmail")
        },
        user => fail("name validation failed")
      )
    }

    "not be created with an empty password" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = ""
      val hasher = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(
        id, version, DateTime.now, name, email, password, salt, avatarUrl).fold(
        err => err.list must (have length 1 and contain("PasswordRequired")),
        user => fail("user password validation failed")
      )
    }

    "not be created with an empty salt option" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = ""
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(id, version, DateTime.now, name, email, password, salt, avatarUrl).fold(
        err => err.list must (have length 1 and contain("SaltRequired")),
        user => fail("user salt validation failed")
      )
    }

    "not be created with an invalid avatar url" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some(nameGenerator.next[User])

      RegisteredUser.create(id, version, DateTime.now, name, email, password, salt, avatarUrl).fold(
        err => {
          err.list must have length 1
          err.list.head must include("InvalidUrl")
        },
        user => fail("user avaltar url validation failed")
      )
    }

    "pass authentication" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some("http://test.com/")

      val v = RegisteredUser.create(id, version, DateTime.now, name, email, password, salt, avatarUrl)
      val user = v.getOrElse(fail("could not create user"))
      val authenticatedUser = user.authenticate(email, password).getOrElse(fail("could authenticate user"))
      authenticatedUser mustBe(user)
    }

    "fail authentication for bad password" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some("http://test.com/")

      val badPassword = nameGenerator.next[User]

      val v = RegisteredUser.create(id, version, DateTime.now, name, email, password, salt, avatarUrl)
      val user = v.getOrElse(fail("could not create user"))
      user.authenticate(email, badPassword).fold(
        err => err.list must (have length 1 and contain("authentication failure")),
        x => fail("authentication must fail")
      )
    }

    "have more than one validation fail" in {
      val id = UserId(nameGenerator.next[User])
      val version = -2L
      val name = ""
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = nameGenerator.next[User]
      val avatarUrl = Some("http://test.com/")

      val badPassword = nameGenerator.next[User]

      RegisteredUser.create(id, version, DateTime.now, name, email, password, salt, avatarUrl).fold(
        err => {
          err.list must have length 2
          err.list.head mustBe ("InvalidVersion")
          err.list.tail.head mustBe ("InvalidName")
        },
        user => fail
      )
    }

  }

}
