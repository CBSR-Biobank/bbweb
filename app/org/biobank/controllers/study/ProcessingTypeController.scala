package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.service.json.Events._
import org.biobank.service.json.ProcessingType._
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

object ProcessingTypeController extends BbwebController  {

  private def studiesService = Play.current.plugin[BbwebPlugin].map(_.studiesService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def get(studyId: String, procTypeId: Option[String]) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"ProcessingTypeController.get: studyId: $studyId, procTypeId: $procTypeId")

    procTypeId.fold {
      Ok(Json.toJson(studiesService.processingTypesForStudy(studyId).toList))
    } {
      id =>
      studiesService.processingTypeWithId(studyId, id).fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        procType => Ok(Json.toJson(procType))
      )
    }
  }

  def addProcessingType = CommandAction { cmd: AddProcessingTypeCmd => implicit userId =>
    val future = studiesService.addProcessingType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def updateProcessingType(id: String) = CommandAction { cmd: UpdateProcessingTypeCmd => implicit userId =>
    val future = studiesService.updateProcessingType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeProcessingType(studyId: String, id: String, ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveProcessingTypeCmd(studyId, id, ver)
    val future = studiesService.removeProcessingType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

}
