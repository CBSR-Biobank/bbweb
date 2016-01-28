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

class SpecimenLinkTypeController @javaxInject() (val authToken:      AuthToken,
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

  def addSpecimenLinkType(procTypeId: String) =
    commandAction { cmd: AddSpecimenLinkTypeCmd =>
      if (cmd.processingTypeId != procTypeId) {
        Future.successful(BadRequest("processing type id mismatch"))
      } else {
        val future = studiesService.addSpecimenLinkType(cmd)
        domainValidationReply(future)
      }
    }

  def updateSpecimenLinkType(procTypeId: String, id: String) =
    commandAction { cmd: UpdateSpecimenLinkTypeCmd =>
      if (cmd.processingTypeId != procTypeId) {
        Future.successful(BadRequest("processing type id mismatch"))
      } else if (cmd.id != id) {
        Future.successful(BadRequest("specimen link type id mismatch"))
      } else {
        val future = studiesService.updateSpecimenLinkType(cmd)
        domainValidationReply(future)
      }
    }

  def removeSpecimenLinkType(processingTypeId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveSpecimenLinkTypeCmd(Some(userId.id), processingTypeId, id, ver)
      val future = studiesService.removeSpecimenLinkType(cmd)
      domainValidationReply(future)
    }

}
