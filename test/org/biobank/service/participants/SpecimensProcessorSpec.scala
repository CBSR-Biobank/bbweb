package org.biobank.service.participants

import akka.pattern._
import org.biobank.fixture._
import org.biobank.domain.participants.SpecimenSpecFixtures
import org.biobank.service.ServiceValidation
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scala.language.reflectiveCalls
import scalaz.Scalaz._

class SpecimensProcessorSpec extends TestFixture with SpecimenSpecFixtures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.SpecimenCommands._
  import org.biobank.infrastructure.event.SpecimenEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    specimenRepository.removeAll
    super.beforeEach()
  }

  "A specimens processor" must {

    "111 allow recovery from journal" in {
      val f = createEntitiesAndSpecimens

      val specimen = f.specimens(1)
      val specimenInfo = SpecimenInfo(inventoryId    = specimen.inventoryId,
                                      specimenSpecId = specimen.specimenSpecId,
                                      timeCreated    = specimen.timeCreated,
                                      locationId     = specimen.originLocationId.id,
                                      amount         = specimen.amount)

      val cmd = AddSpecimensCmd(userId            = nameGenerator.next[String],
                                collectionEventId = f.cevent.id.id,
                                specimenData      = List(specimenInfo))
      centreRepository.put(f.centre)
      studyRepository.put(f.study)
      collectionEventTypeRepository.put(f.ceventType)
      participantRepository.put(f.participant)
      collectionEventRepository.put(f.cevent)

      val v = (specimensProcessor ? cmd).mapTo[ServiceValidation[SpecimenEvent]].futureValue
      v.isSuccess must be (true)
      specimenRepository.getValues.map { s => s.inventoryId } must contain (specimen.inventoryId)
      specimensProcessor ! "persistence_restart"
      specimenRepository.removeAll

      Thread.sleep(250)

      specimenRepository.getValues.size must be (1)
      specimenRepository.getValues.map { s => s.inventoryId } must contain (specimen.inventoryId)
    }

    "allow a snapshot request" in {
      val specimens = (1 to 2).map { _ => factory.createUsableSpecimen }
      specimens.foreach(specimenRepository.put)

      specimensProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    "accept a snapshot offer" in {
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
      }
    }

  }

}
