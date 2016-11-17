package org.biobank.controllers.centres

import com.github.nscala_time.time.Imports._
import org.biobank.dto.{CentreLocationInfo}
import org.biobank.domain.centre._
import org.biobank.fixture.ControllerFixture
import org.biobank.domain.participants._
import scala.language.reflectiveCalls

private[centres] trait ShipmentsControllerSpecFixtures extends ControllerFixture {

  val nonCreatedStates = List(ShipmentState.Packed,
                              ShipmentState.Sent,
                              ShipmentState.Received,
                              ShipmentState.Unpacked,
                              ShipmentState.Lost)

  def centresFixture = {
    val centres = (1 to 2).map { _ =>
        val location = factory.createLocation
        factory.createEnabledCentre.copy(locations = Set(location))
      }
    centres.foreach(centreRepository.put)
    new {
      val fromCentre = centres(0)
      val toCentre = centres(1)
    }
  }

  def makePackedShipment(shipment: CreatedShipment): PackedShipment = {
    shipment.pack(DateTime.now)
  }

  def makeSentShipment(shipment: CreatedShipment): SentShipment = {
    makePackedShipment(shipment).send(DateTime.now).fold(
      err => fail("could not make a sent shipment"), s => s)
  }

  def makeReceivedShipment(shipment: CreatedShipment): ReceivedShipment = {
    makeSentShipment(shipment).receive(DateTime.now).fold(
      err => fail("could not make a received shipment"), s => s)
  }

  def makeUnpackedShipment(shipment: CreatedShipment): UnpackedShipment = {
    makeReceivedShipment(shipment).unpack(DateTime.now).fold(
      err => fail("could not make a unpacked shipment"), s => s)
  }

  def makeLostShipment(shipment: CreatedShipment): LostShipment = {
    makeSentShipment(shipment).lost
  }

  def createdShipmentFixture = {
    val f = centresFixture
    new {
      val fromCentre = f.fromCentre
      val toCentre = f.toCentre
      val shipment = factory.createShipment(f.fromCentre, f.toCentre)
    }
  }

  def createdShipmentFixture(numShipments: Int) = {
    val f = centresFixture
    new {
      val fromCentre = f.fromCentre
      val toCentre = f.toCentre
      val shipmentMap = (1 to numShipments).map { _ =>
          val shipment = factory.createShipment(f.fromCentre, f.toCentre)
          shipment.id -> shipment
        }.toMap
    }
  }

  def packedShipmentFixture = {
    val f = createdShipmentFixture
    new {
      val fromCentre = f.fromCentre
      val toCentre = f.toCentre
      val shipment = makePackedShipment(f.shipment)
    }
  }

  def sentShipmentFixture = {
    val f = createdShipmentFixture
    new {
      val fromCentre = f.fromCentre
      val toCentre = f.toCentre
      val shipment = makeSentShipment(f.shipment)
    }
  }

  def receivedShipmentFixture = {
    val f = createdShipmentFixture
    new {
      val fromCentre = f.fromCentre
      val toCentre = f.toCentre
      val shipment = makeReceivedShipment(f.shipment)
    }
  }

  def unpackedShipmentFixture = {
    val f = createdShipmentFixture
    new {
      val fromCentre = f.fromCentre
      val toCentre = f.toCentre
      val shipment = makeUnpackedShipment(f.shipment)
    }
  }

  def lostShipmentFixture = {
    val f = createdShipmentFixture
    new {
      val fromCentre = f.fromCentre
      val toCentre = f.toCentre
      val shipment = makeLostShipment(f.shipment)
    }
  }

  def allShipmentsFixture = {
    val centres = centresFixture
    new {
      val fromCentre = centres.fromCentre
      val toCentre = centres.toCentre
      val shipments = Map(
          ShipmentState.Created  -> factory.createShipment(fromCentre, toCentre),
          ShipmentState.Packed   -> factory.createPackedShipment(fromCentre, toCentre),
          ShipmentState.Sent     -> factory.createSentShipment(fromCentre, toCentre),
          ShipmentState.Received -> factory.createReceivedShipment(fromCentre, toCentre),
          ShipmentState.Unpacked -> factory.createUnpackedShipment(fromCentre, toCentre),
          ShipmentState.Lost     -> factory.createLostShipment(fromCentre, toCentre))
    }
  }

  def specimensFixture(numSpecimens: Int) = {
    val f = createdShipmentFixture
    val _study = factory.createEnabledStudy
    val _specimenSpec = factory.createCollectionSpecimenSpec
    val _ceventType = factory.createCollectionEventType.copy(studyId = _study.id,
                                                             specimenSpecs = Set(_specimenSpec),
                                                             annotationTypes = Set.empty)
    val _participant = factory.createParticipant.copy(studyId = _study.id)
    val _cevent = factory.createCollectionEvent
    val _specimens = (1 to numSpecimens).map { _ =>
        factory.createUsableSpecimen.copy(originLocationId = f.fromCentre.locations.head.uniqueId,
               locationId = f.fromCentre.locations.head.uniqueId)
      }.toList

    centreRepository.put(f.fromCentre)
    centreRepository.put(f.toCentre)

    studyRepository.put(_study)
    collectionEventTypeRepository.put(_ceventType)
    participantRepository.put(_participant)
    collectionEventRepository.put(_cevent)
    _specimens.foreach { specimen =>
      specimenRepository.put(specimen)
      ceventSpecimenRepository.put(CeventSpecimen(_cevent.id, specimen.id))
    }
    shipmentRepository.put(f.shipment)

    new {
      val fromCentre   = f.fromCentre
      val toCentre     = f.toCentre
      val study        = _study
      val specimenSpec = _specimenSpec
      val ceventType   = _ceventType
      val participant  = _participant
      val cevent       = _cevent
      val specimens    = _specimens
      val shipment     = f.shipment
    }
  }

  def shipmentSpecimensFixture(numSpecimens: Int) = {
    val f = specimensFixture(numSpecimens)

    val map = f.specimens.zipWithIndex.map { case (specimen, index) =>
        val updatedSpecimen = specimen.copy(inventoryId = s"inventoryId_$index")
        specimenRepository.put(updatedSpecimen)
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id)
        val originLocationName = f.fromCentre.locationName(specimen.originLocationId).
          fold(e => "error", n => n)
        val centreLocationInfo = CentreLocationInfo(f.fromCentre.id.id,
                                                    specimen.originLocationId,
                                                    originLocationName)
        val specimenDto =
          specimen.createDto(f.cevent, f.specimenSpec, centreLocationInfo, centreLocationInfo)
        (updatedSpecimen.id, (updatedSpecimen,
                              specimenDto,
                              shipmentSpecimen,
                              shipmentSpecimen.createDto(specimenDto)))
      }.toMap

    map.values.foreach(x => shipmentSpecimenRepository.put(x._3))

    new {
      val fromCentre          = f.fromCentre
      val toCentre            = f.toCentre
      val study               = f.study
      val specimenSpec        = f.specimenSpec
      val ceventType          = f.ceventType
      val participant         = f.participant
      val cevent              = f.cevent
      val specimens           = f.specimens
      val shipment            = f.shipment
      val shipmentSpecimenMap = map
    }
  }

}
