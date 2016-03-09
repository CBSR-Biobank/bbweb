package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService

import javax.inject.{Inject, Singleton}
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
        domainValidationReply(
          studiesService.specimenLinkTypesForProcessingType(processingTypeId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.specimenLinkTypeWithId(processingTypeId, id))
      }
    }

  def addSpecimenLinkType() =
    commandAction { cmd: AddSpecimenLinkTypeCmd =>
      val future = studiesService.processCommand(cmd)
      domainValidationReply(future)
    }

  def updateSpecimenLinkType() =
    commandAction { cmd: UpdateSpecimenLinkTypeCmd =>
      val future = studiesService.processCommand(cmd)
      domainValidationReply(future)
    }

  def removeSpecimenLinkType(processingTypeId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveSpecimenLinkTypeCmd(Some(userId.id), processingTypeId, id, ver)
      val future = studiesService.processCommand(cmd)
      domainValidationReply(future)
    }

}
