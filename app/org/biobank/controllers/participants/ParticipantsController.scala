package org.biobank.controllers.participants

import org.biobank.controllers._
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

  def get(studyId: String, participantId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"ParticipantsController.get: studyId: $studyId, participantId: $participantId")
      validationReply(participantsService.get(studyId, participantId))
    }

  def getByUniqueId(studyId: String, uniqueId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"ParticipantsController.getByUniqueId: studyId: $studyId, uniqueId: $uniqueId")
      validationReply(participantsService.getByUniqueId(studyId, uniqueId))
    }

  def add(studyId: String) =
    commandAction(Json.obj("studyId" -> studyId)) { cmd : AddParticipantCmd => processCommand(cmd) }

  def updateUniqueId(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd: UpdateParticipantUniqueIdCmd => processCommand(cmd) }

  def addAnnotation(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd: ParticipantAddAnnotationCmd => processCommand(cmd) }

  def removeAnnotation(participantId: String, annotTypeId: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = ParticipantRemoveAnnotationCmd(userId           = userId.id,
                                               id               = participantId,
                                               expectedVersion  = ver,
                                               annotationTypeId = annotTypeId)
      processCommand(cmd)
    }

  private def processCommand(cmd: ParticipantCommand) = {
    val future = participantsService.processCommand(cmd)
    validationReply(future)
  }

}
