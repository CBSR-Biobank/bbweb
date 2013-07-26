package controllers.study

import controllers._
import service._
import infrastructure._
import service.commands._
import domain._
import AnnotationValueType._
import domain.study._
import views._

import collection.immutable.ListMap
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.templates._
import play.api.i18n.Messages
import play.Logger
import akka.util.Timeout
import securesocial.core.{ Authorization, Identity, SecuredRequest }

import scalaz._
import Scalaz._

case class AnnotationTypeFormObject(
  annotationTypeId: String, version: Long, studyId: String, studyName: String, name: String,
  description: Option[String], valueType: String,
  maxValueCount: Option[Int] = None, selections: List[String]) {

  def getAddCmd: AddCollectionEventAnnotationTypeCmd = {
    val selectionMap = Some(selections.map(v => (v, v)).toMap)
    AddCollectionEventAnnotationTypeCmd(studyId, name, description,
      AnnotationValueType.withName(valueType), maxValueCount,
      selectionMap)
  }

  def getUpdateCmd: UpdateCollectionEventAnnotationTypeCmd = {
    val selectionMap = Some(selections.map(v => (v, v)).toMap)
    UpdateCollectionEventAnnotationTypeCmd(
      annotationTypeId, Some(version), studyId, name, description,
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
      "annotationTypeId" -> text,
      "version" -> longNumber,
      "studyId" -> text,
      "studyName" -> text,
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "valueType" -> nonEmptyText,
      "maxValueCount" -> optional(number),
      "selections" -> list(text))(AnnotationTypeFormObject.apply)(AnnotationTypeFormObject.unapply))

  private def studyBreadcrumbs(studyId: String, studyName: String) = {
    Map(
      (Messages("biobank.study.plural") -> routes.StudyController.index),
      (studyName -> routes.StudyController.showStudy(studyId)))
  }

  private def addBreadcrumbs(studyId: String, studyName: String) = {
    studyBreadcrumbs(studyId, studyName) +
      (Messages("biobank.study.collection.event.annotation.type.add") -> null)
  }

  private def updateBreadcrumbs(studyId: String, studyName: String) = {
    studyBreadcrumbs(studyId, studyName) +
      (Messages("biobank.study.collection.event.annotation.type.update") -> null)
  }

  private def removeBreadcrumbs(studyId: String, studyName: String) = {
    studyBreadcrumbs(studyId, studyName) +
      (Messages("biobank.study.collection.event.annotation.type.remove") -> null)
  }

  def index(studyId: String, studyName: String) = SecuredAction {
    implicit request =>
      val annotTypes = studyService.collectionEventAnnotationTypesForStudy(studyId)
      Ok(html.study.ceventannotationtype.show(studyId, studyName, annotTypes))
  }

  /**
   * Add an attribute type.
   */
  def addAnnotationType(studyId: String, studyName: String) = SecuredAction {
    implicit request =>
      studyService.getStudy(studyId) match {
        case Failure(x) =>
          if (x.head.contains("study does not exist")) {
            BadRequest(html.serviceError(
              Messages("biobank.annotation.type.add.error.heading"),
              Messages("biobank.study.error"),
              addBreadcrumbs(studyId, studyName)))
          } else {
            throw new Error(x.head)
          }
        case Success(study) =>
          Ok(html.study.ceventannotationtype.add(
            annotationTypeForm, AddFormType(), studyId, study.name,
            addBreadcrumbs(studyId, studyName)))
      }
  }

  def addAnnotationTypeSubmit = SecuredAction {
    implicit request =>
      annotationTypeForm.bindFromRequest.fold(
        formWithErrors => {
          // studyId and studyName are hidden values in the form, they should always be present
          val studyId = formWithErrors("studyId").value.getOrElse("")
          val studyName = formWithErrors("studyName").value.getOrElse("")

          BadRequest(html.study.ceventannotationtype.add(
            formWithErrors, AddFormType(), studyId, studyName, addBreadcrumbs(studyId, studyName)))
        },
        annotTypeForm => {
          implicit val userId = new UserId(request.user.id.id)
          val studyId = annotTypeForm.studyId
          val studyName = annotTypeForm.studyName

          Async {
            studyService.addCollectionEventAnnotationType(annotTypeForm.getAddCmd).map(validation =>
              validation match {
                case Failure(x) =>
                  if (x.head.contains("name already exists")) {
                    val form = annotationTypeForm.fill(annotTypeForm).withError("name",
                      Messages("biobank.study.collection.event.annotation.type.form.error.name"))
                    BadRequest(html.study.ceventannotationtype.add(form, AddFormType(),
                      studyId, studyName, addBreadcrumbs(studyId, studyName)))
                  } else {
                    throw new Error(x.head)
                  }
                case Success(annotType) =>
                  Redirect(routes.CeventAnnotTypeController.index(studyId, studyName)).flashing(
                    "success" -> Messages("biobank.annotation.type.added", annotType.name))
              })
          }
        })
  }

  private def badActionRequest(
    studyId: String,
    studyName: String,
    subheading: String)(implicit request: WrappedRequest[AnyContent]) = {
    BadRequest(html.serviceError(
      Messages("biobank.annotation.type.error.heading"),
      subheading,
      updateBreadcrumbs(studyId, studyName)))
  }

  /**
   * If the annotation type is not in use, the the {@link actionFunc} will be invoked.
   */
  private def checkAnnotationTypeNotInUse(
    studyId: String,
    studyName: String,
    annotationTypeId: String)(actionFunc: (String, String, String) => Result)(implicit request: WrappedRequest[AnyContent]) = {
    studyService.collectionEventAnnotationTypeInUse(studyId, annotationTypeId) match {
      case Failure(x) =>
        if (x.head.contains("study does not have collection event annotation type")) {
          badActionRequest(studyId, studyName, Messages("biobank.study.error"))
        } else if (x.head.contains("annotation type does not exist")) {
          badActionRequest(studyId, studyName, Messages("biobank.annotation.type.invalid"))
        } else {
          throw new Error(x.head)
        }
      case Success(result) =>
        if (result) {
          badActionRequest(studyId, studyName, Messages("biobank.study.specimen.group.in.use.error.message"))
        } else {
          actionFunc(studyId, studyName, annotationTypeId)
        }
    }
  }

  def updateAnnotationType(
    studyId: String,
    studyName: String,
    annotationTypeId: String) = SecuredAction { implicit request =>

    def action(studyId: String, studyName: String, annotationTypeId: String): Result = {
      studyService.collectionEventAnnotationTypeWithId(studyId, annotationTypeId) match {
        case Failure(x) => throw new Error(x.head)
        case Success(annotType) =>
          val form = annotationTypeForm.fill(AnnotationTypeFormObject(
            annotType.id.id, annotType.version, studyId, studyName, annotType.name,
            annotType.description, annotType.valueType.toString, annotType.maxValueCount,
            annotType.options.map(v => v.values.toList).getOrElse(List.empty)))
          Ok(html.study.ceventannotationtype.add(form, UpdateFormType(), studyId, studyName,
            updateBreadcrumbs(studyId, studyName)))
      }
    }

    checkAnnotationTypeNotInUse(studyId, studyName, annotationTypeId)(action)
  }

  def updateAnnotationTypeSubmit = SecuredAction { implicit request =>
    annotationTypeForm.bindFromRequest.fold(
      formWithErrors => {
        // studyId and studyName are hidden values in the form, they should always be present
        val studyId = formWithErrors("studyId").value.getOrElse("")
        val studyName = formWithErrors("studyName").value.getOrElse("")

        BadRequest(html.study.ceventannotationtype.add(
          formWithErrors, UpdateFormType(), studyId, studyName, updateBreadcrumbs(studyId, studyName)))
      },
      annotTypeForm => {
        implicit val userId = new UserId(request.user.id.id)
        val studyId = annotTypeForm.studyId
        val studyName = annotTypeForm.studyName

        Async {
          studyService.updateCollectionEventAnnotationType(annotTypeForm.getUpdateCmd).map(validation =>
            validation match {
              case Failure(x) =>
                if (x.head.contains("name already exists")) {
                  val form = annotationTypeForm.fill(annotTypeForm).withError("name",
                    Messages("biobank.study.collection.event.annotation.type.form.error.name"))
                  BadRequest(html.study.ceventannotationtype.add(
                    form, UpdateFormType(), studyId, studyName, updateBreadcrumbs(studyId, studyName)))
                } else {
                  throw new Error(x.head)
                }
              case Success(annotType) =>
                Redirect(routes.CeventAnnotTypeController.index(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.annotation.type.updated", annotType.name))
            })
        }
      })
  }

  def removeAnnotationType(
    studyId: String,
    studyName: String,
    annotationTypeId: String) = SecuredAction {
    implicit request =>

      def action(studyId: String, studyName: String, annotationTypeId: String): Result = {
        studyService.collectionEventAnnotationTypeWithId(studyId, annotationTypeId) match {
          case Failure(x) => throw new Error(x.head)
          case Success(annotType) =>
            var fields = ListMap(
              (Messages("biobank.common.name") -> annotType.name),
              (Messages("biobank.common.description") -> annotType.description.getOrElse("")),
              (Messages("biobank.annotation.type.field.value.type") -> annotType.valueType.toString))

            if (annotType.valueType == domain.AnnotationValueType.Select) {
              val value = if (annotType.maxValueCount.getOrElse(0) == 1) {
                Messages("biobank.annotation.type.field.max.value.count.single")
              } else if (annotType.maxValueCount.getOrElse(0) > 1) {
                Messages("biobank.annotation.type.field.max.value.count.multiple")
              } else {
                "<span class='label label-warning'>ERROR: " + annotType.maxValueCount + "</span>"
              }

              fields += (Messages("biobank.annotation.type.field.max.value.count") -> value)
              fields += (Messages("biobank.annotation.type.field.options") ->
                annotType.options.map(m => m.values.mkString("<br>")).getOrElse(""))
            }
            Ok(html.study.ceventannotationtype.removeConfirm(studyId, studyName, annotType, fields,
              removeBreadcrumbs(studyId, studyName)))
        }
      }

      checkAnnotationTypeNotInUse(studyId, studyName, annotationTypeId)(action)
  }

  val annotTypeDeleteForm = Form(
    tuple(
      "studyId" -> text,
      "studyName" -> text,
      "annotationTypeId" -> text))

  def removeAnnotationTypeSubmit = SecuredAction { implicit request =>
    annotTypeDeleteForm.bindFromRequest.fold(
      formWithErrors => {
        throw new Error(formWithErrors.globalErrors.mkString(","))
      },
      annotTypeForm => {
        val studyId = annotTypeForm._1
        val studyName = annotTypeForm._2
        val annotationTypeId = annotTypeForm._3

        studyService.collectionEventAnnotationTypeWithId(annotTypeForm._1, annotTypeForm._3) match {
          case Failure(x) => throw new Error(x.head)
          case Success(annotType) =>
            Async {
              implicit val userId = new UserId(request.user.id.id)
              studyService.removeCollectionEventAnnotationType(
                RemoveCollectionEventAnnotationTypeCmd(
                  annotType.id.id, annotType.versionOption, annotType.studyId.id)).map(validation =>
                  validation match {
                    case Success(annotType) =>
                      Redirect(routes.CeventAnnotTypeController.index(studyId, studyName)).flashing(
                        "success" -> Messages("biobank.study.collection.event.annotation.type.removed", annotType.name))
                    case Failure(x) =>
                      throw new Error(x.head)
                  })
            }
        }
      })
  }
}
