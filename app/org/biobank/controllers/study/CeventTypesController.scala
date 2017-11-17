package org.biobank.controllers.study

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.study.{StudyId, CollectionEventTypeId}
import org.biobank.dto.NameDto
import org.biobank.infrastructure.command.CollectionEventTypeCommands._
import org.biobank.service.PagedResults
import org.biobank.service.studies.CollectionEventTypeService
import play.api.libs.json._
import play.api.{ Environment, Logger }
import play.api.mvc.{Action, ControllerComponents, Result}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class CeventTypesController @Inject() (controllerComponents: ControllerComponents,
                                       val action:  BbwebAction,
                                       val env:     Environment,
                                       val service: CollectionEventTypeService)
                                   (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 10

  def get(studyId: StudyId, ceventTypeId: CollectionEventTypeId): Action[Unit] =
    action(parse.empty) { implicit request =>
      log.debug(s"CeventTypeController.list: studyId: $studyId, ceventTypeId: $ceventTypeId")
      val ceventType = service.collectionEventTypeWithId(request.authInfo.userId, studyId, ceventTypeId)
      validationReply(ceventType)
    }

  def list(studyId: StudyId): Action[Unit] = {
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            pagedQuery  <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            ceventTypes <- service.list(request.authInfo.userId,
                                       studyId,
                                       pagedQuery.filter,
                                       pagedQuery.sort)
            validPage   <- pagedQuery.validPage(ceventTypes.size)
            results     <- PagedResults.create(ceventTypes, pagedQuery.page, pagedQuery.limit)
          } yield results
        }
      )
    }
  }

  // returns all the names of the collection events in a list of NameDto.
  def listNames(studyId: StudyId): Action[Unit] = {
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            filterAndSort <- FilterAndSortQuery.create(request.rawQueryString)
            ceventTypes   <- service.list(request.authInfo.userId,
                                          studyId,
                                          filterAndSort.filter,
                                          filterAndSort.sort)
          } yield {
            ceventTypes.map(et => NameDto(et.id.id, et.name))
          }
        }
      )
    }
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

  def addSpecimenDescription(id: CollectionEventTypeId): Action[JsValue] =
    commandAction[AddCollectionSpecimenDescriptionCmd](Json.obj("id" -> id))(processCommand)

  def updateSpecimenDescription(id: CollectionEventTypeId, sdId: String): Action[JsValue] =
    commandAction[UpdateCollectionSpecimenDescriptionCmd](
      Json.obj("id" -> id,"specimenDescriptionId" -> sdId))(processCommand)

  def removeSpecimenDescription(studyId: StudyId, id: CollectionEventTypeId, ver: Long, sdId: String)
      : Action[Unit]=
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveCollectionSpecimenDescriptionCmd(
          sessionUserId         = request.authInfo.userId.id,
          studyId               = studyId.id,
          id                    = id.id,
          expectedVersion       = ver,
          specimenDescriptionId = sdId)
      processCommand(cmd)
    }

  def inUse(id: CollectionEventTypeId): Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.collectionEventTypeInUse(request.authInfo.userId, id))
    }

  private def processCommand(cmd: CollectionEventTypeCommand): Future[Result] = {
    val future = service.processCommand(cmd)
    validationReply(future)
  }
}
