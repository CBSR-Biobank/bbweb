package org.biobank.domain.centre

import org.biobank.domain.{ DomainFreeSpec, DomainValidation }
import org.biobank.fixture.NameGenerator
import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import scalaz.Scalaz._

class ShipmentSpec extends DomainFreeSpec {

  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(shipment: Shipment): DomainValidation[Shipment] = {
    Shipment.create(id             = shipment.id,
                    version        = shipment.version,
                    state          = shipment.state,
                    courierName    = shipment.courierName,
                    trackingNumber = shipment.trackingNumber,
                    fromLocationId = shipment.fromLocationId,
                    toLocationId   = shipment.toLocationId,
                    timePacked     = shipment.timePacked,
                    timeSent       = shipment.timeSent,
                    timeReceived   = shipment.timeReceived,
                    timeUnpacked   = shipment.timeUnpacked)
  }

  "A shipment" - {

    "can be created" - {

      "when valid arguments are used" in {
        val shipment = factory.createShipment.copy(version = 0L)
        createFrom(shipment).mustSucceed { s =>
          s must have (
            'id             (shipment.id),
            'version        (shipment.version),
            'state          (shipment.state),
            'courierName    (shipment.courierName),
            'trackingNumber (shipment.trackingNumber),
            'fromLocationId (shipment.fromLocationId),
            'toLocationId   (shipment.toLocationId),
            'timePacked     (shipment.timePacked),
            'timeSent       (shipment.timeSent),
            'timeReceived   (shipment.timeReceived),
            'timeUnpacked   (shipment.timeUnpacked)
          )

          checkTimeStamps(s, DateTime.now, None)
        }
      }

    }

    "can be updated" - {

      "with a new state" in {
        val shipment = factory.createShipment
        val newState = ShipmentState.Packed
        shipment.withState(newState) mustSucceed { s =>
          s.state must be (newState)
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "with a new courier name" in {
        val shipment = factory.createShipment
        val newCourierName = nameGenerator.next[Shipment]
        shipment.withCourier(newCourierName) mustSucceed { s =>
          s.courierName must be (newCourierName)
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "with a new tracking number" in {
        val shipment = factory.createShipment
        val newTrackingNumber = nameGenerator.next[Shipment]
        shipment.withTrackingNumber(newTrackingNumber) mustSucceed { s =>
          s.trackingNumber must be (newTrackingNumber)
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "with a new from location" in {
        val shipment = factory.createShipment
        val location = factory.createLocation
        shipment.withFromLocation(location) mustSucceed { s =>
          s.fromLocationId must be (location.uniqueId)
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "with a new to location" in {
        val shipment = factory.createShipment
        val location = factory.createLocation
        shipment.withToLocation(location) mustSucceed { s =>
          s.toLocationId must be (location.uniqueId)
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

    }

    "can change state" - {

      "to packed" in {
        val shipment = factory.createShipment
        val timePacked = DateTime.now.minusDays(10)
        shipment.packed(timePacked) mustSucceed { s =>
          s.timePacked must be (Some(timePacked))
          s.version must be (shipment.version + 1)
          s.state must be (ShipmentState.Packed)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "to sent" in {
        val shipment = factory.createPackedShipment
        val timeSent = shipment.timePacked.get.plusDays(1)
        shipment.sent(timeSent) mustSucceed { s =>
          s.state must be (ShipmentState.Sent)
          s.timeSent must be (Some(timeSent))
          s.version must be (shipment.version + 1)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "to received" in {
        val shipment = factory.createSentShipment
        val timeReceived = shipment.timeSent.get.plusDays(1)
        shipment.received(timeReceived) mustSucceed { s =>
          s.timeReceived must be (Some(timeReceived))
          s.version must be (shipment.version + 1)
          s.state must be (ShipmentState.Received)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "to unpacked" in {
        val shipment = factory.createReceivedShipment
        val timeUnpacked = shipment.timeSent.get.plusDays(1)
        shipment.unpacked(timeUnpacked) mustSucceed { s =>
          s.timeUnpacked must be (Some(timeUnpacked))
          s.version must be (shipment.version + 1)
          s.state must be (ShipmentState.Unpacked)
          checkTimeStamps(s, shipment.timeAdded, DateTime.now)
        }
      }

      "to lost" in {
        val shipment: Shipment = factory.createSentShipment
        shipment.lost mustSucceed { s =>
          s.state must be (ShipmentState.Lost)
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
        val shipment = factory.createShipment.copy(fromLocationId = "")
        createFrom(shipment) mustFail "FromLocationIdInvalid"
      }

      "with an invalid to location" in {
        val shipment = factory.createShipment.copy(toLocationId = "")
        createFrom(shipment) mustFail "ToLocationIdInvalid"
      }

      "with a time sent before the time packed" in {
        val shipment = factory.createPackedShipment
        val shipment2 = shipment.copy(timeSent = shipment.timePacked.map(t => t.minusDays(1)))
        createFrom(shipment2) mustFail "TimeSentBeforePacked"
      }

      "with a time received before the time sent" in {
        val shipment = factory.createSentShipment
        val shipment2 = shipment.copy(timeReceived = shipment.timeSent.map(t => t.minusDays(1)))
        createFrom(shipment2) mustFail "TimeReceivedBeforeSent"
      }

      "with a time unpacked before the time received" in {
        val shipment = factory.createReceivedShipment
        val shipment2 = shipment.copy(timeUnpacked = shipment.timeReceived.map(t => t.minusDays(1)))
        createFrom(shipment2) mustFail "TimeUnpackedBeforeReceived"
      }

      "with a time received and NO time sent" in {
        val shipment = factory.createReceivedShipment.copy(timeSent = None)
        createFrom(shipment) mustFail "TimeSentUndefined"
      }

      "with a time unpacked and NO time received" in {
        val shipment = factory.createUnpackedShipment.copy(timeReceived = None)
        createFrom(shipment) mustFail "TimeReceivedUndefined"
      }

    }

    "state cannot be changed" - {

      "to packed from an invalid state" in {
        val shipments = List(factory.createSentShipment,
                             factory.createPackedShipment,
                             factory.createReceivedShipment,
                             factory.createUnpackedShipment,
                             factory.createLostShipment)
        shipments.foreach { shipment =>
          info(s"shipment from ${shipment.state} to Packed fails")
          shipment.packed(DateTime.now) mustFail "InvalidStateTransition.*PACKED.*"
        }
      }

      "to sent from an invalid state" in {
        val shipments = List(factory.createShipment,
                             factory.createSentShipment,
                             factory.createReceivedShipment,
                             factory.createUnpackedShipment,
                             factory.createLostShipment)
        shipments.foreach { shipment =>
          info(s"shipment from ${shipment.state} to Sent fails")
          shipment.sent(DateTime.now) mustFail "InvalidStateTransition.*SENT.*"
        }
      }

      "to received from an invalid state" in {
        val shipments = List(factory.createShipment,
                             factory.createPackedShipment,
                             factory.createReceivedShipment,
                             factory.createUnpackedShipment,
                             factory.createLostShipment)
        shipments.foreach { shipment =>
          info(s"shipment from ${shipment.state} to Received fails")
          shipment.received(DateTime.now) mustFail "InvalidStateTransition.*RECEIVED.*"
        }
      }

      "to unpacked from an invalid state" in {
        val shipments = List(factory.createShipment,
                             factory.createPackedShipment,
                             factory.createSentShipment,
                             factory.createUnpackedShipment,
                             factory.createLostShipment)
        shipments.foreach { shipment =>
          info(s"shipment from ${shipment.state} to Unpacked fails")
          shipment.unpacked(DateTime.now) mustFail "InvalidStateTransition.*UNPACKED.*"
        }
      }

      "to lost from an invalid state" in {
        val shipments = List(factory.createShipment,
                             factory.createPackedShipment,
                             factory.createReceivedShipment,
                             factory.createUnpackedShipment,
                             factory.createLostShipment)
        shipments.foreach { shipment =>
          info(s"shipment from ${shipment.state} to Lost fails")
          shipment.lost mustFail "InvalidStateTransition.*LOST.*"
        }
      }

    }

  }
}
