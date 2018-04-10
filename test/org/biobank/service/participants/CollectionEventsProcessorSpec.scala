package org.biobank.services.participants

import akka.actor.ActorRef
import akka.pattern._
import java.time.OffsetDateTime
import javax.inject.{ Inject, Named }
import org.biobank.fixture._
import org.biobank.domain.participants._
import org.biobank.domain.studies.{StudyRepository, CollectionEventTypeRepository}
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._

case class NamedCollectionEventsProcessor @Inject() (@Named("collectionEventsProcessor") processor: ActorRef)

class CollectionEventsProcessorSpec extends ProcessorTestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.CollectionEventCommands._
  import org.biobank.infrastructure.events.CollectionEventEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val collectionEventsProcessor = app.injector.instanceOf[NamedCollectionEventsProcessor].processor

  val studyRepository = app.injector.instanceOf[StudyRepository]

  val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  val participantRepository = app.injector.instanceOf[ParticipantRepository]

  val collectionEventRepository = app.injector.instanceOf[CollectionEventRepository]

  val nameGenerator = new NameGenerator(this.getClass)

  val persistenceId = "collectionEvents-processor-id"

  override def beforeEach() {
    collectionEventRepository.removeAll
    super.beforeEach()
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
      val v = ask(collectionEventsProcessor, cmd).mapTo[ServiceValidation[CollectionEventEvent]].futureValue
      v.isSuccess must be (true)
      collectionEventRepository.getValues.map { s => s.visitNumber } must contain (collectionEvent.visitNumber)
      collectionEventsProcessor ! "persistence_restart"
      collectionEventRepository.removeAll

      Thread.sleep(250)

      collectionEventRepository.getValues.size must be (1)
      collectionEventRepository.getValues.map { s => s.visitNumber } must contain (collectionEvent.visitNumber)
    }

    it("allow a snapshot request", PersistenceTest) {
      val collectionEvents = (1 to 2).map { _ => factory.createCollectionEvent }
      collectionEvents.foreach(collectionEventRepository.put)

      collectionEventsProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    it("accept a snapshot offer", PersistenceTest) {
      val snapshotFilename = "testfilename"
      val collectionEvents = (1 to 2).map { _ => factory.createCollectionEvent }
      val snapshotCollectionEvent = collectionEvents(1)
      val snapshotState = CollectionEventsProcessor.SnapshotState(Set(snapshotCollectionEvent))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      collectionEvents.foreach(collectionEventRepository.put)
      collectionEventsProcessor ? "snap"
      Thread.sleep(250)
      collectionEventsProcessor ! "persistence_restart"
      collectionEventRepository.removeAll

      Thread.sleep(250)

      collectionEventRepository.getValues.size must be (1)
      collectionEventRepository.getByKey(snapshotCollectionEvent.id) mustSucceed { repoCollectionEvent =>
        repoCollectionEvent.visitNumber must be (snapshotCollectionEvent.visitNumber)
        ()
      }
    }

  }

}
