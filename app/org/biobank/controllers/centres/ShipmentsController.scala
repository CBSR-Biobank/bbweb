package org.biobank.controllers.centres

import javax.inject.{Inject, Singleton}

import play.api.libs.json._
import play.api.{Environment, Logger}
import play.api.mvc._
import org.biobank.controllers._
import org.biobank.domain.centres.{CentreId, ShipmentId, ShipmentSpecimenId}
import org.biobank.dto.ShipmentDto
import org.biobank.services.{FilterString, ServiceValidation, SortString, PagedResults}
import org.biobank.services.centres.{CentresService, ShipmentsService}
import org.biobank.services.participants.SpecimensService
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._

/**
 *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
 */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class ShipmentsController @Inject() (controllerComponents: ControllerComponents,
                                     val action:           BbwebAction,
                                     val env:              Environment,
                                     val centresService:   CentresService,
                                     val shipmentsService: ShipmentsService,
                                     val specimensService: SpecimensService)
                                 (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  import org.biobank.infrastructure.commands.ShipmentCommands._
  import org.biobank.infrastructure.commands.ShipmentSpecimenCommands._

  type ServiceListFunc = (CentreId, FilterString, SortString) => ServiceValidation[List[ShipmentDto]]

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 10

  def get(id: ShipmentId): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(shipmentsService.getShipment(request.authInfo.userId, id))
    }

  def list: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[ShipmentDto]]))
        },
        pagedQuery => {
          validationReply(shipmentsService.getShipments(request.authInfo.userId, pagedQuery))
        }
      )
    }

  def listSpecimens(shipmentId: ShipmentId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[ShipmentDto]]))
        },
        pagedQuery => {
          validationReply(shipmentsService.getShipmentSpecimens(request.authInfo.userId,
                                                                shipmentId,
                                                                pagedQuery))
        }
      )
    }

  def canAddSpecimens(shipmentId: ShipmentId, specimenInventoryId: String): Action[Unit] =
    action(parse.empty) { request =>
      val v = shipmentsService.shipmentCanAddSpecimen(request.authInfo.userId,
                                                      shipmentId,
                                                      specimenInventoryId)
        .map { specimen => true }
      validationReply(v)
    }

  def getSpecimen(shipmentId: ShipmentId, shipmentSpecimenId: String): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(shipmentsService.getShipmentSpecimen(request.authInfo.userId,
                                                           shipmentId,
                                                           ShipmentSpecimenId(shipmentSpecimenId)))
    }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(shipmentsService.snapshotRequest(request.authInfo.userId).map { _ => true })
    }

  def add(): Action[JsValue] = commandAction[AddShipmentCmd](JsNull)(processCommand)

  def remove(shipmentId: ShipmentId, version: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = ShipmentRemoveCmd(sessionUserId   = request.authInfo.userId.id,
                                  id              = shipmentId.id,
                                  expectedVersion = version)
      val future = shipmentsService.removeShipment(cmd)
      validationReply(future)
    }

  def updateCourier(id: ShipmentId): Action[JsValue] =
    commandAction[UpdateShipmentCourierNameCmd](Json.obj("id" -> id))(processCommand)

  def updateTrackingNumber(id: ShipmentId): Action[JsValue] =
    commandAction[UpdateShipmentTrackingNumberCmd](Json.obj("id" -> id))(processCommand)

  def updateFromLocation(id: ShipmentId): Action[JsValue] =
    commandAction[UpdateShipmentFromLocationCmd](Json.obj("id" -> id))(processCommand)

  def updateToLocation(id: ShipmentId): Action[JsValue] =
    commandAction[UpdateShipmentToLocationCmd](Json.obj("id" -> id))(processCommand)

  def created(id: ShipmentId): Action[JsValue] =
    commandAction[CreatedShipmentCmd](Json.obj("id" -> id))(processCommand)

  def packed(id: ShipmentId): Action[JsValue] =
    commandAction[PackShipmentCmd](Json.obj("id" -> id))(processCommand)

  def sent(id: ShipmentId): Action[JsValue] =
    commandAction[SendShipmentCmd](Json.obj("id" -> id))(processCommand)

  def received(id: ShipmentId): Action[JsValue] =
    commandAction[ReceiveShipmentCmd](Json.obj("id" -> id))(processCommand)

  def unpacked(id: ShipmentId): Action[JsValue] =
    commandAction[UnpackShipmentCmd](Json.obj("id" -> id))(processCommand)

  def completed(id: ShipmentId): Action[JsValue] =
    commandAction[CompleteShipmentCmd](Json.obj("id" -> id))(processCommand)

  def lost(id: ShipmentId): Action[JsValue] =
    commandAction[LostShipmentCmd](Json.obj("id" -> id))(processCommand)

  /**
   * Changes the state of a shipment from CREATED to SENT (skipping the PACKED state)
   */
  def skipStateSent(id: ShipmentId): Action[JsValue] =
    commandAction[ShipmentSkipStateToSentCmd](Json.obj("id" -> id))(processCommand)

  /**
   * Changes the state of a shipment from SENT to UNPACKED (skipping the RECEVIED state)
   */
  def skipStateUnpacked(id: ShipmentId): Action[JsValue] =
    commandAction[ShipmentSkipStateToUnpackedCmd](Json.obj("id" -> id))(processCommand)

  def addSpecimen(shipmentId: ShipmentId): Action[JsValue] =
    commandAction[ShipmentAddSpecimensCmd](Json.obj("shipmentId" -> shipmentId))(processSpecimenCommand)

  def removeSpecimen(shipmentId: ShipmentId, shipmentSpecimenId: String, version: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = ShipmentSpecimenRemoveCmd(sessionUserId      = request.authInfo.userId.id,
                                          shipmentId         = shipmentId.id,
                                          expectedVersion    = version,
                                          shipmentSpecimenId = shipmentSpecimenId)
      processSpecimenCommand(cmd)
    }

  def specimenContainer(shipmentId: ShipmentId): Action[JsValue] =
    commandAction[ShipmentSpecimenUpdateContainerCmd](Json.obj("shipmentId" -> shipmentId))(processSpecimenCommand)

  def specimenPresent(shipmentId: ShipmentId): Action[JsValue] =
    commandAction[ShipmentSpecimensPresentCmd](Json.obj("shipmentId" -> shipmentId))(processSpecimenCommand)

  def specimensReceived(shipmentId: ShipmentId): Action[JsValue] =
    commandAction[ShipmentSpecimensReceiveCmd](Json.obj("shipmentId" -> shipmentId))(processSpecimenCommand)

  def specimensMissing(shipmentId: ShipmentId): Action[JsValue] =
    commandAction[ShipmentSpecimenMissingCmd](Json.obj("shipmentId" -> shipmentId))(processSpecimenCommand)

  def specimensExtra(shipmentId: ShipmentId): Action[JsValue] =
    commandAction[ShipmentSpecimenExtraCmd](Json.obj("shipmentId" -> shipmentId))(processSpecimenCommand)

  private def processCommand(cmd: ShipmentCommand): Future[Result] = {
    val future = shipmentsService.processCommand(cmd)
    validationReply(future)
  }

  private def processSpecimenCommand(cmd: ShipmentSpecimenCommand): Future[Result] = {
    val future = shipmentsService.processShipmentSpecimenCommand(cmd)
    validationReply(future)
  }
}
