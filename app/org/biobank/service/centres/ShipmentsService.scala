package org.biobank.service.centres

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.LocationId
import org.biobank.domain.centre._
import org.biobank.domain.access.PermissionId
import org.biobank.domain.participants._
import org.biobank.domain.study.CollectionEventTypeRepository
import org.biobank.domain.user.UserId
import org.biobank.dto.{CentreLocationInfo, ShipmentDto, ShipmentSpecimenDto, SpecimenDto}
import org.biobank.infrastructure.command.ShipmentCommands._
import org.biobank.infrastructure.command.ShipmentSpecimenCommands._
import org.biobank.infrastructure.event.ShipmentEvents._
import org.biobank.infrastructure.event.ShipmentSpecimenEvents._
import org.biobank.infrastructure.AscendingOrder
import org.biobank.service.participants.SpecimensService
import org.biobank.service._
import org.biobank.service.access.AccessService
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

@ImplementedBy(classOf[ShipmentsServiceImpl])
trait ShipmentsService extends BbwebService {

  def getShipment(requestUserId: UserId, id: ShipmentId): ServiceValidation[ShipmentDto]

  /**
   * Returns a set of shipments to or from a Centre. The shipments can be filtered and or sorted using
   * expressions.
   *
   * @param centreId the ID of the centre the shipments belong to.
   *
   * @param filter the string representation of the filter expression to use to filter the shipments.
   *
   * @param sort the string representation of the sort expression to use when sorting the shipments.
   */
  def getShipments(requestUserId: UserId, filter: FilterString, sort: SortString):
      ServiceValidation[List[ShipmentDto]]

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
  def getShipmentSpecimens(requestUserId: UserId,
                           shipmentId:    ShipmentId,
                           filter:        FilterString,
                           sort:          SortString): ServiceValidation[Seq[ShipmentSpecimenDto]]

  def shipmentCanAddSpecimen(requestUserId: UserId, shipmentId: ShipmentId, shipmentSpecimenId: String)
      : ServiceValidation[SpecimenDto]

  def getShipmentSpecimen(requestUserId:      UserId,
                          shipmentId:         ShipmentId,
                          shipmentSpecimenId: String): ServiceValidation[ShipmentSpecimenDto]

  def processCommand(cmd: ShipmentCommand): Future[ServiceValidation[ShipmentDto]]

  def removeShipment(cmd: ShipmentRemoveCmd): Future[ServiceValidation[Boolean]]

  def processShipmentSpecimenCommand(cmd: ShipmentSpecimenCommand):
      Future[ServiceValidation[ShipmentDto]]

  def removeShipmentSpecimen(cmd: ShipmentSpecimenRemoveCmd): Future[ServiceValidation[Boolean]]

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

/**
 * Handles all commands dealing with shipments, shipment specimens, and shipment containers.
 */
class ShipmentsServiceImpl @Inject() (@Named("shipmentsProcessor") val   processor: ActorRef,
                                      val accessService:                 AccessService,
                                      val centreRepository:              CentreRepository,
                                      val shipmentRepository:            ShipmentRepository,
                                      val shipmentSpecimenRepository:    ShipmentSpecimenRepository,
                                      val specimenRepository:            SpecimenRepository,
                                      val ceventSpecimenRepository:      CeventSpecimenRepository,
                                      val collectionEventRepository:     CollectionEventRepository,
                                      val collectionEventTypeRepository: CollectionEventTypeRepository,
                                      val specimensService:              SpecimensService,
                                      val shipmentFilter:                ShipmentFilter)
    extends ShipmentsService
    with AccessChecksSerivce
    with CentreServicePermissionChecks
    with ShipmentConstraints {

  import org.biobank.CommonValidations._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def getShipment(requestUserId: UserId, id: ShipmentId): ServiceValidation[ShipmentDto] = {
    whenShipmentPermitted(requestUserId, id) { shipment =>
      getShipmentDto(shipment)
    }
  }

  /**
   * See:
   *
   * - http://stackoverflow.com/questions/17791933/filter-over-list-with-dynamic-filter-parameter
   *
   * - http://danielwestheide.com/blog/2013/01/23/the-neophytes-guide-to-scala-part-10-staying-dry-with-higher-order-functions.html
   *
   */
  def getShipments(requestUserId: UserId, filter: FilterString, sort: SortString):
      ServiceValidation[List[ShipmentDto]] = {
    withPermittedCentres(requestUserId) { centres =>
      val shipments = centres
        .map { centre => shipmentRepository.withCentre(centre.id) }
        .toList.sequenceU.toSet

      val filteredShipments = shipmentFilter.filterShipments(shipments, filter)
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
  }

  def shipmentCanAddSpecimen(requestUserId: UserId, shipmentId: ShipmentId, specimenInventoryId: String)
      : ServiceValidation[SpecimenDto] = {
    whenShipmentPermitted(requestUserId, shipmentId) { shipment =>
      for {
        specimen     <- specimenRepository.getByInventoryId(specimenInventoryId)
        sameLocation <- {
          if (shipment.fromLocationId == specimen.locationId) {
            specimen.successNel[ServiceError]
          } else {
            ServiceError(s"specimen not at shipment's from location").failureNel[Specimen]
          }
        }
        canBeAdded   <- specimensNotPresentInShipment(specimen)
        specimenDto  <- specimensService.get(requestUserId, specimen.id)
      } yield specimenDto
    }
  }

  def getShipmentSpecimens(requestUserId: UserId,
                           shipmentId:    ShipmentId,
                           filter:        FilterString,
                           sort:          SortString): ServiceValidation[List[ShipmentSpecimenDto]] = {
    whenShipmentPermitted(requestUserId, shipmentId) { shipment =>
      val shipmentSpecimens = shipmentSpecimenRepository.allForShipment(shipment.id)
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
        shipmentSpecimenss <- {
          filteredShipmentSpecimens.flatMap(
            _.map(s => getShipmentSpecimenDto(requestUserId, s)).toList.sequenceU)
        }
      } yield {
        val result = shipmentSpecimenss.sortWith(sortFunc)
        if (sortExpressions(0).order == AscendingOrder) result
        else result.reverse
      }
    }
  }

  def getShipmentSpecimen(requestUserId: UserId, shipmentId: ShipmentId, shipmentSpecimenId: String)
      : ServiceValidation[ShipmentSpecimenDto] = {
    whenShipmentPermitted(requestUserId, shipmentId) { shipment =>
      for {
        ss  <- shipmentSpecimenRepository.getByKey(ShipmentSpecimenId(shipmentSpecimenId))
        dto <- getShipmentSpecimenDto(requestUserId, ss)
      } yield dto
    }
  }

  def processCommand(cmd: ShipmentCommand): Future[ServiceValidation[ShipmentDto]] = {
    val validCommand = cmd match {
        case c: ShipmentRemoveCmd =>
          ServiceError(s"invalid service call: $cmd, use removeShipment").failureNel[ShipmentDto]
        case c => c.successNel[String]
      }

    validCommand.fold(
      err => Future.successful(err.failure[ShipmentDto]),
      _   => whenShipmentPermittedAsync(cmd) { () =>
        ask(processor, cmd).mapTo[ServiceValidation[ShipmentEvent]].map { validation =>
          for {
            event    <- validation
            shipment <- shipmentRepository.getByKey(ShipmentId(event.id))
            dto      <- getShipmentDto(shipment)
          } yield dto
        }
      }
    )
  }

  def removeShipment(cmd: ShipmentRemoveCmd): Future[ServiceValidation[Boolean]] = {
    whenShipmentPermittedAsync(cmd) { () =>
      ask(processor, cmd).mapTo[ServiceValidation[ShipmentEvent]].map { validation =>
        validation.map(_ => true)
      }
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

  //
  // Invokes function "block" if user that invoked this service has the permission and membership
  // to do so.
  //
  private def whenShipmentPermitted[T](requestUserId: UserId, shipmentId: ShipmentId)
                                   (block: Shipment => ServiceValidation[T]): ServiceValidation[T] = {
    for {
      shipment   <- shipmentRepository.getByKey(shipmentId)
      fromCentre <- centreRepository.getByLocationId(shipment.fromLocationId)
      toCentre   <- centreRepository.getByLocationId(shipment.toLocationId)
      isMember   <- accessService.isMember(requestUserId, None, Some(fromCentre.id)).fold(
        err      => err.failure[Boolean],
        isMember => if (isMember) true.successNel[String]
                    else Unauthorized.failureNel[Boolean]
      )
      result     <- whenPermittedAndIsMember(requestUserId,
                                             PermissionId.ShipmentRead,
                                             None,
                                             Some(toCentre.id))(() => block(shipment))
    } yield result
  }

  case class ShipmentCentreIds(fromId: CentreId, toId: CentreId)

  //
  // Invokes function "block" if user that issued the command has the permission and membership
  // to do so.
  //
  private def whenShipmentPermittedAsync[T](cmd: ShipmentCommand)
                                           (block: () => Future[ServiceValidation[T]])
      : Future[ServiceValidation[T]] = {

    val sessionUserId = UserId(cmd.sessionUserId)
    val validCentreIds = cmd match {
        case c: ShipmentModifyCommand => {
          for {
            shipment   <- shipmentRepository.getByKey(ShipmentId(c.id))
            fromCentre <- centreRepository.getByKey(shipment.fromCentreId)
            toCentre   <- centreRepository.getByKey(shipment.toCentreId)
          } yield ShipmentCentreIds(fromCentre.id, toCentre.id)
        }

        case c: AddShipmentCmd =>
          for {
            fromCentre <- centreRepository.getByLocationId(LocationId(c.fromLocationId))
            toCentre   <- centreRepository.getByLocationId(LocationId(c.toLocationId))
          } yield ShipmentCentreIds(fromCentre.id, toCentre.id)
      }

    val isMemberOfCentres = for {
        centreIds  <- validCentreIds
        fromMember <- accessService.isMember(sessionUserId, None, Some(centreIds.fromId)).fold(
          err      => err.failure[Boolean],
          isMember => if (isMember) true.successNel[String]
                      else Unauthorized.failureNel[Boolean]
        )
        toMember   <- accessService.isMember(sessionUserId, None, Some(centreIds.toId)).fold(
          err      => err.failure[Boolean],
          isMember => if (isMember) true.successNel[String]
                      else Unauthorized.failureNel[Boolean]
        )
      } yield toMember

    val permission = cmd match {
        case c: AddShipmentCmd    => PermissionId.ShipmentCreate
        case c: ShipmentRemoveCmd => PermissionId.ShipmentDelete
        case c                    => PermissionId.ShipmentUpdate
      }

    isMemberOfCentres.fold(
      err    => Future.successful(err.failure[T]),
      member => whenPermittedAsync(sessionUserId, permission)(block)
    )
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

  private def getShipmentSpecimenDto(requestUserId: UserId, shipmentSpecimen: ShipmentSpecimen)
      : ServiceValidation[ShipmentSpecimenDto] = {
    specimensService.get(requestUserId, shipmentSpecimen.specimenId).map { specimenDto =>
      shipmentSpecimen.createDto(specimenDto)
    }
  }
}
