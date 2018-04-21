package org.biobank.controllers.studies

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.studies._
import org.biobank.infrastructure.commands.StudyCommands._
import org.biobank.services.PagedResults
import org.biobank.services.studies.StudiesService
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Result}
import play.api.{ Environment, Logger }
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._

/**
 *
 */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class StudiesController @Inject() (controllerComponents: ControllerComponents,
                                   val action:  BbwebAction,
                                   val env:     Environment,
                                   val service: StudiesService)
                               (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  val log: Logger = Logger(this.getClass)

  protected val PageSizeMax = 10

  def collectionStudies(): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      FilterAndSortQueryHelper(request.rawQueryString).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[Study]]))
        },
        query => {
          validationReply(service.collectionStudies(request.authInfo.userId, query))
        }
      )
    }

  def studyCounts(): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(Future(service.getCountsByStatus(request.authInfo.userId)))
    }

  def list: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[Study]]))
        },
        pagedQuery => {
          validationReply(service.getStudies(request.authInfo.userId, pagedQuery))
        }
      )
    }

  def listNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      FilterAndSortQueryHelper(request.rawQueryString).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[CollectionEventType]]))
        },
        query => {
          validationReply(service.getStudyNames(request.authInfo.userId, query))
        }
      )
    }

  def getBySlug(slug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.getStudyBySlug(request.authInfo.userId, slug))
    }

  def enableAllowed(id: StudyId): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.enableAllowed(request.authInfo.userId, id))
    }

  def centresForStudy(studyId: StudyId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(Future(service.getCentresForStudy(request.authInfo.userId, studyId)))
    }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.snapshotRequest(request.authInfo.userId).map { _ => true })
    }

  def add: Action[JsValue] = commandAction[AddStudyCmd](JsNull)(processCommand)

  def updateName(id: StudyId): Action[JsValue] =
    commandAction[UpdateStudyNameCmd](Json.obj("id" -> id))(processCommand)

  def updateDescription(id: StudyId): Action[JsValue] =
    commandAction[UpdateStudyDescriptionCmd](Json.obj("id" -> id))(processCommand)

  def addAnnotationType(id: StudyId): Action[JsValue] =
    commandAction[StudyAddParticipantAnnotationTypeCmd](Json.obj("id" -> id))(processCommand)

  def updateAnnotationType(id: StudyId, annotationTypeId: String): Action[JsValue] = {
    val json = Json.obj("id" -> id, "annotationTypeId" -> annotationTypeId)
    commandAction[StudyUpdateParticipantAnnotationTypeCmd](json)(processCommand)
  }

  def removeAnnotationType(studyId: StudyId, ver: Long, annotationTypeId: String): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = UpdateStudyRemoveAnnotationTypeCmd(Some(request.authInfo.userId.id),
                                                   studyId.id,
                                                   ver,
                                                   annotationTypeId)
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
      Ok(PreservationTemperature.values.map(x => x))
    }

  /** Value types used by Specimen groups.
   *
   */
  def specimenDefinitionValueTypes: Action[Unit] = Action(parse.empty) { request =>
      // FIXME add container types to this response
      Ok(Map(
           "anatomicalSourceType"    -> AnatomicalSourceType.values.map(x => x),
           "preservationType"        -> PreservationType.values.map(x => x),
           "preservationTemperature" -> PreservationTemperature.values.map(x => x),
           "specimenType"            -> SpecimenType.values.map(x => x)
         ))
    }

  private def processCommand(cmd: StudyCommand): Future[Result] = {
    val future = service.processCommand(cmd)
    validationReply(future)
  }

}
