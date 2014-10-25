package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain._
import AnnotationValueType._
import org.biobank.domain.study._

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import com.typesafe.plugin.use
import play.api.Logger
import play.api.Play.current
import scala.language.reflectiveCalls
import scaldi.{Injectable, Injector}

import scalaz._
import Scalaz._

class SpecimenLinkAnnotTypeController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable {

  implicit val usersService = inject [UsersService]

  private def studiesService = inject[StudiesService]

  def get(studyId: String, annotTypeId: Option[String]) =
    AuthAction(parse.empty) { token => userId => implicit request =>
      Logger.debug(s"SpecimenLinkAnnotTypeController.list: studyId: $studyId, annotTypeId: $annotTypeId")

      annotTypeId.fold {
        domainValidationReply(studiesService.specimenLinkAnnotationTypesForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.specimenLinkAnnotationTypeWithId(studyId, id))
      }
    }

  def addAnnotationType(studyId: String) =
    commandAction { cmd: AddSpecimenLinkAnnotationTypeCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else {
        val future = studiesService.addSpecimenLinkAnnotationType(cmd)
        domainValidationReply(future)
      }
    }

  def updateAnnotationType(studyId: String, id: String) =
    commandAction { cmd: UpdateSpecimenLinkAnnotationTypeCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else if (cmd.id != id) {
        Future.successful(BadRequest("annotation type id mismatch"))
      } else {
        val future = studiesService.updateSpecimenLinkAnnotationType(cmd)
        domainValidationReply(future)
      }
    }

  def removeAnnotationType(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
      val cmd = RemoveSpecimenLinkAnnotationTypeCmd(studyId, id, ver)
      val future = studiesService.removeSpecimenLinkAnnotationType(cmd)
      domainValidationReply(future)
  }

}
