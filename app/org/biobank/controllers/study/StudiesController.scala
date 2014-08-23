package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.domain.study.Study
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service._
import org.biobank.service.json.Events._
import org.biobank.service.json.ProcessingType._
import org.biobank.service.json.SpecimenGroup._
import org.biobank.service.json.SpecimenLinkAnnotationType._
import org.biobank.service.json.SpecimenLinkType._
import org.biobank.service.json.Study._

import com.typesafe.plugin.use
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{ Logger, Play }
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.language.reflectiveCalls
import play.api.Play.current

import scalaz._
import scalaz.Scalaz._

/**
  *
  */
object StudiesController extends CommandController with JsonController {

  //private def studiesService = use[BbwebPlugin].studiesService
  private def studiesService = use[BbwebPlugin].studiesService

  def list = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Ok(studiesService.getAll.toList)
  }

  def query(id: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    studiesService.getStudy(id).fold(
      err => BadRequest(err.list.mkString(", ")),
      study => Ok(study)
    )
  }

  def add = commandAction { cmd: AddStudyCmd => implicit userId =>
    val future = studiesService.addStudy(cmd)
    domainValidationReply(future)
  }

  def update(id: String) = commandAction { cmd : UpdateStudyCmd => implicit userId =>
    val future = studiesService.updateStudy(cmd)
    domainValidationReply(future)
  }

  def enable = commandAction { cmd: EnableStudyCmd => implicit userId =>
    val future = studiesService.enableStudy(cmd)
    domainValidationReply(future)
  }

  def disable = commandAction { cmd: DisableStudyCmd => implicit userId =>
    val future = studiesService.disableStudy(cmd)
    domainValidationReply(future)
  }

  def retire = commandAction { cmd: RetireStudyCmd => implicit userId =>
    val future = studiesService.retireStudy(cmd)
    domainValidationReply(future)
  }

  def unretire = commandAction { cmd: UnretireStudyCmd => implicit userId =>
    val future = studiesService.unretireStudy(cmd)
    domainValidationReply(future)
  }

  def valueTypes = Action(parse.empty) { request =>
     Ok(AnnotationValueType.values.map(x => x.toString))
  }

  def anatomicalSourceTypes = Action(parse.empty) { request =>
     Ok(AnatomicalSourceType.values.map(x => x.toString))
  }

  def specimenTypes = Action(parse.empty) { request =>
     Ok(SpecimenType.values.map(x => x.toString))
  }

  def preservTypes = Action(parse.empty) { request =>
     Ok(PreservationType.values.map(x => x.toString))
  }

  def preservTempTypes = Action(parse.empty) { request =>
     Ok(PreservationTemperatureType.values.map(x => x.toString))
  }

  /** Value types used by Specimen groups.
    *
    */
  def specimenGroupValueTypes = Action(parse.empty) { request =>
    // FIXME add container types to this response
    Ok(Map(
      "anatomicalSourceType"        -> AnatomicalSourceType.values.map(x => x.toString),
      "preservationType"            -> PreservationType.values.map(x => x.toString),
      "preservationTemperatureType" -> PreservationTemperatureType.values.map(x => x.toString),
      "specimenType"                -> SpecimenType.values.map(x => x.toString)
    ))
  }

  // FIXME move to DTO file
  case class ProcessingDto(
    processingTypes: List[org.biobank.domain.study.ProcessingType],
    specimenLinkTypes: List[org.biobank.domain.study.SpecimenLinkType],
    specimenLinkAnnotationTypes: List[org.biobank.domain.study.SpecimenLinkAnnotationType],
    specimenGroups: List[org.biobank.domain.study.SpecimenGroup])

  // FIXME move to DTO file
  implicit val processingDtoWriter: Writes[ProcessingDto] = (
    (__ \ "processingTypes").write[List[org.biobank.domain.study.ProcessingType]] and
      (__ \ "specimenLinkTypes").write[List[org.biobank.domain.study.SpecimenLinkType]] and
      (__ \ "specimenLinkAnnotationTypes").write[List[org.biobank.domain.study.SpecimenLinkAnnotationType]] and
      (__ \ "specimenGroups").write[List[org.biobank.domain.study.SpecimenGroup]]
  )(unlift(ProcessingDto.unapply))

  def getProcessingDto(studyId: String) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"ProcessingTypeController.getProcessingDto: studyId: $studyId")

    val processingTypes = studiesService.processingTypesForStudy(studyId)
    val specimenLinkTypes = processingTypes.flatMap { pt =>
      studiesService.specimenLinkTypesForProcessingType(pt.id.id)
    }

    Ok(ProcessingDto(
        processingTypes.toList,
        specimenLinkTypes.toList,
        studiesService.specimenLinkAnnotationTypesForStudy(studyId).toList,
        studiesService.specimenGroupsForStudy(studyId).toList))
  }

}

