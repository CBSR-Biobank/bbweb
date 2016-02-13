package org.biobank.controllers.centres

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.users.UsersService
import org.biobank.service.centres.CentresService
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.domain.centre.Centre

import javax.inject.{Inject => javaxInject, Singleton}
import play.api.Logger
import play.api.libs.json._
import scala.concurrent.Future
import scala.language.reflectiveCalls
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
  */
@Singleton
class CentresController @javaxInject() (val authToken:      AuthToken,
                                        val usersService:   UsersService,
                                        val centresService: CentresService)
    extends CommandController
    with JsonController {

  private val PageSizeMax = 10

  def centreCounts() =
    AuthAction(parse.empty) { (token, userId, request) =>
      Ok(centresService.getCountsByStatus)
    }

  def list(filter: String, status: String, sort: String, page: Int, pageSize: Int, order: String) =
    AuthAction(parse.empty) { (token, userId, request) =>

      Logger.debug(s"CentresController:list: filter/$filter, status/$status, sort/$sort, page/$page, pageSize/$pageSize, order/$order")

      val pagedQuery = PagedQuery(sort, page, pageSize, order)
      val validation = for {
        sortField   <- pagedQuery.getSortField(Seq("name", "status"))
        sortWith    <- (if (sortField == "status") (Centre.compareByStatus _) else (Centre.compareByName _)).success
        sortOrder   <- pagedQuery.getSortOrder
        centres     <- centresService.getCentres(filter, status, sortWith, sortOrder)
        page        <- pagedQuery.getPage(PageSizeMax, centres.size)
        pageSize    <- pagedQuery.getPageSize(PageSizeMax)
        results     <- PagedResults.create(centres, page, pageSize)
      } yield results

      validation.fold(
        err => BadRequest(err.list.toList.mkString),
        results =>  Ok(results)
      )
    }

  def query(id: String) = AuthAction(parse.empty) { (token, userId, request) =>
    domainValidationReply(centresService.getCentre(id))
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
      processCommand(RemoveStudyFromCentreCmd(Some(userId.id), centreId, ver, studyId))
    }

  def addLocation(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : AddCentreLocationCmd => processCommand(cmd) }

  def removeLocation(centreId: String, ver: Long, locationId: String) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      processCommand(RemoveCentreLocationCmd(Some(userId.id), centreId, ver, locationId))
    }

  def enable(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : EnableCentreCmd => processCommand(cmd) }

  def disable(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : DisableCentreCmd => processCommand(cmd) }

  private def processCommand(cmd: CentreCommand) = {
    val future = centresService.processCommand(cmd)
    domainValidationReply(future)
  }

}
