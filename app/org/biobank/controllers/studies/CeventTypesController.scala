package org.biobank.controllers.studies

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.Slug
import org.biobank.domain.studies.{StudyId, CollectionEventType, CollectionEventTypeId}
import org.biobank.infrastructure.commands.CollectionEventTypeCommands._
import org.biobank.services.PagedResults
import org.biobank.services.studies.CollectionEventTypeService
import play.api.libs.json._
import play.api.{ Environment, Logger }
import play.api.mvc.{Action, ControllerComponents, Result}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class CeventTypesController @Inject() (
  controllerComponents: ControllerComponents,
  val action:  BbwebAction,
  val env:     Environment,
  val service: CollectionEventTypeService
) (
  implicit val ec: ExecutionContext
)
    extends CommandController(controllerComponents) {

  val log: Logger = Logger(this.getClass)

  protected val PageSizeMax = 10

  def get(studySlug: Slug, ceventTypeSlug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      val ceventType = service.eventTypeBySlug(request.authInfo.userId, studySlug, ceventTypeSlug)
      validationReply(ceventType)
    }

  def getById(studyId: StudyId, eventTypeId: CollectionEventTypeId): Action[Unit] =
    action(parse.empty) { implicit request =>
      val ceventType = service.eventTypeWithId(request.authInfo.userId, studyId, eventTypeId)
      validationReply(ceventType)
    }

  def list(studySlug: Slug): Action[Unit] = {
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[CollectionEventType]]))
        },
        pagedQuery => {
          validationReply(service.listByStudySlug(request.authInfo.userId,
                                                  studySlug,
                                                  pagedQuery))
        }
      )
    }
  }

  def listNames(studyId: StudyId): Action[Unit] = {
    action.async(parse.empty) { implicit request =>
      FilterAndSortQueryHelper(request.rawQueryString).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[CollectionEventType]]))
        },
        query => {
          validationReply(service.listNamesByStudyId(request.authInfo.userId, studyId, query))
        }
      )
    }
  }

  def specimenDefinitions(studySlug: Slug): Action[Unit] = {
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[CollectionEventType]]))
        },
        pagedQuery => {
          validationReply(service.specimenDefinitionsForStudy(request.authInfo.userId, studySlug))
        }
      )
    }
  }

  def inUse(slug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.eventTypeInUse(request.authInfo.userId, slug))
    }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.snapshotRequest(request.authInfo.userId).map { _ => true })
    }

  def add(studyId: StudyId): Action[JsValue] =
    commandAction[AddCollectionEventTypeCmd](Json.obj("studyId" -> studyId))(processCommand)

  def remove(studyId: StudyId, id: CollectionEventTypeId, ver: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveCollectionEventTypeCmd(request.authInfo.userId.id, studyId.id, id.id, ver)
      val future = service.processRemoveCommand(cmd)
      validationReply(future)
    }

  def updateName(id: CollectionEventTypeId): Action[JsValue] =
    commandAction[UpdateCollectionEventTypeNameCmd](Json.obj("id" -> id))(processCommand)

  def updateDescription(id: CollectionEventTypeId): Action[JsValue] =
    commandAction[UpdateCollectionEventTypeDescriptionCmd](Json.obj("id" -> id))(processCommand)

  def updateRecurring(id: CollectionEventTypeId): Action[JsValue] =
    commandAction[UpdateCollectionEventTypeRecurringCmd](Json.obj("id" -> id))(processCommand)

  def addAnnotationType(id: CollectionEventTypeId): Action[JsValue] =
    commandAction[CollectionEventTypeAddAnnotationTypeCmd](Json.obj("id" -> id))(processCommand)

  def updateAnnotationType(id: CollectionEventTypeId, annotationTypeId: String): Action[JsValue] =
    commandAction[CollectionEventTypeUpdateAnnotationTypeCmd](
      Json.obj("id" -> id, "annotationTypeId" -> annotationTypeId))(processCommand)

  def removeAnnotationType(studyId: StudyId, id: CollectionEventTypeId, ver: Long, annotationTypeId: String)
      : Action[Unit]=
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveCollectionEventTypeAnnotationTypeCmd(
          sessionUserId         = request.authInfo.userId.id,
          studyId               = studyId.id,
          id                    = id.id,
          expectedVersion       = ver,
          annotationTypeId      = annotationTypeId)
      processCommand(cmd)
    }

  def addSpecimenDefinition(id: CollectionEventTypeId): Action[JsValue] =
    commandAction[AddCollectionSpecimenDefinitionCmd](Json.obj("id" -> id))(processCommand)

  def updateSpecimenDefinition(id: CollectionEventTypeId, sdId: String): Action[JsValue] =
    commandAction[UpdateCollectionSpecimenDefinitionCmd](
      Json.obj("id" -> id,"specimenDefinitionId" -> sdId))(processCommand)

  def removeSpecimenDefinition(studyId: StudyId, id: CollectionEventTypeId, ver: Long, sdId: String)
      : Action[Unit]=
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveCollectionSpecimenDefinitionCmd(
          sessionUserId         = request.authInfo.userId.id,
          studyId               = studyId.id,
          id                    = id.id,
          expectedVersion       = ver,
          specimenDefinitionId = sdId)
      processCommand(cmd)
    }

  private def processCommand(cmd: CollectionEventTypeCommand): Future[Result] = {
    val future = service.processCommand(cmd)
    validationReply(future)
  }
}
