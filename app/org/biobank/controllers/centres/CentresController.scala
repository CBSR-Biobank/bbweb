package org.biobank.controllers.centres

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.users.UsersService
import org.biobank.service.centres.CentresService
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.SortOrder
import org.biobank.domain.centre.Centre

import javax.inject.{Inject, Singleton}
import play.api.{ Environment, Logger }
import play.api.libs.json._
import scala.language.reflectiveCalls
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
  */
@Singleton
class CentresController @Inject() (val env:            Environment,
                                   val authToken:      AuthToken,
                                   val usersService:   UsersService,
                                   val centresService: CentresService)
    extends CommandController
    with JsonController {

  private val PageSizeMax = 10

  def centreCounts() =
    AuthAction(parse.empty) { (token, userId, request) =>
      Ok(centresService.getCountsByStatus)
    }

  def list(filterMaybe:   Option[String],
           statusMaybe:   Option[String],
           sortMaybe:     Option[String],
           pageMaybe:     Option[Int],
           pageSizeMaybe: Option[Int],
           orderMaybe:    Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>

      val filter   = filterMaybe.fold { "" } { f => f }
      val status   = statusMaybe.fold { "all" } { s => s }
      val sort     = sortMaybe.fold { "name" } { s => s }
      val page     = pageMaybe.fold { 1 } { p => p }
      val pageSize = pageSizeMaybe.fold { 5 } { ps => ps }
      val order    = orderMaybe.fold { "asc" } { o => o }

      Logger.debug(s"""|CentresController:list: filter/$filter, status/$status, sort/$sort,
                       | page/$page, pageSize/$pageSize, order/$order""".stripMargin)

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
        err => BadRequest(err.list.toList.mkString),
        results =>  Ok(results)
      )
    }

  def listNames(filter: String, order: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      SortOrder.fromString(order).fold(
        err => BadRequest(err.list.toList.mkString),
        so  => Ok(centresService.getCentreNames(filter, so))
      )
    }

  def locations() = AuthAction(parse.empty) { (token, userId, request) =>
      Ok(centresService.centreLocations)
  }

  def query(id: String) = AuthAction(parse.empty) { (token, userId, request) =>
    validationReply(centresService.getCentre(id))
  }

  def add() = commandAction { cmd: AddCentreCmd => processCommand(cmd) }

  def updateName(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : UpdateCentreNameCmd => processCommand(cmd) }

  def updateDescription(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : UpdateCentreDescriptionCmd => processCommand(cmd) }

  def addStudy(centreId: String) =
    commandAction(Json.obj("id" -> centreId)) { cmd : AddStudyToCentreCmd => processCommand(cmd) }

  def removeStudy(centreId: String, ver: Long, studyId: String) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      processCommand(RemoveStudyFromCentreCmd(userId.id, centreId, ver, studyId))
    }

  def addLocation(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : AddCentreLocationCmd => processCommand(cmd) }

  def updateLocation(id: String, locationId: String) =
    commandAction(Json.obj("id"         -> id,
                           "locationId" -> locationId)) { cmd : UpdateCentreLocationCmd =>
      processCommand(cmd)
    }

  def removeLocation(centreId: String, ver: Long, locationId: String) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      processCommand(RemoveCentreLocationCmd(userId.id, centreId, ver, locationId))
    }

  def enable(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : EnableCentreCmd => processCommand(cmd) }

  def disable(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : DisableCentreCmd => processCommand(cmd) }

  private def processCommand(cmd: CentreCommand) = {
    val future = centresService.processCommand(cmd)
    validationReply(future)
  }

}
