package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.service.json.Events._
import org.biobank.service.json.CollectionEventType._
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

object CeventTypeController extends CommandController with JsonController {

  private def studiesService = use[BbwebPlugin].studiesService

  def get(
    studyId: String,
    ceventTypeId: Option[String]) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Logger.debug(s"CeventTypeController.list: studyId: $studyId, ceventTypeId: $ceventTypeId")

    ceventTypeId.fold {
      Ok(studiesService.collectionEventTypesForStudy(studyId).toList)
    } {
      id =>
      studiesService.collectionEventTypeWithId(studyId, id).fold(
        err => BadRequest(err.list.mkString(", ")),
        ceventType => Ok(ceventType)
      )
    }  }

  def addCollectionEventType = commandAction { cmd: AddCollectionEventTypeCmd => implicit userId =>
    val future = studiesService.addCollectionEventType(cmd)
    domainValidationReply(future)
  }

  def updateCollectionEventType(id: String) = commandAction { cmd: UpdateCollectionEventTypeCmd => implicit userId =>
    val future = studiesService.updateCollectionEventType(cmd)
    domainValidationReply(future)
  }

  def removeCollectionEventType(
    studyId: String,
    id: String, ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveCollectionEventTypeCmd(studyId, id, ver)
    val future = studiesService.removeCollectionEventType(cmd)
    domainValidationReply(future)
  }

}
