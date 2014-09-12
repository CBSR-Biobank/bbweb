package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.study._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import com.typesafe.plugin.use
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.Play.current
import scala.language.reflectiveCalls

import scalaz._
import scalaz.Scalaz._

/**
 * Handles all operations user can perform on a Specimen Group.
 */
object SpecimenGroupController extends CommandController with JsonController {

  private def studiesService = use[BbwebPlugin].studiesService

  def get(studyId: String, sgId: Option[String]) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"SpecimenGroupController.get: studyId: $studyId, sgId: $sgId")

    sgId.fold {
      Ok(studiesService.specimenGroupsForStudy(studyId).toList)
    } {
      id =>
      studiesService.specimenGroupWithId(studyId, id).fold(
        err => BadRequest(err.list.mkString(", ")),
        specimenGroup => Ok(specimenGroup)
      )
    }
  }

  def addSpecimenGroup = commandAction(numFields = 8) { cmd: AddSpecimenGroupCmd => implicit userId =>
    val future = studiesService.addSpecimenGroup(cmd)
    domainValidationReply(future)
  }

  def updateSpecimenGroup(id: String) = commandAction { cmd: UpdateSpecimenGroupCmd => implicit userId =>
    val future = studiesService.updateSpecimenGroup(cmd)
    domainValidationReply(future)
  }

  def removeSpecimenGroup(
    studyId: String,
    id: String,
    ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveSpecimenGroupCmd(studyId, id, ver)
    val future = studiesService.removeSpecimenGroup(cmd)
    domainValidationReply(future)
  }

}
