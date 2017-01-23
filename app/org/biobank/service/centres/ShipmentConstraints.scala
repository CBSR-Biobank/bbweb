package org.biobank.service.centres

import org.biobank.service.ServiceValidation
import org.biobank.domain.LocationId
import org.biobank.domain.centre.{ShipmentId, ShipmentRepository, ShipmentSpecimen, ShipmentSpecimenRepository}
import org.biobank.domain.centre.ShipmentItemState
import org.biobank.domain.participants.{Specimen, SpecimenRepository}
//import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

trait ShipmentConstraints {

  import org.biobank.CommonValidations._

  //private val log = LoggerFactory.getLogger(this.getClass)

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

  /**
   * Checks that a specimen is not present in a shipment.
   */
  protected def specimensNotInShipment(shipmentId: ShipmentId,
                                       specimens:  Specimen*): ServiceValidation[List[Specimen]] = {
    specimens.
      map { specimen =>
        shipmentSpecimenRepository.getBySpecimen(shipmentId, specimen).fold(
          err => specimen.successNel[String],
          _   => specimen.inventoryId.failureNel[Specimen]
        )
      }.
      toList.sequenceU.
      leftMap(err =>
        EntityCriteriaError(
          s"specimen inventory IDs already in this shipment: "+ err.list.toList.mkString(", ")).nel)
  }

   def getSpecimens(specimenInventoryIds: String*): ServiceValidation[List[Specimen]] = {
    specimenInventoryIds.
      map { inventoryId =>
        specimenRepository.getByInventoryId(inventoryId).
          leftMap(err => NonEmptyList(inventoryId))
      }.
      toList.sequenceU.
      leftMap(err =>
        EntityCriteriaError("invalid inventory Ids: " + err.list.toList.mkString(", ")).nel)
  }

  /**
   *
   * @param shipSpecimenMap map of inventory ID to Shipment Specimen.
   *
   */
  private def getShipmentSpecimens(shipmentId: ShipmentId, specimens: List[Specimen])
      : ServiceValidation[List[(String, ShipmentSpecimen)]] = {
    specimens.
      map { specimen =>
        shipmentSpecimenRepository.getBySpecimen(shipmentId, specimen).
          map { shipSpecimen => (specimen.inventoryId -> shipSpecimen) }.
          leftMap(err => NonEmptyList(specimen.inventoryId))
      }.
      toList.sequenceU.
      leftMap(err =>
        EntityCriteriaError("specimens not in this shipment: " + err.list.toList.mkString(",")).nel)
  }

  /**
   *
   * @param shipSpecimenMap map of inventory ID to Shipment Specimen.
   *
   */
  def getPackedShipmentSpecimens(shipSpecimenMap: List[(String, ShipmentSpecimen)])
      : ServiceValidation[List[ShipmentSpecimen]] = {
    shipSpecimenMap.
      map { case (inventoryId, shipSpecimen) =>
        shipSpecimen.isStatePresent.
          map { _ => shipSpecimen }.
          leftMap(err => NonEmptyList(inventoryId))
      }.
      toList.sequenceU.
      leftMap(err =>
        EntityCriteriaError("shipment specimens not present: " + err.list.toList.mkString(",")).nel)
  }

  /**
   *
   * @param shipSpecimenMap map of inventory ID to Shipment Specimen.
   *
   */
  def getNonPackedShipmentSpecimens(shipSpecimenMap: List[(String, ShipmentSpecimen)])
      : ServiceValidation[List[ShipmentSpecimen]] = {
    shipSpecimenMap.
      map { case (inventoryId, shipSpecimen) =>
        shipSpecimen.isStateNotPresent.
          map { _ => shipSpecimen }.
          leftMap(err => NonEmptyList(inventoryId))
      }.
      toList.sequenceU.
      leftMap(err =>
        EntityCriteriaError("shipment specimens are present: " + err.list.toList.mkString(",")).nel)
  }

  def shipmentSpecimensPresent(shipmentId: ShipmentId, specimenInventoryIds: String*)
      : ServiceValidation[List[ShipmentSpecimen]] = {
    for {
      specimens           <- getSpecimens(specimenInventoryIds:_*)
      shipSpecimens       <- getShipmentSpecimens(shipmentId, specimens)
      packedShipSpecimens <- getPackedShipmentSpecimens(shipSpecimens)
    } yield packedShipSpecimens

  }

  def shipmentSpecimensNotPresent(shipmentId: ShipmentId, specimenInventoryIds: String*)
      : ServiceValidation[List[ShipmentSpecimen]] = {
    for {
      specimens              <- getSpecimens(specimenInventoryIds:_*)
      shipSpecimens          <- getShipmentSpecimens(shipmentId, specimens)
      nonPackedShipSpecimens <- getNonPackedShipmentSpecimens(shipSpecimens)
    } yield nonPackedShipSpecimens

  }

}
