package org.biobank.controllers.study

import javax.inject.{Inject, Singleton}
import org.biobank.controllers.{BbwebAction, FilterAndSortQuery, CommandController, PagedQuery}
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service._
import org.biobank.service.studies.StudiesService
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, Result, Results}
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
                                   val studiesService: StudiesService)
                               (implicit val ec: ExecutionContext)
    extends CommandController {

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

  def snapshot: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      studiesService.snapshot
      Future.successful(Results.Ok(Json.obj("status" ->"success", "data" -> true)))
    }

  def add: Action[JsValue] = commandAction[AddStudyCmd](JsNull)(processCommand)

  def updateName(id: StudyId): Action[JsValue] =
    commandAction[UpdateStudyNameCmd](Json.obj("id" -> id))(processCommand)

  def updateDescription(id: StudyId): Action[JsValue] =
    commandAction[UpdateStudyDescriptionCmd](Json.obj("id" -> id))(processCommand)

  def addAnnotationType(id: StudyId): Action[JsValue] =
    commandAction[StudyAddParticipantAnnotationTypeCmd](Json.obj("id" -> id))(processCommand)

  def updateAnnotationType(id: StudyId, uniqueId: String): Action[JsValue] = {
    val json = Json.obj("id" -> id, "uniqueId" -> uniqueId)
    commandAction[StudyUpdateParticipantAnnotationTypeCmd](json)(processCommand)
  }

  def removeAnnotationType(studyId: StudyId, ver: Long, uniqueId: String): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = UpdateStudyRemoveAnnotationTypeCmd(Some(request.authInfo.userId.id), studyId.id, ver, uniqueId)
      processCommand(cmd)
    }

  def enable(id: StudyId): Action[JsValue] =
    commandAction[EnableStudyCmd](Json.obj("id" -> id))(processCommand)

  def disable(id: StudyId): Action[JsValue] =
    commandAction[DisableStudyCmd](Json.obj("id" -> id))(processCommand)

  def retire(id: StudyId): Action[JsValue] =
    commandAction[RetireStudyCmd](Json.obj("id" -> id))(processCommand)

  def unretire(id: StudyId): Action[JsValue] =
    commandAction[UnretireStudyCmd](Json.obj("id" -> id))(processCommand)

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
