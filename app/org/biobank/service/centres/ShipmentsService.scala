package org.biobank.service.centres

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.centre._
import org.biobank.domain.participants.{CeventSpecimenRepository, CollectionEventRepository, SpecimenRepository}
import org.biobank.domain.study.CollectionEventTypeRepository
import org.biobank.dto.{CentreLocationInfo, ShipmentDto, ShipmentSpecimenDto}
import org.biobank.infrastructure.command.ShipmentCommands._
import org.biobank.infrastructure.command.ShipmentSpecimenCommands._
import org.biobank.infrastructure.event.ShipmentEvents._
import org.biobank.infrastructure.event.ShipmentSpecimenEvents._
import org.biobank.infrastructure.{AscendingOrder, SortOrder}
import org.biobank.service.{ServiceError, ServiceValidation}
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._


@ImplementedBy(classOf[ShipmentsServiceImpl])
trait ShipmentsService {

  def getShipments(centreId:             String,
                   courierFilter:        String,
                   trackingNumberFilter: String,
                   stateFilter:          String,
                   sortBy:               String,
                   order:                SortOrder): ServiceValidation[List[ShipmentDto]]

  def getShipment(id: String): ServiceValidation[ShipmentDto]

  def getShipmentSpecimens(shipmentId: String,
                           state:      String,
                           sortBy:     String,
                           order:      SortOrder): ServiceValidation[Seq[ShipmentSpecimenDto]]

  def getShipmentSpecimen(shipmentId: String, shipmentSpecimenId: String)
      : ServiceValidation[ShipmentSpecimenDto]

  def processCommand(cmd: ShipmentCommand): Future[ServiceValidation[ShipmentDto]]

  def removeShipment(cmd: ShipmentRemoveCmd): Future[ServiceValidation[Boolean]]

  def processShipmentSpecimenCommand(cmd: ShipmentSpecimenCommand):
      Future[ServiceValidation[ShipmentSpecimenDto]]

  def removeShipmentSpecimen(cmd: ShipmentSpecimenRemoveCmd): Future[ServiceValidation[Boolean]]
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

  def getShipments(centreId:             String,
                   courierFilter:        String,
                   trackingNumberFilter: String,
                   stateFilter:          String,
                   sortBy:               String,
                   order:                SortOrder)
      : ServiceValidation[List[ShipmentDto]] = {
    ShipmentDto.sort2Compare.get(sortBy).
      toSuccessNel(ServiceError(s"invalid sort field: $sortBy")).
      flatMap { sortFunc =>

        val centreShipments = shipmentRepository.withCentre(CentreId(centreId))

        val shipmentsFilteredByCourier = if (!courierFilter.isEmpty) {
            val courierLowerCase = courierFilter.toLowerCase
            centreShipments.filter { _.courierName.toLowerCase.contains(courierLowerCase)}
          } else {
            centreShipments
          }

        val shipmentsFilteredByTrackingNumber: Set[Shipment] =
          if (!trackingNumberFilter.isEmpty) {
            val trackingNumLowerCase = trackingNumberFilter.toLowerCase
            shipmentsFilteredByCourier.filter { _.trackingNumber.toLowerCase.contains(trackingNumLowerCase)}
          } else {
            shipmentsFilteredByCourier
          }

        val stateFilterValidation = if (stateFilter.isEmpty) {
            shipmentsFilteredByTrackingNumber.successNel[String]
          } else {
            val stateFilterLowercase = stateFilter.toLowerCase
            ShipmentState.values.find(_.toString == stateFilterLowercase) match {
              case Some(state) =>
                shipmentsFilteredByTrackingNumber.filter(_.state == state).successNel[String]
              case None =>
                ServiceError(s"invalid shipment state: $stateFilter").failureNel[Set[Shipment]]
            }
          }

        stateFilterValidation.flatMap {
            _.map(getShipmentDto).
              toList.
              sequenceU.
              map { list =>
                val result = list.sortWith(sortFunc)
                if (order == AscendingOrder) result
                else result.reverse
              }
        }
      }
  }

  def getShipment(id: String): ServiceValidation[ShipmentDto] = {
    shipmentRepository.getByKey(ShipmentId(id)).flatMap(getShipmentDto)
  }

  def getShipmentSpecimens(shipmentId:  String,
                           stateFilter: String,
                           sortBy:      String,
                           order:       SortOrder): ServiceValidation[List[ShipmentSpecimenDto]] = {
    ShipmentSpecimenDto.sort2Compare.get(sortBy).
      toSuccessNel(ServiceError(s"invalid sort field: $sortBy")).
      flatMap { sortFunc =>

        val shipmentSpecimens = shipmentSpecimenRepository.allForShipment(ShipmentId(shipmentId))

        val stateFilterValidation = if (stateFilter.isEmpty) {
            shipmentSpecimens.successNel[String]
          } else {
            val sateFilterLowercase = stateFilter.toLowerCase
            ShipmentItemState.values.find(_.toString == sateFilterLowercase) match {
              case Some(state) =>
                shipmentSpecimens.filter(_.state == state).successNel[String]
              case None =>
                ServiceError(s"invalid shipment item state: $stateFilter").
                  failureNel[Set[ShipmentSpecimen]]
            }
          }

        stateFilterValidation.flatMap {
          _.map(getShipmentSpecimenDto).
            toList.
            sequenceU.
            map { list =>
              val result = list.sortWith(sortFunc)
              if (order == AscendingOrder) result
              else result.reverse
            }
        }
      }
  }

  def getShipmentSpecimen(shipmentId: String, shipmentSpecimenId: String)
      : ServiceValidation[ShipmentSpecimenDto] = {
    for {
      shipment <- shipmentRepository.getByKey(ShipmentId(shipmentId))
      ss       <- shipmentSpecimenRepository.getByKey(ShipmentSpecimenId(shipmentSpecimenId))
      dto      <- getShipmentSpecimenDto(ss)
    } yield dto
  }

  private def getShipmentDto(shipment: Shipment): ServiceValidation[ShipmentDto] = {
    for {
      fromCentre       <- centreRepository.getByLocationId(shipment.fromLocationId)
      fromLocationName <- fromCentre.locationName(shipment.fromLocationId)
      toCentre         <- centreRepository.getByLocationId(shipment.toLocationId)
      toLocationName   <- toCentre.locationName(shipment.toLocationId)
    } yield {
      val fromLocationInfo = CentreLocationInfo(fromCentre.id.id,
                                                shipment.fromLocationId,
                                                fromLocationName)
      val toLocationInfo = CentreLocationInfo(toCentre.id.id,
                                              shipment.toLocationId,
                                              toLocationName)
      val specimens = shipmentSpecimenRepository.allForShipment(shipment.id)

      // TODO: update with container count when ready
      shipment.toDto(fromLocationInfo, toLocationInfo, specimens.size, 0)
    }
  }

  private def getShipmentSpecimenDto(shipmentSpecimen: ShipmentSpecimen)
      : ServiceValidation[ShipmentSpecimenDto] = {
    for {
      specimen           <- specimenRepository.getByKey(shipmentSpecimen.specimenId)
      ceventSpecimen     <- ceventSpecimenRepository.withSpecimenId(specimen.id)
      cevent             <- collectionEventRepository.getByKey(ceventSpecimen.ceventId)
      ceventType         <- collectionEventTypeRepository.getByKey(cevent.collectionEventTypeId)
      specimenSpec       <- ceventType.specimenSpec(specimen.specimenSpecId)
      originCentre       <- centreRepository.getByLocationId(specimen.originLocationId)
      originLocationName <- originCentre.locationName(specimen.originLocationId)
      centre             <- centreRepository.getByLocationId(specimen.locationId)
      locationName       <- centre.locationName(specimen.locationId)
    } yield {
      val originLocationInfo = CentreLocationInfo(originCentre.id.id,
                                                  specimen.originLocationId,
                                                  originLocationName)
      val locationInfo = CentreLocationInfo(centre.id.id,
                                            specimen.locationId,
                                            locationName)
      val specimenDto = specimen.createDto(cevent, specimenSpec, originLocationInfo, locationInfo)
      shipmentSpecimen.createDto(specimenDto)
      }
  }

  def processCommand(cmd: ShipmentCommand): Future[ServiceValidation[ShipmentDto]] = {
    ask(processor, cmd).mapTo[ServiceValidation[ShipmentEvent]].map { validation =>
      for {
        event    <- validation
        shipment <- shipmentRepository.getByKey(ShipmentId(event.id))
        dto      <- getShipmentDto(shipment)
      } yield dto
    }
  }

  def removeShipment(cmd: ShipmentRemoveCmd): Future[ServiceValidation[Boolean]] = {
    ask(processor, cmd).mapTo[ServiceValidation[ShipmentEvent]].map { validation =>
      validation.map(_ => true)
    }
  }

  def processShipmentSpecimenCommand(cmd: ShipmentSpecimenCommand)
      : Future[ServiceValidation[ShipmentSpecimenDto]] = {
    ask(processor, cmd).mapTo[ServiceValidation[ShipmentSpecimenEvent]].map { validation =>
      for {
        event            <- validation
        shipmentSpecimen <- shipmentSpecimenRepository.getByKey(ShipmentSpecimenId(event.id))
        dto              <- getShipmentSpecimenDto(shipmentSpecimen)
      } yield dto
    }
  }

  def removeShipmentSpecimen(cmd: ShipmentSpecimenRemoveCmd): Future[ServiceValidation[Boolean]] = {
    ask(processor, cmd).mapTo[ServiceValidation[ShipmentSpecimenEvent]].map { validation =>
      validation.map(_ => true)
    }
  }
}
