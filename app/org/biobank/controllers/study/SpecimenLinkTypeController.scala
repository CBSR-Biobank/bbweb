package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.service.json.Events._
import org.biobank.service.json.Study._
import org.biobank.service.json.ProcessingType._
import org.biobank.service.json.SpecimenLinkType._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain._
import org.biobank.domain.study._
import views._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import play.api.{ Logger, Play }
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._

import scalaz._
import scalaz.Scalaz._

object SpecimenLinkTypeController extends BbwebController  {

  private def studiesService = Play.current.plugin[BbwebPlugin].map(_.studiesService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def get(processingTypeId: String, slTypeId: Option[String]) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"SpecimenLinkTypeController.get: processingTypeId: $processingTypeId, slTypeId: $slTypeId")

    slTypeId.fold {
      Ok(Json.toJson(studiesService.specimenLinkTypesForProcessingType(processingTypeId).toList))
    } {
      id =>
      studiesService.specimenLinkTypeWithId(processingTypeId, id).fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        slType => Ok(Json.toJson(slType))
      )
    }
  }

  def addSpecimenLinkType = CommandAction { cmd: AddSpecimenLinkTypeCmd => implicit userId =>
    val future = studiesService.addSpecimenLinkType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def updateSpecimenLinkType(id: String) = CommandAction { cmd: UpdateSpecimenLinkTypeCmd => implicit userId =>
    val future = studiesService.updateSpecimenLinkType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeSpecimenLinkType(studyId: String, id: String, ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveSpecimenLinkTypeCmd(studyId, id, ver)
    val future = studiesService.removeSpecimenLinkType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

}
