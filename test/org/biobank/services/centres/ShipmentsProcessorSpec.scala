package org.biobank.services.centres

import akka.actor._
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.Global
import org.biobank.domain.centres.ShipmentSpecFixtures
import org.biobank.domain.studies.StudyRepository
import org.biobank.domain.centres._
import org.biobank.domain.participants._
import org.biobank.fixture._
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.Await

final case class NamedShipmentsProcessor @Inject() (@Named("shipmentsProcessor") processor: ActorRef)

class ShipmentsProcessorSpec extends ProcessorTestFixture with ShipmentSpecFixtures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.ShipmentCommands._
  import org.biobank.infrastructure.events.ShipmentEvents._

  private var shipmentsProcessor = app.injector.instanceOf[NamedShipmentsProcessor].processor

  private val studyRepository = app.injector.instanceOf[StudyRepository]

  private val centreRepository = app.injector.instanceOf[CentreRepository]

  private val shipmentRepository = app.injector.instanceOf[ShipmentRepository]

  override def beforeEach() {
    studyRepository.removeAll
    shipmentRepository.removeAll
    centreRepository.removeAll
    super.beforeEach()
  }

  override def centresFixture = {
    val f = super.centresFixture
    centreRepository.put(f.fromCentre)
    centreRepository.put(f.toCentre)
    f
  }

  override def createdShipmentsFixture(numShipments: Int) = {
    val f = super.createdShipmentsFixture(numShipments)
    f.shipmentMap.values.foreach(shipmentRepository.put)
    f
  }

  private def restartProcessor(processor: ActorRef) = {
    val stopped = gracefulStop(processor, 5 seconds, PoisonPill)
    Await.result(stopped, 6 seconds)

    val actor = system.actorOf(Props(new ShipmentsProcessor(
                                       shipmentRepository,
                                       app.injector.instanceOf[ShipmentSpecimenRepository],
                                       centreRepository,
                                       app.injector.instanceOf[SpecimenRepository],
                                       app.injector.instanceOf[SnapshotWriter])),
                               "shipments")
    Thread.sleep(250)
    actor
  }

  describe("A shipments processor must") {

    it("allow recovery from journal", PersistenceTest) {
      val f = createdShipmentFixture
      val cmd = AddShipmentCmd(sessionUserId  = Global.DefaultUserId.id,
                               courierName    = f.shipment.courierName,
                               trackingNumber = f.shipment.trackingNumber,
                               fromLocationId = f.shipment.fromLocationId.id,
                               toLocationId   = f.shipment.toLocationId.id)

      val v = ask(shipmentsProcessor, cmd).mapTo[ServiceValidation[ShipmentEvent]].futureValue
      v.isSuccess must be (true)
      shipmentRepository.getValues.map { s => s.courierName } must contain (f.shipment.courierName)

      shipmentRepository.removeAll
      shipmentsProcessor = restartProcessor(shipmentsProcessor)

      shipmentRepository.getValues.size must be (1)
      shipmentRepository.getValues.map { s => s.courierName } must contain (f.shipment.courierName)
    }

    it("recovers a snapshot", PersistenceTest) {
      val f = createdShipmentsFixture(2)
      val snapshotFilename = "testfilename"
      val snapshotShipment = f.shipmentMap.values.toList(1)
      val snapshotState = ShipmentsProcessor.SnapshotState(Set(snapshotShipment), Set.empty)

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);
      f.shipmentMap.values.foreach(shipmentRepository.put)

      (shipmentsProcessor ? "snap").mapTo[String].futureValue
      shipmentRepository.removeAll
      shipmentsProcessor = restartProcessor(shipmentsProcessor)

      shipmentRepository.getValues.size must be (1)
      shipmentRepository.getByKey(snapshotShipment.id) mustSucceed { repoShipment =>
        repoShipment.courierName must be (snapshotShipment.courierName)
        ()
      }
    }

  }

}
