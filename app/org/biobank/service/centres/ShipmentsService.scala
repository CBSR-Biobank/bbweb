package org.biobank.service.centres

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.centre._
import org.biobank.domain.participants.{CeventSpecimenRepository, CollectionEventRepository, Specimen, SpecimenRepository}
import org.biobank.domain.study.CollectionEventTypeRepository
import org.biobank.dto.{CentreLocationInfo, ShipmentDto, ShipmentSpecimenDto, SpecimenDto}
import org.biobank.infrastructure.command.ShipmentCommands._
import org.biobank.infrastructure.command.ShipmentSpecimenCommands._
import org.biobank.infrastructure.event.ShipmentEvents._
import org.biobank.infrastructure.event.ShipmentSpecimenEvents._
import org.biobank.infrastructure.AscendingOrder
import org.biobank.service.participants.SpecimensService
import org.biobank.service._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

@ImplementedBy(classOf[ShipmentsServiceImpl])
trait ShipmentsService {

  /**
   * Returns a set of shipments. The entities can be filtered and or sorted using expressions.
   *
   * @param centreId the ID of the centre the shipments belong to.
   *
   * @param filter the string representation of the filter expression to use to filter the shipments.
   *
   * @param sort the string representation of the sort expression to use when sorting the shipments.
   */
  def getShipments(centreId: CentreId, filter: FilterString, sort: SortString):
      ServiceValidation[List[ShipmentDto]]

  def getShipment(id: ShipmentId): ServiceValidation[ShipmentDto]

  /**
   * Returns a set of shipment specimens. The entities can be filtered and or sorted using expressions.
   *
   * @param shipmentId the ID of the shipment the shipment specimens belong to.
   *
   * @param filter the string representation of the filter expression to use to filter the shipment specimens
   *               in the shipment.
   *
   * @param sort the string representation of the sort expression to use when sorting the shipment specimens.
   */
  def getShipmentSpecimens(shipmentId: ShipmentId, filter: FilterString, sort: SortString):
      ServiceValidation[Seq[ShipmentSpecimenDto]]

  def shipmentCanAddSpecimen(shipmentId: ShipmentId, shipmentSpecimenId: String)
      : ServiceValidation[SpecimenDto]

  def getShipmentSpecimen(shipmentId: ShipmentId, shipmentSpecimenId: String)
      : ServiceValidation[ShipmentSpecimenDto]

  def processCommand(cmd: ShipmentCommand): Future[ServiceValidation[ShipmentDto]]

  def removeShipment(cmd: ShipmentRemoveCmd): Future[ServiceValidation[Boolean]]

  def processShipmentSpecimenCommand(cmd: ShipmentSpecimenCommand):
      Future[ServiceValidation[ShipmentDto]]

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
                                      val collectionEventTypeRepository: CollectionEventTypeRepository,
                                      val specimensService:              SpecimensService)
    extends ShipmentsService
    with ShipmentConstraints {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  /**
   * See:
   *
   * - http://stackoverflow.com/questions/17791933/filter-over-list-with-dynamic-filter-parameter
   *
   * - http://danielwestheide.com/blog/2013/01/23/the-neophytes-guide-to-scala-part-10-staying-dry-with-higher-order-functions.html
   *
   */
  def getShipments(centreId: CentreId, filter: FilterString, sort: SortString):
      ServiceValidation[List[ShipmentDto]] = {

    val centreShipments = shipmentRepository.withCentre(centreId)
    val filteredShipments = ShipmentFilter.filterShipments(centreShipments, filter)
    val sortStr = if (sort.expression.isEmpty) new SortString("courierName")
                  else sort

    for {
      sortExpressions <- {
        QuerySortParser(sortStr).toSuccessNel(ServiceError(s"could not parse sort expression: $sort"))
      }
      sortFunc <- {
        ShipmentDto.sort2Compare.get(sortExpressions(0).name).
          toSuccessNel(ServiceError(s"invalid sort field: ${sortExpressions(0).name}"))
      }
      shipments <- filteredShipments.flatMap(_.map(getShipmentDto).toList.sequenceU)
    } yield {
      val result = shipments.sortWith(sortFunc)
      if (sortExpressions(0).order == AscendingOrder) result
      else result.reverse
    }
  }

  def getShipment(id: ShipmentId): ServiceValidation[ShipmentDto] = {
    shipmentRepository.getByKey(id).flatMap(getShipmentDto)
  }

  def shipmentCanAddSpecimen(shipmentId: ShipmentId, specimenInventoryId: String)
      : ServiceValidation[SpecimenDto] = {
    for {
        shipment     <- shipmentRepository.getByKey(shipmentId)
        specimen     <- specimenRepository.getByInventoryId(specimenInventoryId)
        sameLocation <- {
          if (shipment.fromLocationId == specimen.locationId) {
            specimen.successNel[ServiceError]
          } else {
            ServiceError(s"specimen not at shipment's from location").failureNel[Specimen]
          }
        }
        canBeAdded <- specimenNotPresentInShipment(specimen)
        specimenDto  <- specimensService.get(specimen.id)
      } yield specimenDto
  }

  def getShipmentSpecimens(shipmentId: ShipmentId, filter: FilterString, sort: SortString):
      ServiceValidation[List[ShipmentSpecimenDto]] = {

    val shipmentSpecimens = shipmentSpecimenRepository.allForShipment(shipmentId)
    val filteredShipmentSpecimens = ShipmentSpecimenFilter.filterShipmentSpecimens(shipmentSpecimens, filter)

    val sortStr = if (sort.expression.isEmpty) new SortString("state")
                  else sort
    for {
      sortExpressions <- {
        QuerySortParser(sortStr).toSuccessNel(ServiceError(s"could not parse sort expression: $sort"))
      }
      sortFunc <- {
        ShipmentSpecimenDto.sort2Compare.get(sortExpressions(0).name).
          toSuccessNel(ServiceError(s"invalid sort field: ${sortExpressions(0).name}"))
      }
      shipmentSpecimenss <- filteredShipmentSpecimens.flatMap(_.map(getShipmentSpecimenDto).toList.sequenceU)
    } yield {
      val result = shipmentSpecimenss.sortWith(sortFunc)
      if (sortExpressions(0).order == AscendingOrder) result
      else result.reverse
    }
  }

  def getShipmentSpecimen(shipmentId: ShipmentId, shipmentSpecimenId: String)
      : ServiceValidation[ShipmentSpecimenDto] = {
    for {
      shipment <- shipmentRepository.getByKey(shipmentId)
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
                                                shipment.fromLocationId.id,
                                                fromLocationName)
      val toLocationInfo = CentreLocationInfo(toCentre.id.id,
                                              shipment.toLocationId.id,
                                              toLocationName)
      val specimens = shipmentSpecimenRepository.allForShipment(shipment.id)

      // TODO: update with container count when ready
      ShipmentDto.create(shipment, fromLocationInfo, toLocationInfo, specimens.size, 0)
    }
  }

  private def getShipmentSpecimenDto(shipmentSpecimen: ShipmentSpecimen)
      : ServiceValidation[ShipmentSpecimenDto] = {
    specimensService.getSpecimenDto(shipmentSpecimen.specimenId).map { specimenDto =>
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
      : Future[ServiceValidation[ShipmentDto]] = {
    ask(processor, cmd).mapTo[ServiceValidation[ShipmentSpecimenEvent]].map { validation =>
      for {
        event    <- validation
        shipment <- shipmentRepository.getByKey(ShipmentId(event.shipmentId))
        dto      <- getShipmentDto(shipment)
      } yield dto
    }
  }

  def removeShipmentSpecimen(cmd: ShipmentSpecimenRemoveCmd): Future[ServiceValidation[Boolean]] = {
    ask(processor, cmd).mapTo[ServiceValidation[ShipmentSpecimenEvent]].map { validation =>
      validation.map(_ => true)
    }
  }
}
