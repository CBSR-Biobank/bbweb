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

@Singleton
class CeventAnnotTypeController @javaxInject() (val authToken:      AuthToken,
                                                val usersService:   UsersService,
                                                val studiesService: StudiesService)
    extends CommandController
    with JsonController {

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
    commandAction { cmd: AddCollectionEventAnnotationTypeCmd =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else {
        val future = studiesService.addCollectionEventAnnotationType(cmd)
        domainValidationReply(future)
      }
    }

  def updateAnnotationType(studyId: String, id: String) =
    commandAction { cmd: UpdateCollectionEventAnnotationTypeCmd =>
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
      val cmd =  RemoveCollectionEventAnnotationTypeCmd(Some(userId.id), studyId, id, ver)
      val future = studiesService.removeCollectionEventAnnotationType(cmd)
      domainValidationReply(future)
    }

}
