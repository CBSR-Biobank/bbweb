package org.biobank.controllers.participants

import org.biobank.controllers._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.participants.ParticipantsService
import org.biobank.infrastructure.command.ParticipantCommands._

import javax.inject.{Inject => javaxInject}
import scala.concurrent.Future
import scala.language.reflectiveCalls

class CollectionEventsController @javaxInject() (val authToken:      AuthToken,
                                                 val usersService:   UsersService,
                                                 val participantsService: ParticipantsService)
    extends CommandController
    with JsonController {

  def get(participantId: String, ceventId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>

      ceventId match {
        case Some(ceId) =>
          domainValidationReply(participantsService.getCollectionEvent(participantId, ceId))
        case None =>
          domainValidationReply(participantsService.getCollectionEvents(participantId).map(_.toList))
      }
    }

  def getByVisitNumber(participantId: String, vn: Int) =
    AuthAction(parse.empty) { (token, userId, request) =>
      domainValidationReply(participantsService.getCollectionEventByVisitNumber(participantId, vn))
    }

  def addCollectionEvent(participantId: String) =
    commandAction { cmd: AddCollectionEventCmd => implicit userId =>
      if (cmd.participantId != participantId) {
        Future.successful(BadRequest("participant id mismatch"))
      } else {
        val future = participantsService.addCollectionEvent(cmd)
        domainValidationReply(future)
      }
    }

  def updateCollectionEvent(participantId: String, id: String) =
    commandAction { cmd: UpdateCollectionEventCmd => implicit userId =>
      if (cmd.participantId != participantId) {
        Future.successful(BadRequest("participant id mismatch"))
      } else if (cmd.id != id) {
        Future.successful(BadRequest("collection event id mismatch"))
      } else {
        val future = participantsService.updateCollectionEvent(cmd)
        domainValidationReply(future)
      }
    }

  def removeCollectionEvent(participantId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveCollectionEventCmd(userId          = Some(userId.id),
                                         id              = id,
                                         participantId   = participantId,
                                         expectedVersion = ver)
      val future = participantsService.removeCollectionEvent(cmd)
      domainValidationReply(future)
    }

}

