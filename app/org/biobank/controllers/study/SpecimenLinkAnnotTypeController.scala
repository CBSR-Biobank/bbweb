package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.infrastructure._
import org.biobank.service.{ ServicesComponent, ServicesComponentImpl }
import org.biobank.service.json.Events._
import org.biobank.service.json.SpecimenLinkAnnotationType._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain._
import AnnotationValueType._
import org.biobank.domain.study._
import views._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.{ Logger, Play }
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._

import scalaz._
import Scalaz._

object SpecimenLinkAnnotTypeController extends CommandController  {

  private def studiesService = Play.current.plugin[BbwebPlugin].map(_.studiesService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def get(studyId: String, annotTypeId: Option[String]) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"SpecimenLinkAnnotTypeController.list: studyId: $studyId, annotTypeId: $annotTypeId")

    annotTypeId.fold {
      Ok(Json.toJson(studiesService.specimenLinkAnnotationTypesForStudy(studyId).toList))
    } {
      id =>
      studiesService.specimenLinkAnnotationTypeWithId(studyId, id).fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        annotType => Ok(Json.toJson(annotType))
      )
    }  }

  def addAnnotationType = CommandAction { cmd: AddSpecimenLinkAnnotationTypeCmd => implicit userId =>
    val future = studiesService.addSpecimenLinkAnnotationType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def updateAnnotationType(id: String) = CommandAction { cmd: UpdateSpecimenLinkAnnotationTypeCmd => implicit userId =>
    val future = studiesService.updateSpecimenLinkAnnotationType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeAnnotationType(studyId: String, id: String, ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveSpecimenLinkAnnotationTypeCmd(studyId, id, ver)
    val future = studiesService.removeSpecimenLinkAnnotationType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

}
