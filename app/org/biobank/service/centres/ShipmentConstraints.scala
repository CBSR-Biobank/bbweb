package org.biobank.service.centres

import org.biobank.service.ServiceValidation
import org.biobank.domain.LocationId
import org.biobank.domain.centre.{ShipmentRepository, ShipmentSpecimenRepository}
import org.biobank.domain.centre.ShipmentItemState
import org.biobank.domain.participants.{Specimen, SpecimenRepository}
import scalaz.Scalaz._

trait ShipmentConstraints {

  import org.biobank.CommonValidations._

  val shipmentRepository: ShipmentRepository

  val shipmentSpecimenRepository: ShipmentSpecimenRepository

  val specimenRepository: SpecimenRepository

  /**
   * Ensures that the inventory IDs are for specimens already in the system.
   *
   * @param specimenInventoyIds one or more inventory IDs.
   *
   * @returns a validation. Success if all inventory IDS match specimens in the system. Failure if not.
   */
  protected def inventoryIdsToSpecimens(specimenInventoryIds: String*): ServiceValidation[List[Specimen]] = {
    specimenInventoryIds.
      map { id =>
        val trimmedId = id.trim
        specimenRepository.getByInventoryId(trimmedId).fold(
          err => trimmedId.failureNel[Specimen],
          spc => spc.successNel[String]
        )
      }.
      toList.sequenceU.
      leftMap(err =>
        EntityCriteriaNotFound{s"invalid specimen inventory IDs: " + err.list.toList.mkString(", ")}.nel)
  }

  protected def specimensAtCentre(locationId: LocationId, specimens: Specimen*)
      : ServiceValidation[List[Specimen]] = {
    specimens.
      map { specimen =>
        if (locationId == specimen.locationId) specimen.successNel[String]
        else specimen.inventoryId.failureNel[Specimen]
      }.
      toList.sequenceU.
      leftMap(err =>
        EntityCriteriaError(
          s"invalid centre for specimen inventory IDs: "+ err.list.toList.mkString(", ")).nel)
  }

  /**
   * Checks that a specimen is not present in any shipment.
   */
  protected def specimensNotPresentInShipment(specimens: Specimen*): ServiceValidation[List[Specimen]] = {
    specimens.
      map { specimen =>
        val present = shipmentSpecimenRepository.allForSpecimen(specimen.id).
          filter { ss => ss.state == ShipmentItemState.Present }

        if (present.isEmpty) specimen.successNel[String]
        else specimen.inventoryId.failureNel[Specimen]
      }.
      toList.sequenceU.
      leftMap(err =>
        EntityCriteriaError(
          s"specimens are already in an active shipment: "+ err.list.toList.mkString(", ")).nel)

  }

}
