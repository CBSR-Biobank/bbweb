package org.biobank.domain.user

import org.biobank.domain.DomainSpec
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

  "A registered user" can {

    "be created" in {
      val user = factory.createRegisteredUser
      val v = RegisteredUser.create(id          = user.id,
                                    version     = 0L,
                                    name        = user.name,
                                    email       = user.email,
                                    password    = user.password,
                                    salt        = user.salt,
                                    avatarUrl   = user.avatarUrl)

      v mustSucceed  { u =>
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

    "become registered" in {
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
      val v = RegisteredUser.create(id          = UserId(""),
                                    version     = 0L,
                                    name        = nameGenerator.next[User],
                                    email       = nameGenerator.nextEmail[User],
                                    password    = nameGenerator.next[User],
                                    salt        = nameGenerator.next[User],
                                    avatarUrl   = Some(nameGenerator.nextUrl[User]))
      v mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val v = RegisteredUser.create(id          = UserId(nameGenerator.next[User]),
                                    version     = -2L,
                                    name        = nameGenerator.next[User],
                                    email       = nameGenerator.nextEmail[User],
                                    password    = nameGenerator.next[User],
                                    salt        = nameGenerator.next[User],
                                    avatarUrl   = Some(nameGenerator.nextUrl[User]))
      v mustFail "InvalidVersion"
    }

    "not be created with an empty name" in {
      val v = RegisteredUser.create(id          = UserId(nameGenerator.next[User]),
                                    version     = 0L,
                                    name        = "",
                                    email       = nameGenerator.nextEmail[User],
                                    password    = nameGenerator.next[User],
                                    salt        = nameGenerator.next[User],
                                    avatarUrl   = Some(nameGenerator.nextUrl[User]))
      v mustFail "InvalidName"
    }

    "not be created with an empty email" in {
      val v = RegisteredUser.create(id          = UserId(nameGenerator.next[User]),
                                    version     = 0L,
                                    name        = nameGenerator.next[User],
                                    email       = "",
                                    password    = nameGenerator.next[User],
                                    salt        = nameGenerator.next[User],
                                    avatarUrl   = Some(nameGenerator.nextUrl[User]))
      v mustFail "InvalidEmail"
    }

    "not be created with an invalid email" in {
      val v = RegisteredUser.create(id          = UserId(nameGenerator.next[User]),
                                    version     = 0L,
                                    name        = nameGenerator.next[User],
                                    email       = "abcdef",
                                    password    = nameGenerator.next[User],
                                    salt        = nameGenerator.next[User],
                                    avatarUrl   = Some(nameGenerator.nextUrl[User]))
      v mustFail "InvalidEmail"
    }

    "not be created with an empty password" in {
      val v = RegisteredUser.create(id          = UserId(nameGenerator.next[User]),
                                    version     = 0L,
                                    name        = nameGenerator.next[User],
                                    email       = nameGenerator.nextEmail[User],
                                    password    = "",
                                    salt        = nameGenerator.next[User],
                                    avatarUrl   = Some(nameGenerator.nextUrl[User]))
      v mustFail "PasswordRequired"
    }

    "not be created with an empty salt option" in {
      val v = RegisteredUser.create(id          = UserId(nameGenerator.next[User]),
                                    version     = 0L,
                                    name        = nameGenerator.next[User],
                                    email       = nameGenerator.nextEmail[User],
                                    password    = nameGenerator.next[User],
                                    salt        = "",
                                    avatarUrl   = Some(nameGenerator.nextUrl[User]))
      v mustFail "SaltRequired"
    }

    "not be created with an invalid avatar url" in {
      val v = RegisteredUser.create(id          = UserId(nameGenerator.next[User]),
                                    version     = 0L,
                                    name        = nameGenerator.next[User],
                                    email       = nameGenerator.nextEmail[User],
                                    password    = nameGenerator.next[User],
                                    salt        = nameGenerator.next[User],
                                    avatarUrl   = Some(nameGenerator.next[User]))
      v mustFail "InvalidUrl"
    }

    "pass authentication" in {
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]

      val v = RegisteredUser.create(id          = UserId(nameGenerator.next[User]),
                                    version     = 0L,
                                    name        = nameGenerator.next[User],
                                    email       = email,
                                    password    = password,
                                    salt        = nameGenerator.next[User],
                                    avatarUrl   = Some(nameGenerator.nextUrl[User]))

      v mustSucceed { user =>
        user.authenticate(email, password) mustSucceed { authenticatedUser =>
          authenticatedUser mustBe(user)
          ()
        }
      }
    }

    "fail authentication for bad password" in {
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val badPassword = nameGenerator.next[User]

      val v = RegisteredUser.create(id          = UserId(nameGenerator.next[User]),
                                    version     = 0L,
                                    name        = nameGenerator.next[User],
                                    email       = email,
                                    password    = password,
                                    salt        = nameGenerator.next[User],
                                    avatarUrl   = Some(nameGenerator.nextUrl[User]))

      v mustSucceed { user =>
        user.authenticate(email, badPassword) mustFail "authentication failure"
      }
    }

    "have more than one validation fail" in {
      val v = RegisteredUser.create(id          = UserId(nameGenerator.next[User]),
                                    version     = -2L,
                                    name        = "",
                                    email       = nameGenerator.nextEmail[User],
                                    password    = nameGenerator.next[User],
                                    salt        = nameGenerator.next[User],
                                    avatarUrl   = Some(nameGenerator.nextUrl[User]))

      v mustFail ("InvalidVersion", "InvalidName")
    }

  }

}
