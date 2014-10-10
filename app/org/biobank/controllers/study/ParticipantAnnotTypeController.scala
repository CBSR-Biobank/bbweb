package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.{ ServicesComponent, ServicesComponentImpl }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.study._
import org.biobank.domain.AnnotationValueType._

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import com.typesafe.plugin.use
import play.api.Logger
import play.api.Play.current
import scala.language.reflectiveCalls

import scalaz._
import Scalaz._

object ParticipantAnnotTypeController extends CommandController with JsonController {

  private def studiesService = use[BbwebPlugin].studiesService

  /**
    * If [[annotTypeId]] is an empty string, then all the participant annotation types for the
    * study are returned. If non empty, the annotation with the matching [[studyId]] and
    * [[annotTypeId]] is returned.
    *
    * If [[studyId]] is invalid then an empty array is returned.
    *
    * If no matching annotation type is found then an error result is returned.
    */
  def get(studyId: String, annotTypeId: Option[String]) =
    AuthAction(parse.empty) { token => userId => implicit request =>
      Logger.info(s"ParticipantAnnotTypeController.get: studyId: $studyId, annotTypeId: $annotTypeId")

      annotTypeId.fold {
        domainValidationReply(studiesService.participantAnnotationTypesForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.participantAnnotationTypeWithId(studyId, id))
      }
  }

  def addAnnotationType = commandAction { cmd: AddParticipantAnnotationTypeCmd => implicit userId =>
    domainValidationReply(studiesService.addParticipantAnnotationType(cmd))
  }

  def updateAnnotationType(
    id: String) = commandAction { cmd: UpdateParticipantAnnotationTypeCmd => implicit userId =>
    val future = studiesService.updateParticipantAnnotationType(cmd)
    domainValidationReply(future)
  }

  def removeAnnotationType(
    studyId: String,
    id: String,
    ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveParticipantAnnotationTypeCmd(studyId, id, ver)
    val future = studiesService.removeParticipantAnnotationType(cmd)
    domainValidationReply(future)
  }

}
