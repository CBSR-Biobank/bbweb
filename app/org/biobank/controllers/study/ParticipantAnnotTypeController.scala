package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.{ ServiceComponent, ServiceComponentImpl }
import org.biobank.service.json.Study._
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

object ParticipantAnnotTypeController extends BbwebController  {

  private def studyService = Play.current.plugin[BbwebPlugin].map(_.studyService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = Action(BodyParsers.parse.json) { request =>
    val idResult = request.body.validate[StudyId]
    idResult.fold(
      errors => {
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors)))
      },
      studyId => {
        Logger.info(s"list: $studyId")
        val json = Json.toJson(studyService.participantAnnotationTypesForStudy(studyId.id).toList)
        Ok(json)
      }
    )
  }

  def addAnnotationType = CommandAction { cmd: AddParticipantAnnotationTypeCmd => userId =>
    val future = studyService.addParticipantAnnotationType(cmd)(null)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"annotation type added: ${event.name}.") ))
      )
    }
  }

  def updateAnnotationType(id: String) = CommandAction { cmd: UpdateParticipantAnnotationTypeCmd => userId =>
    val future = studyService.updateParticipantAnnotationType(cmd)(null)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"annotation type updated: ${event.name}.") ))
      )
    }
  }

  def removeAnnotationType(id: String) = CommandAction { cmd: RemoveParticipantAnnotationTypeCmd => userId =>
    val future = studyService.removeParticipantAnnotationType(cmd)(null)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"annotation type removed: ${event.annotationTypeId}.") ))
      )
    }
  }

}
