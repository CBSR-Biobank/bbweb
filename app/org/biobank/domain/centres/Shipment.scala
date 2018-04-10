package org.biobank.domain.centres

import java.time.OffsetDateTime
import org.biobank.domain._
import org.biobank.ValidationKey
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

final case class ShipmentId(id: String) extends IdentifiedValueObject[String]

object ShipmentId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val shipmentIdReader: Format[ShipmentId] = new Format[ShipmentId] {

      override def writes(id: ShipmentId): JsValue = JsString(id.id)

      override def reads(json: JsValue): JsResult[ShipmentId] =
        Reads.StringReads.reads(json).map(ShipmentId.apply _)
    }

}

trait ShipmentPredicates {
  type ShipmentFilter = Shipment => Boolean

  val fromCentreIdIsOneOf: Set[CentreId] => ShipmentFilter =
    centreIds => shipment => centreIds.contains(shipment.fromCentreId)

  val toCentreIdIsOneOf: Set[CentreId] => ShipmentFilter =
    centreIds => shipment => centreIds.contains(shipment.toCentreId)

  val withCentreIdIsOneOf: Set[CentreId] => ShipmentFilter =
    centreIds => shipment => centreIds.contains(shipment.toCentreId) || centreIds.contains(shipment.fromCentreId)

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
 * [org.biobank.domain.containers.Container]s from one [org.biobank.domain.centres.Centre] to another.
 *
 * @see org.biobank.domain.centres.ShipmentSpecimen
 * @see org.biobank.domain.centres.ShipmentContainer
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
  val timePacked:     Option[OffsetDateTime]
  val timeSent:       Option[OffsetDateTime]
  val timeReceived:   Option[OffsetDateTime]
  val timeUnpacked:   Option[OffsetDateTime]
  val timeCompleted:  Option[OffsetDateTime]

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

  def validateTimeAfter(afterMaybe: Option[OffsetDateTime],
                        beforeMaybe: Option[OffsetDateTime],
                        errUndefined: ValidationKey,
                        errNotAfter: ValidationKey)
      : DomainValidation[Option[OffsetDateTime]] = {
    beforeMaybe.fold {
      if (afterMaybe.isDefined) errUndefined.failureNel[Option[OffsetDateTime]]
      else afterMaybe.successNel[String]
    } { before =>
      if (afterMaybe.isEmpty || afterMaybe.exists(after => after.isAfter(before))) {
        afterMaybe.successNel[String]
      } else {
        errNotAfter.failureNel[Option[OffsetDateTime]]
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
  implicit val shipmentFormat: Format[Shipment] = new Format[Shipment] {
      override def writes(shipment: Shipment): JsValue = {
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
            shipment.timePacked.map("timePacked"       -> Json.toJson(_)) ++
            shipment.timeSent.map("timeSent"           -> Json.toJson(_)) ++
            shipment.timeReceived.map("timeReceived"   -> Json.toJson(_)) ++
            shipment.timeUnpacked.map("timeUnpacked"   -> Json.toJson(_)) ++
            shipment.timeCompleted.map("timeCompleted" -> Json.toJson(_))
        )
      }

      override def reads(json: JsValue): JsResult[Shipment] = (json \ "state") match {
          case JsDefined(JsString(createdState.id))   => json.validate[CreatedShipment]
          case JsDefined(JsString(packedState.id))    => json.validate[PackedShipment]
          case JsDefined(JsString(sentState.id))      => json.validate[SentShipment]
          case JsDefined(JsString(receivedState.id))  => json.validate[ReceivedShipment]
          case JsDefined(JsString(unpackedState.id))  => json.validate[UnpackedShipment]
          case JsDefined(JsString(completedState.id)) => json.validate[CompletedShipment]
          case JsDefined(JsString(lostState.id))      => json.validate[LostShipment]
          case _ => JsError("error")
        }
    }

  implicit val createdShipmentReads: Reads[CreatedShipment]     = Json.reads[CreatedShipment]
  implicit val packedShipmentReads: Reads[PackedShipment]       = Json.reads[PackedShipment]
  implicit val sentShipmentReads: Reads[SentShipment]           = Json.reads[SentShipment]
  implicit val receivedShipmentReads: Reads[ReceivedShipment]   = Json.reads[ReceivedShipment]
  implicit val unpackedShipmentReads: Reads[UnpackedShipment]   = Json.reads[UnpackedShipment]
  implicit val completedShipmentReads: Reads[CompletedShipment] = Json.reads[CompletedShipment]
  implicit val lostShipmentReads: Reads[LostShipment]           = Json.reads[LostShipment]

}

final case class CreatedShipment(id:             ShipmentId,
                                 version:        Long,
                                 timeAdded:      OffsetDateTime,
                                 timeModified:   Option[OffsetDateTime],
                                 courierName:    String,
                                 trackingNumber: String,
                                 fromCentreId:   CentreId,
                                 fromLocationId: LocationId,
                                 toCentreId:     CentreId,
                                 toLocationId:   LocationId,
                                 timePacked:     Option[OffsetDateTime],
                                 timeSent:       Option[OffsetDateTime],
                                 timeReceived:   Option[OffsetDateTime],
                                 timeUnpacked:   Option[OffsetDateTime],
                                 timeCompleted:  Option[OffsetDateTime])
    extends { val state: EntityState = Shipment.createdState }
    with Shipment
    with ShipmentValidations {

  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  def withCourier(name: String): DomainValidation[CreatedShipment] =
    validateNonEmptyString(name, CourierNameInvalid).map { name =>
      copy(courierName  = name,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }

  def withTrackingNumber(trackingNumber: String): DomainValidation[CreatedShipment] =
    validateNonEmptyString(trackingNumber, TrackingNumberInvalid).map { _ =>
      copy(trackingNumber = trackingNumber,
           version        = version + 1,
           timeModified   = Some(OffsetDateTime.now))
    }

  /**
   * Must be a centre's location.
   */
  def withFromLocation(centreId: CentreId, locationId: LocationId): DomainValidation[CreatedShipment] =
    (validateId(centreId, FromCentreIdInvalid) |@|
       validateNonEmptyString(locationId.id, LocationIdInvalid)) { case (_, _) =>
        copy(fromCentreId   = centreId,
             fromLocationId = locationId,
             version        = version + 1,
             timeModified   = Some(OffsetDateTime.now))
    }

  /**
   * Must be a centre's location.
   */
  def withToLocation(centreId: CentreId, locationId: LocationId): DomainValidation[CreatedShipment] =
    (validateId(centreId, ToCentreIdInvalid) |@|
       validateNonEmptyString(locationId.id, LocationIdInvalid)) { case (_, _) =>
        copy(toCentreId   = centreId,
             toLocationId = locationId,
             version      = version + 1,
             timeModified = Some(OffsetDateTime.now))
    }

  def pack(timePacked: OffsetDateTime): PackedShipment =
    PackedShipment(id             = this.id,
                   version        = this.version + 1,
                   timeAdded      = this.timeAdded,
                   timeModified   = Some(OffsetDateTime.now),
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

  def skipToSent(timePacked: OffsetDateTime, timeSent: OffsetDateTime): DomainValidation[SentShipment] = {
    if (timeSent.isBefore(timePacked)) {
      TimeSentBeforePacked.failureNel[SentShipment]
    } else {
      SentShipment(id             = this.id,
                   version        = this.version + 1,
                   timeAdded      = this.timeAdded,
                   timeModified   = Some(OffsetDateTime.now),
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
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  def create(id:             ShipmentId,
             version:        Long,
             timeAdded:      OffsetDateTime,
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
       validateNonEmptyString(courierName, CourierNameInvalid) |@|
       validateNonEmptyString(trackingNumber, TrackingNumberInvalid) |@|
       validateId(fromCentreId, FromCentreIdInvalid) |@|
       validateNonEmptyString(fromLocationId.id, FromLocationIdInvalid) |@|
       validateId(toCentreId, ToCentreIdInvalid) |@|
       validateNonEmptyString(toLocationId.id, ToLocationIdInvalid)) {
      case _ => true
    }

  }

}

final case class PackedShipment(id:             ShipmentId,
                                version:        Long,
                                timeAdded:      OffsetDateTime,
                                timeModified:   Option[OffsetDateTime],
                                courierName:    String,
                                trackingNumber: String,
                                fromCentreId:   CentreId,
                                fromLocationId: LocationId,
                                toCentreId:     CentreId,
                                toLocationId:   LocationId,
                                timePacked:     Option[OffsetDateTime],
                                timeSent:       Option[OffsetDateTime],
                                timeReceived:   Option[OffsetDateTime],
                                timeUnpacked:   Option[OffsetDateTime],
                                timeCompleted:  Option[OffsetDateTime])
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
                    timeModified   = Some(OffsetDateTime.now),
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

  def send(timeSent: OffsetDateTime): DomainValidation[SentShipment] = {
    this.timePacked.
      toSuccessNel(TimePackedUndefined.toString).
      flatMap { timePacked =>
        if (timeSent.isBefore(timePacked)) {
          TimeSentBeforePacked.failureNel[SentShipment]
        } else {
          SentShipment(id             = this.id,
                       version        = this.version + 1,
                       timeAdded      = this.timeAdded,
                       timeModified   = Some(OffsetDateTime.now),
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
                              timeAdded:      OffsetDateTime,
                              timeModified:   Option[OffsetDateTime],
                              courierName:    String,
                              trackingNumber: String,
                              fromCentreId:   CentreId,
                              fromLocationId: LocationId,
                              toCentreId:     CentreId,
                              toLocationId:   LocationId,
                              timePacked:     Option[OffsetDateTime],
                              timeSent:       Option[OffsetDateTime],
                              timeReceived:   Option[OffsetDateTime],
                              timeUnpacked:   Option[OffsetDateTime],
                              timeCompleted:  Option[OffsetDateTime])
    extends { val state: EntityState = Shipment.sentState }
    with Shipment
    with ShipmentValidations {

  def backToPacked: PackedShipment = {
    PackedShipment(id             = this.id,
                   version        = this.version + 1,
                   timeAdded      = this.timeAdded,
                   timeModified   = Some(OffsetDateTime.now),
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

  def receive(timeReceived: OffsetDateTime): DomainValidation[ReceivedShipment] = {
    this.timeSent.
      toSuccessNel(TimeSentUndefined.toString).
      flatMap { timeSent =>
        if (timeReceived.isBefore(timeSent)) {
          TimeReceivedBeforeSent.failureNel[ReceivedShipment]
        } else {
          ReceivedShipment(id             = this.id,
                           version        = this.version + 1,
                           timeAdded      = this.timeAdded,
                           timeModified   = Some(OffsetDateTime.now),
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

  def skipToUnpacked(timeReceived: OffsetDateTime, timeUnpacked: OffsetDateTime): DomainValidation[UnpackedShipment] = {
    this.timeSent.
      toSuccessNel(TimeSentUndefined.toString).
      flatMap { timeSent =>
        if (timeReceived.isBefore(timeSent)) {
          TimeReceivedBeforeSent.failureNel[UnpackedShipment]
        } else if (timeUnpacked.isBefore(timeReceived)) {
          TimeUnpackedBeforeReceived.failureNel[UnpackedShipment]
        } else {
          UnpackedShipment(id             = this.id,
                       version        = this.version + 1,
                       timeAdded      = this.timeAdded,
                       timeModified   = Some(OffsetDateTime.now),
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
  }

  def lost: LostShipment = {
    LostShipment(id             = this.id,
                 version        = this.version + 1,
                 timeAdded      = this.timeAdded,
                 timeModified   = Some(OffsetDateTime.now),
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
                                  timeAdded:      OffsetDateTime,
                                  timeModified:   Option[OffsetDateTime],
                                  courierName:    String,
                                  trackingNumber: String,
                                  fromCentreId:   CentreId,
                                  fromLocationId: LocationId,
                                  toCentreId:     CentreId,
                                  toLocationId:   LocationId,
                                  timePacked:     Option[OffsetDateTime],
                                  timeSent:       Option[OffsetDateTime],
                                  timeReceived:   Option[OffsetDateTime],
                                  timeUnpacked:   Option[OffsetDateTime],
                                  timeCompleted:  Option[OffsetDateTime])
    extends { val state: EntityState = Shipment.receivedState }
    with Shipment
    with ShipmentValidations {

  def backToSent: SentShipment = {
    SentShipment(id             = this.id,
                 version        = this.version + 1,
                 timeAdded      = this.timeAdded,
                 timeModified   = Some(OffsetDateTime.now),
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

  def unpack(timeUnpacked: OffsetDateTime): DomainValidation[UnpackedShipment] = {
    this.timeReceived.
      toSuccessNel(TimeReceivedUndefined.toString).
      flatMap { timeReceived =>
        if (timeUnpacked.isBefore(timeReceived)) {
          TimeUnpackedBeforeReceived.failureNel[UnpackedShipment]
        } else {
          UnpackedShipment(id             = this.id,
                           version        = this.version + 1,
                           timeAdded      = this.timeAdded,
                           timeModified   = Some(OffsetDateTime.now),
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
                                  timeAdded:      OffsetDateTime,
                                  timeModified:   Option[OffsetDateTime],
                                  courierName:    String,
                                  trackingNumber: String,
                                  fromCentreId:   CentreId,
                                  fromLocationId: LocationId,
                                  toCentreId:     CentreId,
                                  toLocationId:   LocationId,
                                  timePacked:     Option[OffsetDateTime],
                                  timeSent:       Option[OffsetDateTime],
                                  timeReceived:   Option[OffsetDateTime],
                                  timeUnpacked:   Option[OffsetDateTime],
                                  timeCompleted:  Option[OffsetDateTime])
    extends { val state: EntityState = Shipment.unpackedState }
    with Shipment
    with ShipmentValidations {

  def backToReceived: ReceivedShipment = {
    ReceivedShipment(id             = this.id,
                     version        = this.version + 1,
                     timeAdded      = this.timeAdded,
                     timeModified   = Some(OffsetDateTime.now),
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

  def complete(timeCompleted: OffsetDateTime): DomainValidation[CompletedShipment] = {
    this.timeUnpacked.
      toSuccessNel(TimeReceivedUndefined.toString).
      flatMap { timeUnpacked =>
        if (timeCompleted.isBefore(timeUnpacked)) {
          TimeCompletedBeforeUnpacked.failureNel[CompletedShipment]
        } else {
          CompletedShipment(id             = this.id,
                            version        = this.version + 1,
                            timeAdded      = this.timeAdded,
                            timeModified   = Some(OffsetDateTime.now),
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
                                   timeAdded:      OffsetDateTime,
                                   timeModified:   Option[OffsetDateTime],
                                   courierName:    String,
                                   trackingNumber: String,
                                   fromCentreId:   CentreId,
                                   fromLocationId: LocationId,
                                   toCentreId:     CentreId,
                                   toLocationId:   LocationId,
                                   timePacked:     Option[OffsetDateTime],
                                   timeSent:       Option[OffsetDateTime],
                                   timeReceived:   Option[OffsetDateTime],
                                   timeUnpacked:   Option[OffsetDateTime],
                                   timeCompleted:  Option[OffsetDateTime])
    extends { val state: EntityState = Shipment.completedState }
    with Shipment
    with ShipmentValidations {

  def backToUnpacked: UnpackedShipment = {
    UnpackedShipment(id             = this.id,
                     version        = this.version + 1,
                     timeAdded      = this.timeAdded,
                     timeModified   = Some(OffsetDateTime.now),
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
                              timeAdded:      OffsetDateTime,
                              timeModified:   Option[OffsetDateTime],
                              courierName:    String,
                              trackingNumber: String,
                              fromCentreId:   CentreId,
                              fromLocationId: LocationId,
                              toCentreId:     CentreId,
                              toLocationId:   LocationId,
                              timePacked:     Option[OffsetDateTime],
                              timeSent:       Option[OffsetDateTime],
                              timeReceived:   Option[OffsetDateTime],
                              timeUnpacked:   Option[OffsetDateTime],
                              timeCompleted:  Option[OffsetDateTime])
    extends { val state: EntityState = Shipment.lostState }
    with Shipment
    with ShipmentValidations {

  def backToSent: SentShipment = {
    SentShipment(id             = this.id,
                 version        = this.version + 1,
                 timeAdded      = this.timeAdded,
                 timeModified   = Some(OffsetDateTime.now),
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
