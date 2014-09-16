package org.biobank.domain.user

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.OptionValues._
import org.scalatest.Tag
import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

/**
 * Note: to run from Eclipse uncomment the @RunWith line. To run from SBT the line should be
 * commented out.
 *
 */
//@RunWith(classOf[JUnitRunner])
class UserSpec extends DomainSpec {

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
      validation should be ('success)
      validation map { user =>
        user shouldBe a[RegisteredUser]
        user should have (
          'id (id),
          'version (0L),
          'name (name),
          'email (email),
          'password (password),
          'salt (salt),
          'avatarUrl (avatarUrl)
        )

        user.addedDate should be (timeNow)
        user.lastUpdateDate should be (None)
      }
    }

    "can be activated, locked, and unlocked" in {
      val user = factory.createRegisteredUser

      val activeUser = user.activate  | fail
      activeUser shouldBe a[ActiveUser]
      activeUser.version should be(user.version + 1)
      activeUser.addedDate should be (user.addedDate)

      val lockedUser = activeUser.lock | fail
      lockedUser shouldBe a[LockedUser]
      lockedUser.version should be(activeUser.version + 1)
      lockedUser.addedDate should be (user.addedDate)

      val unlockedUser = lockedUser.unlock | fail
      unlockedUser shouldBe a[ActiveUser]
      unlockedUser.version should be(lockedUser.version + 1)
      unlockedUser.addedDate should be (user.addedDate)
    }
  }

  "A user" should {

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
        err => err.list should (have length 1 and contain("IdRequired")),
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
        err => err.list should (have length 1 and contain("invalid version value: -2")),
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
        err => err.list should (have length 1 and contain("NameRequired")),
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
          err.list should have length 1
          err.list.head should include("email invalid")
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
          err.list should have length 1
          err.list.head should include("email invalid")
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
        err => err.list should (have length 1 and contain("PasswordRequired")),
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
        err => err.list should (have length 1 and contain("SaltRequired")),
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
          err.list should have length 1
          err.list.head should include("invalid avatar url")
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
      authenticatedUser should be(user)
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
        err => err.list should (have length 1 and contain("authentication failure")),
        x => fail("authentication should fail")
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
          err.list should have length 2
          err.list.head should be ("invalid version value: -2")
          err.list.tail.head should be ("NameRequired")
        },
        user => fail
      )
    }

  }

}
