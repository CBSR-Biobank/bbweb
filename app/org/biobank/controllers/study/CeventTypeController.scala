package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.infrastructure._
import org.biobank.service.json.Events._
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

  def get(studyId: String, ceventTypeId: Option[String]) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Logger.debug(s"CeventTypeController.list: studyId: $studyId, ceventTypeId: $ceventTypeId")

    ceventTypeId.fold {
      Ok(Json.toJson(studyService.collectionEventTypesForStudy(studyId).toList))
    } {
      id =>
      studyService.collectionEventTypeWithId(studyId, id).fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        ceventType => Ok(Json.toJson(ceventType))
      )
    }  }

  def addCollectionEventType = CommandAction { cmd: AddCollectionEventTypeCmd => implicit userId =>
    val future = studyService.addCollectionEventType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def updateCollectionEventType(id: String) = CommandAction { cmd: UpdateCollectionEventTypeCmd => implicit userId =>
    val future = studyService.updateCollectionEventType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeCollectionEventType(id: String) = CommandAction { cmd: RemoveCollectionEventTypeCmd => implicit userId =>
    val future = studyService.removeCollectionEventType(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

}
