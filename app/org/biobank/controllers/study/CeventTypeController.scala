package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.service.json.StudyId._
import org.biobank.service.json.CollectionEventType._
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

object CeventTypeController extends BbwebController  {

  private def studyService = Play.current.plugin[BbwebPlugin].map(_.studyService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = Action(BodyParsers.parse.json) { request =>
    val idResult = request.body.validate[StudyId]
    idResult.fold(
      errors => {
        BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors)))
      },
      studyId => {
        Logger.info(s"list: $studyId")
        val json = Json.toJson(studyService.collectionEventTypesForStudy(studyId.id).toList)
        Ok(json)
      }
    )
  }

  def addCollectionEventType = doCommand { cmd: AddCollectionEventTypeCmd =>
    val future = studyService.addCollectionEventType(cmd)(null)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"collection event type added: ${event.name}.") ))
      )
    }
  }

  def updateCollectionEventType(id: String) = doCommand { cmd: UpdateCollectionEventTypeCmd =>
    val future = studyService.updateCollectionEventType(cmd)(null)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"collection event type updated: ${event.name}.") ))
      )
    }
  }

  def removeCollectionEventType(id: String) = doCommand { cmd: RemoveCollectionEventTypeCmd =>
    val future = studyService.removeCollectionEventType(cmd)(null)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"collection event type removed: ${event.collectionEventTypeId}.") ))
      )
    }
  }

}
