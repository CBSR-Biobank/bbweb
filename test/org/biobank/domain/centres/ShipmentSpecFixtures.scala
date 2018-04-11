package org.biobank.domain.centres

import java.time.OffsetDateTime
import org.biobank.dto._
import org.biobank.domain.{EntityState, Factory}
import org.biobank.domain.studies._
import org.biobank.domain.participants._
import org.biobank.services.centres.CentreLocationInfo
import org.scalatest.Assertions._

trait ShipmentSpecFixtures {

  class ToFromCentres(val fromCentre: Centre, val toCentre: Centre)

  class ShipmentFixture[T <: Shipment](fromCentre:   Centre,
                                       toCentre:     Centre,
                                       val shipment: T)
      extends ToFromCentres(fromCentre, toCentre)

  class ShipmentsFixture(fromCentre:      Centre,
                         toCentre:        Centre,
                         val shipmentMap: Map[ShipmentId, Shipment])
      extends ToFromCentres(fromCentre, toCentre)

  class ShipmentsByStateFixture[T <: Shipment](fromCentre:    Centre,
                                               toCentre:      Centre,
                                               val shipments: Map[EntityState, Shipment])
      extends ToFromCentres(fromCentre, toCentre)

  class CreatedShipmentsFixture(fromCentre:      Centre,
                                toCentre:        Centre,
                                val shipmentMap: Map[ShipmentId, CreatedShipment])
      extends ToFromCentres(fromCentre, toCentre)

  class SpecimenShipmentSpecimen(val specimen: UsableSpecimen, val shipmentSpecimen: ShipmentSpecimen)

  class CollectionEventFixture {
    val study = factory.createEnabledStudy
    val specimenDefinition = factory.createCollectionSpecimenDefinition
    val ceventType = factory.createCollectionEventType.copy(studyId = study.id,
                                                            specimenDefinitions = Set(specimenDefinition),
                                                            annotationTypes = Set.empty)
    val participant = factory.createParticipant.copy(studyId = study.id)
    val cevent = factory.createCollectionEvent
  }

  class SpecimensFixture(fromCentre:              Centre,
                         toCentre:                Centre,
                         shipment:                Shipment,
                         val study:               Study,
                         val specimenDefinition: CollectionSpecimenDefinition,
                         val ceventType:          CollectionEventType,
                         val participant:         Participant,
                         val cevent:              CollectionEvent,
                         val specimens:           List[UsableSpecimen])
      extends ShipmentFixture(fromCentre, toCentre, shipment)

  case class ShipmentSpecimenData(val specimen:            UsableSpecimen,
                                  val specimenDto:         SpecimenDto,
                                  val shipmentSpecimen:    ShipmentSpecimen,
                                  val shipmentSpecimenDto: ShipmentSpecimenDto)

  class ShipmentSpecimensFixture(fromCentre:              Centre,
                                 toCentre:                Centre,
                                 shipment:                Shipment,
                                 study:                   Study,
                                 specimenDefinition:     CollectionSpecimenDefinition,
                                 ceventType:              CollectionEventType,
                                 participant:             Participant,
                                 cevent:                  CollectionEvent,
                                 specimens:               List[UsableSpecimen],
                                 val shipmentSpecimenMap: Map[SpecimenId, ShipmentSpecimenData])
      extends SpecimensFixture(fromCentre,
                               toCentre,
                               shipment,
                               study,
                               specimenDefinition,
                               ceventType,
                               participant,
                               cevent,
                               specimens)

  protected val factory: Factory

  val nonCreatedStates = List(Shipment.packedState,
                              Shipment.sentState,
                              Shipment.receivedState,
                              Shipment.unpackedState,
                              Shipment.lostState)

  def centresFixture = {
    val centres = (1 to 2).map { _ =>
        val location = factory.createLocation
        factory.createEnabledCentre.copy(locations = Set(location))
      }
    new ToFromCentres(centres(0), centres(1))
  }

  def createdShipmentFixture = {
    val f = centresFixture
    new ShipmentFixture(f.fromCentre,
                        f.toCentre,
                        factory.createShipment(f.fromCentre, f.toCentre))
  }

  def makePackedShipment(shipment: Shipment): PackedShipment = {
    shipment match {
      case s: CreatedShipment => s.pack(OffsetDateTime.now)
      case _ => fail(s"bad shipment state: ${shipment.state}")
    }
  }

  def makeSentShipment(shipment: Shipment): SentShipment = {
    makePackedShipment(shipment).send(OffsetDateTime.now).fold(
      err => fail("could not make a sent shipment"), s => s)
  }

  def makeReceivedShipment(shipment: Shipment): ReceivedShipment = {
    makeSentShipment(shipment).receive(OffsetDateTime.now).fold(
      err => fail("could not make a received shipment"), s => s)
  }

  def makeUnpackedShipment(shipment: Shipment): UnpackedShipment = {
    makeReceivedShipment(shipment).unpack(OffsetDateTime.now).fold(
      err => fail("could not make a unpacked shipment"), s => s)
  }

  def makeLostShipment(shipment: Shipment): LostShipment = {
    makeSentShipment(shipment).lost
  }

  def createdShipmentsFixture(numShipments: Int) = {
    val f = centresFixture
    new CreatedShipmentsFixture(
      f.fromCentre,
      f.toCentre,
      (1 to numShipments).map { _ =>
        val shipment = factory.createShipment(f.fromCentre, f.toCentre)
        shipment.id -> shipment
      }.toMap
    )
  }

  def packedShipmentFixture = {
    val f = createdShipmentFixture
    new ShipmentFixture(fromCentre = f.fromCentre,
                        toCentre   = f.toCentre,
                        shipment   = makePackedShipment(f.shipment))
  }

  def sentShipmentFixture = {
    val f = createdShipmentFixture

    new ShipmentFixture(fromCentre = f.fromCentre,
                        toCentre = f.toCentre,
                        shipment = makeSentShipment(f.shipment))
  }

  def receivedShipmentFixture = {
    val f = createdShipmentFixture
    new ShipmentFixture(fromCentre = f.fromCentre,
                        toCentre = f.toCentre,
                        shipment = makeReceivedShipment(f.shipment))
  }

  def unpackedShipmentFixture = {
    val f = createdShipmentFixture
    new ShipmentFixture(fromCentre = f.fromCentre,
                        toCentre   = f.toCentre,
                        shipment   = makeUnpackedShipment(f.shipment))
  }

  def lostShipmentFixture = {
    val f = createdShipmentFixture
    new ShipmentFixture(fromCentre = f.fromCentre,
                        toCentre   = f.toCentre,
                        shipment   = makeLostShipment(f.shipment))
  }

  def allShipmentsFixture = {
    val centres = centresFixture
    val fromCentre = centres.fromCentre
    val toCentre = centres.toCentre
    new ShipmentsByStateFixture(
      fromCentre = centres.fromCentre,
      toCentre = centres.toCentre,
      shipments = Map(
          Shipment.createdState   -> factory.createShipment(fromCentre, toCentre),
          Shipment.packedState    -> factory.createPackedShipment(fromCentre, toCentre),
          Shipment.sentState      -> factory.createSentShipment(fromCentre, toCentre),
          Shipment.receivedState  -> factory.createReceivedShipment(fromCentre, toCentre),
          Shipment.unpackedState  -> factory.createUnpackedShipment(fromCentre, toCentre),
          Shipment.completedState -> factory.createCompletedShipment(fromCentre, toCentre),
          Shipment.lostState      -> factory.createLostShipment(fromCentre, toCentre)))
  }

  def specimensFixture(numSpecimens: Int) = {
    val f = createdShipmentFixture
    val ceventFixture = new CollectionEventFixture

    val specimens = (1 to numSpecimens).map { _ =>
        factory.createUsableSpecimen.copy(originLocationId = f.fromCentre.locations.head.id,
                                          locationId = f.fromCentre.locations.head.id)
      }.toList

    new SpecimensFixture(fromCentre          = f.fromCentre,
                         toCentre            = f.toCentre,
                         study               = ceventFixture.study,
                         specimenDefinition = ceventFixture.specimenDefinition,
                         ceventType          = ceventFixture.ceventType,
                         participant         = ceventFixture.participant,
                         cevent              = ceventFixture.cevent,
                         specimens           = specimens,
                         shipment            = f.shipment)
  }

  def shipmentSpecimensFixture(numSpecimens: Int) = {
    val f = specimensFixture(numSpecimens)

    val map = f.specimens.zipWithIndex.map { case (specimen, index) =>
        val updatedSpecimen = specimen.copy(inventoryId = s"inventoryId_$index")
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id)
        val originLocationName = f.fromCentre.locationName(specimen.originLocationId).
          fold(e => "error", n => n)
        val centreLocationInfo = CentreLocationInfo(f.fromCentre.id.id,
                                                    specimen.originLocationId.id,
                                                    originLocationName)
        val specimenDto = specimen.createDto(f.cevent,
                                             f.ceventType.name,
                                             f.specimenDefinition,
                                             centreLocationInfo,
                                             centreLocationInfo)
        (updatedSpecimen.id, new ShipmentSpecimenData(updatedSpecimen,
                                                      specimenDto,
                                                      shipmentSpecimen,
                                                      shipmentSpecimen.createDto(specimenDto)))
      }.toMap

    new ShipmentSpecimensFixture(fromCentre          = f.fromCentre,
                                 toCentre            = f.toCentre,
                                 study               = f.study,
                                 specimenDefinition = f.specimenDefinition,
                                 ceventType          = f.ceventType,
                                 participant         = f.participant,
                                 cevent              = f.cevent,
                                 specimens           = f.specimens,
                                 shipment            = f.shipment,
                                 shipmentSpecimenMap = map)
  }

  def addSpecimenToShipment(shipment: Shipment, fromCentre: Centre) = {
    val specimen = factory.createUsableSpecimen.
      copy(originLocationId = fromCentre.locations.head.id,
           locationId       = fromCentre.locations.head.id)
    val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = shipment.id,
                                                               specimenId = specimen.id)
    new SpecimenShipmentSpecimen(specimen, shipmentSpecimen)
  }

}
