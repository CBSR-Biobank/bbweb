package org.biobank.controllers.centres

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.{Environment, Logger}
import play.api.mvc._
import org.biobank.controllers.{BbwebAction, CommandController, JsonController, PagedQuery}
import org.biobank.domain.centre.{CentreId, ShipmentId}
import org.biobank.service.centres.ShipmentsService
import org.biobank.service.users.UsersService
import org.biobank.service.{AuthToken, PagedResults}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
 */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
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

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 10

  def list(centreId: CentreId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            pagedQuery <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            shipments  <- shipmentsService.getShipments(centreId, pagedQuery.filter, pagedQuery.sort)
            validPage  <- pagedQuery.validPage(shipments.size)
            results    <- PagedResults.create(shipments, pagedQuery.page, pagedQuery.limit)
          } yield results
        }
      )
    }

  def get(id: ShipmentId): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(shipmentsService.getShipment(id))
    }

  def listSpecimens(shipmentId: ShipmentId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            pagedQuery        <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            shipmentSpecimens <- shipmentsService.getShipmentSpecimens(shipmentId,
                                                                       pagedQuery.filter,
                                                                       pagedQuery.sort)
            validPage         <- pagedQuery.validPage(shipmentSpecimens.size)
            results           <- PagedResults.create(shipmentSpecimens,
                                                     pagedQuery.page,
                                                     pagedQuery.limit)
          } yield results
        }
      )
    }

  def canAddSpecimens(shipmentId: ShipmentId, specimenInventoryId: String): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(shipmentsService.shipmentCanAddSpecimen(shipmentId, specimenInventoryId))
    }

  def getSpecimen(shipmentId: ShipmentId, shipmentSpecimenId: String): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(shipmentsService.getShipmentSpecimen(shipmentId, shipmentSpecimenId))
    }

  def add(): Action[JsValue] = commandActionAsync { cmd: AddShipmentCmd => processCommand(cmd) }

  def remove(shipmentId: ShipmentId, version: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = ShipmentRemoveCmd(userId          = request.authInfo.userId.id,
                                  id              = shipmentId.id,
                                  expectedVersion = version)
      val future = shipmentsService.removeShipment(cmd)
      validationReply(future)
    }

  def updateCourier(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateShipmentCourierNameCmd => processCommand(cmd) }

  def updateTrackingNumber(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateShipmentTrackingNumberCmd => processCommand(cmd) }

  def updateFromLocation(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateShipmentFromLocationCmd => processCommand(cmd) }

  def updateToLocation(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateShipmentToLocationCmd => processCommand(cmd) }

  def created(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : CreatedShipmentCmd => processCommand(cmd) }

  def packed(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : PackShipmentCmd => processCommand(cmd) }

  def sent(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : SendShipmentCmd => processCommand(cmd) }

  def received(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : ReceiveShipmentCmd => processCommand(cmd) }

  def unpacked(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UnpackShipmentCmd => processCommand(cmd) }

  def lost(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : LostShipmentCmd => processCommand(cmd) }

  /**
   * Changes the state of a shipment from CREATED to SENT (skipping the PACKED state)
   */
  def skipStateSent(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : ShipmentSkipStateToSentCmd => processCommand(cmd) }

  /**
   * Changes the state of a shipment from SENT to UNPACKED (skipping the RECEVIED state)
   */
  def skipStateUnpacked(id: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : ShipmentSkipStateToUnpackedCmd => processCommand(cmd) }

  def addSpecimen(shipmentId: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("shipmentId" -> shipmentId)) {
      cmd: ShipmentAddSpecimensCmd => processSpecimenCommand(cmd)
    }

  def removeSpecimen(shipmentId: ShipmentId, shipmentSpecimenId: String, version: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = ShipmentSpecimenRemoveCmd(userId             = request.authInfo.userId.id,
                                          shipmentId         = shipmentId.id,
                                          expectedVersion    = version,
                                          shipmentSpecimenId = shipmentSpecimenId)
      val future = shipmentsService.removeShipmentSpecimen(cmd)
      validationReply(future)
    }

  def specimenContainer(shipmentId: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("shipmentId" -> shipmentId)) {
      cmd: ShipmentSpecimenUpdateContainerCmd => processSpecimenCommand(cmd)
    }

  def specimenPresent(shipmentId: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("shipmentId" -> shipmentId)) {
      cmd: ShipmentSpecimensPresentCmd => processSpecimenCommand(cmd)
    }

  def specimenReceived(shipmentId: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("shipmentId" -> shipmentId)) {
      cmd: ShipmentSpecimensReceiveCmd => processSpecimenCommand(cmd)
    }

  def specimenMissing(shipmentId: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("shipmentId" -> shipmentId)) {
      cmd: ShipmentSpecimenMissingCmd => processSpecimenCommand(cmd)
    }

  def specimenExtra(shipmentId: ShipmentId): Action[JsValue] =
    commandActionAsync(Json.obj("shipmentId" -> shipmentId)) {
      cmd: ShipmentSpecimenExtraCmd => processSpecimenCommand(cmd)
    }

  private def processCommand(cmd: ShipmentCommand): Future[Result] = {
    val future = shipmentsService.processCommand(cmd)
    validationReply(future)
  }

  private def processSpecimenCommand(cmd: ShipmentSpecimenCommand): Future[Result] = {
    val future = shipmentsService.processShipmentSpecimenCommand(cmd)
    validationReply(future)
  }
}
