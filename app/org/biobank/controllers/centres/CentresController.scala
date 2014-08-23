package org.biobank.controllers.centres

import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.service.json.Events._
import org.biobank.service.json.Centre._
import org.biobank.service.json.Study._
import org.biobank.service._
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
import scala.language.reflectiveCalls

import scalaz._
import scalaz.Scalaz._

/**
  *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
  */
object CentresController extends CommandController with JsonController {

  private def centresService = use[BbwebPlugin].centresService

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

  def enable = commandAction { cmd: EnableCentreCmd => implicit userId =>
    val future = centresService.enableCentre(cmd)
    domainValidationReply(future)
  }

  def disable = commandAction { cmd: DisableCentreCmd => implicit userId =>
    val future = centresService.disableCentre(cmd)
    domainValidationReply(future)
  }

  def getLocations(
    centreId: String,
    locationId: Option[String]) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    val locations = centresService.getCentreLocations(centreId)
    locationId.fold {
      Ok(locations)
    } { locationId =>
      val locList = locations.filter(x => x.id.id == locationId).toList
      if (locList.size == 1) {
        Ok(locList(0))
      } else {
        BadRequest(s"location does not exist: $locationId")
      }
    }
  }

  def addLocation = commandAction { cmd: AddCentreLocationCmd => implicit userId =>
    val future = centresService.addCentreLocation(cmd)
    domainValidationReply(future)
  }

  def removeLocation(
    centreId: String,
    id: String) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val future = centresService.removeCentreLocation(RemoveCentreLocationCmd(centreId, id))
    domainValidationReply(future)
  }


  def getLinkedStudies(centreId: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Ok(centresService.getCentreStudies(centreId))
  }

  def addLinkedStudies = commandAction { cmd: AddCentreToStudyCmd => implicit userId =>
    val future = centresService.addCentreToStudy(cmd)
    domainValidationReply(future)
  }

  def removeLinkedStudy(
    centreId: String,
    studyId: String) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val future = centresService.removeCentreFromStudy(RemoveCentreFromStudyCmd(centreId, studyId))
    domainValidationReply(future)
  }

}

