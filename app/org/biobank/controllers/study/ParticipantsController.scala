package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEventsJson
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.study.ParticipantsService

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.language.postfixOps
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.Logger
import play.api.Play.current
import scala.language.reflectiveCalls
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._

class ParticipantsController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable
    with StudyEventsJson {

  implicit override val authToken = inject [AuthToken]

  implicit override val usersService = inject [UsersService]

  private def participantsService = inject[ParticipantsService]

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

  def addParticipant(studyId: String) =
    commandAction { cmd: AddParticipantCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else {
        val future = participantsService.add(cmd)
        domainValidationReply(future)
      }
    }

  def updateParticipant(studyId: String, id: String) =
    commandAction { cmd: UpdateParticipantCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else if (cmd.id != id) {
        Future.successful(BadRequest("participant id mismatch"))
      } else {
        val future = participantsService.update(cmd)
        domainValidationReply(future)
      }
    }

  def checkUnique(uniqueId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"ParticipantsController.checkUnique: uniqueId: $uniqueId")
      domainValidationReply(participantsService.checkUnique(uniqueId))
    }

}
