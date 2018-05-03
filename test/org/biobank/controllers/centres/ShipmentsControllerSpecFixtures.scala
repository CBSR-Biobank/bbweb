package org.biobank.controllers.centres

import org.biobank.domain.centres._
import org.biobank.fixtures.ControllerFixture
import org.biobank.domain.participants._

private[centres] class ShipmentsControllerSpecFixtures extends ControllerFixture with ShipmentSpecFixtures {

  override def centresFixture = {
    val fixture = super.centresFixture
    centreRepository.put(fixture.fromCentre)
    centreRepository.put(fixture.toCentre)
    fixture
  }

  override def specimensFixture(numSpecimens: Int) = {
    val f = super.specimensFixture(numSpecimens)

    centreRepository.put(f.fromCentre)
    centreRepository.put(f.toCentre)
    studyRepository.put(f.study)
    collectionEventTypeRepository.put(f.ceventType)
    participantRepository.put(f.participant)
    collectionEventRepository.put(f.cevent)
    f.specimens.foreach { specimen =>
      specimenRepository.put(specimen)
      ceventSpecimenRepository.put(CeventSpecimen(f.cevent.id, specimen.id))
    }
    shipmentRepository.put(f.shipment)
    f
  }

  override def shipmentSpecimensFixture(numSpecimens: Int) = {
    val f = super.shipmentSpecimensFixture(numSpecimens)
    f.shipmentSpecimenMap.values.foreach { v =>
      specimenRepository.put(v.specimen)
      shipmentSpecimenRepository.put(v.shipmentSpecimen)
    }
    f
  }

  override def addSpecimenToShipment(shipment: Shipment, fromCentre: Centre) = {
    val f = super.addSpecimenToShipment(shipment, fromCentre)
    specimenRepository.put(f.specimen)
    shipmentSpecimenRepository.put(f.shipmentSpecimen)
    f
  }

}
