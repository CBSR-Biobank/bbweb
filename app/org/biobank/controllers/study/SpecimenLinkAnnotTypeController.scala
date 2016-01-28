package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService
import org.biobank.infrastructure.command.StudyCommands._

import javax.inject.{Inject => javaxInject, Singleton}
import scala.concurrent.Future
import play.api.Logger
import play.api.Play.current
import scala.language.reflectiveCalls

class SpecimenLinkAnnotTypeController @javaxInject() (val authToken:      AuthToken,
                                                      val usersService:   UsersService,
                                                      val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  def get(studyId: String, annotTypeId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"SpecimenLinkAnnotTypeController.list: studyId: $studyId, annotTypeId: $annotTypeId")

      annotTypeId.fold {
        domainValidationReply(studiesService.specimenLinkAnnotationTypesForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.specimenLinkAnnotationTypeWithId(studyId, id))
      }
    }

  def addAnnotationType(studyId: String) =
    commandAction { cmd: AddSpecimenLinkAnnotationTypeCmd =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else {
        val future = studiesService.addSpecimenLinkAnnotationType(cmd)
        domainValidationReply(future)
      }
    }

  def updateAnnotationType(studyId: String, id: String) =
    commandAction { cmd: UpdateSpecimenLinkAnnotationTypeCmd =>
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
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveSpecimenLinkAnnotationTypeCmd(Some(userId.id), studyId, id, ver)
      val future = studiesService.removeSpecimenLinkAnnotationType(cmd)
      domainValidationReply(future)
  }

}
