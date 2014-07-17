package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.service.json.Events._
import org.biobank.service.json.Study._
import org.biobank.service.json.SpecimenGroup._
import org.biobank.service.json.ProcessingType._
import org.biobank.service.json.SpecimenLinkType._
import org.biobank.service.json.SpecimenLinkAnnotationType._
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
object StudiesController extends BbwebController {

  private def studiesService = Play.current.plugin[BbwebPlugin].map(_.studiesService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    val json = Json.toJson(studiesService.getAll.toList)
    Ok(json)
  }

  def query(id: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    studiesService.getStudy(id).fold(
      err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
      study => Ok(Json.toJson(study))
    )
  }

  def add = CommandAction { cmd: AddStudyCmd => implicit userId =>
    val future = studiesService.addStudy(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def update(id: String) = CommandAction { cmd : UpdateStudyCmd => implicit userId =>
    val future = studiesService.updateStudy(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def enable = CommandAction { cmd: EnableStudyCmd => implicit userId =>
    val future = studiesService.enableStudy(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def disable = CommandAction { cmd: DisableStudyCmd => implicit userId =>
    val future = studiesService.disableStudy(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def retire = CommandAction { cmd: RetireStudyCmd => implicit userId =>
    val future = studiesService.retireStudy(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def unretire = CommandAction { cmd: UnretireStudyCmd => implicit userId =>
    val future = studiesService.unretireStudy(cmd)
    future.map { validation =>
      validation.fold(
        err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def valueTypes = Action(parse.empty) { request =>
     Ok(Json.toJson(AnnotationValueType.values.map(x =>x.toString)))
  }

  def anatomicalSourceTypes = Action(parse.empty) { request =>
     Ok(Json.toJson(AnatomicalSourceType.values.map(x =>x.toString)))
  }

  def specimenTypes = Action(parse.empty) { request =>
     Ok(Json.toJson(SpecimenType.values.map(x =>x.toString)))
  }

  def preservTypes = Action(parse.empty) { request =>
     Ok(Json.toJson(PreservationType.values.map(x =>x.toString)))
  }

  def preservTempTypes = Action(parse.empty) { request =>
     Ok(Json.toJson(PreservationTemperatureType.values.map(x =>x.toString)))
  }

  /** Value types used by Specimen groups.
    *
    */
  def specimenGroupValueTypes = Action(parse.empty) { request =>
    // FIXME add container types to this response
    Ok(Json.obj(
      "anatomicalSourceType"        -> Json.toJson(AnatomicalSourceType.values.map(x =>x.toString)),
      "preservationType"            -> Json.toJson(PreservationType.values.map(x =>x.toString)),
      "preservationTemperatureType" -> Json.toJson(PreservationTemperatureType.values.map(x =>x.toString)),
      "specimenType"                -> Json.toJson(SpecimenType.values.map(x =>x.toString))
    ))
  }

  def getProcessingDto(studyId: String) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"ProcessingTypeController.getProcessingDto: studyId: $studyId")

    val processingTypes = studiesService.processingTypesForStudy(studyId)
    val specimenLinkTypes = processingTypes.flatMap { pt =>
      studiesService.specimenLinkTypesForProcessingType(pt.id.id)
    }

    Ok(Json.obj(
      "processingTypes" -> Json.toJson(processingTypes.toList),
      "specimenLinkTypes" -> Json.toJson(specimenLinkTypes.toList),
      "specimenLinkAnnotationTypes" -> studiesService.specimenLinkAnnotationTypesForStudy(studyId).toList,
      "specimenGroups" -> Json.toJson(studiesService.specimenGroupsForStudy(studyId).toList)
    ))
  }

}

