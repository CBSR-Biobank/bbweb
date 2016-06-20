package org.biobank.controllers.study

import org.biobank.controllers.{
  CommandController,
  JsonController,
  PagedQuery,
  PagedResults
}
import org.biobank.domain._
import org.biobank.service.users.UsersService
import org.biobank.domain.study._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service._
import org.biobank.service.study.StudiesService
import org.biobank.controllers.PagedResults._

import javax.inject.{Inject, Singleton}
import play.api.{ Environment, Logger }
import play.api.mvc._
import play.api.libs.json._
import scala.language.reflectiveCalls
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 *
 */
@Singleton
class StudiesController @Inject() (val env:            Environment,
                                   val authToken:      AuthToken,
                                   val usersService:   UsersService,
                                   val studiesService: StudiesService)
    extends CommandController
    with JsonController {

  private val PageSizeMax = 10

  val listSortFields = Map[String, (Study, Study) => Boolean](
      "name"   -> Study.compareByName,
      "status" -> Study.compareByStatus)

  def studyCounts() =
    AuthAction(parse.empty) { (token, userId, request) =>
      Ok(studiesService.getCountsByStatus)
    }

  def list(filter:   String,
           status:   String,
           sort:     String,
           page:     Int,
           pageSize: Int,
           order:    String) =
    AuthAction(parse.empty) { (token, userId, request) =>

      Logger.debug(s"""|StudiesController:list: filter/$filter, status/$status, sort/$sort,
                       |  page/$page, pageSize/$pageSize, order/$order""".stripMargin)

      val pagedQuery = PagedQuery(listSortFields, page, pageSize, order)

      val validation = for {
          sortFunc    <- pagedQuery.getSortFunc(sort)
          sortOrder   <- pagedQuery.getSortOrder
          studies     <- studiesService.getStudies(filter, status, sortFunc, sortOrder)
          page        <- pagedQuery.getPage(PageSizeMax, studies.size)
          pageSize    <- pagedQuery.getPageSize(PageSizeMax)
          results     <- PagedResults.create(studies, page, pageSize)
        } yield results

      validation.fold(
        err => BadRequest(err.list.toList.mkString),
        results =>  Ok(results)
      )
    }

  def listNames(filter: String, order: String) =
    AuthAction(parse.empty) { (token, userId, request) =>

      SortOrder.fromString(order).fold(
        err => BadRequest(err.list.toList.mkString),
        so  => Ok(studiesService.getStudyNames(filter, so))
      )
    }

  def get(id: String) = AuthAction(parse.empty) { (token, userId, request) =>
      domainValidationReply(studiesService.getStudy(id))
    }

  def add() = commandAction { cmd: AddStudyCmd =>
      processCommand(cmd)
    }

  def updateName(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : UpdateStudyNameCmd => processCommand(cmd) }

  def updateDescription(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : UpdateStudyDescriptionCmd => processCommand(cmd) }

  def addAnnotationType(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd : StudyAddParticipantAnnotationTypeCmd => processCommand(cmd) }

  def updateAnnotationType(id: String, uniqueId: String) =
    commandAction(Json.obj("id" -> id, "uniqueId" -> uniqueId)) {
      cmd : StudyUpdateParticipantAnnotationTypeCmd => processCommand(cmd)
    }

  def removeAnnotationType(studyId: String, ver: Long, uniqueId: String) =
    AuthActionAsync(parse.empty) { (token, userId, request) =>
      val cmd = UpdateStudyRemoveAnnotationTypeCmd(Some(userId.id), studyId, ver, uniqueId)
      processCommand(cmd)
    }

  def enable(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd: EnableStudyCmd => processCommand(cmd) }

  def disable(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd: DisableStudyCmd => processCommand(cmd) }

  def retire(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd: RetireStudyCmd => processCommand(cmd) }

  def unretire(id: String) =
    commandAction(Json.obj("id" -> id)) { cmd: UnretireStudyCmd => processCommand(cmd) }

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

  private def processCommand(cmd: StudyCommand) = {
    val future = studiesService.processCommand(cmd)
    domainValidationReply(future)
  }

}
