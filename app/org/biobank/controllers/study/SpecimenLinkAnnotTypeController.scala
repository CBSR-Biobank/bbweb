package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.infrastructure._
import org.biobank.service.{ ServiceComponent, ServiceComponentImpl }
import org.biobank.service.json.Study._
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

object SpecimenLinkAnnotTypeController extends BbwebController  {

  private def studyService = Play.current.plugin[BbwebPlugin].map(_.studyService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = AuthAction(BodyParsers.parse.json) { token => implicit userId => implicit request =>
    val idResult = request.body.validate[StudyId]
    idResult.fold(
      errors => {
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors)))
      },
      studyId => {
        Logger.info(s"list: $studyId")
        val json = Json.toJson(studyService.specimenLinkAnnotationTypesForStudy(studyId.id).toList)
        Ok(json)
      }
    )
  }

  def addAnnotationType = CommandAction { cmd: AddSpecimenLinkAnnotationTypeCmd => implicit userId =>
    val future = studyService.addSpecimenLinkAnnotationType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"annotation type added: ${event.name}.") ))
      )
    }
  }

  def updateAnnotationType(id: String) = CommandAction { cmd: UpdateSpecimenLinkAnnotationTypeCmd => implicit userId =>
    val future = studyService.updateSpecimenLinkAnnotationType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"annotation type updated: ${event.name}.") ))
      )
    }
  }

  def removeAnnotationType(id: String) = CommandAction { cmd: RemoveSpecimenLinkAnnotationTypeCmd => implicit userId =>
    val future = studyService.removeSpecimenLinkAnnotationType(cmd)
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
