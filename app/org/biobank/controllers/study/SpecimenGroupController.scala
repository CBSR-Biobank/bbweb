package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.{ ServiceComponent, TopComponentImpl }
import org.biobank.domain._
import org.biobank.domain.study._
import views._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import StudyTab._

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
import play.api.libs.json._
import akka.util.Timeout
import securesocial.core.{ Identity, Authorization, SecureSocial }

import scalaz._
import Scalaz._

case class SpecimenGroupFormObject(
  specimenGroupId: String, version: Long, studyId: String, studyName: String, name: String,
  description: Option[String], units: String, anatomicalSourceType: String, preservationType: String,
  preservationTemperatureType: String, specimenType: String) {

  def getAddCmd: AddSpecimenGroupCmd = {
    AddSpecimenGroupCmd(studyId, name, description, units,
      AnatomicalSourceType.withName(anatomicalSourceType),
      PreservationType.withName(preservationType),
      PreservationTemperatureType.withName(preservationTemperatureType),
      SpecimenType.withName(specimenType))
  }

  def getUpdateCmd: UpdateSpecimenGroupCmd = {
    UpdateSpecimenGroupCmd(studyId, specimenGroupId, some(version), name, description, units,
      AnatomicalSourceType.withName(anatomicalSourceType),
      PreservationType.withName(preservationType),
      PreservationTemperatureType.withName(preservationTemperatureType),
      SpecimenType.withName(specimenType))
  }
}

object SpecimenGroupSelections {
  val anatomicalSourceTypes = Seq("" -> Messages("biobank.form.selection.default")) ++
    AnatomicalSourceType.values.map(x => (x.toString -> x.toString)).toSeq

  val preservationTypes = Seq("" -> Messages("biobank.form.selection.default")) ++
    PreservationType.values.map(x => (x.toString -> x.toString)).toSeq

  val preservationTemperatureTypes = Seq("" -> Messages("biobank.form.selection.default")) ++
    PreservationTemperatureType.values.map(x => (x.toString -> x.toString)).toSeq

  val specimenTypes = Seq("" -> Messages("biobank.form.selection.default")) ++
    SpecimenType.values.map(x => (x.toString -> x.toString)).toSeq
}

/**
 * Handles all operations user can perform on a Specimen Group.
 */
object SpecimenGroupController extends Controller with SecureSocial {

  private lazy val studyService = WebComponent.studyService

  private val specimenGroupForm = Form(
    mapping(
      "specimenGroupId" -> text,
      "version" -> longNumber,
      "studyId" -> text,
      "studyName" -> text,
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "units" -> nonEmptyText,
      "anatomicalSourceType" -> nonEmptyText,
      "preservationType" -> nonEmptyText,
      "preservationTemperatureType" -> nonEmptyText,
      "specimenType" -> nonEmptyText)(SpecimenGroupFormObject.apply)(SpecimenGroupFormObject.unapply))

  private def studyBreadcrumbs(studyId: String, studyName: String) = {
    Map(
      (Messages("biobank.study.plural") -> routes.StudyController.index),
      (studyName -> routes.StudyController.showStudy(studyId)))
  }

  private def addBreadcrumbs(studyId: String, studyName: String) = {
    studyBreadcrumbs(studyId, studyName) +
      (Messages("biobank.study.specimen.group.add") -> null)
  }

  private def updateBreadcrumbs(studyId: String, studyName: String) = {
    studyBreadcrumbs(studyId, studyName) +
      (Messages("biobank.study.specimen.group.update") -> null)
  }

  private def removeBreadcrumbs(studyId: String, studyName: String) = {
    studyBreadcrumbs(studyId, studyName) +
      (Messages("biobank.study.specimen.group.remove") -> null)
  }

  /**
   * Add a specimen group.
   */
  def addSpecimenGroup(studyId: String, studyName: String) = SecuredAction { implicit request =>
    StudyController.validateStudy(studyId, Messages("biobank.study.specimen.group.add.error.heading"))(
      study => {
        Ok(html.study.specimengroup.add(specimenGroupForm, AddFormType(), studyId, study.name,
          addBreadcrumbs(study.id.id, study.name)))
      })
  }

  def addSpecimenGroupSubmit = SecuredAction.async { implicit request =>
    specimenGroupForm.bindFromRequest.fold(
      formWithErrors => {
        // studyId and studyName are hidden values in the form, they should always be present
        val studyId = formWithErrors("studyId").value.getOrElse("")
        val studyName = formWithErrors("studyName").value.getOrElse("")

        Future(BadRequest(html.study.specimengroup.add(
          formWithErrors, AddFormType(), studyId, studyName, addBreadcrumbs(studyId, studyName))))
      },
      sgForm => {
        implicit val userId = new UserId(request.user.identityId.userId)
        val studyId = sgForm.studyId
        val studyName = sgForm.studyName

        studyService.addSpecimenGroup(sgForm.getAddCmd).map(validation =>
          validation match {
            case Failure(x) =>
              if (x.head.contains("name already exists")) {
                val form = specimenGroupForm.fill(sgForm).withError("name",
                  Messages("biobank.study.specimen.group.form.error.name"))
                BadRequest(html.study.specimengroup.add(form, AddFormType(), studyId, studyName,
                  addBreadcrumbs(studyId, studyName)))
              } else {
                throw new Error(x.head)
              }
            case Success(sg) =>
              Redirect(routes.StudyController.showStudy(studyId)).flashing(
                "success" -> Messages("biobank.study.specimen.group.added", sg.name))
          })
      })
  }

  private def badActionRequest(
    studyId: String,
    studyName: String,
    subheading: String)(implicit request: WrappedRequest[AnyContent]) = {
    BadRequest(html.serviceError(
      Messages("biobank.study.specimen.group.error.heading"),
      subheading,
      updateBreadcrumbs(studyId, studyName),
      routes.StudyController.showStudy(studyId)))
  }

  def validateSpecimenGroup(
    studyId: String,
    studyName: String,
    specimenGroup: DomainValidation[SpecimenGroup])(f: (String, String, SpecimenGroup) => Result)(
      implicit request: WrappedRequest[AnyContent]): Result = {
    specimenGroup match {
      case Failure(x) =>
        if (x.head.contains("study does not have specimen group")) {
          badActionRequest(studyId, studyName, Messages("biobank.study.error"))
        } else if (x.head.contains("specimen group does not exist")) {
          badActionRequest(studyId, studyName, Messages("biobank.study.specimen.group.invalid"))
        } else {
          throw new Error(x.head)
        }
      case Success(sg) =>
        f(studyId, studyName, sg)
    }
  }

  /**
   * If the annotation type is not in use, the the {@link actionFunc} will be invoked.
   */
  private def checkSpecimenGroupNotInUse(
    studyId: String,
    studyName: String,
    specimenGroup: DomainValidation[SpecimenGroup])(
      actionFunc: (String, String, SpecimenGroup) => Result)(
        implicit request: WrappedRequest[AnyContent]) = {
    validateSpecimenGroup(studyId, studyName, specimenGroup) {
      (studyId, studyName, specimenGroup) =>
        studyService.specimenGroupInUse(studyId, specimenGroup.id.id) match {
          case Failure(err) => throw new Error(err.list.mkString(", "))
          case Success(result) =>
            if (result) {
              badActionRequest(studyId, studyName,
                Messages("biobank.study.specimen.group.in.use.error.message", specimenGroup.name))
            } else {
              actionFunc(studyId, studyName, specimenGroup)
            }
        }
    }

  }

  def updateSpecimenGroup(
    studyId: String,
    studyName: String,
    specimenGroupId: String) = SecuredAction { implicit request =>
    val specimenGroup = studyService.specimenGroupWithId(studyId, specimenGroupId)
    checkSpecimenGroupNotInUse(studyId, studyName, specimenGroup) {
      (studyId, studyName, sg) =>
        val form = specimenGroupForm.fill(SpecimenGroupFormObject(
          sg.id.id, sg.version, studyId, studyName, sg.name, sg.description, sg.units,
          sg.anatomicalSourceType.toString, sg.preservationType.toString,
          sg.preservationTemperatureType.toString, sg.specimenType.toString))
        Ok(html.study.specimengroup.add(form, UpdateFormType(), studyId, studyName,
          updateBreadcrumbs(studyId, studyName)))
    }
  }

  def updateSpecimenGroupSubmit = SecuredAction.async { implicit request =>
    specimenGroupForm.bindFromRequest.fold(
      formWithErrors => {
        // studyId and studyName are hidden values in the form, they should always be present
        val studyId = formWithErrors("studyId").value.getOrElse("")
        val studyName = formWithErrors("studyName").value.getOrElse("")

        Future(BadRequest(html.study.specimengroup.add(
          formWithErrors,
          UpdateFormType(),
          studyId,
          studyName,
          updateBreadcrumbs(studyId, studyName))))
      },
      sgForm => {
        implicit val userId = new UserId(request.user.identityId.userId)
        val studyId = sgForm.studyId
        val studyName = sgForm.studyName

        studyService.updateSpecimenGroup(sgForm.getUpdateCmd).map(validation =>
          validation match {
            case Failure(x) =>
              if (x.head.contains("name already exists")) {
                val form = specimenGroupForm.fill(sgForm).withError("name",
                  Messages("biobank.study.specimen.group.form.error.name"))
                BadRequest(html.study.specimengroup.add(form, UpdateFormType(), studyId, studyName,
                  updateBreadcrumbs(studyId, studyName)))
              } else {
                throw new Error(x.head)
              }
            case Success(sg) =>
              Redirect(routes.StudyController.showStudy(studyId)).flashing(
                "success" -> Messages("biobank.study.specimen.group.updated", sg.name))
          })
      })
  }

  def removeSpecimenGroup(
    studyId: String,
    studyName: String,
    specimenGroupId: String) = SecuredAction { implicit request =>
    val specimenGroup = studyService.specimenGroupWithId(studyId, specimenGroupId)
    checkSpecimenGroupNotInUse(studyId, studyName, specimenGroup) {
      (studyId, studyName, sg) =>
        Ok(views.html.study.specimengroup.removeConfirm(studyId, studyName, sg,
          removeBreadcrumbs(studyId, studyName)))
    }
  }

  val specimenGroupDeleteForm = Form(
    tuple(
      "studyId" -> text,
      "studyName" -> text,
      "specimenGroupId" -> text))

  def removeSpecimenGroupSubmit = SecuredAction.async { implicit request =>
    specimenGroupDeleteForm.bindFromRequest.fold(
      formWithErrors =>
        throw new Error(formWithErrors.globalError.toString),
      sgForm => {
        val studyId = sgForm._1
        val studyName = sgForm._2
        val specimenGroupId = sgForm._3

        studyService.specimenGroupWithId(studyId, specimenGroupId) match {
          case Failure(err) => throw new Error(err.list.mkString(", "))
          case Success(sg) =>
            implicit val userId = new UserId(request.user.identityId.userId)
            studyService.removeSpecimenGroup(RemoveSpecimenGroupCmd(
              sg.studyId.id, sg.id.id, sg.versionOption)).map(validation =>
              validation match {
                case Success(sgRemoved) =>
                  Redirect(routes.StudyController.showStudy(studyId)).flashing(
                    "success" -> Messages("biobank.study.specimen.group.removed", sg.name))
                case Failure(x) =>
                  throw new Error(x.head)
              })
        }
      })
  }
}