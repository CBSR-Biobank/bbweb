package org.biobank.services.access

import akka.actor._
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.Global
import org.biobank.fixtures._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.Inside
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.Await

final case class NamedMembershipProcessor @Inject() (@Named("membershipProcessor") processor: ActorRef)

class MembershipProcessorSpec
    extends ProcessorTestFixture
    with Inside {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.MembershipCommands._
  import org.biobank.infrastructure.events.MembershipEvents._

  class MembershipFixture {
    val membership = factory.createMembership
    Set(membership).foreach(addToRepository)
  }

  val log = LoggerFactory.getLogger(this.getClass)

  var membershipProcessor = app.injector.instanceOf[NamedMembershipProcessor].processor

  val membershipRepository = app.injector.instanceOf[MembershipRepository]

  val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    membershipRepository.removeAll
    super.beforeEach()
  }

  protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    inside(entity) { case e: Membership =>
      membershipRepository.put(e)
    }
  }

  private def restartProcessor(processor: ActorRef) = {
    val stopped = gracefulStop(processor, 5 seconds, PoisonPill)
    Await.result(stopped, 6 seconds)

    val actor = system.actorOf(Props(new MembershipProcessor(
                                       membershipRepository,
                                       app.injector.instanceOf[SnapshotWriter])),
                               "access")
    Thread.sleep(250)
    actor
  }

  describe("An membership processor must") {

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

      val v = ask(membershipProcessor, cmd).mapTo[ServiceValidation[MembershipEvent]].futureValue
      v mustSucceed { event =>
        membershipRepository.getByKey(MembershipId(event.id)) mustSucceed { membership =>
          membership.userIds must contain (user.id)
          membershipProcessor = restartProcessor(membershipProcessor)

          membershipRepository.getByKey(MembershipId(event.id)).isSuccess must be (true)
        }
      }
    }

    it("recovers a snapshot", PersistenceTest) {
      val f = new MembershipFixture
      val user = factory.createRegisteredUser
      val snapshotFilename = "testfilename"
      val snapshotMembership = f.membership.copy(userIds = Set(user.id))
      val snapshotState = MembershipProcessor.SnapshotState(Set(snapshotMembership))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      membershipRepository.put(f.membership)
      (membershipProcessor ? "snap").mapTo[String].futureValue

      membershipRepository.removeAll
      membershipProcessor = restartProcessor(membershipProcessor)

      membershipRepository.getByKey(snapshotMembership.id) mustSucceed { repoAccess =>
        repoAccess.userIds must contain (user.id)
        ()
      }
    }

  }

}
