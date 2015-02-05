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

class SpecimenLinkTypeController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable
    with StudyEventsJson {

  implicit override val authToken = inject [AuthToken]

  implicit override val usersService = inject [UsersService]

  private val studiesService  = inject[StudiesService]

  def get(processingTypeId: String, slTypeId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"SpecimenLinkTypeController.get: processingTypeId: $processingTypeId, slTypeId: $slTypeId")

      slTypeId.fold {
        domainValidationReply(
          studiesService.specimenLinkTypesForProcessingType(processingTypeId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.specimenLinkTypeWithId(processingTypeId, id))
      }
    }

  def addSpecimenLinkType(procTypeId: String) =
    commandAction { cmd: AddSpecimenLinkTypeCmd => implicit userId =>
      if (cmd.processingTypeId != procTypeId) {
        Future.successful(BadRequest("processing type id mismatch"))
      } else {
        val future = studiesService.addSpecimenLinkType(cmd)
        domainValidationReply(future)
      }
    }

  def updateSpecimenLinkType(procTypeId: String, id: String) =
    commandAction { cmd: UpdateSpecimenLinkTypeCmd => implicit userId =>
      if (cmd.processingTypeId != procTypeId) {
        Future.successful(BadRequest("processing type id mismatch"))
      } else if (cmd.id != id) {
        Future.successful(BadRequest("annotation type id mismatch"))
      } else {
        val future = studiesService.updateSpecimenLinkType(cmd)
        domainValidationReply(future)
      }
    }

  def removeSpecimenLinkType(processingTypeId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveSpecimenLinkTypeCmd(processingTypeId, id, ver)
      val future = studiesService.removeSpecimenLinkType(cmd)(userId)
      domainValidationReply(future)
    }

}
