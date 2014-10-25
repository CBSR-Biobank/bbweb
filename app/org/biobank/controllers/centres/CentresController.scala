package org.biobank.controllers.centres

import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.service._
import org.biobank.service.users.UsersService
import org.biobank.service.centre.CentresService
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.biobank.domain.centre.Centre

import play.api.Play.current
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import com.typesafe.plugin.use
import play.api.Logger
import play.api.Play.current
import scala.concurrent.Future
import scala.language.reflectiveCalls
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._

/**
  *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
  */
class CentresController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable {

  implicit val usersService = inject [UsersService]

  private def centresService = inject [CentresService]

  def list = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Ok(centresService.getAll.toList)
  }

  def query(id: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    centresService.getCentre(id).fold(
      err => BadRequest(err.list.mkString(", ")),
      centres => Ok(centres)
    )
  }

  def add = commandAction { cmd: AddCentreCmd => implicit userId =>
    val future = centresService.addCentre(cmd)
    domainValidationReply(future)
  }

  def update(id: String) = commandAction { cmd : UpdateCentreCmd => implicit userId =>
    val future = centresService.updateCentre(cmd)
    domainValidationReply(future)
  }

  def enable(id: String) = commandAction { cmd: EnableCentreCmd => implicit userId =>
      if (cmd.id != id) {
        Future.successful(BadRequest("centre id mismatch"))
      } else {
        val future = centresService.enableCentre(cmd)
        domainValidationReply(future)
      }
  }

  def disable(id: String) = commandAction { cmd: DisableCentreCmd => implicit userId =>
      if (cmd.id != id) {
        Future.successful(BadRequest("centre id mismatch"))
      } else {
        val future = centresService.disableCentre(cmd)
        domainValidationReply(future)
      }
  }

  def getLocations(centreId: String, locationId: Option[String]) =
    AuthAction(parse.empty) { token => implicit userId => implicit request =>
      val validation = centresService.getCentreLocations(centreId, locationId)
      locationId.fold {
        domainValidationReply(validation)
      } { id =>
        // return the first element
        domainValidationReply(validation.map(_.head))
      }
    }

  def addLocation(centreId: String) = commandAction { cmd: AddCentreLocationCmd => implicit userId =>
    val future = centresService.addCentreLocation(cmd)
    domainValidationReply(future)
  }

  def removeLocation(centreId: String, id: String) =
    AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
      val future = centresService.removeCentreLocation(RemoveCentreLocationCmd(centreId, id))
      domainValidationReply(future)
    }

  def getStudies(centreId: String) =
    AuthAction(parse.empty) { token => implicit userId => implicit request =>
      domainValidationReply(centresService.getCentreStudies(centreId))
    }

  def addStudy(centreId: String) =
    commandAction { cmd: AddStudyToCentreCmd => implicit userId =>
      if (cmd.centreId != centreId) {
        Future.successful(BadRequest("centre id mismatch"))
      } else {
        val future = centresService.addStudyToCentre(cmd)
        domainValidationReply(future)
      }
    }

  def removeStudy(centreId: String, studyId: String) =
    AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
      Logger.debug(s"removeStudy: $centreId, $studyId")
      val future = centresService.removeStudyFromCentre(RemoveStudyFromCentreCmd(centreId, studyId))
      domainValidationReply(future)
    }

}

