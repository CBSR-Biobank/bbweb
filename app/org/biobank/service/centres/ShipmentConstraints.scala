package org.biobank.service.centres

import org.biobank.service.ServiceValidation
import org.biobank.domain.centre.{Shipment, ShipmentRepository, ShipmentSpecimenRepository}
import org.biobank.domain.centre.ShipmentItemState
import org.biobank.domain.participants.{Specimen, SpecimenRepository}
import scalaz.Scalaz._

trait ShipmentConstraints {

  import org.biobank.CommonValidations._

  val shipmentRepository: ShipmentRepository

  val shipmentSpecimenRepository: ShipmentSpecimenRepository

  val specimenRepository: SpecimenRepository

  /**
   * Checks that a specimen is not present in any shipment.
   */
  def specimenNotPresentInShipment(specimen: Specimen): ServiceValidation[Boolean] = {
    val present = shipmentSpecimenRepository.allForSpecimen(specimen.id).
      filter { ss => ss.state == ShipmentItemState.Present }

    if (present.isEmpty) true.successNel[String]
    else EntityCriteriaError(s"specimen is already in active shipment").failureNel[Boolean]
}

  def validCentreLocation(shipment: Shipment, specimen: Specimen): ServiceValidation[Boolean] = {
    if (specimen.locationId == shipment.fromLocationId) {
      true.successNel[String]
    } else {
      EntityCriteriaError(s"specimen not present at shipment's originating centre: ${specimen.inventoryId}")
        .failureNel[Boolean]
    }
  }

}
