package org.biobank.controllers.centres

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.centre.Centre
import org.biobank.domain.centre.CentreId
import org.biobank.infrastructure.SortOrder
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.service._
import org.biobank.service.centres.CentresService
import org.biobank.service.users.UsersService
import play.api.libs.json._
import play.api.mvc._
import play.api.{ Environment, Logger }
import scala.concurrent.{ExecutionContext, Future}
import scala.language.reflectiveCalls
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
 */
@Singleton
class CentresController @Inject() (val action:         BbwebAction,
                                   val env:            Environment,
                                   val authToken:      AuthToken,
                                   val usersService:   UsersService,
                                   val centresService: CentresService)
                               (implicit ec: ExecutionContext)
    extends CommandController
    with JsonController {

  val log = Logger(this.getClass)

  private val PageSizeMax = 10

  def centreCounts(): Action[Unit] =
    action(parse.empty) { implicit request =>
      Ok(centresService.getCountsByStatus)
    }

  def list(filterMaybe:   Option[String],
           statusMaybe:   Option[String],
           sortMaybe:     Option[String],
           pageMaybe:     Option[Int],
           pageSizeMaybe: Option[Int],
           orderMaybe:    Option[String]): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      Future {
        val filter   = filterMaybe.fold { "" } { f => f }
        val status   = statusMaybe.fold { "all" } { s => s }
        val sort     = sortMaybe.fold { "name" } { s => s }
        val page     = pageMaybe.fold { 1 } { p => p }
        val pageSize = pageSizeMaybe.fold { 5 } { ps => ps }
        val order    = orderMaybe.fold { "asc" } { o => o }

        val pagedQuery = PagedQuery(page, pageSize, order)

        val validation = for {
            sortFunc    <- Centre.sort2Compare.get(sort).toSuccessNel(ControllerError(s"invalid sort field: $sort"))
            sortOrder   <- pagedQuery.getSortOrder
            centres     <- centresService.getCentres[Centre](filter, status, sortFunc, sortOrder)
            page        <- pagedQuery.getPage(PageSizeMax, centres.size)
            pageSize    <- pagedQuery.getPageSize(PageSizeMax)
            results     <- PagedResults.create(centres, page, pageSize)
          } yield results

        validation.fold(
          err     => BadRequest(err.list.toList.mkString),
          results => Ok(results)
        )
      }
    }

  def listNames(filterMaybe: Option[String], orderMaybe: Option[String]) =
    action.async(parse.empty) { implicit request =>
      Future {
        val filter = filterMaybe.fold { "" } { f => f }
        val order = orderMaybe.fold { "asc" } { o => o }

        SortOrder.fromString(order).fold(
          err => BadRequest(err.list.toList.mkString),
          so  => Ok(centresService.getCentreNames(filter, so))
        )
      }
    }

  // def locations() = action(parse.empty) { implicit request =>
  //     Ok(centresService.centreLocations)
  // }

  def searchLocations() =
    commandAction { cmd: SearchCentreLocationsCmd =>
      Ok(centresService.searchLocations(cmd))
    }

  def query(id: CentreId) =
    action(parse.empty) { implicit request =>
      validationReply(centresService.getCentre(id))
    }

  def add() = commandActionAsync { cmd: AddCentreCmd => processCommand(cmd) }

  def updateName(id: CentreId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateCentreNameCmd => processCommand(cmd) }

  def updateDescription(id: CentreId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateCentreDescriptionCmd => processCommand(cmd) }

  def addStudy(centreId: CentreId) =
    commandActionAsync(Json.obj("id" -> centreId)) { cmd : AddStudyToCentreCmd => processCommand(cmd) }

  def removeStudy(centreId: CentreId, ver: Long, studyId: String) =
    action.async(parse.empty) { implicit request =>
      processCommand(RemoveStudyFromCentreCmd(request.authInfo.userId.id, centreId.id, ver, studyId))
    }

  def addLocation(id: CentreId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : AddCentreLocationCmd => processCommand(cmd) }

  def updateLocation(id: CentreId, locationId: String) =
    commandActionAsync(Json.obj("id"         -> id,
                                "locationId" -> locationId)) { cmd : UpdateCentreLocationCmd =>
      processCommand(cmd)
    }

  def removeLocation(centreId: CentreId, ver: Long, locationId: String) =
    action.async(parse.empty) { implicit request =>
      processCommand(RemoveCentreLocationCmd(request.authInfo.userId.id, centreId.id, ver, locationId))
    }

  def enable(id: CentreId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : EnableCentreCmd => processCommand(cmd) }

  def disable(id: CentreId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd : DisableCentreCmd => processCommand(cmd) }

  private def processCommand(cmd: CentreCommand) = {
    val future = centresService.processCommand(cmd)
    validationReply(future)
  }

}
