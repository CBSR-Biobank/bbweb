package org.biobank.controllers.studies

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.studies.{ProcessingTypeId, SpecimenLinkTypeId}
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.services.studies.StudiesService
import play.api.Logger
import play.api.libs.json._
import play.api.{ Environment, Logger }
import play.api.mvc.{Action, ControllerComponents}
import scala.concurrent.ExecutionContext

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class SpecimenLinkTypesController @Inject() (controllerComponents: ControllerComponents,
                                             val action:         BbwebAction,
                                             val env:            Environment,
                                             val studiesService: StudiesService)
                               (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  val log: Logger = Logger(this.getClass)

  def get(processingTypeId: ProcessingTypeId, slTypeId: Option[SpecimenLinkTypeId]): Action[Unit] =
    action(parse.empty) { implicit request =>
      log.debug(s"SpecimenLinkTypeController.get: processingTypeId: $processingTypeId, slTypeId: $slTypeId")

      slTypeId.fold {
        validationReply(
          studiesService.specimenLinkTypesForProcessingType(processingTypeId).map(_.toList))
      } { id =>
        validationReply(studiesService.specimenLinkTypeWithId(processingTypeId, id))
      }
    }

  def addSpecimenLinkType(processingTypeId: ProcessingTypeId): Action[JsValue] =
    commandAction[AddSpecimenLinkTypeCmd](Json.obj("processingTypeId" -> processingTypeId)) { cmd =>
      val future = studiesService.processCommand(cmd)
      validationReply(future)
    }

  def updateSpecimenLinkType(processingTypeId: ProcessingTypeId, id: SpecimenLinkTypeId): Action[JsValue] =
    commandAction[UpdateSpecimenLinkTypeCmd](Json.obj("processingTypeId" -> processingTypeId, "id" -> id)) { cmd =>
      val future = studiesService.processCommand(cmd)
      validationReply(future)
    }

  def removeSpecimenLinkType(processingTypeId: ProcessingTypeId, id: SpecimenLinkTypeId, ver: Long)
      : Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveSpecimenLinkTypeCmd(Some(request.authInfo.userId.id), processingTypeId.id, id.id, ver)
      val future = studiesService.processCommand(cmd)
      validationReply(future)
    }

}
