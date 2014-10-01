package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService

import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.language.postfixOps
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import com.typesafe.plugin.use
import play.api.Logger
import play.api.Play.current
import scala.language.reflectiveCalls
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._

class SpecimenLinkTypeController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable {

  implicit val usersService = inject [UsersService]

  private def studiesService = inject[StudiesService]

  def get(processingTypeId: String, slTypeId: Option[String]) =
    AuthAction(parse.empty) { token => userId => implicit request =>
      Logger.debug(s"SpecimenLinkTypeController.get: processingTypeId: $processingTypeId, slTypeId: $slTypeId")

      slTypeId.fold {
        domainValidationReply(
          studiesService.specimenLinkTypesForProcessingType(processingTypeId).map(_.toList))
      } { id =>
        domainValidationReply(studiesService.specimenLinkTypeWithId(processingTypeId, id))
      }
    }

  def addSpecimenLinkType = commandAction { cmd: AddSpecimenLinkTypeCmd => implicit userId =>
    val future = studiesService.addSpecimenLinkType(cmd)
    domainValidationReply(future)
  }

  def updateSpecimenLinkType(id: String) =
    commandAction { cmd: UpdateSpecimenLinkTypeCmd => implicit userId =>
      val future = studiesService.updateSpecimenLinkType(cmd)
      domainValidationReply(future)
    }

  def removeSpecimenLinkType(studyId: String, id: String, ver: Long) =
    AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
      val cmd = RemoveSpecimenLinkTypeCmd(studyId, id, ver)
      val future = studiesService.removeSpecimenLinkType(cmd)
      domainValidationReply(future)
    }

}
