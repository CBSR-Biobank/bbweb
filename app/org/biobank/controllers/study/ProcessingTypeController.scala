package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.service.json.Study._
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

  def list(studyId: String) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"ProcessingTypeController.list: studyId: $studyId")
    Ok(Json.toJson(studyService.processingTypesForStudy(studyId).toList))
  }

  def addProcessingType = CommandAction { cmd: AddProcessingTypeCmd => implicit userId =>
    val future = studyService.addProcessingType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"processing type added: ${event.name}.") ))
      )
    }
  }

  def updateProcessingType(id: String) = CommandAction { cmd: UpdateProcessingTypeCmd => implicit userId =>
    val future = studyService.updateProcessingType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"processing type updated: ${event.name}.") ))
      )
    }
  }

  def removeProcessingType(id: String) = CommandAction { cmd: RemoveProcessingTypeCmd => implicit userId =>
    val future = studyService.removeProcessingType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"processing type removed: ${event.processingTypeId}.") ))
      )
    }
  }

}
