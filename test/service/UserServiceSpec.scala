package service

import fixture._
import service.commands.UserCommands._
import service.events.UserEvents._
import domain._

import org.specs2.scalaz.ValidationMatchers._
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.slf4j.LoggerFactory
import scala.util.{ Success, Failure }
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._
import scalaz.Scalaz._

@RunWith(classOf[JUnitRunner])
class UserServiceSpec extends UserServiceFixture {

  args(
    //include = "tag1",
    sequential = true) // forces all tests to be run sequentially

  val log = LoggerFactory.getLogger(this.getClass)

  override val nameGenerator = new NameGenerator(this.getClass.getName)

  "User" can {

    "be added" in {
      val name = nameGenerator.next[User]
      val email = nameGenerator.next[User]
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some(nameGenerator.next[User])

      val event = await(userService.add(
        new AddUserCommand(name, email, password, hasher, salt, avatarUrl)))

      event must beSuccessful.like {
        case e: UserAddedEvent =>
          e.name must be(name)
          e.email must be(email)
          e.password must be(password)
          e.hasher must be(hasher)
          e.salt must be(salt)
          e.avatarUrl must be(avatarUrl)
          userRepository.userWithId(e.id) must beSuccessful.like {
            case u: User =>
              u.version must beEqualTo(u.version)
          }
      }

    }
  }
}
