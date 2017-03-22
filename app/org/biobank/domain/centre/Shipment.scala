package org.biobank.domain.centre

import com.github.nscala_time.time.Imports._
import org.biobank.domain._
import org.biobank.infrastructure.JsonUtils._
import org.biobank.ValidationKey
import org.joda.time.DateTime
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Reads._
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

final case class ShipmentId(id: String) extends IdentifiedValueObject[String]

object ShipmentId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val shipmentIdReader: Reads[ShipmentId] =
    (__ \ "id").read[String].map( new ShipmentId(_) )

  implicit val shipmentIdWriter: Writes[ShipmentId] =
    Writes{ (shipmentId: ShipmentId) => JsString(shipmentId.id) }

}

trait ShipmentPredicates {
  type ShipmentFilter = Shipment => Boolean

  val courierNameIsOneOf: Set[String] => ShipmentFilter =
    courierNames => shipment => courierNames.contains(shipment.courierName)

  val trackingNumberIsOneOf: Set[String] => ShipmentFilter =
    trackingNumbers => shipment => trackingNumbers.contains(shipment.trackingNumber)

  val stateIsOneOf: Set[EntityState] => ShipmentFilter =
    states => shipment => states.contains(shipment.state)

  val courierNameIsLike: Set[String] => ShipmentFilter =
    courierNames => shipment => {
      val lc = shipment.courierName.toLowerCase
      courierNames.forall(n => lc.contains(n.toLowerCase))
    }

  val trackingNumberIsLike: Set[String] => ShipmentFilter =
    trackingNumbers => shipment => {
      val lc = shipment.trackingNumber.toLowerCase
      trackingNumbers.forall(n => lc.contains(n.toLowerCase))
    }

}

/**
 * Represents a transfer of [org.biobank.domain.participants.Specimen]s and / or
 * [org.biobank.domain.containers.Container]s from one [org.biobank.domain.centre.Centre] to another.
 *
 * @see org.biobank.domain.centre.ShipmentSpecimen
 * @see org.biobank.domain.centre.ShipmentContainer
 */
sealed trait Shipment
    extends ConcurrencySafeEntity[ShipmentId]
    with HasState {
  import org.biobank.CommonValidations._

  protected val log: Logger = LoggerFactory.getLogger(this.getClass)

  val courierName:    String
  val trackingNumber: String
  val fromCentreId:   CentreId
  val fromLocationId: LocationId
  val toCentreId:     CentreId
  val toLocationId:   LocationId
  val timePacked:     Option[DateTime]
  val timeSent:       Option[DateTime]
  val timeReceived:   Option[DateTime]
  val timeUnpacked:   Option[DateTime]
  val timeCompleted:  Option[DateTime]

  def isCreated: DomainValidation[CreatedShipment] = {
    this match {
      case s: CreatedShipment => s.successNel[String]
      case s => InvalidState(s"shipment not created: ${s.id}").failureNel[CreatedShipment]
    }
  }

  def isPacked: DomainValidation[PackedShipment] = {
    this match {
      case s: PackedShipment => s.successNel[String]
      case s => InvalidState(s"shipment not packed: ${s.id}").failureNel[PackedShipment]
    }
  }

  def isSent: DomainValidation[SentShipment] = {
    this match {
      case s: SentShipment => s.successNel[String]
      case s => InvalidState(s"shipment not sent: ${s.id}").failureNel[SentShipment]
    }
  }

  def isReceived: DomainValidation[ReceivedShipment] = {
    this match {
      case s: ReceivedShipment => s.successNel[String]
      case s => InvalidState(s"shipment not received: ${s.id}").failureNel[ReceivedShipment]
    }
  }

  def isUnpacked: DomainValidation[UnpackedShipment] = {
    this match {
      case s: UnpackedShipment => s.successNel[String]
      case s => InvalidState(s"shipment not unpacked: ${s.id}").failureNel[UnpackedShipment]
    }
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
        |  fromCentreId:   $fromCentreId,
        |  fromLocationId: $fromLocationId,
        |  toCentreId:     $toCentreId,
        |  toLocationId:   $toLocationId,
        |  timePacked:     $timePacked,
        |  timeSent:       $timeSent,
        |  timeReceived:   $timeReceived,
        |  timeUnpacked:   $timeUnpacked,
        |  timeCompleted:  $timeCompleted
        |}""".stripMargin
}

trait ShipmentValidations {

  case object CourierNameInvalid extends ValidationKey

  case object TrackingNumberInvalid extends ValidationKey

  case object FromCentreIdInvalid extends ValidationKey

  case object FromLocationIdInvalid extends ValidationKey

  case object ToCentreIdInvalid extends ValidationKey

  case object ToLocationIdInvalid extends ValidationKey

  case object TimePackedUndefined extends ValidationKey

  case object TimeSentBeforePacked extends ValidationKey

  case object TimeSentUndefined extends ValidationKey

  case object TimeReceivedBeforeSent extends ValidationKey

  case object TimeReceivedUndefined extends ValidationKey

  case object TimeUnpackedBeforeReceived extends ValidationKey

  case object TimeCompletedBeforeUnpacked extends ValidationKey

  def validateTimeAfter(afterMaybe: Option[DateTime],
                        beforeMaybe: Option[DateTime],
                        errUndefined: ValidationKey,
                        errNotAfter: ValidationKey)
      : DomainValidation[Option[DateTime]] = {
    beforeMaybe.fold {
      if (afterMaybe.isDefined) errUndefined.failureNel[Option[DateTime]]
      else afterMaybe.successNel[String]
    } { before =>
      if (afterMaybe.isEmpty || afterMaybe.exists(after => after > before)) {
        afterMaybe.successNel[String]
      } else {
        errNotAfter.failureNel[Option[DateTime]]
      }
    }
  }

}

object Shipment extends ShipmentValidations {

  val createdState: EntityState   = new EntityState("created")
  val packedState: EntityState    = new EntityState("packed")
  val sentState: EntityState      = new EntityState("sent")
  val receivedState: EntityState  = new EntityState("received")
  val unpackedState: EntityState  = new EntityState("unpacked")
  val completedState: EntityState = new EntityState("completed")
  val lostState: EntityState      = new EntityState("lost")

  val shipmentStates: List[EntityState] = List(createdState,
                                               packedState,
                                               sentState,
                                               receivedState,
                                               unpackedState,
                                               completedState,
                                               lostState)

  @SuppressWarnings(Array("org.wartremover.warts.Option2Iterable"))
  implicit val shipmentWrites: Writes[Shipment] = new Writes[Shipment] {
      def writes(shipment: Shipment): JsValue = {
        ConcurrencySafeEntity.toJson(shipment) ++
        Json.obj("state"          -> shipment.state.id,
                 "courierName"    -> shipment.courierName,
                 "trackingNumber" -> shipment.trackingNumber,
                 "fromCentreId"   -> shipment.fromCentreId,
                 "fromLocationId" -> shipment.fromLocationId.id,
                 "toCentreId"     -> shipment.toCentreId,
                 "toLocationId"   -> shipment.toLocationId.id) ++
        JsObject(
          Seq[(String, JsValue)]() ++
            shipment.timePacked.map("timePacked" -> Json.toJson(_)) ++
            shipment.timeSent.map("timeSent" -> Json.toJson(_)) ++
            shipment.timeReceived.map("timeReceived"  -> Json.toJson(_)) ++
            shipment.timeUnpacked.map("timeUnpacked"  -> Json.toJson(_)) ++
            shipment.timeUnpacked.map("timeCompleted" -> Json.toJson(_))
        )
      }

    }
}

final case class CreatedShipment(id:             ShipmentId,
                                 version:        Long,
                                 timeAdded:      DateTime,
                                 timeModified:   Option[DateTime],
                                 courierName:    String,
                                 trackingNumber: String,
                                 fromCentreId:   CentreId,
                                 fromLocationId: LocationId,
                                 toCentreId:     CentreId,
                                 toLocationId:   LocationId,
                                 timePacked:     Option[DateTime],
                                 timeSent:       Option[DateTime],
                                 timeReceived:   Option[DateTime],
                                 timeUnpacked:   Option[DateTime],
                                 timeCompleted:  Option[DateTime])
    extends { val state: EntityState = Shipment.createdState }
    with Shipment
    with ShipmentValidations {

  import org.biobank.domain.CommonValidations._
  import org.biobank.CommonValidations._

  def withCourier(name: String): DomainValidation[CreatedShipment] =
    validateString(name, CourierNameInvalid).map { name =>
      copy(courierName  = name,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }

  def withTrackingNumber(trackingNumber: String): DomainValidation[CreatedShipment] =
    validateString(trackingNumber, TrackingNumberInvalid).map { _ =>
      copy(trackingNumber = trackingNumber,
           version        = version + 1,
           timeModified   = Some(DateTime.now))
    }

  /**
   * Must be a centre's location.
   */
  def withFromLocation(centreId: CentreId, locationId: LocationId): DomainValidation[CreatedShipment] =
    (validateString(centreId.id, FromCentreIdInvalid) |@|
       validateString(locationId.id, LocationIdInvalid)) { case (_, _) =>
        copy(fromCentreId   = centreId,
             fromLocationId = locationId,
             version        = version + 1,
             timeModified   = Some(DateTime.now))
    }

  /**
   * Must be a centre's location.
   */
  def withToLocation(centreId: CentreId, locationId: LocationId): DomainValidation[CreatedShipment] =
    (validateString(centreId.id, ToCentreIdInvalid) |@|
       validateString(locationId.id, LocationIdInvalid)) { case (_, _) =>
        copy(toCentreId   = centreId,
             toLocationId = locationId,
             version      = version + 1,
             timeModified = Some(DateTime.now))
    }

  def pack(timePacked: DateTime): PackedShipment =
    PackedShipment(id             = this.id,
                   version        = this.version + 1,
                   timeAdded      = this.timeAdded,
                   timeModified   = Some(DateTime.now),
                   courierName    = this.courierName,
                   trackingNumber = this.trackingNumber,
                   fromCentreId   = this.fromCentreId,
                   fromLocationId = this.fromLocationId,
                   toCentreId     = this.toCentreId,
                   toLocationId   = this.toLocationId,
                   timePacked     = Some(timePacked),
                   timeSent       = None,
                   timeReceived   = None,
                   timeUnpacked   = None,
                   timeCompleted  = None)

  def skipToSent(timePacked: DateTime, timeSent: DateTime): DomainValidation[SentShipment] = {
    if (timeSent < timePacked) {
      TimeSentBeforePacked.failureNel[SentShipment]
    } else {
      SentShipment(id             = this.id,
                   version        = this.version + 1,
                   timeAdded      = this.timeAdded,
                   timeModified   = Some(DateTime.now),
                   courierName    = this.courierName,
                   trackingNumber = this.trackingNumber,
                   fromCentreId   = this.fromCentreId,
                   fromLocationId = this.fromLocationId,
                   toCentreId     = this.toCentreId,
                   toLocationId   = this.toLocationId,
                   timePacked     = Some(timePacked),
                   timeSent       = Some(timeSent),
                   timeReceived   = None,
                   timeUnpacked   = None,
                   timeCompleted  = None).successNel[String]
    }
  }

}

object CreatedShipment extends ShipmentValidations {
  import org.biobank.domain.CommonValidations._

  def create(id:             ShipmentId,
             version:        Long,
             timeAdded:      DateTime,
             courierName:    String,
             trackingNumber: String,
             fromCentreId:   CentreId,
             fromLocationId: LocationId,
             toCentreId:     CentreId,
             toLocationId:   LocationId): DomainValidation[CreatedShipment] = {
    validate(id,
             version,
             courierName,
             trackingNumber,
             fromCentreId,
             fromLocationId,
             toCentreId,
             toLocationId).map(_ => CreatedShipment(id,
                                                    version,
                                                    timeAdded,
                                                    None,
                                                    courierName,
                                                    trackingNumber,
                                                    fromCentreId,
                                                    fromLocationId,
                                                    toCentreId,
                                                    toLocationId,
                                                    None,
                                                    None,
                                                    None,
                                                    None,
                                                    None))
  }

  def validate(id:             ShipmentId,
               version:        Long,
               courierName:    String,
               trackingNumber: String,
               fromCentreId:   CentreId,
               fromLocationId: LocationId,
               toCentreId:     CentreId,
               toLocationId:   LocationId): DomainValidation[Boolean] = {
    (validateId(id) |@|
       validateVersion(version) |@|
       validateString(courierName, CourierNameInvalid) |@|
       validateString(trackingNumber, TrackingNumberInvalid) |@|
       validateId(fromCentreId, FromCentreIdInvalid) |@|
       validateString(fromLocationId.id, FromLocationIdInvalid) |@|
       validateId(toCentreId, ToCentreIdInvalid) |@|
       validateString(toLocationId.id, ToLocationIdInvalid)) {
      case _ => true
    }

  }

}

final case class PackedShipment(id:             ShipmentId,
                                version:        Long,
                                timeAdded:      DateTime,
                                timeModified:   Option[DateTime],
                                courierName:    String,
                                trackingNumber: String,
                                fromCentreId:   CentreId,
                                fromLocationId: LocationId,
                                toCentreId:     CentreId,
                                toLocationId:   LocationId,
                                timePacked:     Option[DateTime],
                                timeSent:       Option[DateTime],
                                timeReceived:   Option[DateTime],
                                timeUnpacked:   Option[DateTime],
                                timeCompleted:  Option[DateTime])
    extends { val state: EntityState = Shipment.packedState }
    with Shipment
    with ShipmentValidations {

  /**
   * Returns shipment to created state.
   *
   */
  def created: CreatedShipment =
    CreatedShipment(id             = this.id,
                    version        = this.version + 1,
                    timeAdded      = this.timeAdded,
                    timeModified   = Some(DateTime.now),
                    courierName    = this.courierName,
                    trackingNumber = this.trackingNumber,
                    fromCentreId   = this.fromCentreId,
                    fromLocationId = this.fromLocationId,
                    toCentreId     = this.toCentreId,
                    toLocationId   = this.toLocationId,
                    timePacked     = None,
                    timeSent       = None,
                    timeReceived   = None,
                    timeUnpacked   = None,
                    timeCompleted  = None)

  def send(timeSent: DateTime): DomainValidation[SentShipment] = {
    this.timePacked.
      toSuccessNel(TimePackedUndefined.toString).
      flatMap { timePacked =>
        if (timeSent < timePacked) {
          TimeSentBeforePacked.failureNel[SentShipment]
        } else {
          SentShipment(id             = this.id,
                       version        = this.version + 1,
                       timeAdded      = this.timeAdded,
                       timeModified   = Some(DateTime.now),
                       courierName    = this.courierName,
                       trackingNumber = this.trackingNumber,
                       fromCentreId   = this.fromCentreId,
                       fromLocationId = this.fromLocationId,
                       toCentreId     = this.toCentreId,
                       toLocationId   = this.toLocationId,
                       timePacked     = this.timePacked,
                       timeSent       = Some(timeSent),
                       timeReceived   = None,
                       timeUnpacked   = None,
                       timeCompleted  = None).successNel[String]
        }
      }
  }

}

final case class SentShipment(id:             ShipmentId,
                              version:        Long,
                              timeAdded:      DateTime,
                              timeModified:   Option[DateTime],
                              courierName:    String,
                              trackingNumber: String,
                              fromCentreId:   CentreId,
                              fromLocationId: LocationId,
                              toCentreId:     CentreId,
                              toLocationId:   LocationId,
                              timePacked:     Option[DateTime],
                              timeSent:       Option[DateTime],
                              timeReceived:   Option[DateTime],
                              timeUnpacked:   Option[DateTime],
                              timeCompleted:  Option[DateTime])
    extends { val state: EntityState = Shipment.sentState }
    with Shipment
    with ShipmentValidations {

  def backToPacked: PackedShipment = {
    PackedShipment(id             = this.id,
                   version        = this.version + 1,
                   timeAdded      = this.timeAdded,
                   timeModified   = Some(DateTime.now),
                   courierName    = this.courierName,
                   trackingNumber = this.trackingNumber,
                   fromCentreId   = this.fromCentreId,
                   fromLocationId = this.fromLocationId,
                   toCentreId     = this.toCentreId,
                   toLocationId   = this.toLocationId,
                   timePacked     = this.timePacked,
                   timeSent       = None,
                   timeReceived   = None,
                   timeUnpacked   = None,
                   timeCompleted  = None)
  }

  def receive(timeReceived: DateTime): DomainValidation[ReceivedShipment] = {
    this.timeSent.
      toSuccessNel(TimeSentUndefined.toString).
      flatMap { timeSent =>
        if (timeReceived < timeSent) {
          TimeReceivedBeforeSent.failureNel[ReceivedShipment]
        } else {
          ReceivedShipment(id             = this.id,
                           version        = this.version + 1,
                           timeAdded      = this.timeAdded,
                           timeModified   = Some(DateTime.now),
                           courierName    = this.courierName,
                           trackingNumber = this.trackingNumber,
                           fromCentreId   = this.fromCentreId,
                           fromLocationId = this.fromLocationId,
                           toCentreId     = this.toCentreId,
                           toLocationId   = this.toLocationId,
                           timePacked     = this.timePacked,
                           timeSent       = this.timeSent,
                           timeReceived   = Some(timeReceived),
                           timeUnpacked   = None,
                           timeCompleted  = None).successNel[String]
        }
      }
  }

  def skipToUnpacked(timeReceived: DateTime, timeUnpacked: DateTime): DomainValidation[UnpackedShipment] = {
    if (timeUnpacked < timeReceived) {
      TimeUnpackedBeforeReceived.failureNel[UnpackedShipment]
    } else {
      UnpackedShipment(id             = this.id,
                       version        = this.version + 1,
                       timeAdded      = this.timeAdded,
                       timeModified   = Some(DateTime.now),
                       courierName    = this.courierName,
                       trackingNumber = this.trackingNumber,
                       fromCentreId   = this.fromCentreId,
                       fromLocationId = this.fromLocationId,
                       toCentreId     = this.toCentreId,
                       toLocationId   = this.toLocationId,
                       timePacked     = this.timePacked,
                       timeSent       = this.timeSent,
                       timeReceived   = Some(timeReceived),
                       timeUnpacked   = Some(timeUnpacked),
                       timeCompleted  = None).successNel[String]
    }
  }

  def lost: LostShipment = {
    LostShipment(id             = this.id,
                 version        = this.version + 1,
                 timeAdded      = this.timeAdded,
                 timeModified   = Some(DateTime.now),
                 courierName    = this.courierName,
                 trackingNumber = this.trackingNumber,
                 fromCentreId   = this.fromCentreId,
                 fromLocationId = this.fromLocationId,
                 toCentreId     = this.toCentreId,
                 toLocationId   = this.toLocationId,
                 timePacked     = this.timePacked,
                 timeSent       = this.timeSent,
                 timeReceived   = None,
                 timeUnpacked   = None,
                 timeCompleted  = None)
  }
}

final case class ReceivedShipment(id:             ShipmentId,
                                  version:        Long,
                                  timeAdded:      DateTime,
                                  timeModified:   Option[DateTime],
                                  courierName:    String,
                                  trackingNumber: String,
                                  fromCentreId:   CentreId,
                                  fromLocationId: LocationId,
                                  toCentreId:     CentreId,
                                  toLocationId:   LocationId,
                                  timePacked:     Option[DateTime],
                                  timeSent:       Option[DateTime],
                                  timeReceived:   Option[DateTime],
                                  timeUnpacked:   Option[DateTime],
                                  timeCompleted:  Option[DateTime])
    extends { val state: EntityState = Shipment.receivedState }
    with Shipment
    with ShipmentValidations {

  def backToSent: SentShipment = {
    SentShipment(id             = this.id,
                 version        = this.version + 1,
                 timeAdded      = this.timeAdded,
                 timeModified   = Some(DateTime.now),
                 courierName    = this.courierName,
                 trackingNumber = this.trackingNumber,
                 fromCentreId   = this.fromCentreId,
                 fromLocationId = this.fromLocationId,
                 toCentreId     = this.toCentreId,
                 toLocationId   = this.toLocationId,
                 timePacked     = this.timePacked,
                 timeSent       = this.timeSent,
                 timeReceived   = None,
                 timeUnpacked   = None,
                 timeCompleted  = None)
  }

  def unpack(timeUnpacked: DateTime): DomainValidation[UnpackedShipment] = {
    this.timeReceived.
      toSuccessNel(TimeReceivedUndefined.toString).
      flatMap { timeReceived =>
        if (timeUnpacked < timeReceived) {
          TimeUnpackedBeforeReceived.failureNel[UnpackedShipment]
        } else {
          UnpackedShipment(id             = this.id,
                           version        = this.version + 1,
                           timeAdded      = this.timeAdded,
                           timeModified   = Some(DateTime.now),
                           courierName    = this.courierName,
                           trackingNumber = this.trackingNumber,
                           fromCentreId   = this.fromCentreId,
                           fromLocationId = this.fromLocationId,
                           toCentreId     = this.toCentreId,
                           toLocationId   = this.toLocationId,
                           timePacked     = this.timePacked,
                           timeSent       = this.timeSent,
                           timeReceived   = this.timeReceived,
                           timeUnpacked   = Some(timeUnpacked),
                           timeCompleted  = None).successNel[String]
        }
      }
  }

}

final case class UnpackedShipment(id:             ShipmentId,
                                  version:        Long,
                                  timeAdded:      DateTime,
                                  timeModified:   Option[DateTime],
                                  courierName:    String,
                                  trackingNumber: String,
                                  fromCentreId:   CentreId,
                                  fromLocationId: LocationId,
                                  toCentreId:     CentreId,
                                  toLocationId:   LocationId,
                                  timePacked:     Option[DateTime],
                                  timeSent:       Option[DateTime],
                                  timeReceived:   Option[DateTime],
                                  timeUnpacked:   Option[DateTime],
                                  timeCompleted:  Option[DateTime])
    extends { val state: EntityState = Shipment.unpackedState }
    with Shipment
    with ShipmentValidations {

  def backToReceived: ReceivedShipment = {
    ReceivedShipment(id             = this.id,
                     version        = this.version + 1,
                     timeAdded      = this.timeAdded,
                     timeModified   = Some(DateTime.now),
                     courierName    = this.courierName,
                     trackingNumber = this.trackingNumber,
                     fromCentreId   = this.fromCentreId,
                     fromLocationId = this.fromLocationId,
                     toCentreId     = this.toCentreId,
                     toLocationId   = this.toLocationId,
                     timePacked     = this.timePacked,
                     timeSent       = this.timeSent,
                     timeReceived   = this.timeReceived,
                     timeUnpacked   = None,
                     timeCompleted  = None)
  }

  def complete(timeCompleted: DateTime): DomainValidation[CompletedShipment] = {
    this.timeUnpacked.
      toSuccessNel(TimeReceivedUndefined.toString).
      flatMap { timeUnpacked =>
        if (timeCompleted < timeUnpacked) {
          TimeCompletedBeforeUnpacked.failureNel[CompletedShipment]
        } else {
          CompletedShipment(id             = this.id,
                            version        = this.version + 1,
                            timeAdded      = this.timeAdded,
                            timeModified   = Some(DateTime.now),
                            courierName    = this.courierName,
                            trackingNumber = this.trackingNumber,
                            fromCentreId   = this.fromCentreId,
                            fromLocationId = this.fromLocationId,
                            toCentreId     = this.toCentreId,
                            toLocationId   = this.toLocationId,
                            timePacked     = this.timePacked,
                            timeSent       = this.timeSent,
                            timeReceived   = this.timeReceived,
                            timeUnpacked   = this.timeUnpacked,
                            timeCompleted  = Some(timeCompleted)).successNel[String]
        }
      }
  }
}

final case class CompletedShipment(id:             ShipmentId,
                                   version:        Long,
                                   timeAdded:      DateTime,
                                   timeModified:   Option[DateTime],
                                   courierName:    String,
                                   trackingNumber: String,
                                   fromCentreId:   CentreId,
                                   fromLocationId: LocationId,
                                   toCentreId:     CentreId,
                                   toLocationId:   LocationId,
                                   timePacked:     Option[DateTime],
                                   timeSent:       Option[DateTime],
                                   timeReceived:   Option[DateTime],
                                   timeUnpacked:   Option[DateTime],
                                   timeCompleted:  Option[DateTime])
    extends { val state: EntityState = Shipment.completedState }
    with Shipment
    with ShipmentValidations {

  def backToUnpacked: UnpackedShipment = {
    UnpackedShipment(id             = this.id,
                     version        = this.version + 1,
                     timeAdded      = this.timeAdded,
                     timeModified   = Some(DateTime.now),
                     courierName    = this.courierName,
                     trackingNumber = this.trackingNumber,
                     fromCentreId   = this.fromCentreId,
                     fromLocationId = this.fromLocationId,
                     toCentreId     = this.toCentreId,
                     toLocationId   = this.toLocationId,
                     timePacked     = this.timePacked,
                     timeSent       = this.timeSent,
                     timeReceived   = this.timeReceived,
                     timeUnpacked   = this.timeUnpacked,
                     timeCompleted  = None)
  }

}


final case class LostShipment(id:             ShipmentId,
                              version:        Long,
                              timeAdded:      DateTime,
                              timeModified:   Option[DateTime],
                              courierName:    String,
                              trackingNumber: String,
                              fromCentreId:   CentreId,
                              fromLocationId: LocationId,
                              toCentreId:     CentreId,
                              toLocationId:   LocationId,
                              timePacked:     Option[DateTime],
                              timeSent:       Option[DateTime],
                              timeReceived:   Option[DateTime],
                              timeUnpacked:   Option[DateTime],
                              timeCompleted:  Option[DateTime])
    extends { val state: EntityState = Shipment.lostState }
    with Shipment
    with ShipmentValidations {

  def backToSent: SentShipment = {
    SentShipment(id             = this.id,
                 version        = this.version + 1,
                 timeAdded      = this.timeAdded,
                 timeModified   = Some(DateTime.now),
                 courierName    = this.courierName,
                 trackingNumber = this.trackingNumber,
                 fromCentreId   = this.fromCentreId,
                 fromLocationId = this.fromLocationId,
                 toCentreId     = this.toCentreId,
                 toLocationId   = this.toLocationId,
                 timePacked     = this.timePacked,
                 timeSent       = this.timeSent,
                 timeReceived   = None,
                 timeUnpacked   = None,
                 timeCompleted  = None)
  }

}
