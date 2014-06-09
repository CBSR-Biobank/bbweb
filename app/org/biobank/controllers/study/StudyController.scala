package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.service.json.SpecimenGroup._
import org.biobank.service.json.Study._
import org.biobank.service._
import org.biobank.infrastructure.command.StudyCommands._
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

object StudyController extends BbwebController {

  private def studyService = Play.current.plugin[BbwebPlugin].map(_.studyService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = Action {
    val json = Json.toJson(studyService.getAll.toList)
    Ok(json)
  }

  def query(id: String) = Action { request =>
    studyService.getStudy(id).fold(
      err => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
      study => Ok(Json.toJson(study))
    )
  }

  def add = CommandAction { cmd: AddStudyCmd => userId =>
    val future = studyService.addStudy(cmd)(null)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"study added: ${event.name}.") ))
      )
    }
  }

  def update(id: String) = CommandAction { cmd : UpdateStudyCmd => userId =>
    val future = studyService.updateStudy(cmd)(null)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"study updated: ${event.name}.") ))
      )
    }
  }

  def enable = CommandAction { cmd: EnableStudyCmd => userId =>
    val future = studyService.enableStudy(cmd)(null)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"study enabled: ${event.id}.") ))
      )
    }
  }

  def disable = CommandAction { cmd: DisableStudyCmd => userId =>
    val future = studyService.disableStudy(cmd)(null)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"study disabled: ${event.id}.") ))
      )
    }
  }

  def retire = CommandAction { cmd: RetireStudyCmd => userId =>
    val future = studyService.retireStudy(cmd)(null)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"study retired: ${event.id}.") ))
      )
    }
  }

  def unretire = CommandAction { cmd: UnretireStudyCmd => userId =>
    val future = studyService.unretireStudy(cmd)(null)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"study unretired: ${event.id}.") ))
      )
    }
  }

}

