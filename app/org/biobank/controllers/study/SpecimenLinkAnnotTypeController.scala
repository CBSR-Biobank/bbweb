package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.infrastructure._
import org.biobank.service.{ ServicesComponent, ServicesComponentImpl }
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

import scalaz._
import Scalaz._

object SpecimenLinkAnnotTypeController extends CommandController with JsonController {

  private def studiesService = use[BbwebPlugin].studiesService

  def get(studyId: String, annotTypeId: Option[String]) =
    AuthAction(parse.empty) { token => userId => implicit request =>
      Logger.debug(s"SpecimenLinkAnnotTypeController.list: studyId: $studyId, annotTypeId: $annotTypeId")

      annotTypeId.fold {
        domainValidationReply(studiesService.specimenLinkAnnotationTypesForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.specimenLinkAnnotationTypeWithId(studyId, id))
      }
    }

  def addAnnotationType = commandAction { cmd: AddSpecimenLinkAnnotationTypeCmd => implicit userId =>
    val future = studiesService.addSpecimenLinkAnnotationType(cmd)
    domainValidationReply(future)
  }

  def updateAnnotationType(
    id: String) = commandAction { cmd: UpdateSpecimenLinkAnnotationTypeCmd => implicit userId =>
    val future = studiesService.updateSpecimenLinkAnnotationType(cmd)
    domainValidationReply(future)
  }

  def removeAnnotationType(
    studyId: String,
    id: String,
    ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveSpecimenLinkAnnotationTypeCmd(studyId, id, ver)
    val future = studiesService.removeSpecimenLinkAnnotationType(cmd)
    domainValidationReply(future)
  }

}
