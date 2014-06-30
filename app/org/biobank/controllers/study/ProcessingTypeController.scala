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

  private def studyService = Play.current.plugin[BbwebPlugin].map(_.studyService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def get(studyId: String, procTypeId: Option[String]) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"ProcessingTypeController.list: studyId: $studyId, procTypeId: $procTypeId")

    procTypeId.fold {
      Ok(Json.toJson(studyService.processingTypesForStudy(studyId).toList))
    } {
      id =>
      studyService.processingTypeWithId(studyId, id).fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        procType => Ok(Json.toJson(procType))
      )
    }  }

  def addProcessingType = CommandAction { cmd: AddProcessingTypeCmd => implicit userId =>
    val future = studyService.addProcessingType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def updateProcessingType(id: String) = CommandAction { cmd: UpdateProcessingTypeCmd => implicit userId =>
    val future = studyService.updateProcessingType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeProcessingType(id: String) = CommandAction { cmd: RemoveProcessingTypeCmd => implicit userId =>
    val future = studyService.removeProcessingType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

}
