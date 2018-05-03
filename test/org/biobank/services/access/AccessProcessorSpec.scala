package org.biobank.services.access

import akka.actor._
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.Global
import org.biobank.fixtures._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.services._
import org.scalatest.Inside
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.Await

final case class NamedAccessProcessor @Inject() (@Named("accessProcessor") processor: ActorRef)

class AccesssProcessorSpec
    extends ProcessorTestFixture
    with Inside {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.AccessCommands._
  import org.biobank.infrastructure.events.AccessEvents._

  class AccessItemsFixture {
    val role = factory.createRole
    val permission = factory.createPermission
    Set(role, permission).foreach(addToRepository)
  }

  private var accesssProcessor = app.injector.instanceOf[NamedAccessProcessor].processor

  private val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  private val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    accessItemRepository.removeAll
    accessItemRepository.removeAll
    super.beforeEach()
  }

  private def restartProcessor(processor: ActorRef) = {
    val stopped = gracefulStop(processor, 5 seconds, PoisonPill)
    Await.result(stopped, 6 seconds)

    val actor = system.actorOf(Props(new AccessProcessor(
                                       accessItemRepository,
                                       app.injector.instanceOf[SnapshotWriter])),
                               "access")
    Thread.sleep(250)
    actor
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

            accessItemRepository.removeAll
            accesssProcessor = restartProcessor(accesssProcessor)

            accessItemRepository.getByKey(AccessItemId(event.getRole.getId)).isSuccess must be (true)
          }
        }
      }
    }

    it("recovers a snapshot", PersistenceTest) {
      val f = new AccessItemsFixture
      val user = factory.createRegisteredUser
      val snapshotFilename = "testfilename"
      val snapshotRole = f.role.copy(userIds = Set(user.id))
      val snapshotState = AccessProcessor.SnapshotState(Set(snapshotRole))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      accessItemRepository.put(f.role)
      (accesssProcessor ? "snap").mapTo[String].futureValue

      accessItemRepository.removeAll
      accesssProcessor = restartProcessor(accesssProcessor)

      accessItemRepository.getByKey(snapshotRole.id) mustSucceed { accessItem =>
        inside(accessItem) { case repoRole: Role =>
          repoRole.userIds must contain (user.id)
          ()
        }
      }
    }

  }

}
