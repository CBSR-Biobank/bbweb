package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.json.Events._
import org.biobank.service.json.SpecimenGroup._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.study._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.{ Logger, Play }
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._

import scalaz._
import scalaz.Scalaz._

/**
 * Handles all operations user can perform on a Specimen Group.
 */
object SpecimenGroupController extends BbwebController {

  private def studiesService = Play.current.plugin[BbwebPlugin].map(_.studiesService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def get(studyId: String, sgId: Option[String]) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"SpecimenGroupController.get: studyId: $studyId, sgId: $sgId")

    sgId.fold {
      Ok(Json.toJson(studiesService.specimenGroupsForStudy(studyId).toList))
    } {
      id =>
      studiesService.specimenGroupWithId(studyId, id).fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        specimenGroup => Ok(Json.toJson(specimenGroup))
      )
    }
  }

  def addSpecimenGroup = CommandAction(numFields = 8) { cmd: AddSpecimenGroupCmd => implicit userId =>
    val future = studiesService.addSpecimenGroup(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def updateSpecimenGroup(id: String) = CommandAction { cmd: UpdateSpecimenGroupCmd => implicit userId =>
    val future = studiesService.updateSpecimenGroup(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeSpecimenGroup(studyId: String, id: String, ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveSpecimenGroupCmd(studyId, id, ver)
    val future = studiesService.removeSpecimenGroup(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

}
