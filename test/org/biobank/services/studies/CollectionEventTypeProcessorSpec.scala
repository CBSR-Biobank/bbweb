package org.biobank.services.studies

import akka.actor._
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.Global
import org.biobank.fixture._
import org.biobank.domain.studies.CollectionEventTypeRepository
import org.biobank.domain.participants.CollectionEventRepository
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._
import scala.concurrent.duration._
import scala.concurrent.Await

case class NamedCollectionEventTypeProcessor @Inject() (@Named("collectionEventType") processor: ActorRef)

class CollectionEventTypesProcessorSpec extends ProcessorTestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.CollectionEventTypeCommands._
  import org.biobank.infrastructure.events.CollectionEventTypeEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  private var collectionEventTypeProcessor =
    app.injector.instanceOf[NamedCollectionEventTypeProcessor].processor

  private val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  override def beforeEach() {
    collectionEventTypeRepository.removeAll
    super.beforeEach()
  }

  private def restartProcessor(processor: ActorRef) = {
    val stopped = gracefulStop(processor, 5 seconds, PoisonPill)
    Await.result(stopped, 6 seconds)

    val actor = system.actorOf(Props(new CollectionEventTypeProcessor(
                                       collectionEventTypeRepository,
                                       app.injector.instanceOf[CollectionEventRepository],
                                       app.injector.instanceOf[SnapshotWriter])),
                               "actor")
    Thread.sleep(250)
    actor
  }

  describe("A collectionEventTypes processor must") {

    it("allow recovery from journal", PersistenceTest) {
      val collectionEventType = factory.createCollectionEventType
      val study = factory.defaultDisabledStudy
      val cmd = AddCollectionEventTypeCmd(sessionUserId = Global.DefaultUserId.id,
                                          studyId       = study.id.id,
                                          name          = collectionEventType.name,
                                          description   = collectionEventType.description,
                                          recurring     = true)
      val v = ask(collectionEventTypeProcessor, cmd)
        .mapTo[ServiceValidation[CollectionEventTypeEvent]]
        .futureValue
      v.isSuccess must be (true)
      collectionEventTypeRepository.getValues.map { cet => cet.name } must contain (collectionEventType.name)
      collectionEventTypeRepository.removeAll
      collectionEventTypeProcessor = restartProcessor(collectionEventTypeProcessor)

      collectionEventTypeRepository.getValues.size must be (1)
      collectionEventTypeRepository.getValues.map { cet => cet.name } must contain (collectionEventType.name)
    }

    it("recovers a snapshot", PersistenceTest) {
      val snapshotFilename = "testfilename"
      val collectionEventTypes = (1 to 2).map { _ => factory.createCollectionEventType }
      val snapshotCollectionEventType = collectionEventTypes(1)
      val snapshotState = CollectionEventTypeProcessor.SnapshotState(Set(snapshotCollectionEventType))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      collectionEventTypes.foreach(collectionEventTypeRepository.put)
      (collectionEventTypeProcessor ? "snap").mapTo[String].futureValue

      collectionEventTypeRepository.removeAll
      collectionEventTypeProcessor = restartProcessor(collectionEventTypeProcessor)

      collectionEventTypeRepository.getValues.size must be (1)
      collectionEventTypeRepository.getByKey(snapshotCollectionEventType.id)
        .mustSucceed { repoCollectionEventType =>
          repoCollectionEventType.name must be (snapshotCollectionEventType.name)
          ()
        }
    }

  }

}
