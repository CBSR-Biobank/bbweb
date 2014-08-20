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

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._

import scalaz._
import scalaz.Scalaz._

/**
  *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
  */
object CentresController extends CommandController {

  private def centresService = Play.current.plugin[BbwebPlugin].map(_.centresService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    val json = Json.toJson(centresService.getAll.toList)
    Ok(json)
  }

  def query(id: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    centresService.getCentre(id).fold(
      err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
      centres => Ok(Json.toJson(centres))
    )
  }

  def add = CommandAction { cmd: AddCentreCmd => implicit userId =>
    val future = centresService.addCentre(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def update(id: String) = CommandAction { cmd : UpdateCentreCmd => implicit userId =>
    val future = centresService.updateCentre(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def enable = CommandAction { cmd: EnableCentreCmd => implicit userId =>
    val future = centresService.enableCentre(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def disable = CommandAction { cmd: DisableCentreCmd => implicit userId =>
    val future = centresService.disableCentre(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def getLocations(
    centreId: String,
    locationId: Option[String]) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    val locations = centresService.getCentreLocations(centreId)
    locationId.fold {
      Ok(Json.toJson(locations))
    } { locationId =>
      val locList = locations.filter(x => x.id.id == locationId).toList
      if (locList.size == 1) {
        Ok(Json.toJson(locList(0)))
      } else {
        BadRequest(Json.obj("status" ->"error", "message" -> s"location does not exist: $locationId"))
      }
    }
  }

  def addLocation = CommandAction { cmd: AddCentreLocationCmd => implicit userId =>
    val future = centresService.addCentreLocation(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeLocation(
    centreId: String,
    id: String) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val future = centresService.removeCentreLocation(RemoveCentreLocationCmd(centreId, id))
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }


  def getLinkedStudies(centreId: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Ok(Json.toJson(centresService.getCentreStudies(centreId)))
  }

  def addLinkedStudies = CommandAction { cmd: AddCentreToStudyCmd => implicit userId =>
    val future = centresService.addCentreToStudy(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeLinkedStudy(
    centreId: String,
    studyId: String) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val future = centresService.removeCentreFromStudy(RemoveCentreFromStudyCmd(centreId, studyId))
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }


}

