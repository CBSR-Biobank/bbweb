package org.biobank.services.access

import akka.actor.ActorRef
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.Global
import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._
import org.scalatest.Inside

final case class NamedAccessProcessor @Inject() (@Named("accessProcessor") processor: ActorRef)

class AccesssProcessorSpec
    extends ProcessorTestFixture
    with Inside {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.AccessCommands._
  import org.biobank.infrastructure.event.AccessEvents._

  class AccessItemsFixture {
    val role = factory.createRole
    val permission = factory.createPermission
    Set(role, permission).foreach(addToRepository)
  }

  val log = LoggerFactory.getLogger(this.getClass)

  val accesssProcessor = app.injector.instanceOf[NamedAccessProcessor].processor

  val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    accessItemRepository.removeAll
    accessItemRepository.removeAll
    super.beforeEach()
  }

  protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    inside(entity) { case e: AccessItem =>
      accessItemRepository.put(e)
    }
  }

  describe("An accesss processor must") {

    it("allow recovery from journal", PersistenceTest) {
      val f = new AccessItemsFixture
      val user = factory.createRegisteredUser
      val cmd = AddRoleCmd(sessionUserId = Global.DefaultUserId.id,
                           userIds       = List(user.id.id),
                           name          = nameGenerator.next[Membership],
                           description   = Some(nameGenerator.next[Membership]),
                           parentIds     = f.role.parentIds.map(_.id).toList,
                           childrenIds   = f.role.childrenIds.map(_.id).toList)

      val v = ask(accesssProcessor, cmd).mapTo[ServiceValidation[AccessEvent]].futureValue
      v mustSucceed { event =>
        accessItemRepository.getByKey(AccessItemId(event.getRole.getId)) mustSucceed { accessItem =>
          inside(accessItem) { case repoRole: Role =>
            repoRole.userIds must contain (user.id)
            accesssProcessor ! "persistence_restart"

            Thread.sleep(250)

            accessItemRepository.getByKey(AccessItemId(event.getRole.getId)).isSuccess must be (true)
          }
        }
      }
    }

    it("allow a snapshot request", PersistenceTest) {
      val f = new AccessItemsFixture
      accessItemRepository.put(f.role)

      accesssProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    it("accept a snapshot offer", PersistenceTest) {
      val f = new AccessItemsFixture
      val user = factory.createRegisteredUser
      val snapshotFilename = "testfilename"
      val snapshotRole = f.role.copy(userIds = Set(user.id))
      val snapshotState = AccessProcessor.SnapshotState(Set(snapshotRole))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      accessItemRepository.put(f.role)
      accesssProcessor ? "snap"
      Thread.sleep(250)
      accesssProcessor ! "persistence_restart"

      Thread.sleep(250)

      accessItemRepository.getByKey(snapshotRole.id) mustSucceed { accessItem =>
        inside(accessItem) { case repoRole: Role =>
          repoRole.userIds must contain (user.id)
          ()
        }
      }
    }

  }

}
