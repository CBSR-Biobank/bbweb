package org.biobank.service.studies

import akka.actor.ActorRef
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.fixture._
import org.biobank.domain.study.{StudyRepository, CollectionEventTypeRepository}
import org.biobank.service.ServiceValidation
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._

case class NamedCollectionEventTypeProcessor @Inject() (@Named("collectionEventType") processor: ActorRef)

class CollectionEventTypesProcessorSpec extends ProcessorTestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.CollectionEventTypeCommands._
  import org.biobank.infrastructure.event.CollectionEventTypeEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val collectionEventTypeProcessor =
    app.injector.instanceOf[NamedCollectionEventTypeProcessor].processor

  val studyRepository = app.injector.instanceOf[StudyRepository]

  val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  val nameGenerator = new NameGenerator(this.getClass)

  val persistenceId = "collection-event-type-processor-id"

  override def beforeEach() {
    collectionEventTypeRepository.removeAll
    super.beforeEach()
  }

  describe("A collectionEventTypes processor must") {

    it("allow recovery from journal") {
      val collectionEventType = factory.createCollectionEventType
      val study = factory.defaultDisabledStudy
      val cmd = AddCollectionEventTypeCmd(userId      = None,
                                          studyId     = study.id.id,
                                          name        = collectionEventType.name,
                                          description = collectionEventType.description,
                                          recurring   = true)
      val v = ask(collectionEventTypeProcessor, cmd)
        .mapTo[ServiceValidation[CollectionEventTypeEvent]]
        .futureValue
      v.isSuccess must be (true)
      collectionEventTypeRepository.getValues.map { cet => cet.name } must contain (collectionEventType.name)
      collectionEventTypeProcessor ! "persistence_restart"
      collectionEventTypeRepository.removeAll

      Thread.sleep(250)

      collectionEventTypeRepository.getValues.size must be (1)
      collectionEventTypeRepository.getValues.map { cet => cet.name } must contain (collectionEventType.name)
    }

    it("allow a snapshot request") {
      val collectionEventTypes = (1 to 2).map { _ => factory.createCollectionEventType }
      val study = factory.defaultDisabledStudy
      collectionEventTypes.foreach(collectionEventTypeRepository.put)
      studyRepository.put(study)

      collectionEventTypeProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    it("accept a snapshot offer") {
      val snapshotFilename = "testfilename"
      val collectionEventTypes = (1 to 2).map { _ => factory.createCollectionEventType }
      val snapshotCollectionEventType = collectionEventTypes(1)
      val snapshotState = CollectionEventTypeProcessor.SnapshotState(Set(snapshotCollectionEventType))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      collectionEventTypes.foreach(collectionEventTypeRepository.put)
      collectionEventTypeProcessor ? "snap"
      Thread.sleep(250)
      collectionEventTypeProcessor ! "persistence_restart"
      collectionEventTypeRepository.removeAll

      Thread.sleep(250)

      collectionEventTypeRepository.getValues.size must be (1)
      collectionEventTypeRepository.getByKey(snapshotCollectionEventType.id)
        .mustSucceed { repoCollectionEventType =>
          repoCollectionEventType.name must be (snapshotCollectionEventType.name)
          ()
        }
    }

  }

}
