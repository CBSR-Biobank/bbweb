package org.biobank.domain.centre

import com.github.nscala_time.time.Imports._
import org.biobank.domain.{ DomainFreeSpec, DomainValidation, LocationId }
import org.biobank.fixture.NameGenerator
import org.slf4j.LoggerFactory
import scalaz.Scalaz._
import scala.language.reflectiveCalls

class ShipmentSpec extends DomainFreeSpec {

  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def centresFixture = {
    val centres = (1 to 2).map { _ =>
        val location = factory.createLocation
        factory.createEnabledCentre.copy(locations = Set(location))
      }
    new {
      val fromCentre = centres(0)
      val toCentre = centres(1)
    }
  }

  def allShipmentsFixture = {
    val f = centresFixture
    new {
      val fromCentre = f.fromCentre
      val toCentre = f.toCentre
      val shipments = Map[String, Shipment](
          "created"   -> factory.createShipment(fromCentre, toCentre),
          "packed"    -> factory.createPackedShipment(fromCentre, toCentre),
          "sent"      -> factory.createSentShipment(fromCentre, toCentre),
          "received"  -> factory.createReceivedShipment(fromCentre, toCentre),
          "unpacked"  -> factory.createUnpackedShipment(fromCentre, toCentre),
          "completed" -> factory.createCompletedShipment(fromCentre, toCentre),
          "lost"      -> factory.createLostShipment(fromCentre, toCentre))
    }
  }

  def createFrom(shipment: Shipment): DomainValidation[CreatedShipment] = {
    CreatedShipment.create(id             = shipment.id,
                           version        = shipment.version,
                           timeAdded      = shipment.timeAdded,
                           courierName    = shipment.courierName,
                           trackingNumber = shipment.trackingNumber,
                           fromCentreId   = shipment.fromCentreId,
                           fromLocationId = shipment.fromLocationId,
                           toCentreId     = shipment.toCentreId,
                           toLocationId   = shipment.toLocationId)
  }

  "A shipment" - {

    "can be created" - {

      "when valid arguments are used" in {
        val shipment = factory.createShipment.copy(version = 0L)
        createFrom(shipment).mustSucceed { s =>
          s must have (
            'id             (shipment.id),
            'version        (shipment.version),
            'courierName    (shipment.courierName),
            'trackingNumber (shipment.trackingNumber),
            'fromCentreId   (shipment.fromCentreId),
            'fromLocationId (shipment.fromLocationId.id),
            'toCentreId     (shipment.toCentreId),
            'toLocationId   (shipment.toLocationId.id),
            'timePacked     (shipment.timePacked),
            'timeSent       (shipment.timeSent),
            'timeReceived   (shipment.timeReceived),
            'timeUnpacked   (shipment.timeUnpacked)
          )

          checkTimeStamps(s, DateTime.now, None)
        }
      }

    }

    "can change state" - {

      "to packed" in {
        val shipment = factory.createShipment
        val timePacked = DateTime.now.minusDays(10)
        val packedShipment = shipment.pack(timePacked)

        packedShipment mustBe a[PackedShipment]
        packedShipment.timePacked must be (Some(timePacked))
        packedShipment.version must be (shipment.version + 1)
        checkTimeStamps(packedShipment, shipment.timeAdded, DateTime.now)
      }

      "to sent" in {
        val f = centresFixture
        val shipment = factory.createPackedShipment(f.fromCentre, f.toCentre)
        val timeSent = shipment.timePacked.get.plusDays(1)
        shipment.send(timeSent) mustSucceed { s =>
          s mustBe a[SentShipment]
          s.timeSent must be (Some(timeSent))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "to received" in {
        val f = centresFixture
        val shipment = factory.createSentShipment(f.fromCentre, f.toCentre)
        val timeReceived = shipment.timeSent.get.plusDays(1)
        shipment.receive(timeReceived) mustSucceed { s =>
          s mustBe a[ReceivedShipment]
          s.timeReceived must be (Some(timeReceived))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "to unpacked" in {
        val f = centresFixture
        val shipment = factory.createReceivedShipment(f.fromCentre, f.toCentre)
        val timeUnpacked = shipment.timeReceived.get.plusDays(1)
        shipment.unpack(timeUnpacked) mustSucceed { s =>
          s mustBe a[UnpackedShipment]
          s.timeUnpacked must be (Some(timeUnpacked))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "to completed" in {
        val f = centresFixture
        val shipment = factory.createUnpackedShipment(f.fromCentre, f.toCentre)
        val timeCompleted = shipment.timeUnpacked.get.plusDays(1)
        shipment.complete(timeCompleted) mustSucceed { s =>
          s mustBe a[CompletedShipment]
          s.timeCompleted must be (Some(timeCompleted))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "to lost" in {
        val f = centresFixture
        val shipment = factory.createSentShipment(f.fromCentre, f.toCentre)
        val lostShipment = shipment.lost

        lostShipment mustBe a[LostShipment]
        lostShipment.version must be (shipment.version + 1)
        checkTimeStamps(lostShipment, shipment.timeAdded, DateTime.now)
      }

    }

    "can go to previous state" - {

      "from packed to created" in {
        val f = centresFixture
        val shipment = factory.createPackedShipment(f.fromCentre, f.toCentre)
        val createdShipment = shipment.created
        createdShipment mustBe a[CreatedShipment]
      }

      "from sent to packed" in {
        val f = centresFixture
        val shipment = factory.createSentShipment(f.fromCentre, f.toCentre)
        val packedShipment = shipment.backToPacked
        packedShipment mustBe a[PackedShipment]
      }

      "from received to sent" in {
        val f = centresFixture
        val shipment = factory.createReceivedShipment(f.fromCentre, f.toCentre)
        val sentShipment = shipment.backToSent
        sentShipment mustBe a[SentShipment]
      }

      "from unpacked to received" in {
        val f = centresFixture
        val shipment = factory.createUnpackedShipment(f.fromCentre, f.toCentre)
        val receivedShipment = shipment.backToReceived
        receivedShipment mustBe a[ReceivedShipment]
      }

      "from completed to unpacked" in {
        val f = centresFixture
        val shipment = factory.createCompletedShipment(f.fromCentre, f.toCentre)
        val unpackedShipment = shipment.backToUnpacked
        unpackedShipment mustBe a[UnpackedShipment]
      }

      "from lost to sent" in {
        val f = centresFixture
        val shipment = factory.createLostShipment(f.fromCentre, f.toCentre)
        val sentShipment = shipment.backToSent
        sentShipment mustBe a[SentShipment]
      }

    }

    "can skip state" - {

      "from created to sent" in {
        val shipment = factory.createShipment
        val timePacked = DateTime.now.minusDays(10)
        val timeSent = timePacked.plusDays(1)
        shipment.skipToSent(timePacked, timeSent) mustSucceed { s =>
          s mustBe a[SentShipment]
          s.timePacked must be (Some(timePacked))
          s.timeSent must be (Some(timeSent))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "from sent to unpacked" in {
        val f = centresFixture
        val shipment = factory.createSentShipment(f.fromCentre, f.toCentre)
        val timeReceived = shipment.timeSent.fold { DateTime.now } { t => t }
        val timeUnpacked = timeReceived.plusDays(1)
        shipment.skipToUnpacked(timeReceived, timeUnpacked) mustSucceed { s =>
          s mustBe a[UnpackedShipment]
          s.timeReceived must be (Some(timeReceived))
          s.timeUnpacked must be (Some(timeUnpacked))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

    }

    "cannot be created" - {

      "with an invalid ID" in {
        val shipment = factory.createShipment.copy(id = ShipmentId(""))
        createFrom(shipment) mustFail "IdRequired"
      }

      "with an invalid version" in {
        val shipment = factory.createShipment.copy(version = -2L)
        createFrom(shipment) mustFail "InvalidVersion"
      }

      "with an invalid courier name" in {
        val shipment = factory.createShipment.copy(courierName = "")
        createFrom(shipment) mustFail "CourierNameInvalid"
      }

      "with an invalid tracking number" in {
        val shipment = factory.createShipment.copy(trackingNumber = "")
        createFrom(shipment) mustFail "TrackingNumberInvalid"
      }

      "with an invalid from location" in {
        val shipment = factory.createShipment.copy(fromLocationId = LocationId(""))
        createFrom(shipment) mustFail "FromLocationIdInvalid"
      }

      "with an invalid to location" in {
        val shipment = factory.createShipment.copy(toLocationId = LocationId(""))
        createFrom(shipment) mustFail "ToLocationIdInvalid"
      }

    }

  }
}
