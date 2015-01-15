package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.service.users.UsersService
import org.biobank.domain.study.Study
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEventsJson._
import org.biobank.infrastructure.event.StudyEventsJson._
import org.biobank.service._
import org.biobank.service.study.StudiesService

import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import scala.concurrent.Future
import scala.language.reflectiveCalls
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  *
  */
class StudiesController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable {

  implicit val usersService = inject [UsersService]

  private def studiesService = inject[StudiesService]

  private val PageSizeDefault = 10

  case class PaginationResult(studies: Seq[Study], page: Int, pageSize: Int)

  def list(filter : String, sort : String, page : Int, pageSize : Int, order : String) =
    AuthAction(parse.empty) { token => implicit userId => implicit request =>

      Logger.debug(s"StudiesController: list: filter/$filter, sort/$sort, page/$page, pageSize/$pageSize, order/$order")

      val pagedQuery = PagedQuery(sort, page, pageSize, order)
      val validation = for {
        sortField <- pagedQuery.getSortField(Seq("name", "status"))
        sortWith  <- (if (sortField == "status") (Study.compareByStatus _) else (Study.compareByName _)).success
        sortOrder <- pagedQuery.getSortOrder
        studies   <- studiesService.getStudies(filter, sortWith, sortOrder)
        page      <- pagedQuery.getPage(studies.size)
        pageSize  <- pagedQuery.getPageSize
      } yield { PaginationResult(studies, page, pageSize) }

      validation.fold(
        err => BadRequest(err.list.mkString),
        pq => {
          val offset = pq.pageSize * (pq.page - 1)
          if (offset > pq.studies.size) {
            BadRequest(s"invalid page requested: ${pq.page}")
          } else {
            Ok(pq.studies.drop(offset).take(pq.pageSize))
          }
        }
      )
    }

  def listNames(filter: String, order: String) =
    AuthAction(parse.empty) { token => implicit userId => implicit request =>

      SortOrder.fromString(order).fold(
        err => BadRequest(err.list.mkString),
        so  => studiesService.getStudyNames(filter, so).fold(
          err => BadRequest(err.list.mkString),
          studies => Ok(studies.toList)
        )
      )
    }

  def query(id: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    studiesService.getStudy(id).fold(
      err => {
        val errStr = err.list.mkString(", ")
        if (errStr.contains("not found")) {
          BadRequest(s"study with id not found: $id")
        } else {
          BadRequest(errStr)
        }
      },
      study => Ok(study)
    )
  }

  def add = commandAction { cmd: AddStudyCmd => implicit userId =>
    val future = studiesService.addStudy(cmd)
    domainValidationReply(future)
  }

  def update(id: String) = commandAction { cmd : UpdateStudyCmd => implicit userId =>
    if (cmd.id != id) {
      Future.successful(BadRequest("study id mismatch"))
    } else {
      val future = studiesService.updateStudy(cmd)
      domainValidationReply(future)
    }
  }

  def enable(id: String) = commandAction { cmd: EnableStudyCmd => implicit userId =>
    if (cmd.id != id) {
      Future.successful(BadRequest("study id mismatch"))
    } else {
      val future = studiesService.enableStudy(cmd)
      domainValidationReply(future)
    }
  }

  def disable(id: String) = commandAction { cmd: DisableStudyCmd => implicit userId =>
    if (cmd.id != id) {
      Future.successful(BadRequest("study id mismatch"))
    } else {
      val future = studiesService.disableStudy(cmd)
      domainValidationReply(future)
    }
  }

  def retire(id: String) = commandAction { cmd: RetireStudyCmd => implicit userId =>
    if (cmd.id != id) {
      Future.successful(BadRequest("study id mismatch"))
    } else {
      val future = studiesService.retireStudy(cmd)
      domainValidationReply(future)
    }
  }

  def unretire(id: String) = commandAction { cmd: UnretireStudyCmd => implicit userId =>
    if (cmd.id != id) {
      Future.successful(BadRequest("study id mismatch"))
    } else {
      val future = studiesService.unretireStudy(cmd)
      domainValidationReply(future)
    }
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

  def getCollectionDto(studyId: String) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"StudiesController.getCollectionDto: studyId: $studyId")
    domainValidationReply(studiesService.getCollectionDto(studyId))
  }

  def getProcessingDto(studyId: String) = AuthAction(parse.empty) { token => userId => implicit request =>
    Logger.debug(s"StudiesController.getProcessingDto: studyId: $studyId")
    domainValidationReply(studiesService.getProcessingDto(studyId))
  }

}

