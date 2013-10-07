package controllers.study

import controllers._
import service._
import infrastructure._
import service.{ ServiceComponent, ServiceComponentImpl }
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
import securesocial.core.{ Authorization, Identity, SecuredRequest, SecureSocial }

import scalaz._
import Scalaz._

/**
 * Used to map the form inputs into an object that can be saved via a service call.
 */
trait StudyAnnotationTypeMapper {
  val annotationTypeId: String
  val version: Long
  val studyId: String
  val studyName: String
  val name: String
  val description: Option[String]
  val valueType: String
  val maxValueCount: Option[Int]
  val selections: List[String]

  /**
   * Returns a command that can be used to save the annotation type using a service call.
   */
  def getAddCmd: StudyAnnotationTypeCommand

  /**
   * Returns a command that can be used to update the annotation type using a service call.
   */
  def getUpdateCmd: StudyAnnotationTypeCommand
}

trait StudyAnnotationTypeController[A <: StudyAnnotationType] extends Controller with SecureSocial {

  lazy val studyService = WebComponent.studyService

  private val studyNoAnnotationTypePattern = "study does not have .* annotation type".r

  protected def studyBreadcrumbs(studyId: String, studyName: String): Map[String, Call]

  protected def addBreadcrumbs(studyId: String, studyName: String): Map[String, Call]

  protected def updateBreadcrumbs(studyId: String, studyName: String): Map[String, Call]

  protected def removeBreadcrumbs(studyId: String, studyName: String): Map[String, Call]

  protected def isAnnotationTypeInUse(
    studyId: String,
    annotationTypeId: String): DomainValidation[Boolean]

  protected def annotationTypeInUseErrorMsg(annotName: String): String

  protected def validMaxValue(valueType: AnnotationValueType, maxValueCount: Option[Int]): Boolean = {
    // if value type is "Select" the ensure max value count is valid          
    if (valueType == AnnotationValueType.Select) {
      maxValueCount match {
        case None => false
        case Some(x) => if (1 to 2 contains x) true else false
      }
    } else {
      true
    }
  }

  def validateAnnotationType(
    studyId: String,
    studyName: String,
    annotationType: DomainValidation[A],
    breadcrumbs: Map[String, Call])(f: (String, String, A) => Result)(
      implicit request: WrappedRequest[AnyContent]): Result = {
    annotationType match {
      case Failure(x) =>
        x.head match {
          case studyNoAnnotationTypePattern() =>
            badActionRequested(studyId, studyName, Messages("biobank.study.error"), breadcrumbs)
          case _ =>
            if (x.head.contains("annotation type does not exist")) {
              badActionRequested(studyId, studyName, Messages("biobank.annotation.type.invalid"), breadcrumbs)
            } else {
              throw new Error(x.head)
            }
        }
      case Success(at) =>
        f(studyId, studyName, at)
    }
  }

  /**
   * If the annotation type is not in use, the the {@link actionFunc} will be invoked.
   */
  protected def checkAnnotationTypeNotInUse(
    studyId: String,
    studyName: String,
    annotationType: DomainValidation[A],
    breadcrumbs: Map[String, Call])(actionFunc: (String, String, A) => Result)(
      implicit request: WrappedRequest[AnyContent]): Result = {
    validateAnnotationType(studyId, studyName, annotationType, breadcrumbs)(
      (studyId, studyName, at) =>
        isAnnotationTypeInUse(studyId, at.id.id) match {
          case Failure(x) =>
            throw new Error(x.head)
          case Success(result) =>
            if (result) {
              badActionRequested(studyId, studyName, annotationTypeInUseErrorMsg(at.name), breadcrumbs)
            } else {
              actionFunc(studyId, studyName, at)
            }
        })
  }

  protected def annotationTypeFieldsMap(annotationType: A): ListMap[String, String] = {
    var fields = ListMap(
      (Messages("biobank.common.name") -> annotationType.name),
      (Messages("biobank.common.description") -> annotationType.description.getOrElse("")),
      (Messages("biobank.annotation.type.field.value.type") -> annotationType.valueType.toString))

    if (annotationType.valueType == domain.AnnotationValueType.Select) {
      val value = if (annotationType.maxValueCount.getOrElse(0) == 1) {
        Messages("biobank.annotation.type.field.max.value.count.single")
      } else if (annotationType.maxValueCount.getOrElse(0) > 1) {
        Messages("biobank.annotation.type.field.max.value.count.multiple")
      } else {
        "<span class='label label-warning'>ERROR: " + annotationType.maxValueCount + "</span>"
      }

      fields += (Messages("biobank.annotation.type.field.max.value.count") -> value)
      fields += (Messages("biobank.annotation.type.field.options") ->
        annotationType.options.map(m => m.values.mkString("<br>")).getOrElse(""))
    }
    fields
  }

  protected def badActionRequested(
    studyId: String,
    studyName: String,
    subheading: String,
    breadcrumbs: Map[String, Call])(implicit request: WrappedRequest[AnyContent]) = {
    BadRequest(html.serviceError(
      Messages("biobank.annotation.type.error.heading"),
      subheading,
      breadcrumbs,
      routes.StudyController.showStudy(studyId)))
  }

  /**
   * Add an attribute type.
   */
  protected def addAnnotationType[T <: StudyAnnotationTypeMapper](
    studyId: String,
    studyName: String)(
      f: Study => Result)(
        implicit request: WrappedRequest[AnyContent]): Result = {
    StudyController.validateStudy(studyId, Messages("biobank.annotation.type.add.error.heading"))(
      study => f(study))

  }

  protected def addAnnotationTypeSubmit[T <: StudyAnnotationTypeMapper](
    annotationTypeForm: Form[T])(
      f: T => Result)(
        implicit request: WrappedRequest[AnyContent]): Result = {
    annotationTypeForm.bindFromRequest.fold(
      formWithErrors => {
        // studyId and studyName are hidden values in the form, they should always be present
        val studyId = formWithErrors("studyId").value.getOrElse("")
        val studyName = formWithErrors("studyName").value.getOrElse("")

        BadRequest(html.study.ceventannotationtype.add(
          formWithErrors, AddFormType(), studyId, studyName, addBreadcrumbs(studyId, studyName)))
      },
      submittedForm => {
        val studyId = submittedForm.studyId
        val studyName = submittedForm.studyName

        if (!validMaxValue(AnnotationValueType.withName(submittedForm.valueType),
          submittedForm.maxValueCount)) {
          val form = annotationTypeForm.fill(submittedForm).withError("maxValueCount",
            Messages("biobank.annotation.type.form.max.value.count.error"))
          BadRequest(html.study.ceventannotationtype.add(
            form, AddFormType(), studyId, studyName, updateBreadcrumbs(studyId, studyName)))
        } else {
          f(submittedForm)
        }
      })
  }

  protected def updateAnnotationType(
    studyId: String,
    studyName: String,
    annotationType: DomainValidation[A])(
      actionFunc: (String, String, A) => Result)(
        implicit request: WrappedRequest[AnyContent]): Result = {
    checkAnnotationTypeNotInUse(
      studyId,
      studyName,
      annotationType,
      updateBreadcrumbs(studyId, studyName))(actionFunc)
  }

  protected def updateAnnotationTypeSubmit[T <: StudyAnnotationTypeMapper](
    annotationTypeForm: Form[T])(f: T => Result)(
      implicit request: WrappedRequest[AnyContent]): Result = {
    annotationTypeForm.bindFromRequest.fold(
      formWithErrors => {
        // studyId and studyName are hidden values in the form, they should always be present
        val studyId = formWithErrors("studyId").value.getOrElse("")
        val studyName = formWithErrors("studyName").value.getOrElse("")

        BadRequest(html.study.ceventannotationtype.add(
          formWithErrors, UpdateFormType(), studyId, studyName, updateBreadcrumbs(studyId, studyName)))
      },
      submittedForm => {
        val studyId = submittedForm.studyId
        val studyName = submittedForm.studyName

        if (!validMaxValue(AnnotationValueType.withName(submittedForm.valueType),
          submittedForm.maxValueCount)) {
          val form = annotationTypeForm.fill(submittedForm).withError("maxValueCount",
            Messages("biobank.annotation.type.form.max.value.count.error"))
          BadRequest(html.study.ceventannotationtype.add(
            form, UpdateFormType(), studyId, studyName, updateBreadcrumbs(studyId, studyName)))
        } else {
          f(submittedForm)
        }
      })
  }

  protected def removeAnnotationType(
    studyId: String,
    studyName: String,
    annotationType: DomainValidation[A])(
      actionFunc: (String, String, A) => Result)(
        implicit request: WrappedRequest[AnyContent]): Result = {

    checkAnnotationTypeNotInUse(
      studyId,
      studyName,
      annotationType,
      removeBreadcrumbs(studyId, studyName))(actionFunc)
  }

  private val annotTypeDeleteForm = Form(
    tuple(
      "studyId" -> text,
      "studyName" -> text,
      "annotationTypeId" -> text))

  protected def removeAnnotationTypeSubmit(f: (String, String, String) => Result)(
    implicit request: WrappedRequest[AnyContent]): Result = {
    annotTypeDeleteForm.bindFromRequest.fold(
      formWithErrors => {
        throw new Error(formWithErrors.globalErrors.mkString(","))
      },
      annotTypeForm => {
        val studyId = annotTypeForm._1
        val studyName = annotTypeForm._2
        val annotationTypeId = annotTypeForm._3

        f(studyId, studyName, annotationTypeId)
      })
  }
}
