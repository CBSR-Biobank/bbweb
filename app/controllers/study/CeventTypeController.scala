package controllers.study

import controllers._
import service.commands._
import domain._
import domain.study._
import views._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.Logger

import scalaz._
import Scalaz._

/**
 * This object is needed since it handles the Add and Update commands. It is used by the form to access the
 * data for adding or updating a CollectionEventType.
 *
 * @param collectionEventTypeId
 * @param version
 * @param studyId
 * @param name
 * @param description
 * @param recurring
 * @param specimenGroupData
 * @param annotationTypeData
 */
case class CeventTypeFormObject(
  collectionEventTypeId: String, version: Long, studyId: String, name: String,
  description: Option[String], recurring: Boolean,
  specimenGroupData: List[SpecimenGroupCollectionEventType],
  annotationTypeData: List[CollectionEventTypeAnnotationType]) {

  def getAddCmd: AddCollectionEventTypeCmd = {
    AddCollectionEventTypeCmd(studyId, name, description, recurring,
      specimenGroupData.toSet, annotationTypeData.toSet)
  }

  def getUpdateCmd: UpdateCollectionEventTypeCmd = {
    UpdateCollectionEventTypeCmd(
      collectionEventTypeId, Some(version), studyId, name, description, recurring,
      specimenGroupData.toSet, annotationTypeData.toSet)
  }
}

object CollectionEventTypeSelections {
  val annotationValueTypes = Seq("" -> Messages("biobank.form.selection.default")) ++
    AnnotationValueType.values.map(x =>
      x.toString -> Messages("biobank.enumaration.annotation.value.type." + x.toString)).toSeq
}

object CeventTypeController extends Controller with securesocial.core.SecureSocial {

  lazy val studyService = Global.services.studyService

  val ceventTypeForm = Form(
    mapping(
      "collectionEventTypeId" -> text,
      "version" -> longNumber,
      "studyId" -> text,
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "recurring" -> boolean,
      "specimenGroupData" -> list(mapping(
        "specimenGroupId" -> text,
        "specimenGroupCount" -> number,
        "specimenGroupAmount" -> bigDecimal)(
          SpecimenGroupCollectionEventType.apply)(SpecimenGroupCollectionEventType.unapply)),
      "annotationTypeData" -> list(mapping(
        "annotationTypeId" -> text,
        "annotationTypeRequired" -> boolean)(
          CollectionEventTypeAnnotationType.apply)(CollectionEventTypeAnnotationType.unapply)))(
        CeventTypeFormObject.apply)(CeventTypeFormObject.unapply))

  def index(studyId: String, studyName: String) = SecuredAction { implicit request =>
    val ceventTypes = studyService.getCollectionEventTypes(studyId)
    Ok(html.study.ceventtype.show(studyId, studyName, ceventTypes))
  }

  private def specimenGroupInfo(studyId: String) = {
    studyService.getSpecimenGroups(studyId).map(x => (x.id.id, x.name, x.units)).toSeq
  }

  private def annotationTypeInfo(studyId: String) = {
    studyService.getCollectionEventAnnotationTypes(studyId).map(x => (x.id.id, x.name)).toSeq
  }

  /**
   * Add an attribute type.
   */
  def addCeventType(studyId: String) = SecuredAction { implicit request =>
    studyService.getStudy(studyId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(study) =>
        Ok(html.study.ceventtype.add(ceventTypeForm, AddFormType(), studyId, study.name,
          specimenGroupInfo(studyId), annotationTypeInfo(studyId)))
    }
  }

  def addCeventTypeSubmit(studyId: String, studyName: String) = SecuredAction { implicit request =>
    ceventTypeForm.bindFromRequest.fold(
      formWithErrors => {
        Logger.debug("addCeventTypeSubmit: formWithErrors: " + formWithErrors)
        BadRequest(html.study.ceventtype.add(formWithErrors, AddFormType(), studyId, studyName,
          specimenGroupInfo(studyId), annotationTypeInfo(studyId)))
      },
      submittedForm => {
        Async {
          Logger.debug("ceventTypeForm: " + ceventTypeForm)
          implicit val userId = new UserId(request.user.id.id)
          studyService.addCollectionEventType(submittedForm.getAddCmd).map(validation =>
            validation match {
              case Success(ceventType) =>
                Redirect(routes.CeventTypeController.index(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.study.collection.event.type.added", ceventType.name))
              case Failure(x) =>
                if (x.head.contains("name already exists")) {
                  val form = ceventTypeForm.fill(submittedForm).withError("name",
                    Messages("biobank.study.collection.event.type.form.error.name"))
                  Logger.debug("bad name: " + form)
                  BadRequest(html.study.ceventtype.add(form, AddFormType(),
                    studyId, studyName, specimenGroupInfo(studyId), annotationTypeInfo(studyId)))
                } else {
                  throw new Error(x.head)
                }
            })
        }
      })
  }

  def updateCeventType(studyId: String, studyName: String, ceventTypeId: String) = SecuredAction { implicit request =>
    studyService.getCollectionEventType(studyId, ceventTypeId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(ceventType) =>
        Logger.info("*** " + ceventType)
        val form = ceventTypeForm.fill(CeventTypeFormObject(
          ceventType.id.id, ceventType.version, ceventType.studyId.id, ceventType.name,
          ceventType.description, ceventType.recurring, ceventType.specimenGroupData.toList,
          ceventType.annotationTypeData.toList))
        Ok(html.study.ceventtype.add(form, UpdateFormType(), studyId, studyName,
          specimenGroupInfo(studyId), annotationTypeInfo(studyId)))
    }
  }

  def updateCeventTypeSubmit(studyId: String, studyName: String) = SecuredAction { implicit request =>
    ceventTypeForm.bindFromRequest.fold(
      formWithErrors => {
        //Logger.debug("updateCeventTypeSubmit: formWithErrors: " + formWithErrors)
        BadRequest(html.study.ceventtype.add(
          formWithErrors, UpdateFormType(), studyId, studyName, specimenGroupInfo(studyId),
          annotationTypeInfo(studyId)))
      },
      submittedForm => {
        Async {
          implicit val userId = new UserId(request.user.id.id)
          studyService.updateCollectionEventType(submittedForm.getUpdateCmd).map(validation =>
            validation match {
              case Success(ceventType) =>
                Redirect(routes.CeventTypeController.index(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.study.collection.event.type.updated", ceventType.name))
              case Failure(x) =>
                if (x.head.contains("name already exists")) {
                  val form = ceventTypeForm.fill(submittedForm).withError("name",
                    Messages("biobank.study.collection.event.type.form.error.name"))
                  BadRequest(html.study.ceventtype.add(
                    form, UpdateFormType(), studyId, studyName, specimenGroupInfo(studyId),
                    annotationTypeInfo(studyId)))
                } else {
                  throw new Error(x.head)
                }
            })
        }
      })
  }

  def removeCeventTypeConfirm(studyId: String,
    studyName: String,
    ceventTypeId: String) = SecuredAction { implicit request =>
    studyService.getCollectionEventType(studyId, ceventTypeId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(ceventType) =>
        Ok(html.study.ceventtype.removeConfirm(studyId, studyName, ceventType))
    }
  }

  def removeCeventType(studyId: String,
    studyName: String,
    ceventTypeId: String) = SecuredAction { implicit request =>
    studyService.getCollectionEventType(studyId, ceventTypeId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(ceventType) =>
        Async {
          implicit val userId = new UserId(request.user.id.id)
          studyService.removeCollectionEventType(
            RemoveCollectionEventTypeCmd(
              ceventType.id.id, ceventType.versionOption, ceventType.studyId.id)).map(validation =>
              validation match {
                case Success(ceventType) =>
                  Redirect(routes.CeventTypeController.index(studyId, studyName)).flashing(
                    "success" -> Messages("biobank.study.collection.event.type.removed", ceventType.name))
                case Failure(x) =>
                  throw new Error(x.head)
              })
        }
    }
  }
}
