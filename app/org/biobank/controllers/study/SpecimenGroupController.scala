package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEventsJson
import org.biobank.domain.study._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.Play.current
import scala.language.reflectiveCalls
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._

/**
 * Handles all operations user can perform on a Specimen Group.
 */
class SpecimenGroupController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable
    with StudyEventsJson {

  implicit val usersService = inject [UsersService]

  private def studiesService = inject[StudiesService]

  def get(studyId: String, sgId: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"SpecimenGroupController.get: studyId: $studyId, sgId: $sgId")

      sgId.fold {
        domainValidationReply(studiesService.specimenGroupsForStudy(studyId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.specimenGroupWithId(studyId, id))
      }
    }

  def getInUse(studyId: String, sgId: Option[String]) = AuthAction(parse.empty) { (token, userId, request) =>
    Logger.debug(s"SpecimenGroupController.getInUse: studyId: $studyId, sgId: $sgId")
    domainValidationReply(studiesService.specimenGroupsInUse(studyId))
  }


  def addSpecimenGroup(studyId: String) =
    commandAction { cmd: AddSpecimenGroupCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else {
        val future = studiesService.addSpecimenGroup(cmd)
        domainValidationReply(future)
      }
    }

  def updateSpecimenGroup(studyId: String, id: String) =
    commandAction { cmd: UpdateSpecimenGroupCmd => implicit userId =>
      if (cmd.studyId != studyId) {
        Future.successful(BadRequest("study id mismatch"))
      } else if (cmd.id != id) {
        Future.successful(BadRequest("annotation type id mismatch"))
      } else {
        val future = studiesService.updateSpecimenGroup(cmd)
        domainValidationReply(future)
      }
    }

  def removeSpecimenGroup(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveSpecimenGroupCmd(studyId, id, ver)
      val future = studiesService.removeSpecimenGroup(cmd)(userId)
      domainValidationReply(future)
    }

}
