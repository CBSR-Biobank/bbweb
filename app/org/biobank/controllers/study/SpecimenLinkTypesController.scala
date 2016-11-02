package org.biobank.controllers.study

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.study.{ProcessingTypeId, SpecimenLinkTypeId}
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.AuthToken
import org.biobank.service.studies.StudiesService
import org.biobank.service.users.UsersService
import play.api.Logger
import play.api.libs.json._
import play.api.{ Environment, Logger }

@Singleton
class SpecimenLinkTypesController @Inject() (val action:         BbwebAction,
                                             val env:            Environment,
                                             val authToken:      AuthToken,
                                             val usersService:   UsersService,
                                             val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  val log = Logger(this.getClass)

  def get(processingTypeId: ProcessingTypeId, slTypeId: Option[SpecimenLinkTypeId]) =
    action(parse.empty) { implicit request =>
      log.debug(s"SpecimenLinkTypeController.get: processingTypeId: $processingTypeId, slTypeId: $slTypeId")

      slTypeId.fold {
        validationReply(
          studiesService.specimenLinkTypesForProcessingType(processingTypeId).map(_.toList))
      } { id =>
        validationReply(studiesService.specimenLinkTypeWithId(processingTypeId, id))
      }
    }

  def addSpecimenLinkType(processingTypeId: ProcessingTypeId) =
    commandActionAsync(Json.obj("processingTypeId" -> processingTypeId)) { cmd: AddSpecimenLinkTypeCmd =>
      val future = studiesService.processCommand(cmd)
      validationReply(future)
    }

  def updateSpecimenLinkType(processingTypeId: ProcessingTypeId, id: SpecimenLinkTypeId) =
    commandActionAsync(Json.obj("processingTypeId" -> processingTypeId, "id" -> id)) {
      cmd: UpdateSpecimenLinkTypeCmd =>
      val future = studiesService.processCommand(cmd)
      validationReply(future)
    }

  def removeSpecimenLinkType(processingTypeId: ProcessingTypeId, id: SpecimenLinkTypeId, ver: Long) =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveSpecimenLinkTypeCmd(Some(request.authInfo.userId.id), processingTypeId.id, id.id, ver)
      val future = studiesService.processCommand(cmd)
      validationReply(future)
    }

}
