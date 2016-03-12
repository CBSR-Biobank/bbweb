package org.biobank.controllers.study

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.AuthToken
import org.biobank.service.study.StudiesService
import org.biobank.service.users.UsersService
import play.api.libs.json._
import play.api.{ Environment, Logger }

@Singleton
class ProcessingTypeController @Inject() (val env:            Environment,
                                          val authToken:      AuthToken,
                                          val usersService:   UsersService,
                                          val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  def get(studyId: String, procTypeId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"ProcessingTypeController.get: studyId: $studyId, procTypeId: $procTypeId")

      procTypeId.fold {
        domainValidationReply(studiesService.processingTypesForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.processingTypeWithId(studyId, id))
      }
    }

  def addProcessingType(studyId: String) =
    commandAction(Json.obj("studyId" -> studyId)) { cmd: AddProcessingTypeCmd =>
      val future = studiesService.processCommand(cmd)
      domainValidationReply(future)
  }

  def updateProcessingType(studyId: String, id: String) =
    commandAction(Json.obj("studyId" -> studyId, "id" -> id)) { cmd: UpdateProcessingTypeCmd =>
      val future = studiesService.processCommand(cmd)
      domainValidationReply(future)
  }

  def removeProcessingType(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveProcessingTypeCmd(Some(userId.id), studyId, id, ver)
      val future = studiesService.processCommand(cmd)
      domainValidationReply(future)
    }

}
