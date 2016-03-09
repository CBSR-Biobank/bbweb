package org.biobank.controllers.participants

import org.biobank.controllers._
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.participants._

import javax.inject.{Inject => javaxInject}
import play.api.{ Environment, Logger }

class ParticipantsController @javaxInject() (val env:            Environment,
                                             val authToken:      AuthToken,
                                             val usersService:   UsersService,
                                             val participantsService: ParticipantsService)
    extends CommandController
    with JsonController {

  def get(studyId: String, participantId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"ParticipantsController.get: studyId: $studyId, participantId: $participantId")
      domainValidationReply(participantsService.get(studyId, participantId))
    }

  def getByUniqueId(studyId: String, uniqueId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"ParticipantsController.getByUniqueId: studyId: $studyId, uniqueId: $uniqueId")
      domainValidationReply(participantsService.getByUniqueId(studyId, uniqueId))
    }

  def addParticipant() =
    commandAction { cmd: AddParticipantCmd =>
      val future = participantsService.add(cmd)
      domainValidationReply(future)
    }

  def updateParticipant() =
    commandAction { cmd: UpdateParticipantCmd =>
      val future = participantsService.update(cmd)
      domainValidationReply(future)
    }

}
