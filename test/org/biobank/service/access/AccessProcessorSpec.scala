package org.biobank.service.access

import akka.actor.ActorRef
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.Global
import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.service._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._

final case class NamedAccessProcessor @Inject() (@Named("accessProcessor") processor: ActorRef)

class AccesssProcessorSpec extends ProcessorTestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.AccessCommands._
  import org.biobank.infrastructure.event.AccessEvents._

  class AccessItemsFixture {
    val role = factory.createRole
    val permission = factory.createPermission
    Set(role, permission).foreach(addToRepository)
  }

  class MembershipFixture {
    val membership = factory.createMembership
    Set(membership).foreach(addToRepository)
  }

  val log = LoggerFactory.getLogger(this.getClass)

  val accesssProcessor = app.injector.instanceOf[NamedAccessProcessor].processor

  val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  val membershipRepository = app.injector.instanceOf[MembershipRepository]

  val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    accessItemRepository.removeAll
    membershipRepository.removeAll
    super.beforeEach()
  }

  protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case e: AccessItem          => accessItemRepository.put(e)
      case e: Membership          => membershipRepository.put(e)
      case _                      => fail("invalid entity")
    }
  }

  describe("An accesss processor must") {

    it("allow recovery from journal", PersistenceTest) {
      val f = new MembershipFixture
      val user = factory.createRegisteredUser
      val cmd = AddMembershipCmd(sessionUserId = Global.DefaultUserId.id,
                                 userIds       = List(user.id.id),
                                 name          = nameGenerator.next[Membership],
                                 description   = Some(nameGenerator.next[Membership]),
                                 allStudies    = f.membership.studyData.allEntities,
                                 studyIds      = f.membership.studyData.ids.map(_.id).toList,
                                 allCentres    = f.membership.centreData.allEntities,
                                 centreIds     = f.membership.centreData.ids.map(_.id).toList)

      val v = ask(accesssProcessor, cmd).mapTo[ServiceValidation[AccessEvent]].futureValue
      v mustSucceed { event =>
        membershipRepository.getByKey(MembershipId(event.getMembership.getId)) mustSucceed { membership =>
          membership.userIds must contain (user.id)
          accesssProcessor ! "persistence_restart"

          Thread.sleep(250)

          membershipRepository.getByKey(MembershipId(event.getMembership.getId)).isSuccess must be (true)
        }
      }
    }

    it("allow a snapshot request", PersistenceTest) {
      val f = new MembershipFixture
      membershipRepository.put(f.membership)

      accesssProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    it("accept a snapshot offer", PersistenceTest) {
      val f = new MembershipFixture
      val user = factory.createRegisteredUser
      val snapshotFilename = "testfilename"
      val snapshotMembership = f.membership.copy(userIds = Set(user.id))
      val snapshotState = AccessProcessor.SnapshotState(Set.empty, Set(snapshotMembership))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      membershipRepository.put(f.membership)
      accesssProcessor ? "snap"
      Thread.sleep(250)
      accesssProcessor ! "persistence_restart"

      Thread.sleep(250)

      membershipRepository.getByKey(snapshotMembership.id) mustSucceed { repoAccess =>
        repoAccess.userIds must contain (user.id)
        ()
      }
    }

  }

}
