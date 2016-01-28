package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService

import javax.inject.{Inject => javaxInject, Singleton}
import scala.concurrent.Future
import scala.language.postfixOps
import play.api.Logger
import play.api.Play.current
import scala.language.reflectiveCalls

@Singleton
class CeventTypeController @javaxInject() (val authToken:      AuthToken,
                                           val usersService:   UsersService,
                                           val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  def get(studyId: String, ceventTypeId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"CeventTypeController.list: studyId: $studyId, ceventTypeId: $ceventTypeId")

      ceventTypeId.fold {
        domainValidationReply(studiesService.collectionEventTypesForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.collectionEventTypeWithId(studyId, id))
      }
    }

  def addCollectionEventType(studyId: String) =
    commandAction { cmd: AddCollectionEventTypeCmd =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else {
        val future = studiesService.addCollectionEventType(cmd)
        domainValidationReply(future)
      }
    }

  def updateCollectionEventType(studyId: String, id: String) =
    commandAction { cmd: UpdateCollectionEventTypeCmd =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else if (cmd.id != id) {
        Future.successful(BadRequest("annotation type id mismatch"))
      } else {
        val future = studiesService.updateCollectionEventType(cmd)
        domainValidationReply(future)
      }
    }

  def removeCollectionEventType(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveCollectionEventTypeCmd(Some(userId.id), studyId, id, ver)
      val future = studiesService.removeCollectionEventType(cmd)
      domainValidationReply(future)
  }

}
