package org.biobank.domain.user

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

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
      val user = factory.createRegisteredUser
      val v = RegisteredUser.create(id        = user.id,
                                    version   = -1,
                                    name      = user.name,
                                    email     = user.email,
                                    password  = user.password,
                                    salt      = user.salt,
                                    avatarUrl = user.avatarUrl)

      v mustSucceed  { u =>
        u mustBe a[RegisteredUser]
        u must have (
          'id        (user.id),
          'version   (0),
          'name      (user.name),
          'email     (user.email),
          'password  (user.password),
          'salt      (user.salt),
          'avatarUrl (user.avatarUrl)
        )

        checkTimeStamps(u, DateTime.now, None)
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
      val v = RegisteredUser.create(id        = UserId(""),
                                    version   = -1L,
                                    name      = nameGenerator.next[User],
                                    email     = nameGenerator.nextEmail[User],
                                    password  = nameGenerator.next[User],
                                    salt      = nameGenerator.next[User],
                                    avatarUrl = Some(nameGenerator.nextUrl[User]))
      v mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val v = RegisteredUser.create(id        = UserId(nameGenerator.next[User]),
                                    version   = -2L,
                                    name      = nameGenerator.next[User],
                                    email     = nameGenerator.nextEmail[User],
                                    password  = nameGenerator.next[User],
                                    salt      = nameGenerator.next[User],
                                    avatarUrl = Some(nameGenerator.nextUrl[User]))
      v mustFail "InvalidVersion"
    }

    "not be created with an empty name" in {
      val v = RegisteredUser.create(id        = UserId(nameGenerator.next[User]),
                                    version   = -1L,
                                    name      = "",
                                    email     = nameGenerator.nextEmail[User],
                                    password  = nameGenerator.next[User],
                                    salt      = nameGenerator.next[User],
                                    avatarUrl = Some(nameGenerator.nextUrl[User]))
      v mustFail "InvalidName"
    }

    "not be created with an empty email" in {
      val v = RegisteredUser.create(id        = UserId(nameGenerator.next[User]),
                                    version   = -1L,
                                    name      = nameGenerator.next[User],
                                    email     = "",
                                    password  = nameGenerator.next[User],
                                    salt      = nameGenerator.next[User],
                                    avatarUrl = Some(nameGenerator.nextUrl[User]))
      v mustFail "InvalidEmail"
    }

    "not be created with an invalid email" in {
      val v = RegisteredUser.create(id        = UserId(nameGenerator.next[User]),
                                    version   = -1L,
                                    name      = nameGenerator.next[User],
                                    email     = "abcdef",
                                    password  = nameGenerator.next[User],
                                    salt      = nameGenerator.next[User],
                                    avatarUrl = Some(nameGenerator.nextUrl[User]))
      v mustFail "InvalidEmail"
    }

    "not be created with an empty password" in {
      val v = RegisteredUser.create(id        = UserId(nameGenerator.next[User]),
                                    version   = -1L,
                                    name      = nameGenerator.next[User],
                                    email     = nameGenerator.nextEmail[User],
                                    password  = "",
                                    salt      = nameGenerator.next[User],
                                    avatarUrl = Some(nameGenerator.nextUrl[User]))
      v mustFail "PasswordRequired"
    }

    "not be created with an empty salt option" in {
      val v = RegisteredUser.create(id        = UserId(nameGenerator.next[User]),
                                    version   = -1L,
                                    name      = nameGenerator.next[User],
                                    email     = nameGenerator.nextEmail[User],
                                    password  = nameGenerator.next[User],
                                    salt      = "",
                                    avatarUrl = Some(nameGenerator.nextUrl[User]))
      v mustFail "SaltRequired"
    }

    "not be created with an invalid avatar url" in {
      val v = RegisteredUser.create(id        = UserId(nameGenerator.next[User]),
                                    version   = -1L,
                                    name      = nameGenerator.next[User],
                                    email     = nameGenerator.nextEmail[User],
                                    password  = nameGenerator.next[User],
                                    salt      = nameGenerator.next[User],
                                    avatarUrl = Some(nameGenerator.next[User]))
      v mustFail "InvalidUrl"
    }

    "pass authentication" in {
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]

      val v = RegisteredUser.create(id        = UserId(nameGenerator.next[User]),
                                    version   = 0L,
                                    name      = nameGenerator.next[User],
                                    email     = email,
                                    password  = password,
                                    salt      = nameGenerator.next[User],
                                    avatarUrl = Some(nameGenerator.nextUrl[User]))

      v mustSucceed { user =>
        user.authenticate(email, password) mustSucceed { authenticatedUser =>
          authenticatedUser mustBe(user)
        }
      }
    }

    "fail authentication for bad password" in {
      val email = nameGenerator.nextEmail[User]
      val password = nameGenerator.next[User]
      val badPassword = nameGenerator.next[User]

      val v = RegisteredUser.create(id        = UserId(nameGenerator.next[User]),
                                    version   = 0L,
                                    name      = nameGenerator.next[User],
                                    email     = email,
                                    password  = password,
                                    salt      = nameGenerator.next[User],
                                    avatarUrl = Some(nameGenerator.nextUrl[User]))

      v mustSucceed { user =>
        user.authenticate(email, badPassword) mustFail "authentication failure"
      }
    }

    "have more than one validation fail" in {
      val v = RegisteredUser.create(id        = UserId(nameGenerator.next[User]),
                                    version   = -2L,
                                    name      = "",
                                    email     = nameGenerator.nextEmail[User],
                                    password  = nameGenerator.next[User],
                                    salt      = nameGenerator.next[User],
                                    avatarUrl = Some(nameGenerator.nextUrl[User]))

      v mustFail ("InvalidVersion", "InvalidName")
    }

  }

}
