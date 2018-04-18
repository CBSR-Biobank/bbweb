package org.biobank.services.participants

import akka.actor._
import akka.pattern._
import java.time.OffsetDateTime
import javax.inject.{ Inject, Named }
import org.biobank.fixture._
import org.biobank.domain.participants._
import org.biobank.domain.studies.{StudyRepository, CollectionEventTypeRepository}
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.Await

case class NamedCollectionEventsProcessor @Inject() (@Named("collectionEventsProcessor") processor: ActorRef)

class CollectionEventsProcessorSpec extends ProcessorTestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.CollectionEventCommands._
  import org.biobank.infrastructure.events.CollectionEventEvents._

  private var collectionEventsProcessor = app.injector.instanceOf[NamedCollectionEventsProcessor].processor

  private val studyRepository = app.injector.instanceOf[StudyRepository]

  private val collectionEventRepository = app.injector.instanceOf[CollectionEventRepository]

  private val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  private val participantRepository = app.injector.instanceOf[ParticipantRepository]

  private val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    collectionEventRepository.removeAll
    super.beforeEach()
  }

  private def restartProcessor(processor: ActorRef) = {
    val stopped = gracefulStop(processor, 5 seconds, PoisonPill)
    Await.result(stopped, 6 seconds)

    val actor = system.actorOf(Props(new CollectionEventsProcessor(
                                       collectionEventRepository,
                                       collectionEventTypeRepository,
                                       participantRepository,
                                       app.injector.instanceOf[StudyRepository],
                                       app.injector.instanceOf[SnapshotWriter])),
                               "collectionEvents")
    Thread.sleep(250)
    actor
  }

  describe("A collectionEvents processor must") {

    it("allow recovery from journal", PersistenceTest) {
      val collectionEvent = factory.createCollectionEvent
      val participant = factory.defaultParticipant
      val study = factory.defaultEnabledStudy
      val ceventType = factory.defaultCollectionEventType.copy(studyId = participant.studyId)
      val cmd = AddCollectionEventCmd(sessionUserId         = nameGenerator.next[String],
                                      participantId         = participant.id.id,
                                      collectionEventTypeId = ceventType.id.id,
                                      timeCompleted         = OffsetDateTime.now,
                                      visitNumber           = 1,
                                      annotations           = List.empty)
      studyRepository.put(study)
      participantRepository.put(participant)
      collectionEventTypeRepository.put(ceventType)
      val v = ask(collectionEventsProcessor, cmd)
        .mapTo[ServiceValidation[CollectionEventEvent]]
        .futureValue
      v.isSuccess must be (true)
      collectionEventRepository.getValues.map { s => s.visitNumber } must contain (collectionEvent.visitNumber)
      collectionEventRepository.removeAll
      collectionEventsProcessor = restartProcessor(collectionEventsProcessor)

      collectionEventRepository.getValues.size must be (1)
      collectionEventRepository.getValues.map { s => s.visitNumber } must contain (collectionEvent.visitNumber)
    }

    it("recovers a snapshot", PersistenceTest) {
      val snapshotFilename = "testfilename"
      val collectionEvents = (1 to 2).map { _ => factory.createCollectionEvent }
      val snapshotCollectionEvent = collectionEvents(1)
      val snapshotState = CollectionEventsProcessor.SnapshotState(Set(snapshotCollectionEvent))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      collectionEvents.foreach(collectionEventRepository.put)

      (collectionEventsProcessor ? "snap").mapTo[String].futureValue

      collectionEventRepository.removeAll
      collectionEventsProcessor = restartProcessor(collectionEventsProcessor)

      collectionEventRepository.getValues.size must be (1)
      collectionEventRepository.getByKey(snapshotCollectionEvent.id) mustSucceed { repoCollectionEvent =>
        repoCollectionEvent.visitNumber must be (snapshotCollectionEvent.visitNumber)
        ()
      }
    }

  }

}
