package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.service.json.Study._
import org.biobank.service._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.study.Study

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.mvc._
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

  def readStudy(id: String) = Action { request =>
    Logger.info(s"readStudy: id: $id")
    val validation = studyService.getStudy(id)
    validation match {
      case Success(study) =>
        Ok(Json.toJson(study))
      case Failure(err) =>
        BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", ")))
    }
  }

  def addStudy = doCommand { cmd: AddStudyCmd =>
    val future = studyService.addStudy(cmd)(null)
    future.map { validation =>
      validation.fold(
        error   => BadRequest(Json.obj("status" ->"KO", "message" -> error.list.mkString(", "))),
        success => Ok(Json.obj("status" ->"OK", "message" -> (s"Study added: ${success.name}.") ))
      )
    }
  }

  def updateStudy(id: String) = doCommand { cmd : UpdateStudyCmd =>
    val future = studyService.updateStudy(cmd)(null)
    future.map { validation =>
      validation match {
        case Success(event) =>
          Ok(Json.obj("status" ->"OK", "message" -> (s"Study updated: ${event.name}.") ))
        case Failure(err) =>
          BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", ")))
      }
    }
  }

  def enableStudy = doCommand { cmd: EnableStudyCmd =>
    val future = studyService.enableStudy(cmd)(null)
    future.map { validation =>
      validation match {
        case Success(event) =>
          Ok(Json.obj("status" ->"OK", "message" -> (s"Study enabled: ${event.id}.") ))
        case Failure(err) =>
          BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", ")))
      }
    }
  }

  def disableStudy = doCommand { cmd: DisableStudyCmd =>
    val future = studyService.disableStudy(cmd)(null)
    future.map { validation =>
      validation match {
        case Success(event) =>
          Ok(Json.obj("status" ->"OK", "message" -> (s"Study disabled: ${event.id}.") ))
        case Failure(err) =>
          BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", ")))
      }
    }
  }

  def retireStudy = doCommand { cmd: RetireStudyCmd =>
    val future = studyService.retireStudy(cmd)(null)
    future.map { validation =>
      validation match {
        case Success(event) =>
          Ok(Json.obj("status" ->"OK", "message" -> (s"Study retired: ${event.id}.") ))
        case Failure(err) =>
          BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", ")))
      }
    }
  }

  def unretireStudy = doCommand { cmd: UnretireStudyCmd =>
    val future = studyService.unretireStudy(cmd)(null)
    future.map { validation =>
      validation match {
        case Success(event) =>
          Ok(Json.obj("status" ->"OK", "message" -> (s"Study unretired: ${event.id}.") ))
        case Failure(err) =>
          BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", ")))
      }
    }
  }

}

