package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.{ ServicesComponent, ServicesComponentImpl }
import org.biobank.service.json.Events._
import org.biobank.service.json.ParticipantAnnotationType._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.study._
import org.biobank.domain.AnnotationValueType._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.{ Logger, Play }
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._

import scalaz._
import Scalaz._

object ParticipantAnnotTypeController extends CommandController  {

  private def studiesService = Play.current.plugin[BbwebPlugin].map(_.studiesService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  /**
    * If [[annotTypeId]] is an empty string, then all the participant annotation types for the
    * study are returned. If non empty, the annotation with the matching [[studyId]] and
    * [[annotTypeId]] is returned.
    *
    * If [[studyId]] is invalid then an empty array is returned.
    *
    * If no matching annotation type is found then an error result is returned.
    */
  def get(studyId: String, annotTypeId: Option[String]) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.info(s"ParticipantAnnotTypeController.get: studyId: $studyId, annotTypeId: $annotTypeId")

    annotTypeId.fold {
      Ok(Json.toJson(studiesService.participantAnnotationTypesForStudy(studyId).toList))
    } {
      id =>
      studiesService.participantAnnotationTypeWithId(studyId, id).fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        annotType => Ok(Json.toJson(annotType))
      )
    }
  }

  def addAnnotationType = CommandAction { cmd: AddParticipantAnnotationTypeCmd => implicit userId =>
    val future = studiesService.addParticipantAnnotationType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def updateAnnotationType(id: String) = CommandAction { cmd: UpdateParticipantAnnotationTypeCmd => implicit userId =>
    val future = studiesService.updateParticipantAnnotationType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeAnnotationType(studyId: String, id: String, ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveParticipantAnnotationTypeCmd(studyId, id, ver)
    val future = studiesService.removeParticipantAnnotationType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

}
