package org.biobank.domain.centre

import java.time.OffsetDateTime
import org.biobank._
import org.biobank.dto.{ShipmentSpecimenDto, SpecimenDto}
import org.biobank.domain._
import org.biobank.domain.centre.ShipmentItemState._
import org.biobank.domain.participants.SpecimenId
import play.api.libs.json._
import scalaz.Scalaz._

final case class ShipmentSpecimenId(id: String) extends IdentifiedValueObject[String]

object ShipmentSpecimenId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val shipmentSpecimenIdReader: Format[ShipmentSpecimenId] = new Format[ShipmentSpecimenId] {

      override def writes(id: ShipmentSpecimenId): JsValue = JsString(id.id)

      override def reads(json: JsValue): JsResult[ShipmentSpecimenId] =
        Reads.StringReads.reads(json).map(ShipmentSpecimenId.apply _)
    }
}

trait ShipmentSpecimenPredicates {
  type ShipmentSpecimenFilter = ShipmentSpecimen => Boolean

  val stateIsOneOf: Set[ShipmentItemState] => ShipmentSpecimenFilter =
    states => shipmentSpecimen => states.contains(shipmentSpecimen.state)

}

trait ShipmentSpecimenValidations {

  case object ShipmentIdRequired extends ValidationKey

  case object ShipmentContainerIdInvalid extends ValidationKey

  case object ShipmentSpecimenNotPresent extends ValidationKey

}

/**
 * Marks a specific [org.biobank.domain.participants.Specimen] as having been in a specific
 * [org.biobank.domain.centre.Shipment].
 *
 */
final case class ShipmentSpecimen(id:                  ShipmentSpecimenId,
                                  version:             Long,
                                  timeAdded:           OffsetDateTime,
                                  timeModified:        Option[OffsetDateTime],
                                  shipmentId:          ShipmentId,
                                  specimenId:          SpecimenId,
                                  state:               ShipmentItemState,
                                  shipmentContainerId: Option[ShipmentContainerId])
    extends ConcurrencySafeEntity[ShipmentSpecimenId]
    with ShipmentSpecimenValidations {

  import org.biobank.domain.DomainValidations._

  def withShipmentContainer(id: Option[ShipmentContainerId]): DomainValidation[ShipmentSpecimen] = {
    validateIdOption(id, ShipmentContainerIdInvalid) map { _ =>
      copy(shipmentContainerId = id,
           version             = version + 1,
           timeModified        = Some(OffsetDateTime.now))
    }
  }

  def present: DomainValidation[ShipmentSpecimen] = {
    if (state == ShipmentItemState.Present) {
      DomainError("cannot change state to PRESENT from PRESENT state").failureNel[ShipmentSpecimen]
    } else {
      copy(state        = ShipmentItemState.Present,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now)).successNel[String]
    }
  }

  def received: DomainValidation[ShipmentSpecimen] = {
    if (state != ShipmentItemState.Present) {
      DomainError(s"cannot change state to RECEIVED: invalid state: $state").failureNel[ShipmentSpecimen]
    } else {
      copy(state        = ShipmentItemState.Received,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now)).successNel[String]
    }
  }

  def missing: DomainValidation[ShipmentSpecimen] = {
    if (state != ShipmentItemState.Present) {
      DomainError(s"cannot change state to MISSING: invalid state: $state").failureNel[ShipmentSpecimen]
    } else {
      copy(state        = ShipmentItemState.Missing,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now)).successNel[String]
    }
  }

  def extra: DomainValidation[ShipmentSpecimen] = {
    if (state != ShipmentItemState.Present) {
      DomainError(s"cannot change state to EXTRA: invalid state: $state").failureNel[ShipmentSpecimen]
    } else {
      copy(state        = ShipmentItemState.Extra,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now)).successNel[String]
    }
  }

  def isStatePresent(): DomainValidation[Boolean] = {
    if (state == ShipmentItemState.Present) true.successNel[String]
    else DomainError(s"shipment specimen is not in present state").failureNel[Boolean]
  }

  def isStateNotPresent(): DomainValidation[Boolean] = {
    if (state != ShipmentItemState.Present) true.successNel[String]
    else DomainError(s"shipment specimen in present state").failureNel[Boolean]
  }

  def isStateExtra(): DomainValidation[Boolean] = {
    if (state == ShipmentItemState.Extra) true.successNel[String]
    else DomainError(s"shipment specimen is not in extra state").failureNel[Boolean]
  }

  def createDto(specimenDto: SpecimenDto): ShipmentSpecimenDto = {
    ShipmentSpecimenDto(id                  = this.id.id,
                        version             = this.version,
                        timeAdded           = this.timeAdded,
                        timeModified        = this.timeModified,
                        shipmentId          = this.shipmentId.id,
                        shipmentContainerId = this.shipmentContainerId.map(id => id.id),
                        state               = this.state.toString,
                        specimen            = specimenDto)
  }

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:                  $id,
        |  version:             $version,
        |  timeAdded:           $timeAdded,
        |  timeModified:        $timeModified,
        |  shipmentId:          $shipmentId,
        |  specimenId:          $specimenId,
        |  state:               $state,
        |  shipmentContainerId: $shipmentContainerId,
        |""".stripMargin
}

object ShipmentSpecimen extends ShipmentSpecimenValidations {
  import org.biobank.domain.DomainValidations._
  import org.biobank.CommonValidations._

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val shipmentIdReader: Reads[ShipmentId] =
    (__ \ "id").read[String].map( new ShipmentId(_) )

  implicit val shipmentIdWriter: Writes[ShipmentId] =
    Writes{ (shipmentId: ShipmentId) => JsString(shipmentId.id) }


  implicit val shipmentSpecimenFormat: Format[ShipmentSpecimen] = Json.format[ShipmentSpecimen]

  def compareByState(a: ShipmentSpecimen, b: ShipmentSpecimen): Boolean = (a.state compareTo b.state) < 0

  def create(id:                  ShipmentSpecimenId,
             version:             Long,
             shipmentId:          ShipmentId,
             specimenId:          SpecimenId,
             state:               ShipmentItemState,
             shipmentContainerId: Option[ShipmentContainerId]): DomainValidation[ShipmentSpecimen] = {
    validate(id, version, shipmentId, specimenId, shipmentContainerId)
      .map(_ => ShipmentSpecimen(id,
                                 version,
                                 OffsetDateTime.now,
                                 None,
                                 shipmentId,
                                 specimenId,
                                 state,
                                 shipmentContainerId))
  }

  def validate(id:                  ShipmentSpecimenId,
               version:             Long,
               shipmentId:          ShipmentId,
               specimenId:          SpecimenId,
               shipmentContainerId: Option[ShipmentContainerId]): DomainValidation[Boolean] = {
    (validateId(id) |@|
       validateVersion(version) |@|
       validateId(shipmentId, ShipmentIdRequired) |@|
       validateId(specimenId, SpecimenIdRequired) |@|
       validateIdOption(shipmentContainerId, ShipmentContainerIdInvalid)) {
      case _ => true
    }
  }
}
