package org.biobank.service.centres

import org.biobank.service.ServiceValidation
import org.biobank.domain.centre.{ShipmentRepository, ShipmentSpecimenRepository}
import org.biobank.domain.centre.ShipmentItemState
import org.biobank.domain.participants.SpecimenId
import scalaz.Scalaz._

trait ShipmentConstraints {

  import org.biobank.CommonValidations._

  /**
   * Checks that a specimen is not present in any shipment.
   */
  def specimenNotPresentInShipment(shipmentRepository:         ShipmentRepository,
                                  shipmentSpecimenRepository: ShipmentSpecimenRepository,
                                  specimenId:                 SpecimenId):
      ServiceValidation[Boolean] = {
    val present = shipmentSpecimenRepository.allForSpecimen(specimenId).
      filter { ss => ss.state == ShipmentItemState.Present }

    if (present.isEmpty) true.successNel[String]
    else EntityCriteriaError(s"specimen is already in active shipment").failureNel[Boolean]
  }

}
