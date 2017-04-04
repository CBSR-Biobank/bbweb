package org.biobank.service.participants

import akka.pattern._
import org.biobank.fixture._
import org.biobank.service.ServiceValidation
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._

class CollectionEventsProcessorSpec extends TestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.CollectionEventCommands._
  import org.biobank.infrastructure.event.CollectionEventEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  val persistenceId = "collectionEvents-processor-id"

  override def beforeEach() {
    collectionEventRepository.removeAll
    super.beforeEach()
  }

  "A collectionEvents processor" must {

    "allow recovery from journal" in {
      val collectionEvent = factory.createCollectionEvent
      val participant = factory.defaultParticipant
      val study = factory.defaultEnabledStudy
      val ceventType = factory.defaultCollectionEventType.copy(studyId = participant.studyId)
      val cmd = AddCollectionEventCmd(userId                = nameGenerator.next[String],
                                      participantId         = participant.id.id,
                                      collectionEventTypeId = ceventType.id.id,
                                      timeCompleted         = DateTime.now,
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

    "allow a snapshot request" in {
      val collectionEvents = (1 to 2).map { _ => factory.createCollectionEvent }
      collectionEvents.foreach(collectionEventRepository.put)

      collectionEventsProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    "accept a snapshot offer" in {
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
      }
    }

  }

}
