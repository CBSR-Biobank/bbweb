package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.service.json.Study._
import org.biobank.service._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.study.Study
import views._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.{ Play }
import play.api.cache.Cache
import play.api.Play.current
import play.api.mvc._
import play.api.data._
import play.api.libs.json._
import play.Logger
import securesocial.core.SecureSocial
import securesocial.core.SecuredRequest

import scalaz._
import scalaz.Scalaz._

object StudyController extends Controller with SecureSocial {

  private def studyService = Play.current.plugin[BbwebPlugin].map(_.studyService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = Action {
    val json = Json.toJson(studyService.getAll.toList)
    Ok(json)
  }

  def readStudy(id: String) = Action { request =>
    Logger.info(s"$request")
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
      validation match {
        case Success(event) =>
          Ok(Json.obj("status" ->"OK", "message" -> (s"Study added: ${event.name}.") ))
        case Failure(err) =>
          BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", ")))
      }
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
          Ok(Json.obj("status" ->"OK", "message" -> (s"Study disabled: ${event.id}.") ))
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
          Ok(Json.obj("status" ->"OK", "message" -> (s"Study diabled: ${event.id}.") ))
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

  private def doCommand[T <: StudyCommand](
    func: T => Future[Result])(
    implicit reads: Reads[T]) = Action.async(BodyParsers.parse.json) { request =>
    val cmdResult = request.body.validate(reads)
    cmdResult.fold(
      errors => {
        Future.successful(
          BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors))))
      },
      cmd => {
        Logger.info(s"$cmd")
        func(cmd)
      }
    )
  }

}

