package org.biobank.services.users

import akka.actor.ActorRef
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.fixture._
import org.biobank.domain.users.UserRepository
import org.biobank.infrastructure.commands.UserCommands._
import org.biobank.infrastructure.events.UserEvents._
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._

case class NamedUsersProcessor @Inject() (@Named("usersProcessor") processor: ActorRef)

class UsersProcessorSpec extends ProcessorTestFixture with PresistenceQueryEvents {

  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val usersProcessor = app.injector.instanceOf[NamedUsersProcessor].processor

  val userRepository = app.injector.instanceOf[UserRepository]

  val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    userRepository.removeAll
    super.beforeEach()
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
      usersProcessor ! "persistence_restart"

      Thread.sleep(500)

      userRepository.getValues.map { c => c.name } must contain (user.name)
    }

    it("allow a snapshot request", PersistenceTest) {
      val users = (1 to 2).map { _ => factory.createActiveUser }
      users.foreach(userRepository.put)

      usersProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
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
      usersProcessor ? "snap"
      Thread.sleep(250)
      usersProcessor ! "persistence_restart"
      userRepository.removeAll

      Thread.sleep(250)

      userRepository.getByKey(snapshotUser.id) mustSucceed { repoUser =>
        repoUser.name must be (snapshotUser.name)
        ()
      }
    }

  }

}
