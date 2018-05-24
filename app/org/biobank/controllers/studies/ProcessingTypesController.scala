package org.biobank.controllers.studies

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.Slug
import org.biobank.domain.studies.{StudyId, ProcessingType, ProcessingTypeId}
import org.biobank.infrastructure.commands.ProcessingTypeCommands._
import org.biobank.services.PagedResults
import org.biobank.services.studies.ProcessingTypeService
import play.api.libs.json._
import play.api.Environment
import play.api.mvc.{Action, ControllerComponents, Result}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class ProcessingTypesController @Inject() (
  controllerComponents: ControllerComponents,
  val action:           BbwebAction,
  val env:              Environment,
  val service:          ProcessingTypeService
) (
  implicit val ec: ExecutionContext
)
    extends CommandController(controllerComponents) {

  case class UpdateJson(sessionUserId:   String,
                        studyId:         String,
                        id:              String,
                        expectedVersion: Long,
                        property:        String,
                        newValue:        JsValue)

  implicit val updateJsonReads: Reads[UpdateJson] = Json.reads[UpdateJson]

  protected val PageSizeMax = 10

  def get(studySlug: Slug, procTypeSlug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      val processingType = service.processingTypeBySlug(request.authInfo.userId, studySlug, procTypeSlug)
      validationReply(processingType)
    }

  def list(studySlug: Slug): Action[Unit] = {
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[ProcessingType]]))
        },
        pagedQuery => {
          validationReply(service.processingTypesForStudy(request.authInfo.userId, studySlug, pagedQuery))
        }
      )
    }
  }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      val reply = service.snapshotRequest(request.authInfo.userId).map { _ => true }
      validationReply(reply)
    }

  def addProcessingType(studyId: StudyId): Action[JsValue] =
    commandAction[AddProcessingTypeCmd](Json.obj("studyId" -> studyId))(processCommand)

  def update(studyId: StudyId, id: ProcessingTypeId): Action[JsValue] =
    action.async(parse.json) { request =>
      val reqJson = request.body.as[JsObject] ++ Json.obj("studyId"      -> studyId,
                                                         "id"            -> id,
                                                         "sessionUserId" -> request.authInfo.userId.id)
      reqJson.validate[UpdateJson].fold(
        errors => {
          Future.successful(BadRequest(Json.obj("status" -> "error",
                                                "message" -> "invalid json values")))
        },
        updateEntity => {
          updateEntityJsonToCommand(updateEntity).fold(
            errors => {
              val message = (JsError.toJson(errors) \ "obj" \ 0 \ "msg" \ 0).as[String]
              Future.successful(BadRequest(Json.obj("status" -> "error", "message" -> message)))
            },
            command => validationReply(service.processCommand(command))
          )
        }
      )
    }

  def removeProcessingType(studyId: StudyId, id: ProcessingTypeId, ver: Long): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveProcessingTypeCmd(request.authInfo.userId.id, studyId.id, id.id, ver)
      val future = service.processRemoveCommand(cmd)
      validationReply(future)
    }

  private def processCommand(cmd: ProcessingTypeCommand): Future[Result] = {
    val future = service.processCommand(cmd)
    validationReply(future)
  }

  private def updateEntityJsonToCommand(json: UpdateJson): JsResult[ProcessingTypeCommand] = {
    json.property match {
      case "name" =>
        json.newValue.validate[String].map { newName =>
          UpdateNameCmd(sessionUserId   = json.sessionUserId,
                        studyId         = json.studyId,
                        id              = json.id,
                        expectedVersion = json.expectedVersion,
                        name            = newName)
        }
      case "description" =>
        json.newValue.validate[String].map { newValue =>
          val descMaybe = if (newValue.isEmpty) None else Some(newValue)
          UpdateDescriptionCmd(sessionUserId   = json.sessionUserId,
                               studyId         = json.studyId,
                               id              = json.id,
                               expectedVersion = json.expectedVersion,
                               description     = descMaybe)
        }
      case "enabled" =>
        json.newValue.validate[Boolean].map { newValue =>
          UpdateEnabledCmd(sessionUserId   = json.sessionUserId,
                           studyId         = json.studyId,
                           id              = json.id,
                           expectedVersion = json.expectedVersion,
                           enabled         = newValue)
        }
      case change @ ("expectedInputChange" | "expectedOutputChange") =>
        val inputType = if (change == "expectedInputChange") specimenProcessingInput
                                 else specimenProcessingOutput
        json.newValue.validate[BigDecimal].map { newValue =>
          UpdateExpectedChangeCmd(sessionUserId   = json.sessionUserId,
                                  studyId         = json.studyId,
                                  id              = json.id,
                                  expectedVersion = json.expectedVersion,
                                  inputType       = inputType,
                                  expectedChange  = newValue)
        }
      case count @ ("inputCount" | "outputCount") =>
        val inputType = if (count == "inputCount") specimenProcessingInput
                                 else specimenProcessingOutput
        json.newValue.validate[Int].map { newValue =>
          UpdateCountCmd(sessionUserId   = json.sessionUserId,
                         studyId         = json.studyId,
                         id              = json.id,
                         expectedVersion = json.expectedVersion,
                         inputType       = inputType,
                         count           = newValue)
        }
      case container @ ("inputContainerType" | "outputContainerType") =>
        val inputType = if (container == "inputContainerType") specimenProcessingInput
                                 else specimenProcessingOutput
        json.newValue.validate[String].map { newValue =>
          val containerTypeIdMaybe = if (newValue.isEmpty) None else Some(newValue)
          UpdateContainerTypeCmd(sessionUserId   = json.sessionUserId,
                                 studyId         = json.studyId,
                                 id              = json.id,
                                 expectedVersion = json.expectedVersion,
                                 inputType       = inputType,
                                 containerTypeId = containerTypeIdMaybe)
        }
      case "inputSpecimenDefinition" =>
        json.newValue.validate[InputSpecimenProcessing].map { newValue =>
          UpdateInputSpecimenDefinitionCmd(sessionUserId        = json.sessionUserId,
                                           studyId              = json.studyId,
                                           id                   = json.id,
                                           expectedVersion      = json.expectedVersion,
                                           definitionType       = newValue.definitionType,
                                           entityId             = newValue.entityId,
                                           specimenDefinitionId = newValue.specimenDefinitionId)
        }
      case "outputSpecimenDefinition" =>
        json.newValue.validate[SpecimenDefinition].map { newValue =>
          UpdateOutputSpecimenDefinitionCmd(sessionUserId      = json.sessionUserId,
                                            studyId            = json.studyId,
                                            id                 = json.id,
                                            expectedVersion    = json.expectedVersion,
                                            specimenDefinition = newValue)
        }
      case _ =>
        JsError(JsonValidationError(s"processing type does not support updates to property ${json.property}"))
      }
  }

  def addAnnotationType(id: ProcessingTypeId): Action[JsValue] =
    commandAction[AddProcessingTypeAnnotationTypeCmd](Json.obj("id" -> id))(processCommand)

  def updateAnnotationType(id: ProcessingTypeId, annotationTypeId: String): Action[JsValue] =
    commandAction[UpdateProcessingTypeAnnotationTypeCmd](
      Json.obj("id" -> id, "annotationTypeId" -> annotationTypeId))(processCommand)

  def removeAnnotationType(studyId: StudyId, id: ProcessingTypeId, ver: Long, annotationTypeId: String)
      : Action[Unit]=
    action.async(parse.empty) { implicit request =>
      val cmd = RemoveProcessingTypeAnnotationTypeCmd(
          sessionUserId         = request.authInfo.userId.id,
          studyId               = studyId.id,
          id                    = id.id,
          expectedVersion       = ver,
          annotationTypeId      = annotationTypeId)
      processCommand(cmd)
    }

 }
