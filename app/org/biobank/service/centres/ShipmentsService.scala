package org.biobank.service.centres

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.dto.{ShipmentDto, ShipmentSpecimenDto}
import org.biobank.domain.{DomainError, DomainValidation}
import org.biobank.domain.centre._
import org.biobank.domain.study.CollectionEventTypeRepository
import org.biobank.domain.participants.{
  CeventSpecimenRepository,
  CollectionEventRepository,
  SpecimenRepository
}
import org.biobank.infrastructure.command.ShipmentCommands._
import org.biobank.infrastructure.command.ShipmentSpecimenCommands._
import org.biobank.infrastructure.event.ShipmentEvents._
import org.biobank.infrastructure.event.ShipmentSpecimenEvents._
import org.biobank.infrastructure.{AscendingOrder, SortOrder}
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[ShipmentsServiceImpl])
trait ShipmentsService {

  def getShipments(courierFilter:        String,
                   trackingNumberFilter: String,
                   stateFilter:          String,
                   sortBy:               String,
                   order:                SortOrder): DomainValidation[List[ShipmentDto]]

  def getShipment(id: String): DomainValidation[ShipmentDto]

  def getShipmentSpecimens(shipmentId: String,
                           sortBy:     String,
                           order:      SortOrder): DomainValidation[Seq[ShipmentSpecimenDto]]

  def getShipmentSpecimen(shipmentId: String, shipmentSpecimenId: String)
      : DomainValidation[ShipmentSpecimenDto]

  def processCommand(cmd: ShipmentCommand): Future[DomainValidation[Shipment]]

  def removeShipment(cmd: ShipmentRemoveCmd): Future[DomainValidation[Boolean]]

  def processShipmentSpecimenCommand(cmd: ShipmentSpecimenCommand):
      Future[DomainValidation[ShipmentSpecimenDto]]

  def removeShipmentSpecimen(cmd: ShipmentSpecimenRemoveCmd): Future[DomainValidation[Boolean]]
}

/**
 * Handles all commands dealing with shipments, shipment specimens, and shipment containers.
 */
class ShipmentsServiceImpl @Inject() (@Named("shipmentsProcessor") val   processor: ActorRef,
                                      val centreRepository:              CentreRepository,
                                      val shipmentRepository:            ShipmentRepository,
                                      val shipmentSpecimenRepository:    ShipmentSpecimenRepository,
                                      val specimenRepository:            SpecimenRepository,
                                      val ceventSpecimenRepository:      CeventSpecimenRepository,
                                      val collectionEventRepository:     CollectionEventRepository,
                                      val collectionEventTypeRepository: CollectionEventTypeRepository)
    extends ShipmentsService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  def getShipments(courierFilter:        String,
                   trackingNumberFilter: String,
                   stateFilter:          String,
                   sortBy:               String,
                   order:                SortOrder)
      : DomainValidation[List[ShipmentDto]] = {
    ShipmentDto.sort2Compare.get(sortBy).toSuccessNel(DomainError(s"invalid sort field: $sortBy"))
      .flatMap { sortFunc =>
      val allShipments = shipmentRepository.getValues

      val shipmentsFilteredByCourier = if (!courierFilter.isEmpty) {
          val courierLowerCase = courierFilter.toLowerCase
          allShipments.filter { _.courierName.toLowerCase.contains(courierLowerCase)}
        } else {
          allShipments
        }

      val shipmentsFilteredByTrackingNumber = if (!trackingNumberFilter.isEmpty) {
          val trackingNumLowerCase = trackingNumberFilter.toLowerCase
          shipmentsFilteredByCourier.filter { _.trackingNumber.toLowerCase.contains(trackingNumLowerCase)}
        } else {
          shipmentsFilteredByCourier
        }

      val shipmentsFilteredByState = if (!stateFilter.isEmpty) {
          val stateLowerCase = stateFilter.toLowerCase
          shipmentsFilteredByTrackingNumber.filter { _.state.toString.toLowerCase.contains(stateLowerCase)}
        } else {
          shipmentsFilteredByTrackingNumber
        }

      shipmentsFilteredByState.toSeq.map(getShipmentDto).toList.sequenceU.map { list =>
        val result = list.sortWith(sortFunc)
        if (order == AscendingOrder) result
        else result.reverse
      }
    }
  }

  def getShipment(id: String): DomainValidation[ShipmentDto] = {
    shipmentRepository.getByKey(ShipmentId(id)).flatMap(getShipmentDto)
  }

  def getShipmentSpecimens(shipmentId: String,
                           sortBy:     String,
                           order:      SortOrder): DomainValidation[List[ShipmentSpecimenDto]] = {
    ShipmentSpecimenDto.sort2Compare.get(sortBy).toSuccessNel(DomainError(s"invalid sort field: $sortBy"))
      .flatMap { sortFunc =>
      shipmentSpecimenRepository.allForShipment(ShipmentId(shipmentId)).map { ss =>
        getShipmentSpecimenDto(ss)
      }.toList.sequenceU.map { list =>
        val result = list.sortWith(sortFunc)
        if (order == AscendingOrder) result else result.reverse
      }
    }
  }

  def getShipmentSpecimen(shipmentId: String, shipmentSpecimenId: String)
      : DomainValidation[ShipmentSpecimenDto] = {
    for {
      shipment <- shipmentRepository.getByKey(ShipmentId(shipmentId))
      ss       <- shipmentSpecimenRepository.getByKey(ShipmentSpecimenId(shipmentSpecimenId))
      dto      <- getShipmentSpecimenDto(ss)
    } yield dto
  }

  private def getShipmentDto(shipment: Shipment): DomainValidation[ShipmentDto] = {
    for {
      fromCentre <- centreRepository.getByLocationId(shipment.fromLocationId)
      fromLocationName <- fromCentre.locationName(shipment.fromLocationId)
      toCentre <- centreRepository.getByLocationId(shipment.toLocationId)
      toLocationName <- toCentre.locationName(shipment.toLocationId)
    } yield shipment.toDto(fromLocationName, toLocationName)
  }

  private def getShipmentSpecimenDto(shipmentSpecimen: ShipmentSpecimen)
      : DomainValidation[ShipmentSpecimenDto] = {
    for {
      specimen           <- specimenRepository.getByKey(shipmentSpecimen.specimenId)
      ceventSpecimen     <- ceventSpecimenRepository.withSpecimenId(specimen.id)
      cevent             <- collectionEventRepository.getByKey(ceventSpecimen.ceventId)
      ceventType         <- collectionEventTypeRepository.getByKey(cevent.collectionEventTypeId)
      specimenSpec       <- ceventType.specimenSpec(specimen.specimenSpecId)
      centre             <- centreRepository.getByLocationId(specimen.originLocationId)
      centreLocationName <- centre.locationName(specimen.locationId)
    } yield shipmentSpecimen.createDto(specimen, centreLocationName, specimenSpec.units)
  }

  def processCommand(cmd: ShipmentCommand): Future[DomainValidation[Shipment]] = {
    ask(processor, cmd).mapTo[DomainValidation[ShipmentEvent]].map { validation =>
      for {
        event    <- validation
        shipment <- shipmentRepository.getByKey(ShipmentId(event.id))
      } yield shipment
    }
  }

  def removeShipment(cmd: ShipmentRemoveCmd): Future[DomainValidation[Boolean]] = {
    ask(processor, cmd).mapTo[DomainValidation[ShipmentEvent]].map { validation =>
      validation.map(_ => true)
    }
  }

  def processShipmentSpecimenCommand(cmd: ShipmentSpecimenCommand)
      : Future[DomainValidation[ShipmentSpecimenDto]] = {
    ask(processor, cmd).mapTo[DomainValidation[ShipmentSpecimenEvent]].map { validation =>
      for {
        event            <- validation
        shipmentSpecimen <- shipmentSpecimenRepository.getByKey(ShipmentSpecimenId(event.id))
        dto              <- getShipmentSpecimenDto(shipmentSpecimen)
      } yield dto
    }
  }

  def removeShipmentSpecimen(cmd: ShipmentSpecimenRemoveCmd): Future[DomainValidation[Boolean]] = {
    ask(processor, cmd).mapTo[DomainValidation[ShipmentSpecimenEvent]].map { validation =>
      validation.map(_ => true)
    }
  }
}
