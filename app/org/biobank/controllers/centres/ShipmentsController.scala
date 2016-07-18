package org.biobank.controllers.centres

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
  import org.biobank.infrastructure.command.ShipmentSpecimenCommands._

  private val PageSizeMax = 10

  def list(courierFilterMaybe:        Option[String],
           trackingNumberFilterMaybe: Option[String],
           stateFilterMaybe:          Option[String],
           sortMaybe:                 Option[String],
           pageMaybe:                 Option[Int],
           pageSizeMaybe:             Option[Int],
           orderMaybe:                Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>

      val courierFilter        = courierFilterMaybe.fold { "" } { cn        => cn }
      val trackingNumberFilter = trackingNumberFilterMaybe.fold { "" } { tn => tn }
      val stateFilter          = stateFilterMaybe.fold { "" } { st => st }
      val sort                 = sortMaybe.fold { "courierName" } { s       => s }
      val page                 = pageMaybe.fold { 1 } { p                   => p }
      val pageSize             = pageSizeMaybe.fold { 5 } { ps              => ps }
      val order                = orderMaybe.fold { "asc" } { o              => o }

      Logger.debug(
        s"""|ShipmentsController:list: courierFilter/$courierFilter, trackingNumberFilter/$trackingNumberFilter,
            |  stateFilter/$stateFilter, sort/$sort, page/$page, pageSize/$pageSize, order/$order""".stripMargin)

      val pagedQuery = PagedQuery(page, pageSize, order)

      val validation = for {
          sortOrder   <- pagedQuery.getSortOrder
          shipments   <- shipmentsService.getShipments(courierFilter,
                                                       trackingNumberFilter,
                                                       stateFilter,
                                                       sort,
                                                       sortOrder)
          page        <- pagedQuery.getPage(PageSizeMax, shipments.size)
          pageSize    <- pagedQuery.getPageSize(PageSizeMax)
          results     <- PagedResults.create(shipments, page, pageSize)
        } yield results

      validation.fold(
        err =>     BadRequest(err.toList.mkString),
        results => Ok(results)
      )
    }

  def get(id: String) = AuthAction(parse.empty) { (token, userId, request) =>
    domainValidationReply(shipmentsService.getShipment(id))
  }

  def listSpecimens(shipmentId:      String,
                    sortMaybe:       Option[String],
                    pageMaybe:       Option[Int],
                    pageSizeMaybe:   Option[Int],
                    orderMaybe:      Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>

      val sort     = sortMaybe.fold { "inventoryId" } { s => s }
      val page     = pageMaybe.fold { 1 } { p => p }
      val pageSize = pageSizeMaybe.fold { 5 } { ps => ps }
      val order    = orderMaybe.fold { "asc" } { o => o }

      Logger.debug(s"""|ShipmentsController:listSpecimens: shipmentId/$shipmentId, sort/$sort,
                       |  page/$page,pageSize/$pageSize, order/$order""".stripMargin)

      val pagedQuery = PagedQuery(page, pageSize, order)

      val validation = for {
          sortOrder         <- pagedQuery.getSortOrder
          shipmentSpecimens <- shipmentsService.getShipmentSpecimens(shipmentId, sort, sortOrder)
          page              <- pagedQuery.getPage(PageSizeMax, shipmentSpecimens.size)
          pageSize          <- pagedQuery.getPageSize(PageSizeMax)
          results           <- PagedResults.create(shipmentSpecimens, page, pageSize)
        } yield results

      validation.fold(
        err =>     BadRequest(err.list.toList.mkString),
        results => Ok(results)
      )
    }

  def getSpecimen(shipmentId: String, shipmentSpecimenId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      domainValidationReply(shipmentsService.getShipmentSpecimen(shipmentId, shipmentSpecimenId))
    }

  def add() = commandAction { cmd: AddShipmentCmd => processCommand(cmd) }

  def remove(shipmentId: String, version: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = ShipmentRemoveCmd(userId          = userId.id,
                                  id              = shipmentId,
                                  expectedVersion = version)
      val future = shipmentsService.removeShipment(cmd)
      domainValidationReply(future)
    }

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

  def addSpecimen(shipmentId: String) = commandAction(Json.obj("shipmentId" -> shipmentId)) {
      cmd: ShipmentSpecimenAddCmd => processSpecimenCommand(cmd)
    }

  def removeSpecimen(shipmentId: String, shipmentSpecimenId: String, version: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = ShipmentSpecimenRemoveCmd(userId          = userId.id,
                                          shipmentId      = shipmentId,
                                          id              = shipmentSpecimenId,
                                          expectedVersion = version)
      val future = shipmentsService.removeShipmentSpecimen(cmd)
      domainValidationReply(future)
    }

  def specimenContainer(shipmentId: String, shipmentSpecimenId: String) =
    commandAction(Json.obj("shipmentId" -> shipmentId, "id" -> shipmentSpecimenId)) {
      cmd: ShipmentSpecimenUpdateContainerCmd => processSpecimenCommand(cmd)
    }

  def specimenReceived(shipmentId: String, shipmentSpecimenId: String) =
    commandAction(Json.obj("shipmentId" -> shipmentId, "id" -> shipmentSpecimenId)) {
      cmd: ShipmentSpecimenReceivedCmd => processSpecimenCommand(cmd)
    }

  def specimenMissing(shipmentId: String, shipmentSpecimenId: String) =
    commandAction(Json.obj("shipmentId" -> shipmentId, "id" -> shipmentSpecimenId)) {
      cmd: ShipmentSpecimenMissingCmd => processSpecimenCommand(cmd)
    }

  def specimenExtra(shipmentId: String, shipmentSpecimenId: String) =
    commandAction(Json.obj("shipmentId" -> shipmentId, "id" -> shipmentSpecimenId)) {
      cmd: ShipmentSpecimenExtraCmd => processSpecimenCommand(cmd)
    }

  private def processCommand(cmd: ShipmentCommand) = {
    val future = shipmentsService.processCommand(cmd)
    domainValidationReply(future)
  }

  private def processSpecimenCommand(cmd: ShipmentSpecimenCommand) = {
    val future = shipmentsService.processShipmentSpecimenCommand(cmd)
    domainValidationReply(future)
  }
}
