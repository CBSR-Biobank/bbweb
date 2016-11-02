package org.biobank.controllers.study

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.study.{StudyId, ProcessingTypeId}
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.AuthToken
import org.biobank.service.studies.StudiesService
import org.biobank.service.users.UsersService
import play.api.libs.json._
import play.api.Environment

@Singleton
class ProcessingTypesController @Inject() (val action:         BbwebAction,
                                           val env:            Environment,
                                           val authToken:      AuthToken,
                                           val usersService:   UsersService,
                                           val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  def get(studyId: StudyId, procTypeId: Option[ProcessingTypeId]) =
    action(parse.empty) { implicit request =>
      procTypeId.fold {
        validationReply(studiesService.processingTypesForStudy(studyId).map(_.toList))
      } { id =>
        validationReply(studiesService.processingTypeWithId(studyId, id))
      }
    }

  def addProcessingType(studyId: StudyId) =
    commandActionAsync(Json.obj("studyId" -> studyId)) { cmd: AddProcessingTypeCmd =>
      processCommand(cmd)
    }

  def updateProcessingType(studyId: StudyId, id: ProcessingTypeId) =
    commandActionAsync(Json.obj("studyId" -> studyId, "id" -> id)) { cmd: UpdateProcessingTypeCmd =>
      processCommand(cmd)
    }

  def removeProcessingType(studyId: StudyId, id: ProcessingTypeId, ver: Long) =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveProcessingTypeCmd(Some(request.authInfo.userId.id), studyId.id, id.id, ver)
      val future = studiesService.processRemoveProcessingTypeCommand(cmd)
      validationReply(future)
    }

  private def processCommand(cmd: StudyCommand) = {
    val future = studiesService.processProcessingTypeCommand(cmd)
    validationReply(future)
  }

}
