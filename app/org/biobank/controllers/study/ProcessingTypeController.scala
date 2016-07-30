package org.biobank.controllers.study

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.AuthToken
import org.biobank.service.study.StudiesService
import org.biobank.service.users.UsersService
import play.api.libs.json._
import play.api.Environment

@Singleton
class ProcessingTypeController @Inject() (val env:            Environment,
                                          val authToken:      AuthToken,
                                          val usersService:   UsersService,
                                          val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  def get(studyId: String, procTypeId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      procTypeId.fold {
        validationReply(studiesService.processingTypesForStudy(studyId).map(_.toList))
      } { id =>
        validationReply(studiesService.processingTypeWithId(studyId, id))
      }
    }

  def addProcessingType(studyId: String) =
    commandAction(Json.obj("studyId" -> studyId)) { cmd: AddProcessingTypeCmd =>
      processCommand(cmd)
  }

  def updateProcessingType(studyId: String, id: String) =
    commandAction(Json.obj("studyId" -> studyId, "id" -> id)) { cmd: UpdateProcessingTypeCmd =>
      processCommand(cmd)
  }

  def removeProcessingType(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveProcessingTypeCmd(Some(userId.id), studyId, id, ver)
    val future = studiesService.processRemoveProcessingTypeCommand(cmd)
    validationReply(future)
    }

  private def processCommand(cmd: StudyCommand) = {
    val future = studiesService.processProcessingTypeCommand(cmd)
    validationReply(future)
  }

}
