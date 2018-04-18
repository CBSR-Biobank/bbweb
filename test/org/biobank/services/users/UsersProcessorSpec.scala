package org.biobank.services.users

import akka.actor._
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.fixture._
import org.biobank.domain.users.UserRepository
import org.biobank.infrastructure.commands.UserCommands._
import org.biobank.infrastructure.events.UserEvents._
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import play.api.{Configuration, Environment}
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.Await

case class NamedUsersProcessor @Inject() (@Named("usersProcessor") processor: ActorRef)

class UsersProcessorSpec extends ProcessorTestFixture with PresistenceQueryEvents {

  import org.biobank.TestUtils._

  private var usersProcessor = app.injector.instanceOf[NamedUsersProcessor].processor

  private val userRepository = app.injector.instanceOf[UserRepository]

  override def beforeEach() {
    userRepository.removeAll
    super.beforeEach()
  }

  private def restartProcessor(processor: ActorRef) = {
    val stopped = gracefulStop(processor, 5 seconds, PoisonPill)
    Await.result(stopped, 6 seconds)

    val actor = system.actorOf(Props(new UsersProcessor(
                                       app.injector.instanceOf[Configuration],
                                       userRepository,
                                       app.injector.instanceOf[PasswordHasher],
                                       app.injector.instanceOf[EmailService],
                                       app.injector.instanceOf[Environment],
                                       app.injector.instanceOf[SnapshotWriter])),
                               "users")
    Thread.sleep(250)
    actor
  }

  describe("A user processor must") {

    it("allow recovery from journal", PersistenceTest) {
      val user = factory.createActiveUser
      val cmd = RegisterUserCmd(name      = user.name,
                                email     = user.email,
                                password  = user.password,
                                avatarUrl = user.avatarUrl)
      val v = ask(usersProcessor, cmd).mapTo[ServiceValidation[UserEvent]].futureValue
      v.isSuccess must be (true)
      userRepository.getValues.map { c => c.name } must contain (user.name)

      userRepository.removeAll
      usersProcessor = restartProcessor(usersProcessor)

      userRepository.getValues.map { c => c.name } must contain (user.name)
    }

    it("accept a snapshot offer", PersistenceTest) {
      val snapshotFilename = "testfilename"
      val users = (1 to 2).map { _ => factory.createActiveUser }
      val snapshotUser = users(1)
      val snapshotState = UsersProcessor.SnapshotState(Set(snapshotUser))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      users.foreach(userRepository.put)
      (usersProcessor ? "snap").mapTo[String].futureValue

      userRepository.removeAll
      usersProcessor = restartProcessor(usersProcessor)

      userRepository.getByKey(snapshotUser.id) mustSucceed { repoUser =>
        repoUser.name must be (snapshotUser.name)
        ()
      }
    }

  }

}
