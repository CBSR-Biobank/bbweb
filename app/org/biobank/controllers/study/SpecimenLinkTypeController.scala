package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.service.json.ProcessingType._
import org.biobank.service.json.SpecimenLinkType._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain._
import org.biobank.domain.study._
import views._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import play.api.{ Logger, Play }
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._

import scalaz._
import scalaz.Scalaz._

object SpecimenLinkTypeController extends BbwebController  {

  private def studyService = Play.current.plugin[BbwebPlugin].map(_.studyService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list(processingTypeId: String) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"SpecimenLinkTypeController.list: processingTypeId: $processingTypeId")
    Ok(Json.toJson(studyService.specimenLinkTypesForProcessingType(processingTypeId).toList))
  }

  def addSpecimenLinkType = CommandAction { cmd: AddSpecimenLinkTypeCmd => implicit userId =>
    val future = studyService.addSpecimenLinkType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"specimen link type added: ${event.specimenLinkTypeId}.") ))
      )
    }
  }

  def updateSpecimenLinkType(id: String) = CommandAction { cmd: UpdateSpecimenLinkTypeCmd => implicit userId =>
    val future = studyService.updateSpecimenLinkType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"specimen link type updated: ${event.specimenLinkTypeId}.") ))
      )
    }
  }

  def removeSpecimenLinkType(id: String) = CommandAction { cmd: RemoveSpecimenLinkTypeCmd => implicit userId =>
    val future = studyService.removeSpecimenLinkType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj(
          "status"  ->"OK",
          "message" -> (s"specimen link type removed: ${event.specimenLinkTypeId}.") ))
      )
    }
  }

}
