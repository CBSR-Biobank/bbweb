package org.biobank.service.centres

import akka.pattern._
import org.biobank.domain.centre.ShipmentSpecFixtures
import org.biobank.fixture._
import org.biobank.service.ServiceValidation
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._
import scala.language.reflectiveCalls

class ShipmentsProcessorSpec extends TestFixture with ShipmentSpecFixtures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.ShipmentCommands._
  import org.biobank.infrastructure.event.ShipmentEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  val persistenceId = "shipments-processor-id"

  override def beforeEach() {
    studyRepository.removeAll
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

  "A shipments processor" must {

    "allow recovery from journal" in {
      val f = createdShipmentFixture
      val cmd = AddShipmentCmd(userId         = "",
                               courierName    = f.shipment.courierName,
                               trackingNumber = f.shipment.trackingNumber,
                               fromLocationId = f.shipment.fromLocationId.id,
                               toLocationId   = f.shipment.toLocationId.id)

      val v = ask(shipmentsProcessor, cmd).mapTo[ServiceValidation[ShipmentEvent]].futureValue
      v.isSuccess must be (true)
      shipmentRepository.getValues.map { s => s.courierName } must contain (f.shipment.courierName)
      shipmentsProcessor ! "persistence_restart"
      shipmentRepository.removeAll

      Thread.sleep(250)

      shipmentRepository.getValues.size must be (1)
      shipmentRepository.getValues.map { s => s.courierName } must contain (f.shipment.courierName)
    }

    "allow a snapshot request" in {
      val f = createdShipmentFixture
      shipmentRepository.put(f.shipment)

      shipmentsProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    "accept a snapshot offer" in {
      val f = createdShipmentsFixture(2)
      val snapshotFilename = "testfilename"
      val snapshotShipment = f.shipmentMap.values.toList(1)
      val snapshotState = ShipmentsProcessor.SnapshotState(Set(snapshotShipment), Set.empty)

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      f.shipmentMap.values.foreach(shipmentRepository.put)
      shipmentsProcessor ? "snap"
      Thread.sleep(250)
      shipmentsProcessor ! "persistence_restart"
      shipmentRepository.removeAll

      Thread.sleep(250)

      shipmentRepository.getValues.size must be (1)
      shipmentRepository.getByKey(snapshotShipment.id) mustSucceed { repoShipment =>
        repoShipment.courierName must be (snapshotShipment.courierName)
      }
    }

  }

}
