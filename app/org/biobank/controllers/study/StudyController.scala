package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.service.json.Events._
import org.biobank.service.json.Study._
import org.biobank.service.json.SpecimenGroup._
import org.biobank.service._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.study.Study

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._

import scalaz._
import scalaz.Scalaz._

/**
  *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
  */
object StudyController extends BbwebController {

  private def studyService = Play.current.plugin[BbwebPlugin].map(_.studyService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    val json = Json.toJson(studyService.getAll.toList)
    Ok(json)
  }

  def query(id: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    studyService.getStudy(id).fold(
      err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
      study => Ok(Json.toJson(study))
    )
  }

  def add = CommandAction { cmd: AddStudyCmd => implicit userId =>
    val future = studyService.addStudy(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def update(id: String) = CommandAction { cmd : UpdateStudyCmd => implicit userId =>
    val future = studyService.updateStudy(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def enable = CommandAction { cmd: EnableStudyCmd => implicit userId =>
    val future = studyService.enableStudy(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def disable = CommandAction { cmd: DisableStudyCmd => implicit userId =>
    val future = studyService.disableStudy(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def retire = CommandAction { cmd: RetireStudyCmd => implicit userId =>
    val future = studyService.retireStudy(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def unretire = CommandAction { cmd: UnretireStudyCmd => implicit userId =>
    val future = studyService.unretireStudy(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

}

