package org.biobank.controllers.study

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service._
import org.biobank.service.studies.StudiesService
import org.biobank.service.users.UsersService
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, Result}
import play.api.{ Environment, Logger }
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 *
 */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class StudiesController @Inject() (val action:         BbwebAction,
                                   val env:            Environment,
                                   val authToken:      AuthToken,
                                   val usersService:   UsersService,
                                   val studiesService: StudiesService)
                               (implicit ec: ExecutionContext)
    extends CommandController
    with JsonController {

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 10

  def studyCounts(): Action[Unit] =
    action(parse.empty) { implicit request =>
      Ok(studiesService.getCountsByStatus)
    }

  def list: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            pagedQuery <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            studies    <- studiesService.getStudies(pagedQuery.filter, pagedQuery.sort)
            validPage  <- pagedQuery.validPage(studies.size)
            results    <- PagedResults.create(studies, pagedQuery.page, pagedQuery.limit)
          } yield results
        }
      )
    }

  def listNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            filterAndSort <- FilterAndSortQuery.create(request.rawQueryString)
            studyNames    <- studiesService.getStudyNames(filterAndSort.filter, filterAndSort.sort)
          } yield studyNames
        }
      )
    }

  def get(id: StudyId): Action[Unit] = action(parse.empty) { implicit request =>
      validationReply(studiesService.getStudy(id))
    }

  def centresForStudy(studyId: StudyId): Action[Unit] = action(parse.empty) { implicit request =>
      Ok(studiesService.getCentresForStudy(studyId))
    }

  def add: Action[JsValue] = commandActionAsync { cmd: AddStudyCmd =>
      processCommand(cmd)
    }

  def updateName(id: StudyId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateStudyNameCmd => processCommand(cmd) }

  def updateDescription(id: StudyId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : UpdateStudyDescriptionCmd => processCommand(cmd) }

  def addAnnotationType(id: StudyId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd : StudyAddParticipantAnnotationTypeCmd => processCommand(cmd) }

  def updateAnnotationType(id: StudyId, uniqueId: String): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id, "uniqueId" -> uniqueId)) {
      cmd : StudyUpdateParticipantAnnotationTypeCmd => processCommand(cmd)
    }

  def removeAnnotationType(studyId: StudyId, ver: Long, uniqueId: String): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = UpdateStudyRemoveAnnotationTypeCmd(Some(request.authInfo.userId.id), studyId.id, ver, uniqueId)
      processCommand(cmd)
    }

  def enable(id: StudyId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd: EnableStudyCmd => processCommand(cmd) }

  def disable(id: StudyId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd: DisableStudyCmd => processCommand(cmd) }

  def retire(id: StudyId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd: RetireStudyCmd => processCommand(cmd) }

  def unretire(id: StudyId): Action[JsValue] =
    commandActionAsync(Json.obj("id" -> id)) { cmd: UnretireStudyCmd => processCommand(cmd) }

  def valueTypes: Action[Unit] = Action(parse.empty) { request =>
      Ok(AnnotationValueType.values.map(x => x))
    }

  def anatomicalSourceTypes: Action[Unit] = Action(parse.empty) { request =>
      Ok(AnatomicalSourceType.values.map(x => x))
    }

  def specimenTypes: Action[Unit] = Action(parse.empty) { request =>
      Ok(SpecimenType.values.map(x => x))
    }

  def preservTypes: Action[Unit] = Action(parse.empty) { request =>
      Ok(PreservationType.values.map(x => x))
    }

  def preservTempTypes: Action[Unit] = Action(parse.empty) { request =>
      Ok(PreservationTemperatureType.values.map(x => x))
    }

  /** Value types used by Specimen groups.
   *
   */
  def specimenGroupValueTypes: Action[Unit] = Action(parse.empty) { request =>
      // FIXME add container types to this response
      Ok(Map(
           "anatomicalSourceType"        -> AnatomicalSourceType.values.map(x => x),
           "preservationType"            -> PreservationType.values.map(x => x),
           "preservationTemperatureType" -> PreservationTemperatureType.values.map(x => x),
           "specimenType"                -> SpecimenType.values.map(x => x)
         ))
    }

  private def processCommand(cmd: StudyCommand): Future[Result] = {
    val future = studiesService.processCommand(cmd)
    validationReply(future)
  }

}
