package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEventsJson._
import org.biobank.domain.study._
import org.biobank.domain.AnnotationValueType._

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.Logger
import play.api.Play.current
import scala.language.reflectiveCalls
import scaldi.{Injectable, Injector}

import scalaz._
import Scalaz._

class CeventAnnotTypeController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable {

  implicit val usersService = inject [UsersService]

  private def studiesService = inject[StudiesService]

  def get(studyId: String, annotTypeId : Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"CeventAnnotTypeController.list: studyId: $studyId, annotTypeId: $annotTypeId")

      annotTypeId.fold {
        domainValidationReply(studiesService.collectionEventAnnotationTypesForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.collectionEventAnnotationTypeWithId(studyId, id))
      }
    }

  def addAnnotationType(studyId: String) =
    commandAction { cmd: AddCollectionEventAnnotationTypeCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else {
        val future = studiesService.addCollectionEventAnnotationType(cmd)
        domainValidationReply(future)
      }
    }

  def updateAnnotationType(studyId: String, id: String) =
    commandAction { cmd: UpdateCollectionEventAnnotationTypeCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else if (cmd.id != id) {
        Future.successful(BadRequest("annotation type id mismatch"))
      } else {
        val future = studiesService.updateCollectionEventAnnotationType(cmd)
        domainValidationReply(future)
      }
  }

  def removeAnnotationType(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd =  RemoveCollectionEventAnnotationTypeCmd(studyId, id, ver)
      val future = studiesService.removeCollectionEventAnnotationType(cmd)(userId)
      domainValidationReply(future)
    }

}
