package org.biobank.domain.centre

import com.github.nscala_time.time.Imports._
import play.api.libs.json._
import play.api.libs.json.Reads._
import org.biobank.{ValidationKey, ValidationMsgKey}
import org.biobank.domain.centre.ShipmentState._
import org.biobank.domain._
import org.biobank.infrastructure.JsonUtils._
import org.joda.time.DateTime
import scalaz.Scalaz._

case class ShipmentId(id: String) extends IdentifiedValueObject[String]

object ShipmentId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val shipmentIdReader =
    (__ \ "id").read[String].map( new ShipmentId(_) )

  implicit val shipmentIdWriter =
    Writes{ (shipmentId: ShipmentId) => JsString(shipmentId.id) }

}

trait ShipmentValidations {

  case object CourierNameInvalid extends ValidationKey

  case object TrackingNumberInvalid extends ValidationKey

  case object FromLocationIdInvalid extends ValidationKey

  case object ToLocationIdInvalid extends ValidationKey

  case object TimePackedUndefined extends ValidationKey

  case object TimeSentBeforePacked extends ValidationKey

  case object TimeSentUndefined extends ValidationKey

  case object TimeReceivedBeforeSent extends ValidationKey

  case object TimeReceivedUndefined extends ValidationKey

  case object TimeUnpackedBeforeReceived extends ValidationKey

  case class InvalidStateTransition(msg: String) extends ValidationMsgKey

  def validateTimeAfter(afterMaybe: Option[DateTime],
                        beforeMaybe: Option[DateTime],
                        errUndefined: ValidationKey,
                        errNotAfter: ValidationKey)
      : DomainValidation[Option[DateTime]] = {
    if (beforeMaybe.isEmpty) {
      if (afterMaybe.isDefined) errUndefined.failureNel
      else afterMaybe.success
    } else if (afterMaybe.isEmpty || afterMaybe.exists(after => after > beforeMaybe.get)) {
      afterMaybe.success
    } else {
      errNotAfter.failureNel
    }
  }

}

/**
 * Represents a transfer of [org.biobank.domain.participants.Specimen]s and / or
 * [org.biobank.domain.containers.Container]s from one [org.biobank.domain.centre.Centre] to antoher.
 *
 * @see org.biobank.domain.centre.ShipmentSpecimen
 * @see org.biobank.domain.centre.ShipmentContainer
 */
case class Shipment(id:             ShipmentId,
                    version:        Long,
                    timeAdded:      DateTime,
                    timeModified:   Option[DateTime],
                    state:          ShipmentState,
                    courierName:    String,
                    trackingNumber: String,
                    fromLocationId: String,
                    toLocationId:   String,
                    timePacked:     Option[DateTime],
                    timeSent:       Option[DateTime],
                    timeReceived:   Option[DateTime],
                    timeUnpacked:   Option[DateTime])
    extends ConcurrencySafeEntity[ShipmentId]
    with ShipmentValidations {

  import org.biobank.domain.CommonValidations._
  import org.biobank.CommonValidations._

  def withState(state: ShipmentState): DomainValidation[Shipment] =
    copy(state       = state,
         version      = version + 1,
         timeModified = Some(DateTime.now)).success

  def withCourier(name: String): DomainValidation[Shipment] =
    validateString(name, CourierNameInvalid).map { name =>
      copy(courierName  = name,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }

  def withTrackingNumber(trackingNumber: String): DomainValidation[Shipment] =
    validateString(trackingNumber, TrackingNumberInvalid).map { _ =>
      copy(trackingNumber = trackingNumber,
           version        = version + 1,
           timeModified   = Some(DateTime.now))
    }

  /**
   * Must be a centre's location.
   */
  def withFromLocation(location: Location): DomainValidation[Shipment] =
    validateString(location.uniqueId, LocationIdInvalid).map { _ =>
      copy(fromLocationId = location.uniqueId,
           version        = version + 1,
           timeModified   = Some(DateTime.now))
    }

  /**
   * Must be a centre's location.
   */
  def withToLocation(location: Location): DomainValidation[Shipment] =
    validateString(location.uniqueId, LocationIdInvalid).map { _ =>
      copy(toLocationId = location.uniqueId,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }

  def packed(time: DateTime): DomainValidation[Shipment] =
    if (state != ShipmentState.Created) {
      InvalidStateTransition(s"cannot set state to PACKED: shipment state is invalid: $state").failureNel
    } else {
      copy(state        = ShipmentState.Packed,
           timePacked   = Some(time),
           timeSent     = None,
           timeReceived = None,
           timeUnpacked = None,
           version      = version + 1,
           timeModified = Some(DateTime.now)).success
    }

  def sent(time: DateTime): DomainValidation[Shipment] = {
    if (state != ShipmentState.Packed) {
      InvalidStateTransition(s"cannot set state to SENT: shipment state is invalid: $state").failureNel
    } else {
      if (timePacked.isEmpty) {
        TimePackedUndefined.failureNel
      } else if (time < timePacked.get) {
        TimeSentBeforePacked.failureNel
      } else {
        copy(state        = ShipmentState.Sent,
             timeSent     = Some(time),
             timeReceived = None,
             timeUnpacked = None,
             version      = version + 1,
             timeModified = Some(DateTime.now)).success
      }
    }
  }

  def received(time: DateTime): DomainValidation[Shipment] =
    if (state != ShipmentState.Sent) {
      InvalidStateTransition(s"cannot set state to RECEIVED: shipment state is invalid: $state").failureNel
    } else {
      if (timeSent.isEmpty) {
        TimeSentUndefined.failureNel
      } else if (time < timeSent.get) {
        TimeReceivedBeforeSent.failureNel
      } else {
        copy(state        = ShipmentState.Received,
             timeReceived = Some(time),
             timeUnpacked = None,
             version      = version + 1,
             timeModified = Some(DateTime.now)).success
      }
    }

  def unpacked(time: DateTime): DomainValidation[Shipment] =
    if (state != ShipmentState.Received) {
      InvalidStateTransition(s"cannot set state to UNPACKED: shipment state is invalid: $state").failureNel
    } else {
      if (timeReceived.isEmpty) {
        TimeReceivedUndefined.failureNel
      } else if (time < timeReceived.get) {
        TimeUnpackedBeforeReceived.failureNel
      } else {
        copy(state        = ShipmentState.Unpacked,
             timeUnpacked = Some(time),
             version      = version + 1,
             timeModified = Some(DateTime.now)).success
      }
    }

  def lost(): DomainValidation[Shipment] =
    if (state != ShipmentState.Sent) {
      InvalidStateTransition(s"cannot set state to LOST: shipment state is invalid: $state").failureNel
    } else {
      copy(state        = ShipmentState.Lost,
           version      = version + 1,
           timeModified = Some(DateTime.now)).success
    }

  override def toString: String =
    s"""|Shipment:{
        |  id:             $id,
        |  version:        $version,
        |  timeAdded:      $timeAdded,
        |  timeModified:   $timeModified,
        |  state:          $state,
        |  courierName:    $courierName,
        |  trackingNumber: $trackingNumber,
        |  fromLocationId: $fromLocationId,
        |  toLocationId:   $toLocationId,
        |  timePacked:     $timePacked,
        |  timeSent:       $timeSent,
        |  timeReceived:   $timeReceived,
        |  timeUnpacked:   $timeUnpacked
        |}""".stripMargin
}

object Shipment extends ShipmentValidations {
  import org.biobank.domain.CommonValidations._

  implicit val shipmentWrites = Json.writes[Shipment]

  def compareByCourier(a: Shipment, b: Shipment) = (a.courierName compareToIgnoreCase b.courierName) < 0

  def compareByTrackingNumber(a: Shipment, b: Shipment) = (a.trackingNumber compareToIgnoreCase b.trackingNumber) < 0

  def compareByTimePacked(a: Shipment, b: Shipment) =
    (a.timePacked, b.timePacked) match {
      case (Some(aTimePacked), Some(bTimePacked)) => (aTimePacked compareTo bTimePacked) < 0
      case _ => false
    }

  def compareByTimeSent(a: Shipment, b: Shipment) =
    (a.timeSent, b.timeSent) match {
      case (Some(aTimeSent), Some(bTimeSent)) => (aTimeSent compareTo bTimeSent) < 0
      case _ => false
    }

  def compareByTimeReceived(a: Shipment, b: Shipment) =
    (a.timeReceived, b.timeReceived) match {
      case (Some(aTimeReceived), Some(bTimeReceived)) => (aTimeReceived compareTo bTimeReceived) < 0
      case _ => false
    }

  def compareByTimeUnpacked(a: Shipment, b: Shipment) =
    (a.timeUnpacked, b.timeUnpacked) match {
      case (Some(aTimeUnpacked), Some(bTimeUnpacked)) => (aTimeUnpacked compareTo bTimeUnpacked) < 0
      case _ => false
    }

  def create(id:             ShipmentId,
             version:        Long,
             state:          ShipmentState,
             courierName:    String,
             trackingNumber: String,
             fromLocationId: String,
             toLocationId:   String,
             timePacked:     Option[DateTime],
             timeSent:       Option[DateTime],
             timeReceived:   Option[DateTime],
             timeUnpacked:   Option[DateTime]): DomainValidation[Shipment] = {
    validate(id,
             version,
             state,
             courierName,
             trackingNumber,
             fromLocationId,
             toLocationId,
             timePacked,
             timeSent,
             timeReceived,
             timeUnpacked).map(_ => Shipment(id,
                                             version,
                                             DateTime.now,
                                             None,
                                             state,
                                             courierName,
                                             trackingNumber,
                                             fromLocationId,
                                             toLocationId,
                                             timePacked,
                                             timeSent,
                                             timeReceived,
                                             timeUnpacked))
  }

  def validate(id:             ShipmentId,
               version:        Long,
               state:          ShipmentState,
               courierName:    String,
               trackingNumber: String,
               fromLocationId: String,
               toLocationId:   String,
               timePacked:     Option[DateTime],
               timeSent:       Option[DateTime],
               timeReceived:   Option[DateTime],
               timeUnpacked:   Option[DateTime]): DomainValidation[Boolean] = {
    (validateId(id) |@|
       validateVersion(version) |@|
       validateString(courierName, CourierNameInvalid) |@|
       validateString(trackingNumber, TrackingNumberInvalid) |@|
       validateString(fromLocationId, FromLocationIdInvalid) |@|
       validateString(toLocationId, ToLocationIdInvalid) |@|
       validateTimeAfter(timeSent, timePacked, TimePackedUndefined, TimeSentBeforePacked)  |@|
       validateTimeAfter(timeReceived, timeSent, TimeSentUndefined, TimeReceivedBeforeSent)  |@|
       validateTimeAfter(timeUnpacked, timeReceived, TimeReceivedUndefined, TimeUnpackedBeforeReceived)) {
      case (_, _, _, _, _, _, _, _, _) => true
    }

  }

}
