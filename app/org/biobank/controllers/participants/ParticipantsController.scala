package org.biobank.controllers.participants

import org.biobank.controllers._
import org.biobank.domain.study.StudyId
import org.biobank.domain.participants.ParticipantId
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.participants._

import javax.inject.{Inject, Singleton}
import play.api.{ Environment, Logger }
import play.api.libs.json._

@Singleton
class ParticipantsController @Inject() (val env:            Environment,
                                        val authToken:      AuthToken,
                                        val usersService:   UsersService,
                                        val participantsService: ParticipantsService)
    extends CommandController
    with JsonController {

  val log = Logger(this.getClass)

  def get(studyId: StudyId, participantId: ParticipantId) =
    AuthAction(parse.empty) { (token, userId, request) =>
      log.debug(s"ParticipantsController.get: studyId: $studyId, participantId: $participantId")
      validationReply(participantsService.get(studyId, participantId))
    }

  def getByUniqueId(studyId: StudyId, uniqueId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      log.debug(s"ParticipantsController.getByUniqueId: studyId: $studyId, uniqueId: $uniqueId")
      validationReply(participantsService.getByUniqueId(studyId, uniqueId))
    }

  def add(studyId: StudyId) =
    commandActionAsync(Json.obj("studyId" -> studyId)) { cmd : AddParticipantCmd => processCommand(cmd) }

  def updateUniqueId(id: ParticipantId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd: UpdateParticipantUniqueIdCmd => processCommand(cmd) }

  def addAnnotation(id: ParticipantId) =
    commandActionAsync(Json.obj("id" -> id)) { cmd: ParticipantAddAnnotationCmd => processCommand(cmd) }

  def removeAnnotation(participantId: ParticipantId, annotTypeId: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = ParticipantRemoveAnnotationCmd(userId           = userId.id,
                                               id               = participantId.id,
                                               expectedVersion  = ver,
                                               annotationTypeId = annotTypeId)
      processCommand(cmd)
    }

  private def processCommand(cmd: ParticipantCommand) = {
    validationReply(participantsService.processCommand(cmd))
  }

}
