package org.biobank.services.studies

import akka.actor._
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.Global
import org.biobank.domain.ConcurrencySafeEntity
import org.biobank.domain.studies._
import org.biobank.fixtures._
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scala.language.reflectiveCalls
import scala.concurrent.duration._
import scala.concurrent.Await
import scalaz.Scalaz._

/*
 * This code helped a lot to write these tests:
 *
 * https://github.com/dnvriend/akka-persistence-inmemory-test/blob/master/src/main/scala/com/github/dnvriend/HelloWorld.scala
 */

case class NamedProcessingTypeProcessor @Inject() (@Named("processingType") processor: ActorRef)

class ProcessingTypesProcessorSpec extends ProcessorTestFixture with ProcessingTypeFixtures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.ProcessingTypeCommands._
  import org.biobank.infrastructure.events.ProcessingTypeEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  // processor is recreated in these tests
  var processingTypeProcessor =
    app.injector.instanceOf[NamedProcessingTypeProcessor].processor

  val studyRepository = app.injector.instanceOf[StudyRepository]

  val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  val processingTypeRepository = app.injector.instanceOf[ProcessingTypeRepository]

  val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    studyRepository.removeAll
    collectionEventTypeRepository.removeAll
    processingTypeRepository.removeAll
    super.beforeEach()
  }

  private def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case s: Study               => studyRepository.put(s)
      case e: CollectionEventType => collectionEventTypeRepository.put(e)
      case e: ProcessingType      => processingTypeRepository.put(e)
      case e                      => fail(s"cannot add entity: $e")
    }
  }

  override def collectedSpecimenDerivationFixtures() = {
    val f = super.collectedSpecimenDerivationFixtures()
    Set(f.study, f.collectionEventType)
      .foreach(addToRepository)
    f
  }

  private def restartProcessor(processor: ActorRef) = {
    val stopped = gracefulStop(processor, 5 seconds, PoisonPill)
    Await.result(stopped, 6 seconds)

    val actor = system.actorOf(Props(new ProcessingTypeProcessor(
                                       app.injector.instanceOf[ProcessingTypeRepository],
                                       app.injector.instanceOf[CollectionEventTypeRepository],
                                       app.injector.instanceOf[SnapshotWriter])),
                               "processingType")
    Thread.sleep(100)
    actor
  }

  describe("A processingTypes processor must") {

    it("allow recovery from journal", PersistenceTest) {
      val f = collectedSpecimenDerivationFixtures
      val sd = SpecimenDefinition(
          name                    = f.processingSpecimenDefinition.name,
          description             = f.processingSpecimenDefinition.description,
          units                   = f.processingSpecimenDefinition.units,
          anatomicalSourceType    = f.processingSpecimenDefinition.anatomicalSourceType,
          preservationType        = f.processingSpecimenDefinition.preservationType,
          preservationTemperature = f.processingSpecimenDefinition.preservationTemperature,
          specimenType            = f.processingSpecimenDefinition.specimenType)

      val cmd = AddCollectedProcessingTypeCmd(
          sessionUserId             = Global.DefaultUserId.id,
          studyId                   = f.processingType.studyId.id,
          name                      = f.processingType.name,
          description               = f.processingType.description,
          enabled                   = f.processingType.enabled,
          expectedInputChange       = f.processingType.expectedInputChange,
          expectedOutputChange      = f.processingType.expectedOutputChange,
          inputCount                = f.processingType.inputCount,
          outputCount               = f.processingType.outputCount,
          inputContainerTypeId      = f.processingType.inputContainerTypeId.map(_.id),
          outputContainerTypeId     = f.processingType.outputContainerTypeId.map(_.id),
          collectionEventTypeId     = f.specimenDerivation.collectionEventTypeId.id,
          inputSpecimenDefinitionId = f.specimenDerivation.inputSpecimenDefinitionId.id,
          outputSpecimenDefinition  = sd)

      val v = ask(processingTypeProcessor, cmd)
        .mapTo[ServiceValidation[ProcessingTypeEvent]]
        .futureValue

      v.isSuccess must be (true)
      processingTypeRepository.getValues.map { pt => pt.name } must contain (f.processingType.name)

      processingTypeRepository.removeAll
      processingTypeProcessor = restartProcessor(processingTypeProcessor)

      processingTypeRepository.getValues.size must be (1)
      processingTypeRepository.getValues.map { pt => pt.name } must contain (f.processingType.name)
    }

    it("recovers a snapshot", PersistenceTest) {
      val snapshotFilename = "testfilename"
      val fixtures = (1 to 2).map { _ => collectedSpecimenDerivationFixtures }
      val processingTypes = fixtures.map { f => f.processingType }
      val snapshotProcessingType = processingTypes(1)
      val snapshotState = ProcessingTypeProcessor.SnapshotState(Set(snapshotProcessingType))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      processingTypes.foreach(processingTypeRepository.put)

      (processingTypeProcessor ? "snap").mapTo[String].futureValue

      processingTypeRepository.removeAll
      processingTypeProcessor = restartProcessor(processingTypeProcessor)

      processingTypeRepository.getValues.size must be (1)
      processingTypeRepository.getByKey(snapshotProcessingType.id)
        .mustSucceed { repoProcessingType =>
          repoProcessingType.name must be (snapshotProcessingType.name)
        }

      ()
    }

  }

}
