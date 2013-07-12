package controllers.study

import controllers._
import service._
import infrastructure._
import infrastructure.commands._
import domain._
import AnnotationValueType._
import domain.study._
import views._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.Logger
import akka.util.Timeout
import securesocial.core.{ Identity, Authorization }

import scalaz._
import Scalaz._

case class AnnotationTypeFormObject(
  specimenGroupId: String, version: Long, studyId: String, name: String,
  description: Option[String], valueType: String,
  maxValueCount: Option[Int] = None, selections: Option[List[String]] = None) {

  def getAddCmd: AddCollectionEventAnnotationTypeCmd = {
    val selectionMap = selections.map { x => x.map(v => (v, v)).toMap }
    AddCollectionEventAnnotationTypeCmd(studyId, name, description,
      AnnotationValueType.withName(valueType), maxValueCount,
      selectionMap)
  }

  def getUpdateCmd: UpdateCollectionEventAnnotationTypeCmd = {
    val selectionMap = selections.map { x => x.map(v => (v, v)).toMap }
    UpdateCollectionEventAnnotationTypeCmd(
      specimenGroupId, Some(version), studyId, name, description,
      AnnotationValueType.withName(valueType), maxValueCount,
      selectionMap)
  }
}

object CollectionEventAnnotationTypeSelections {
  val annotationValueTypes = Seq("" -> Messages("biobank.form.selection.default")) ++
    AnnotationValueType.values.map(x =>
      x.toString -> Messages("biobank.enumaration.annotation.value.type." + x.toString)).toSeq
}

object CeventAnnotTypeController extends Controller with securesocial.core.SecureSocial {

  lazy val studyService = Global.services.studyService

  val annotationTypeForm = Form(
    mapping(
      "specimenGroupId" -> text,
      "version" -> longNumber,
      "studyId" -> text,
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "valueType" -> nonEmptyText,
      "maxValueCount" -> optional(number),
      "options" -> optional(list(text)))(AnnotationTypeFormObject.apply)(AnnotationTypeFormObject.unapply))

  def index(studyId: String, studyName: String) = SecuredAction { implicit request =>
    studyService.getCollectionEventAnnotationTypes(studyId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(attrSet) =>
        Ok(views.html.study.showCollectionEventAnnotationTypes(studyId, studyName, attrSet))
    }
  }

  /**
   * Add an attribute type.
   */
  def addAnnotationType(studyId: String) = SecuredAction { implicit request =>
    studyService.getStudy(studyId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(study) =>
        Ok(html.study.addCollectionEventAnnotationType(annotationTypeForm, AddFormType(), studyId, study.name))
    }
  }

  def addAnnotationTypeSubmit(studyId: String, studyName: String) = SecuredAction { implicit request =>
    annotationTypeForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.study.addCollectionEventAnnotationType(
        formWithErrors, AddFormType(), studyId, studyName)),
      annotTypeForm => {
        Async {
          Logger.debug("annotTypeForm: " + annotTypeForm)
          implicit val userId = new UserId(request.user.id.id)
          studyService.addCollectionEventAnnotationType(annotTypeForm.getAddCmd).map(validation =>
            validation match {
              case Success(annotType) =>
                Redirect(routes.CeventAnnotTypeController.index(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.annotation.type.added",
                    annotType.name))
              case Failure(x) =>
                if (x.head.contains("name already exists")) {
                  val form = annotationTypeForm.fill(annotTypeForm).withError("name",
                    Messages("biobank.study.specimengroup.form.error.name"))
                  BadRequest(html.study.addCollectionEventAnnotationType(form, AddFormType(),
                    studyId, studyName))
                } else {
                  throw new Error(x.head)
                }
            })
        }
      })
  }

  def updateAnnotationType(studyId: String, studyName: String, specimenGroupId: String) = SecuredAction { implicit request =>
    ???
  }

  def updateAnnotationTypeSubmit(studyId: String, studyName: String) = SecuredAction { implicit request =>
    ???
  }

  def removeAnnotationTypeConfirm(studyId: String,
    studyName: String,
    specimenGroupId: String) = SecuredAction { implicit request =>
    ???
  }

  def removeAnnotationType(studyId: String,
    studyName: String,
    sgId: String) = SecuredAction { implicit request =>
    ???
  }
}
