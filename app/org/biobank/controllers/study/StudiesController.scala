package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.service.users.UsersService
import org.biobank.domain.study._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service._
import org.biobank.service.study.StudiesService
import org.biobank.controllers.PagedResults._

import javax.inject.{Inject => javaxInject, Singleton}
import play.api.Logger
import play.api.Play.current
import play.api.mvc._
import scala.concurrent.Future
import scala.language.reflectiveCalls
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  *
  */
class StudiesController @javaxInject() (val authToken:      AuthToken,
                                        val usersService:   UsersService,
                                        val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  private val PageSizeDefault = 5

  private val PageSizeMax = 10

  def studyCounts() =
    AuthAction(parse.empty) { (token, userId, request) =>
      Ok(studiesService.getCountsByStatus)
    }

  def list(filter: String, status: String, sort: String, page: Int, pageSize: Int, order: String) =
    AuthAction(parse.empty) { (token, userId, request) =>

      Logger.debug(s"StudiesController:list: filter/$filter, status/$status, sort/$sort, page/$page, pageSize/$pageSize, order/$order")

      val pagedQuery = PagedQuery(sort, page, pageSize, order)
      val validation = for {
        sortField   <- pagedQuery.getSortField(Seq("name", "status"))
        sortWith    <- (if (sortField == "status") (Study.compareByStatus _) else (Study.compareByName _)).success
        sortOrder   <- pagedQuery.getSortOrder
        studies     <- studiesService.getStudies(filter, status, sortWith, sortOrder)
        page        <- pagedQuery.getPage(PageSizeMax, studies.size)
        pageSize    <- pagedQuery.getPageSize(PageSizeMax)
        results     <- PagedResults.create(studies, page, pageSize)
      } yield results

      validation.fold(
        err => BadRequest(err.list.mkString),
        results =>  Ok(results)
      )
    }

  def listNames(filter: String, order: String) =
    AuthAction(parse.empty) { (token, userId, request) =>

      SortOrder.fromString(order).fold(
        err => BadRequest(err.list.mkString),
        so  => studiesService.getStudyNames(filter, so).fold(
          err => BadRequest(err.list.mkString),
          studies => Ok(studies.toList)
        )
      )
    }

  def query(id: String) = AuthAction(parse.empty) { (token, userId, request) =>
    domainValidationReply(studiesService.getStudy(id))
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

  def getCollectionDto(studyId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"StudiesController.getCollectionDto: studyId: $studyId")
      domainValidationReply(studiesService.getCollectionDto(studyId))
    }

  def getProcessingDto(studyId: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"StudiesController.getProcessingDto: studyId: $studyId")
      domainValidationReply(studiesService.getProcessingDto(studyId))
    }

}

