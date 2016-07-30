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
class SpecimenLinkTypeController @Inject() (val env:            Environment,
                                            val authToken:      AuthToken,
                                            val usersService:   UsersService,
                                            val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  def get(processingTypeId: String, slTypeId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"SpecimenLinkTypeController.get: processingTypeId: $processingTypeId, slTypeId: $slTypeId")

      slTypeId.fold {
        validationReply(
          studiesService.specimenLinkTypesForProcessingType(processingTypeId).map(_.toList))
      } { id =>
        validationReply(studiesService.specimenLinkTypeWithId(processingTypeId, id))
      }
    }

  def addSpecimenLinkType(procTypeId: String) =
    commandAction(Json.obj("processingTypeId" -> procTypeId)) { cmd: AddSpecimenLinkTypeCmd =>
      val future = studiesService.processCommand(cmd)
      validationReply(future)
    }

  def updateSpecimenLinkType(procTypeId: String, id: String) =
    commandAction(Json.obj("processingTypeId" -> procTypeId, "id" -> id)) { cmd: UpdateSpecimenLinkTypeCmd =>
      val future = studiesService.processCommand(cmd)
      validationReply(future)
    }

  def removeSpecimenLinkType(processingTypeId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveSpecimenLinkTypeCmd(Some(userId.id), processingTypeId, id, ver)
      val future = studiesService.processCommand(cmd)
      validationReply(future)
    }

}
