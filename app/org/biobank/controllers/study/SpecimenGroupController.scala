package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService
import org.biobank.infrastructure.command.StudyCommands._

import javax.inject.{Inject => javaxInject, Singleton}
import scala.concurrent.Future
import play.api.Logger
import play.api.Play.current
import scala.language.reflectiveCalls

/**
 * Handles all operations user can perform on a Specimen Group.
 */
class SpecimenGroupController @javaxInject() (val authToken:      AuthToken,
                                              val usersService:   UsersService,
                                              val studiesService: StudiesService)
    extends CommandController
    with JsonController {

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
        Future.successful(BadRequest("specimen group id mismatch"))
      } else {
        val future = studiesService.updateSpecimenGroup(cmd)
        domainValidationReply(future)
      }
    }

  def removeSpecimenGroup(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = RemoveSpecimenGroupCmd(Some(userId.id), studyId, id, ver)
      val future = studiesService.removeSpecimenGroup(cmd)
      domainValidationReply(future)
    }

}
