package org.biobank.controllers.centres

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.users.UsersService
import org.biobank.service.centres.CentresService
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.domain.centre.Centre

import javax.inject.{Inject => javaxInject, Singleton}
import play.api.Play.current
import scala.concurrent.Future
import play.api.Logger
import play.api.Play.current
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

  def add = commandAction { cmd: AddCentreCmd =>
    val future = centresService.addCentre(cmd)
    domainValidationReply(future)
  }

  def update(id: String) = commandAction { cmd : UpdateCentreCmd =>
    val future = centresService.updateCentre(cmd)
    domainValidationReply(future)
  }

  def enable(id: String) = commandAction { cmd: EnableCentreCmd =>
      if (cmd.id != id) {
        Future.successful(BadRequest("centre id mismatch"))
      } else {
        val future = centresService.enableCentre(cmd)
        domainValidationReply(future)
      }
  }

  def disable(id: String) = commandAction { cmd: DisableCentreCmd =>
      if (cmd.id != id) {
        Future.successful(BadRequest("centre id mismatch"))
      } else {
        val future = centresService.disableCentre(cmd)
        domainValidationReply(future)
      }
  }

  def getLocations(centreId: String, locationId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      val validation = centresService.getCentreLocations(centreId, locationId)
      locationId.fold {
        domainValidationReply(validation)
      } { id =>
        // return the first element
        domainValidationReply(validation.map(_.head))
      }
    }

  def addLocation(centreId: String) = commandAction { cmd: AddCentreLocationCmd =>
    val future = centresService.addCentreLocation(cmd)
    domainValidationReply(future)
  }

  def removeLocation(centreId: String, id: String) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val future = centresService.removeCentreLocation(
        RemoveCentreLocationCmd(Some(userId.id), centreId, id))
      domainValidationReply(future)
    }

  def getStudies(centreId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      domainValidationReply(centresService.getCentreStudies(centreId))
    }

  def addStudy(centreId: String, studyId: String) =
    commandAction { cmd: AddStudyToCentreCmd =>
      if (cmd.centreId != centreId) {
        Future.successful(BadRequest("centre id mismatch"))
      } else if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else {
        val future = centresService.addStudyToCentre(cmd)
        domainValidationReply(future)
      }
    }

  def removeStudy(centreId: String, studyId: String) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      Logger.debug(s"removeStudy: $centreId, $studyId")
      val future = centresService.removeStudyFromCentre(
        RemoveStudyFromCentreCmd(Some(userId.id), centreId, studyId))
      domainValidationReply(future)
    }

}
