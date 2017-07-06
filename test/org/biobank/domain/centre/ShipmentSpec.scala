package org.biobank.domain.centre

import java.time.OffsetDateTime
import org.biobank.domain.{ DomainSpec, DomainValidation, LocationId }
import org.biobank.fixture.NameGenerator
import org.slf4j.LoggerFactory
import scalaz.Scalaz._
import scala.language.reflectiveCalls

class ShipmentSpec extends DomainSpec {

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

  describe("A shipment") {

    describe("can be created") {

      it("when valid arguments are used") {
        val shipment = factory.createShipment.copy(version = 0L)
        createFrom(shipment).mustSucceed { s =>
          s must have (
            'id             (shipment.id),
            'version        (shipment.version),
            'courierName    (shipment.courierName),
            'trackingNumber (shipment.trackingNumber),
            'fromCentreId   (shipment.fromCentreId),
            'fromLocationId (shipment.fromLocationId),
            'toCentreId     (shipment.toCentreId),
            'toLocationId   (shipment.toLocationId),
            'timePacked     (shipment.timePacked),
            'timeSent       (shipment.timeSent),
            'timeReceived   (shipment.timeReceived),
            'timeUnpacked   (shipment.timeUnpacked)
          )

          checkTimeStamps(s, OffsetDateTime.now, None)
        }
      }

    }

    describe("can change state") {

      it("to packed") {
        val shipment = factory.createShipment
        val timePacked = OffsetDateTime.now.minusDays(10)
        val packedShipment = shipment.pack(timePacked)

        packedShipment mustBe a[PackedShipment]
        packedShipment.state must be (Shipment.packedState)
        packedShipment.timePacked must be (Some(timePacked))
        packedShipment.version must be (shipment.version + 1)
        checkTimeStamps(packedShipment, shipment.timeAdded, OffsetDateTime.now)
      }

      it("to sent") {
        val f = centresFixture
        val shipment = factory.createPackedShipment(f.fromCentre, f.toCentre)
        val timeSent = shipment.timePacked.get.plusDays(1)
        shipment.send(timeSent) mustSucceed { s =>
          s mustBe a[SentShipment]
          s.state must be (Shipment.sentState)
          s.timeSent must be (Some(timeSent))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, OffsetDateTime.now)
        }
      }

      it("to received") {
        val f = centresFixture
        val shipment = factory.createSentShipment(f.fromCentre, f.toCentre)
        val timeReceived = shipment.timeSent.get.plusDays(1)
        shipment.receive(timeReceived) mustSucceed { s =>
          s mustBe a[ReceivedShipment]
          s.state must be (Shipment.receivedState)
          s.timeReceived must be (Some(timeReceived))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, OffsetDateTime.now)
        }
      }

      it("to unpacked") {
        val f = centresFixture
        val shipment = factory.createReceivedShipment(f.fromCentre, f.toCentre)
        val timeUnpacked = shipment.timeReceived.get.plusDays(1)
        shipment.unpack(timeUnpacked) mustSucceed { s =>
          s mustBe a[UnpackedShipment]
          s.state must be (Shipment.unpackedState)
          s.timeUnpacked must be (Some(timeUnpacked))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, OffsetDateTime.now)
        }
      }

      it("to completed") {
        val f = centresFixture
        val shipment = factory.createUnpackedShipment(f.fromCentre, f.toCentre)
        val timeCompleted = shipment.timeUnpacked.get.plusDays(1)
        shipment.complete(timeCompleted) mustSucceed { s =>
          s mustBe a[CompletedShipment]
          s.state must be (Shipment.completedState)
          s.timeCompleted must be (Some(timeCompleted))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, OffsetDateTime.now)
        }
      }

      it("to lost") {
        val f = centresFixture
        val shipment = factory.createSentShipment(f.fromCentre, f.toCentre)
        val lostShipment = shipment.lost

        lostShipment mustBe a[LostShipment]
        lostShipment.state must be (Shipment.lostState)
        lostShipment.version must be (shipment.version + 1)
        checkTimeStamps(lostShipment, shipment.timeAdded, OffsetDateTime.now)
      }

    }

    describe("can go to previous state") {

      it("from packed to created") {
        val f = centresFixture
        val shipment = factory.createPackedShipment(f.fromCentre, f.toCentre)
        val createdShipment = shipment.created
        createdShipment mustBe a[CreatedShipment]
      }

      it("from sent to packed") {
        val f = centresFixture
        val shipment = factory.createSentShipment(f.fromCentre, f.toCentre)
        val packedShipment = shipment.backToPacked
        packedShipment mustBe a[PackedShipment]
      }

      it("from received to sent") {
        val f = centresFixture
        val shipment = factory.createReceivedShipment(f.fromCentre, f.toCentre)
        val sentShipment = shipment.backToSent
        sentShipment mustBe a[SentShipment]
      }

      it("from unpacked to received") {
        val f = centresFixture
        val shipment = factory.createUnpackedShipment(f.fromCentre, f.toCentre)
        val receivedShipment = shipment.backToReceived
        receivedShipment mustBe a[ReceivedShipment]
      }

      it("from completed to unpacked") {
        val f = centresFixture
        val shipment = factory.createCompletedShipment(f.fromCentre, f.toCentre)
        val unpackedShipment = shipment.backToUnpacked
        unpackedShipment mustBe a[UnpackedShipment]
      }

      it("from lost to sent") {
        val f = centresFixture
        val shipment = factory.createLostShipment(f.fromCentre, f.toCentre)
        val sentShipment = shipment.backToSent
        sentShipment mustBe a[SentShipment]
      }

    }

    describe("can skip state") {

      it("from created to sent") {
        val shipment = factory.createShipment
        val timePacked = OffsetDateTime.now.minusDays(10)
        val timeSent = timePacked.plusDays(1)
        shipment.skipToSent(timePacked, timeSent) mustSucceed { s =>
          s mustBe a[SentShipment]
          s.timePacked must be (Some(timePacked))
          s.timeSent must be (Some(timeSent))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, OffsetDateTime.now)
        }
      }

      it("from sent to unpacked") {
        val f = centresFixture
        val shipment = factory.createSentShipment(f.fromCentre, f.toCentre)
        val timeReceived = shipment.timeSent.fold { OffsetDateTime.now } { t => t }
        val timeUnpacked = timeReceived.plusDays(1)
        shipment.skipToUnpacked(timeReceived, timeUnpacked) mustSucceed { s =>
          s mustBe a[UnpackedShipment]
          s.timeReceived must be (Some(timeReceived))
          s.timeUnpacked must be (Some(timeUnpacked))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, OffsetDateTime.now)
        }
      }

    }

    describe("must not skip state") {

      it("when time sent is before time packed") {
        val shipment = factory.createShipment
        val timePacked = OffsetDateTime.now.minusDays(10)
        val timeSent = timePacked.minusDays(1)
        shipment.skipToSent(timePacked, timeSent) mustFailContains "TimeSentBeforePacked"
      }

      it("when time unpacked is before time received") {
        val f = centresFixture
        val shipment = factory.createSentShipment(f.fromCentre, f.toCentre)
        val timeReceived = shipment.timeSent.fold { OffsetDateTime.now } { t => t }
        val timeUnpacked = timeReceived.minusDays(1)
        shipment.skipToUnpacked(timeReceived, timeUnpacked) mustFailContains "TimeUnpackedBeforeReceived"
      }

      it("when time received is before time sent") {
        val f = centresFixture
        val shipment = factory.createSentShipment(f.fromCentre, f.toCentre)
        val timeReceived = shipment.timeSent.fold { OffsetDateTime.now } { t => t.minusDays(1) }
        val timeUnpacked = timeReceived.plusDays(2)
        shipment.skipToUnpacked(timeReceived, timeUnpacked) mustFailContains "TimeReceivedBeforeSent"
      }

    }

    describe("cannot be created") {

      it("with an invalid ID") {
        val shipment = factory.createShipment.copy(id = ShipmentId(""))
        createFrom(shipment) mustFail "IdRequired"
      }

      it("with an invalid version") {
        val shipment = factory.createShipment.copy(version = -2L)
        createFrom(shipment) mustFail "InvalidVersion"
      }

      it("with an invalid courier name") {
        val shipment = factory.createShipment.copy(courierName = "")
        createFrom(shipment) mustFail "CourierNameInvalid"
      }

      it("with an invalid tracking number") {
        val shipment = factory.createShipment.copy(trackingNumber = "")
        createFrom(shipment) mustFail "TrackingNumberInvalid"
      }

      it("with an invalid from location") {
        val shipment = factory.createShipment.copy(fromLocationId = LocationId(""))
        createFrom(shipment) mustFail "FromLocationIdInvalid"
      }

      it("with an invalid to location") {
        val shipment = factory.createShipment.copy(toLocationId = LocationId(""))
        createFrom(shipment) mustFail "ToLocationIdInvalid"
      }

    }

  }
}
