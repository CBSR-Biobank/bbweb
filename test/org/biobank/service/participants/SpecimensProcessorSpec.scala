package org.biobank.service.participants

import akka.actor.ActorRef
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.fixture._
import org.biobank.domain.study.{StudyRepository, CollectionEventTypeRepository}
import org.biobank.domain.centre.CentreRepository
import org.biobank.domain.participants._
import org.biobank.service._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scala.language.reflectiveCalls
import scalaz.Scalaz._

case class NamedSpecimensProcessor @Inject() (@Named("specimensProcessor") processor: ActorRef)

class SpecimensProcessorSpec
    extends ProcessorTestFixture
    with SpecimenSpecFixtures
    with PresistenceQueryEvents {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.SpecimenCommands._
  import org.biobank.infrastructure.event.SpecimenEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val specimensProcessor = app.injector.instanceOf[NamedSpecimensProcessor].processor

  val studyRepository = app.injector.instanceOf[StudyRepository]

  val centreRepository = app.injector.instanceOf[CentreRepository]

  val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  val participantRepository = app.injector.instanceOf[ParticipantRepository]

  val collectionEventRepository = app.injector.instanceOf[CollectionEventRepository]

  val specimenRepository = app.injector.instanceOf[SpecimenRepository]

  val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    specimenRepository.removeAll
    super.beforeEach()
  }

  describe("A specimens processor must") {

    ignore("allow recovery from journal", PersistenceTest) {
      val f = createEntitiesAndSpecimens

      centreRepository.put(f.centre)
      studyRepository.put(f.study)
      collectionEventTypeRepository.put(f.ceventType)
      participantRepository.put(f.participant)
      collectionEventRepository.put(f.cevent)

      f.specimens.foreach { specimen =>
        val specimenInfo = SpecimenInfo(inventoryId           = specimen.inventoryId,
                                        specimenDescriptionId = specimen.specimenDescriptionId.id,
                                        timeCreated           = specimen.timeCreated,
                                        locationId            = specimen.originLocationId.id,
                                        amount                = specimen.amount)

        val cmd = AddSpecimensCmd(sessionUserId     = nameGenerator.next[String],
                                  collectionEventId = f.cevent.id.id,
                                  specimenData      = List(specimenInfo))

        val v = (specimensProcessor ? cmd).mapTo[ServiceValidation[SpecimenEvent]].futureValue
        v.isSuccess must be (true)
        specimenRepository.getValues.map { s => s.inventoryId } must contain (specimen.inventoryId)
      }

      specimenRepository.removeAll
      specimensProcessor ! "persistence_restart"

      Thread.sleep(2500)
      logEvents("specimens-processor-id")

      specimenRepository.getValues.size must be (f.specimens.size)
      f.specimens.foreach { specimen =>
        specimenRepository.getValues.map { s => s.inventoryId } must contain (specimen.inventoryId)
      }
    }

    it("allow a snapshot request", PersistenceTest) {
      val specimens = (1 to 2).map { _ => factory.createUsableSpecimen }
      specimens.foreach(specimenRepository.put)

      specimensProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    it("accept a snapshot offer", PersistenceTest) {
      val snapshotFilename = "testfilename"
      val specimens = (1 to 2).map { _ => factory.createUsableSpecimen }
      val snapshotSpecimen = specimens(1)
      val snapshotState = SpecimensProcessor.SnapshotState(Set(snapshotSpecimen))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      specimens.foreach(specimenRepository.put)
      specimensProcessor ? "snap"
      Thread.sleep(250)
      specimensProcessor ! "persistence_restart"
      specimenRepository.removeAll

      Thread.sleep(250)

      specimenRepository.getValues.size must be (1)
      specimenRepository.getByKey(snapshotSpecimen.id) mustSucceed { repoSpecimen =>
        repoSpecimen.inventoryId must be (snapshotSpecimen.inventoryId)
        ()
      }
    }

  }

}
