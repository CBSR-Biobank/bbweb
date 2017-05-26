package org.biobank.controllers.participants

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.study.StudyId
import org.biobank.domain.participants.ParticipantId
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.service.participants._
import play.api.{ Environment, Logger }
import play.api.libs.json._
import play.api.mvc.Action
import scala.concurrent.ExecutionContext

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class ParticipantsController @Inject() (val action:              BbwebAction,
                                        val env:                 Environment,
                                        val participantsService: ParticipantsService)
                               (implicit val ec: ExecutionContext)
    extends CommandController {

  val log: Logger = Logger(this.getClass)

  def get(studyId: StudyId, participantId: ParticipantId): Action[Unit] =
    action(parse.empty) { implicit request =>
      log.debug(s"ParticipantsController.get: studyId: $studyId, participantId: $participantId")
      validationReply(participantsService.get(request.authInfo.userId, studyId, participantId))
    }

  def getByUniqueId(studyId: StudyId, uniqueId: String): Action[Unit] =
    action(parse.empty) { implicit request =>
      log.debug(s"ParticipantsController.getByUniqueId: studyId: $studyId, uniqueId: $uniqueId")
      validationReply(participantsService.getByUniqueId(request.authInfo.userId, studyId, uniqueId))
    }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(participantsService.snapshotRequest(request.authInfo.userId).map(_ => true))
    }

  def add(studyId: StudyId): Action[JsValue] =
    commandAction[AddParticipantCmd](Json.obj("studyId" -> studyId))(processCommand)

  def updateUniqueId(id: ParticipantId): Action[JsValue] =
    commandAction[UpdateParticipantUniqueIdCmd](Json.obj("id" -> id))(processCommand)

  def addAnnotation(id: ParticipantId): Action[JsValue] =
    commandAction[ParticipantAddAnnotationCmd](Json.obj("id" -> id))(processCommand)

  def removeAnnotation(participantId: ParticipantId, annotTypeId: String, ver: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = ParticipantRemoveAnnotationCmd(sessionUserId    = request.authInfo.userId.id,
                                               id               = participantId.id,
                                               expectedVersion  = ver,
                                               annotationTypeId = annotTypeId)
      processCommand(cmd)
    }

  private def processCommand(cmd: ParticipantCommand) = {
    validationReply(participantsService.processCommand(cmd))
  }

}
