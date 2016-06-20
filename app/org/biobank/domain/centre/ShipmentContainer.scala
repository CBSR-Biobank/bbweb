package org.biobank.domain.centre

import org.biobank._
import org.biobank.domain._
import org.biobank.domain.centre.ShipmentItemState._
import org.biobank.domain.containers.ContainerId
import org.joda.time.DateTime
import play.api.libs.json._
import scalaz.Scalaz._

case class ShipmentContainerId(id: String) extends IdentifiedValueObject[String]

object ShipmentContainerId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val shipmentContainerIdReader =
    (__ \ "id").read[String].map( new ShipmentContainerId(_) )

  implicit val shipmentContainerIdWriter =
    Writes{ (shipmentContainerId: ShipmentContainerId) => JsString(shipmentContainerId.id) }

}

/**
 * Marks a specific [org.biobank.domain.containers.Container] as having been in a specific
 * [org.biobank.domain.centre.Shipment].
 *
 */
case class ShipmentContainer(id:          ShipmentContainerId,
                            version:      Long,
                            timeAdded:    DateTime,
                            timeModified: Option[DateTime],
                            shipmentId:   ShipmentId,
                            containerId:  ContainerId,
                            state:        ShipmentItemState)
    extends ConcurrencySafeEntity[ShipmentContainerId] {
}

object ShipmentContainer {
  import org.biobank.CommonValidations._
  import org.biobank.domain.CommonValidations._

  case object ShipmentIdRequired extends ValidationKey

  def create(id:           ShipmentContainerId,
             version:      Long,
             shipmentId:   ShipmentId,
             containerId:  ContainerId,
             state:        ShipmentItemState): DomainValidation[ShipmentContainer] = {
    validate(id,
             version,
             shipmentId,
             containerId,
             state).map(_ => ShipmentContainer(id,
                                               version,
                                               DateTime.now,
                                               None,
                                               shipmentId,
                                               containerId,
                                               state))
  }

  def validate(id:          ShipmentContainerId,
               version:     Long,
               shipmentId:  ShipmentId,
               containerId: ContainerId,
               state:       ShipmentItemState): DomainValidation[Boolean] = {
    (validateId(id) |@|
       validateVersion(version) |@|
       validateId(shipmentId, ShipmentIdRequired) |@|
       validateId(containerId, ContainerIdInvalid)) {
      case (_, _, _, _) => true
    }
  }
}
