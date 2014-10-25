package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService

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
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._

class CeventTypeController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable {

  implicit val usersService = inject [UsersService]

  private def studiesService = inject[StudiesService]

  def get(studyId: String, ceventTypeId: Option[String]) =
    AuthAction(parse.empty) { token => implicit userId => implicit request =>
      Logger.debug(s"CeventTypeController.list: studyId: $studyId, ceventTypeId: $ceventTypeId")

      ceventTypeId.fold {
        domainValidationReply(studiesService.collectionEventTypesForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.collectionEventTypeWithId(studyId, id))
      }
    }

  def addCollectionEventType(studyId: String) =
    commandAction { cmd: AddCollectionEventTypeCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else {
        val future = studiesService.addCollectionEventType(cmd)
        domainValidationReply(future)
      }
    }

  def updateCollectionEventType(studyId: String, id: String) =
    commandAction { cmd: UpdateCollectionEventTypeCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else if (cmd.id != id) {
        Future.successful(BadRequest("annotation type id mismatch"))
      } else {
        val future = studiesService.updateCollectionEventType(cmd)
        domainValidationReply(future)
      }
    }

  def removeCollectionEventType(
    studyId: String,
    id: String, ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveCollectionEventTypeCmd(studyId, id, ver)
    val future = studiesService.removeCollectionEventType(cmd)
    domainValidationReply(future)
  }

}
