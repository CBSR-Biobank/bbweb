package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEventsJson
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService

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

class ProcessingTypeController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable
    with StudyEventsJson {

  implicit override val authToken = inject [AuthToken]

  implicit override val usersService = inject [UsersService]

  private val studiesService = inject[StudiesService]

  def get(studyId: String, procTypeId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"ProcessingTypeController.get: studyId: $studyId, procTypeId: $procTypeId")

      procTypeId.fold {
        domainValidationReply(studiesService.processingTypesForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.processingTypeWithId(studyId, id))
      }
    }

  def addProcessingType(studyId: String) =
    commandAction { cmd: AddProcessingTypeCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else {
        val future = studiesService.addProcessingType(cmd)
        domainValidationReply(future)
      }
  }

  def updateProcessingType(studyId: String, id: String) =
    commandAction { cmd: UpdateProcessingTypeCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else if (cmd.id != id) {
        Future.successful(BadRequest("processing type id mismatch"))
      } else {
        val future = studiesService.updateProcessingType(cmd)
        domainValidationReply(future)
      }
  }

  def removeProcessingType(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveProcessingTypeCmd(Some(userId.id), studyId, id, ver)
      val future = studiesService.removeProcessingType(cmd)
      domainValidationReply(future)
    }

}
