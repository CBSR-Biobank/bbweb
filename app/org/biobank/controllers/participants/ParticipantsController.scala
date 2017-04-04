package org.biobank.controllers.participants

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.study.StudyId
import org.biobank.domain.participants.ParticipantId
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.participants._
import play.api.{ Environment, Logger }
import play.api.libs.json._
import play.api.mvc.{Action, Results}
import scala.concurrent.Future

@Singleton
class ParticipantsController @Inject() (val action:              BbwebAction,
                                        val env:                 Environment,
                                        val authToken:           AuthToken,
                                        val usersService:        UsersService,
                                        val participantsService: ParticipantsService)
    extends CommandController
    with JsonController {

  val log: Logger = Logger(this.getClass)

  def get(studyId: StudyId, participantId: ParticipantId): Action[Unit] =
    action(parse.empty) { implicit request =>
      log.debug(s"ParticipantsController.get: studyId: $studyId, participantId: $participantId")
      validationReply(participantsService.get(studyId, participantId))
    }

  def getByUniqueId(studyId: StudyId, uniqueId: String): Action[Unit] =
    action(parse.empty) { implicit request =>
      log.debug(s"ParticipantsController.getByUniqueId: studyId: $studyId, uniqueId: $uniqueId")
      validationReply(participantsService.getByUniqueId(studyId, uniqueId))
    }

  def snapshot: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      participantsService.snapshot
      Future.successful(Results.Ok(Json.obj("status" ->"success", "data" -> true)))
    }

  def add(studyId: StudyId): Action[JsValue] =
    commandActionAsync(Json.obj("studyId" -> studyId)) { cmd : AddParticipantCmd => processCommand(cmd) }

  def updateUniqueId(id: ParticipantId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd: UpdateParticipantUniqueIdCmd => processCommand(cmd) }

  def addAnnotation(id: ParticipantId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd: ParticipantAddAnnotationCmd => processCommand(cmd) }

  def removeAnnotation(participantId: ParticipantId, annotTypeId: String, ver: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = ParticipantRemoveAnnotationCmd(userId           = request.authInfo.userId.id,
                                               id               = participantId.id,
                                               expectedVersion  = ver,
                                               annotationTypeId = annotTypeId)
      processCommand(cmd)
    }

  private def processCommand(cmd: ParticipantCommand) = {
    validationReply(participantsService.processCommand(cmd))
  }

}
