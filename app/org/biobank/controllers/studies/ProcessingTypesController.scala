package org.biobank.controllers.studies

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.studies.{StudyId, ProcessingTypeId}
import org.biobank.infrastructure.commands.StudyCommands._
import org.biobank.services.studies.StudiesService
import play.api.libs.json._
import play.api.Environment
import play.api.mvc.{Action, ControllerComponents, Result}
import scala.concurrent.{ExecutionContext, Future}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class ProcessingTypesController @Inject() (controllerComponents: ControllerComponents,
                                           val action:         BbwebAction,
                                           val env:            Environment,
                                           val studiesService: StudiesService)
                               (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  def get(studyId: StudyId, procTypeId: Option[ProcessingTypeId]): Action[Unit] =
    action(parse.empty) { implicit request =>
      procTypeId.fold {
        validationReply(studiesService.processingTypesForStudy(studyId).map(_.toList))
      } { id =>
        validationReply(studiesService.processingTypeWithId(studyId, id))
      }
    }

  def addProcessingType(studyId: StudyId): Action[JsValue] =
    commandAction[AddProcessingTypeCmd](Json.obj("studyId" -> studyId))(processCommand)

  def updateProcessingType(studyId: StudyId, id: ProcessingTypeId): Action[JsValue] =
    commandAction[UpdateProcessingTypeCmd](Json.obj("studyId" -> studyId, "id" -> id))(processCommand)

  def removeProcessingType(studyId: StudyId, id: ProcessingTypeId, ver: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveProcessingTypeCmd(Some(request.authInfo.userId.id), studyId.id, id.id, ver)
      val future = studiesService.processRemoveProcessingTypeCommand(cmd)
      validationReply(future)
    }

  private def processCommand(cmd: StudyCommand): Future[Result] = {
    val future = studiesService.processProcessingTypeCommand(cmd)
    validationReply(future)
  }

}
