package org.biobank.controllers.participants

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.Slug
import org.biobank.domain.studies.StudyId
import org.biobank.domain.participants.ParticipantId
import org.biobank.infrastructure.commands.ParticipantCommands._
import org.biobank.services.participants._
import play.api.{ Environment, Logger }
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import scala.concurrent.ExecutionContext

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class ParticipantsController @Inject() (controllerComponents: ControllerComponents,
                                        val action:              BbwebAction,
                                        val env:                 Environment,
                                        val participantsService: ParticipantsService)
                               (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  val log: Logger = Logger(this.getClass)

  def get(studyId: StudyId, participantId: ParticipantId): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(participantsService.get(request.identity.user.id, studyId, participantId))
    }

  def getBySlug(slug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(participantsService.getBySlug(request.identity.user.id, slug))
    }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(participantsService.snapshotRequest(request.identity.user.id).map(_ => true))
    }

  def add(studyId: StudyId): Action[JsValue] =
    commandAction[AddParticipantCmd](Json.obj("studyId" -> studyId))(processCommand)

  def updateUniqueId(id: ParticipantId): Action[JsValue] =
    commandAction[UpdateParticipantUniqueIdCmd](Json.obj("id" -> id))(processCommand)

  def addAnnotation(id: ParticipantId): Action[JsValue] =
    commandAction[ParticipantUpdateAnnotationCmd](Json.obj("id" -> id))(processCommand)

  def removeAnnotation(participantId: ParticipantId, annotTypeId: String, ver: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = ParticipantRemoveAnnotationCmd(sessionUserId    = request.identity.user.id.id,
                                               id               = participantId.id,
                                               expectedVersion  = ver,
                                               annotationTypeId = annotTypeId)
      processCommand(cmd)
    }

  private def processCommand(cmd: ParticipantCommand) = {
    validationReply(participantsService.processCommand(cmd))
  }

}
