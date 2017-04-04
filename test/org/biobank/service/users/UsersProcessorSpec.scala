package org.biobank.service.users

import akka.pattern._
import org.biobank.fixture._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._

class UsersProcessorSpec extends TestFixture {

  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    userRepository.removeAll
    super.beforeEach()
  }

  "A user processor" must {

    "allow recovery from journal" in {
      val user = factory.createActiveUser
      val cmd = RegisterUserCmd(userId    = None,
                                name      = user.name,
                                email     = user.email,
                                password  = user.password,
                                avatarUrl = user.avatarUrl)
      val v = ask(usersProcessor, cmd).mapTo[ServiceValidation[UserEvent]].futureValue
      v.isSuccess must be (true)
      userRepository.getValues.map { c => c.name } must contain (user.name)
      usersProcessor ! "persistence_restart"
      userRepository.removeAll

      Thread.sleep(250)

      userRepository.getValues.map { c => c.name } must contain (user.name)
    }

    "allow a snapshot request" in {
      val users = (1 to 2).map { _ => factory.createActiveUser }
      users.foreach(userRepository.put)

      usersProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    "accept a snapshot offer" in {
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
      }
    }

  }

}
