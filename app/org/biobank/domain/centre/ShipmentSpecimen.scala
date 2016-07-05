package org.biobank.domain.centre

import org.biobank._
import org.biobank.domain._
import org.biobank.domain.centre.ShipmentItemState._
import org.biobank.domain.participants.SpecimenId
import org.joda.time.DateTime
import play.api.libs.json._
import scalaz.Scalaz._

final case class ShipmentSpecimenId(id: String) extends IdentifiedValueObject[String]

object ShipmentSpecimenId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val shipmentSpecimenIdReader: Reads[ShipmentSpecimenId] =
    (__ \ "id").read[String].map( new ShipmentSpecimenId(_) )

  implicit val shipmentSpecimenIdWriter: Writes[ShipmentSpecimenId] =
    Writes{ (shipmentSpecimenId: ShipmentSpecimenId) => JsString(shipmentSpecimenId.id) }

}

trait ShipmentSpecimenValidations {

  case object ShipmentIdRequired extends ValidationKey

  case object ShipmentContainerIdInvalid extends ValidationKey

}

/**
 * Marks a specific [org.biobank.domain.participants.Specimen] as having been in a specific
 * [org.biobank.domain.centre.Shipment].
 *
 */
final case class ShipmentSpecimen(id:                  ShipmentSpecimenId,
                                  version:             Long,
                                  timeAdded:           DateTime,
                                  timeModified:        Option[DateTime],
                                  shipmentId:          ShipmentId,
                                  specimenId:          SpecimenId,
                                  state:               ShipmentItemState,
                                  shipmentContainerId: Option[ShipmentContainerId])
    extends ConcurrencySafeEntity[ShipmentSpecimenId]
    with ShipmentSpecimenValidations {

  import org.biobank.domain.CommonValidations._

  def withShipmentContainer(id: Option[ShipmentContainerId]): DomainValidation[ShipmentSpecimen] = {
    validateId(shipmentContainerId, ShipmentContainerIdInvalid) map { _ =>
      copy(shipmentContainerId = shipmentContainerId,
           version             = version + 1,
           timeModified        = Some(DateTime.now))
    }
  }

  def received(): DomainValidation[ShipmentSpecimen] = {
    if (state != ShipmentItemState.Present) {
      DomainError(s"cannot change state to RECEIVED: invalid state: $state").failureNel[ShipmentSpecimen]
    } else {
      copy(state        = ShipmentItemState.Received,
           version      = version + 1,
           timeModified = Some(DateTime.now)).successNel[String]
    }
  }

  def missing(): DomainValidation[ShipmentSpecimen] = {
    if (state != ShipmentItemState.Present) {
      DomainError(s"cannot change state to MISSING: invalid state: $state").failureNel[ShipmentSpecimen]
    } else {
      copy(state        = ShipmentItemState.Missing,
           version      = version + 1,
           timeModified = Some(DateTime.now)).successNel[String]
    }
  }

  def extra(): DomainValidation[ShipmentSpecimen] = {
    if (state != ShipmentItemState.Present) {
      DomainError(s"cannot change state to EXTRA: invalid state: $state").failureNel[ShipmentSpecimen]
    } else {
      copy(state        = ShipmentItemState.Extra,
           version      = version + 1,
           timeModified = Some(DateTime.now)).successNel[String]
    }
  }
}

object ShipmentSpecimen extends ShipmentSpecimenValidations {
  import org.biobank.domain.CommonValidations._
  import org.biobank.CommonValidations._

  def compareByState(a: ShipmentSpecimen, b: ShipmentSpecimen) = (a.state compareTo b.state) < 0

  def create(id:                  ShipmentSpecimenId,
             version:             Long,
             shipmentId:          ShipmentId,
             specimenId:          SpecimenId,
             state:               ShipmentItemState,
             shipmentContainerId: Option[ShipmentContainerId]): DomainValidation[ShipmentSpecimen] = {
    validate(id,
             version,
             shipmentId,
             specimenId,
             state,
             shipmentContainerId).map(_ =>
      ShipmentSpecimen(id,
                       version,
                       DateTime.now,
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
               state:               ShipmentItemState,
               shipmentContainerId: Option[ShipmentContainerId]): DomainValidation[Boolean] = {
    (validateId(id) |@|
       validateVersion(version) |@|
       validateId(shipmentId, ShipmentIdRequired) |@|
       validateId(specimenId, SpecimenIdRequired) |@|
       validateId(shipmentContainerId, ShipmentContainerIdInvalid)) {
      case (_, _, _, _, _) => true
    }
  }
}
