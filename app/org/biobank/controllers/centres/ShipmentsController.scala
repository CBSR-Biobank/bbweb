package org.biobank.controllers.centres

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.{Environment, Logger}
import org.biobank.controllers.{BbwebAction, CommandController, JsonController, Pagination}
import org.biobank.domain.centre.{CentreId, ShipmentId}
import org.biobank.service.centres.ShipmentsService
import org.biobank.service.users.UsersService
import org.biobank.service.{AuthToken, PagedQuery, PagedResults}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.reflectiveCalls
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
 */
@Singleton
class ShipmentsController @Inject() (val action:           BbwebAction,
                                     val env:              Environment,
                                     val authToken:        AuthToken,
                                     val usersService:     UsersService,
                                     val shipmentsService: ShipmentsService)
                                 (implicit ec: ExecutionContext)
    extends CommandController
    with JsonController {

  import org.biobank.infrastructure.command.ShipmentCommands._
  import org.biobank.infrastructure.command.ShipmentSpecimenCommands._

  val log = Logger(this.getClass)

  private val PageSizeMax = 10

  def list(centreId: CentreId) =
    action.async(parse.empty) { implicit request =>
      val pagination = Pagination("", "courierName", 1, 5)
      log.info(s"--------> ${request.rawQueryString}")
      Future {
        val pagedQuery = PagedQuery(pagination.page, pagination.limit, pagination.sort)

        val validation = for {
            shipments        <- shipmentsService.getShipments(centreId, pagination.filter, pagination.sort)
            page             <- pagedQuery.getPage(PageSizeMax, shipments.size)
            limit            <- pagedQuery.getPageSize(PageSizeMax)
            results          <- PagedResults.create(shipments, page, limit)
          } yield results

        validation.fold(
          err =>     BadRequest(err.toList.mkString),
          results => Ok(results)
        )
      }
    }

  def get(id: ShipmentId) =
    action(parse.empty) { implicit request =>
      validationReply(shipmentsService.getShipment(id))
    }

  def listSpecimens(shipmentId:       ShipmentId,
                    stateFilterMaybe: Option[String],
                    sortMaybe:        Option[String],
                    pageMaybe:        Option[Int],
                    limitMaybe:       Option[Int],
                    orderMaybe:       Option[String]) =
    action.async(parse.empty) { implicit request =>
      Future {
        val stateFilter = stateFilterMaybe.fold { "" } { s => s }
        val sort        = sortMaybe.fold { "inventoryId" } { s => s }
        val page        = pageMaybe.fold { 1 } { p => p }
        val limit    = limitMaybe.fold { 5 } { ps => ps }
        val order       = orderMaybe.fold { "asc" } { o => o }

        log.debug(s"""|ShipmentsController:listSpecimens:
                      | shipmentId:  $shipmentId,
                      | stateFilter: $stateFilter,
                      | sort:        $sort,
                      | page:        $page,
                      | limit:    $limit,
                      | order:       $order""".stripMargin)

        val pagedQuery = PagedQuery(page, limit, order)

        val validation = for {
            sortOrder         <- pagedQuery.getSortOrder
            shipmentSpecimens <- shipmentsService.getShipmentSpecimens(shipmentId, stateFilter, sort, sortOrder)
            page              <- pagedQuery.getPage(PageSizeMax, shipmentSpecimens.size)
            limit          <- pagedQuery.getPageSize(PageSizeMax)
            results           <- PagedResults.create(shipmentSpecimens, page, limit)
          } yield results

        validation.fold(
          err =>     BadRequest(err.list.toList.mkString),
          results => Ok(results)
        )
      }
    }

  def canAddSpecimen(shipmentId: ShipmentId, specimenInventoryId: String) =
    action(parse.empty) { implicit request =>
      validationReply(shipmentsService.shipmentCanAddSpecimen(shipmentId, specimenInventoryId))
    }

  def getSpecimen(shipmentId: ShipmentId, shipmentSpecimenId: String) =
    action(parse.empty) { implicit request =>
      validationReply(shipmentsService.getShipmentSpecimen(shipmentId, shipmentSpecimenId))
    }

  def add() = commandActionAsync { cmd: AddShipmentCmd => processCommand(cmd) }

  def remove(shipmentId: ShipmentId, version: Long) =
    action.async(parse.empty) { implicit request =>
      val cmd = ShipmentRemoveCmd(userId          = request.authInfo.userId.id,
                                  id              = shipmentId.id,
                                  expectedVersion = version)
      val future = shipmentsService.removeShipment(cmd)
      validationReply(future)
    }

  def updateCourier(id: ShipmentId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateShipmentCourierNameCmd => processCommand(cmd) }

  def updateTrackingNumber(id: ShipmentId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateShipmentTrackingNumberCmd => processCommand(cmd) }

  def updateFromLocation(id: ShipmentId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateShipmentFromLocationCmd => processCommand(cmd) }

  def updateToLocation(id: ShipmentId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateShipmentToLocationCmd => processCommand(cmd) }

  /**
   * Changes the state of a shipment from CREATED to SENT (skipping the PACKED state)
   */
  def skipStateSent(id: ShipmentId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : ShipmentSkipStateToSentCmd => processCommand(cmd) }

  /**
   * Changes the state of a shipment from SENT to UNPACKED (skipping the RECEVIED state)
   */
  def skipStateUnpacked(id: ShipmentId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : ShipmentSkipStateToUnpackedCmd => processCommand(cmd) }

  def changeState(id: ShipmentId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : ShipmentChangeStateCmd => processCommand(cmd) }

  def addSpecimen(shipmentId: ShipmentId) = commandActionAsync(Json.obj("shipmentId" -> shipmentId)) {
      cmd: ShipmentSpecimenAddCmd => processSpecimenCommand(cmd)
    }

  def removeSpecimen(shipmentId: ShipmentId, shipmentSpecimenId: String, version: Long) =
    action.async(parse.empty) { implicit request =>
      val cmd = ShipmentSpecimenRemoveCmd(userId          = request.authInfo.userId.id,
                                          shipmentId      = shipmentId.id,
                                          id              = shipmentSpecimenId,
                                          expectedVersion = version)
      val future = shipmentsService.removeShipmentSpecimen(cmd)
      validationReply(future)
    }

  def specimenContainer(shipmentId: ShipmentId, shipmentSpecimenId: String) =
    commandActionAsync(Json.obj("shipmentId" -> shipmentId, "id" -> shipmentSpecimenId)) {
      cmd: ShipmentSpecimenUpdateContainerCmd => processSpecimenCommand(cmd)
    }

  def specimenReceived(shipmentId: ShipmentId, shipmentSpecimenId: String) =
    commandActionAsync(Json.obj("shipmentId" -> shipmentId, "id" -> shipmentSpecimenId)) {
      cmd: ShipmentSpecimenReceivedCmd => processSpecimenCommand(cmd)
    }

  def specimenMissing(shipmentId: ShipmentId, shipmentSpecimenId: String) =
    commandActionAsync(Json.obj("shipmentId" -> shipmentId, "id" -> shipmentSpecimenId)) {
      cmd: ShipmentSpecimenMissingCmd => processSpecimenCommand(cmd)
    }

  def specimenExtra(shipmentId: ShipmentId, shipmentSpecimenId: String) =
    commandActionAsync(Json.obj("shipmentId" -> shipmentId, "id" -> shipmentSpecimenId)) {
      cmd: ShipmentSpecimenExtraCmd => processSpecimenCommand(cmd)
    }

  private def processCommand(cmd: ShipmentCommand) = {
    val future = shipmentsService.processCommand(cmd)
    validationReply(future)
  }

  private def processSpecimenCommand(cmd: ShipmentSpecimenCommand) = {
    val future = shipmentsService.processShipmentSpecimenCommand(cmd)
    validationReply(future)
  }
}
