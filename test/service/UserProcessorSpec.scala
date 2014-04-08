package service

import fixture._
import service.commands.UserCommands._
import service.events.UserEvents._
import domain._

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.ask
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.testkit.TestProbe
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._
import scalaz.Scalaz._

@RunWith(classOf[JUnitRunner])
class UserProcessorSpec extends UserProcessorFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  override val nameGenerator = new NameGenerator(this.getClass.getName)

  "User" must {

    "be added" in {
      val name = nameGenerator.next[User]
      val email = java.util.UUID.randomUUID.toString.toUpperCase
      val password = nameGenerator.next[User]
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some(nameGenerator.next[User])

      val cmd = AddUserCommand(name, email, password, hasher, salt, avatarUrl)
      val future = userProcessor ? cmd map (_.asInstanceOf[DomainValidation[UserAddedEvent]])

      future.value.get.map(e => e match {
        case Success(event) =>
          event.name should be(name)
          event.email should be(email)
          event.password should be(password)
          event.hasher should be(hasher)
          event.salt should be(salt)
          event.avatarUrl should be(avatarUrl)

          val Some(user: User) = userRepository.userWithId(event.id)
          user.version should be(0L)
        case Failure(x) =>
      })
    }
  }
}
