package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.service.json.Events._
import org.biobank.service.json.ProcessingType._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain._
import org.biobank.domain.study._
import views._

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.language.postfixOps
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import com.typesafe.plugin.use
import play.api.Logger
import play.api.Play.current
import scala.language.reflectiveCalls

import scalaz._
import scalaz.Scalaz._

object ProcessingTypeController extends CommandController with JsonController {

  private def studiesService = use[BbwebPlugin].studiesService

  def get(studyId: String, procTypeId: Option[String]) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"ProcessingTypeController.get: studyId: $studyId, procTypeId: $procTypeId")

    procTypeId.fold {
      Ok(studiesService.processingTypesForStudy(studyId).toList)
    } {
      id =>
      studiesService.processingTypeWithId(studyId, id).fold(
        err => BadRequest(err.list.mkString(", ")),
        procType => Ok(procType)
      )
    }
  }

  def addProcessingType = commandAction { cmd: AddProcessingTypeCmd => implicit userId =>
    val future = studiesService.addProcessingType(cmd)
    domainValidationReply(future)
  }

  def updateProcessingType(id: String) = commandAction { cmd: UpdateProcessingTypeCmd => implicit userId =>
    val future = studiesService.updateProcessingType(cmd)
    domainValidationReply(future)
  }

  def removeProcessingType(
    studyId: String,
    id: String,
    ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveProcessingTypeCmd(studyId, id, ver)
    val future = studiesService.removeProcessingType(cmd)
    domainValidationReply(future)
  }

}
