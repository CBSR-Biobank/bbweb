package org.biobank.controllers.centres

import org.biobank.domain.centre.Shipment
import org.biobank.controllers.{CommandController, JsonController, PagedQuery, PagedResults}
import javax.inject.{Inject, Singleton}
import org.biobank.service.AuthToken
import org.biobank.service.centres.ShipmentsService
import org.biobank.service.users.UsersService
import play.api.{Environment, Logger}
import play.api.libs.json._
import scala.language.reflectiveCalls
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
 */
@Singleton
class ShipmentsController @Inject() (val env:              Environment,
                                     val authToken:        AuthToken,
                                     val usersService:     UsersService,
                                     val shipmentsService: ShipmentsService)
    extends CommandController
    with JsonController {

  import org.biobank.infrastructure.command.ShipmentCommands._

  private val PageSizeMax = 10

  val listSortFields = Map[String, (Shipment, Shipment) => Boolean](
      "courierName"    -> Shipment.compareByCourier,
      "trackingNumber" -> Shipment.compareByTrackingNumber,
      "timePacked"     -> Shipment.compareByTimePacked,
      "timeSent"       -> Shipment.compareByTimeSent,
      "timeReceived"   -> Shipment.compareByTimeReceived,
      "timeUnpacked"   -> Shipment.compareByTimeUnpacked)

  def list(courierFilter:        String,
           trackingNumberFilter: String,
           sort:                 String,
           page:                 Int,
           pageSize:             Int,
           order:                String) =
    AuthAction(parse.empty) { (token, userId, request) =>

      Logger.debug(s"""|ShipmentsController:list: courierFilter/$courierFilter,
                       |  trackingNumberFilter/$trackingNumberFilter, sort/$sort, page/$page,
                       |  pageSize/$pageSize, order/$order""".stripMargin)

      val pagedQuery = PagedQuery(listSortFields, page, pageSize, order)

      val validation = for {
          sortWith    <- pagedQuery.getSortFunc(sort)
          sortOrder   <- pagedQuery.getSortOrder
          shipments   <- shipmentsService.getShipments(courierFilter,
                                                       trackingNumberFilter,
                                                       sortWith,
                                                       sortOrder).success
          page        <- pagedQuery.getPage(PageSizeMax, shipments.size)
          pageSize    <- pagedQuery.getPageSize(PageSizeMax)
          results     <- PagedResults.create(shipments, page, pageSize)
        } yield results

      validation.fold(
        err =>      BadRequest(err.list.toList.mkString),
        results =>  Ok(results)
      )
    }

  def get(id: String) = AuthAction(parse.empty) { (token, userId, request) =>
    domainValidationReply(shipmentsService.getShipment(id))
  }

  def add() = commandAction { cmd: AddShipmentCmd => processCommand(cmd) }

  def updateCourier(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : UpdateShipmentCourierNameCmd => processCommand(cmd) }

  def updateTrackingNumber(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : UpdateShipmentTrackingNumberCmd => processCommand(cmd) }

  def updateFromLocation(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : UpdateShipmentFromLocationCmd => processCommand(cmd) }

  def updateToLocation(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : UpdateShipmentToLocationCmd => processCommand(cmd) }

  def packed(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : ShipmentPackedCmd => processCommand(cmd) }

  def sent(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : ShipmentSentCmd => processCommand(cmd) }

  def received(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : ShipmentReceivedCmd => processCommand(cmd) }

  def unpacked(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : ShipmentUnpackedCmd => processCommand(cmd) }

  def lost(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : ShipmentLostCmd => processCommand(cmd) }

  private def processCommand(cmd: ShipmentCommand) = {
    val future = shipmentsService.processCommand(cmd)
    domainValidationReply(future)
  }
}
