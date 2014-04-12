package org.biobank.domain

import fixture.NameGenerator

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.OptionValues._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

/**
 * Note: to run from Eclipse uncomment the @RunWith line. To run from SBT the line should be
 * commented out.
 *
 */
//@RunWith(classOf[JUnitRunner])
class UserSpec extends WordSpecLike with Matchers {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass.getName)

  "A user" can {

    "be created" in {
      val version = -1L
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val id = UserId(email)
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val v = RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl)
      val user = v.getOrElse(fail("could not create user"))
      user shouldBe a[RegisteredUser]


      user.id should be(id)
      user.version should be(0L)
      user.name should be(name)
      user.email should be(email)
      user.password should be(password)
      user.hasher should be(hasher)
      user.salt should be(salt)
      user.avatarUrl should be(avatarUrl)
    }

    "can be activated, locked, and unlocked" in {
      val version = -1L
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val id = UserId(email)
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val v = RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl)
      val user = v.getOrElse(fail("could not create user"))
      user shouldBe a[RegisteredUser]

      val activeUser = user.activate.getOrElse(fail("could not activate user"))
      activeUser shouldBe a[ActiveUser]
      activeUser.version should be(user.version + 1)

      val lockedUser = activeUser.lock.getOrElse(fail("could not lock user"))
      lockedUser shouldBe a[LockedUser]
      lockedUser.version should be(activeUser.version + 1)

      val unlockedUser = lockedUser.unlock.getOrElse(fail("could not unlock user"))
      unlockedUser shouldBe a[ActiveUser]
      unlockedUser.version should be(lockedUser.version + 1)
    }
  }

  "A user" should {

    "not be created with an empty id" in {
      val id = UserId("")
      val version = -1L
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl) match {
        case Success(user) => fail("id validation failed")
        case Failure(err) =>
          err.list.mkString(",") should include("id is null or empty")
      }
    }

    "not be created with an invalid version" in {
      val id = UserId(nameGenerator.next[User])
      val version = -2L
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl) match {
        case Success(user) => fail("version validation failed")
        case Failure(err) =>
          err.list.mkString(",") should include("invalid version value")
      }
    }

    "not be created with an empty name" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = ""
      val email = "user1@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl) match {
        case Success(user) => fail("name validation failed")
        case Failure(err) =>
          err.list.mkString(",") should include("name is null or empty")
      }
    }

    "not be created with an empty password" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val password = ""
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl) match {
        case Success(user) => fail("user password validation failed")
        case Failure(err) =>
          err.list.mkString(",") should include("password is null or empty")
      }
    }

    "not be created with an empty hasher" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val password = nameGenerator.next[User]
      val hasher = ""
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl) match {
        case Success(user) => fail("user hasher validation failed")
        case Failure(err) =>
          err.list.mkString(",") should include("hasher is null or empty")
      }
    }

    "not be created with an empty salt option" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some("")
      val avatarUrl = Some("http://test.com/")

      RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl) match {
        case Success(user) => fail("user salt validation failed")
        case Failure(err) =>
          err.list.mkString(",") should include("salt is empty")
      }
    }

    "not be created with an invalid avatar url" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some(nameGenerator.next[User])

      RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl) match {
        case Success(user) => fail("user avaltar url validation failed")
        case Failure(err) =>
          err.list.mkString(",") should include("invalid avatar url")
      }
    }

    "pass authentication" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val v = RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl)
      val user = v.getOrElse(fail("could not create user"))
      val authenticatedUser = user.authenticate(email, password).getOrElse(fail("could authenticate user"))
      authenticatedUser should be(user)
    }

    "fail authentication for bad password" in {
      val id = UserId(nameGenerator.next[User])
      val version = 0L
      val name = nameGenerator.next[User]
      val email = "user1@test.com"
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val badPassword = nameGenerator.next[User]

      val v = RegisteredUser.create(id, version, name, email, password, hasher, salt, avatarUrl)
      val user = v.getOrElse(fail("could not create user"))
      user.authenticate(email, badPassword) match {
        case Success(x) => fail("authentication should fail")
        case Failure(err) =>
          err.list.mkString(",") should include("authentication failure")
      }
    }

  }

}
