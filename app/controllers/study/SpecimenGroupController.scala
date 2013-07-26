package controllers.study

import controllers._
import service._
import infrastructure._
import service.commands._
import domain._
import domain.study._
import views._
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._

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
    UpdateSpecimenGroupCmd(specimenGroupId, some(version), studyId, name, description, units,
      AnatomicalSourceType.withName(anatomicalSourceType),
      PreservationType.withName(preservationType),
      PreservationTemperatureType.withName(preservationTemperatureType),
      SpecimenType.withName(specimenType))
  }
}

/**
 * Handles all operations user can perform on a Specimen Group.
 */
object SpecimenGroupController extends Controller with securesocial.core.SecureSocial {

  lazy val studyService = Global.services.studyService

  val specimenGroupForm = Form(
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

  def index(studyId: String, studyName: String) = SecuredAction { implicit request =>
    studyService.getStudy(studyId) match {
      case Failure(x) =>
        if (x.head.contains("study does not exist")) {
          BadRequest(html.serviceError(
            Messages("biobank.study.specimen.group.error.heading"),
            Messages("biobank.study.error"),
            addBreadcrumbs(studyId, studyName)))
        } else {
          throw new Error(x.head)
        }
      case Success(study) =>
        val specimenGroups = studyService.specimenGroupsForStudy(studyId)
        Ok(html.study.specimengroup.show(studyId, studyName, specimenGroups))
    }
  }

  /**
   * Add a specimen group.
   */
  def addSpecimenGroup(studyId: String, studyName: String) = SecuredAction { implicit request =>
    studyService.getStudy(studyId) match {
      case Failure(x) =>
        if (x.head.contains("study does not exist")) {
          BadRequest(html.serviceError(
            Messages("biobank.study.specimen.group.add.error.heading"),
            Messages("biobank.study.error"),
            addBreadcrumbs(studyId, studyName)))
        } else {
          throw new Error(x.head)
        }
      case Success(study) =>
        Ok(html.study.specimengroup.add(specimenGroupForm, AddFormType(), studyId, study.name,
          addBreadcrumbs(study.id.id, study.name)))
    }
  }

  def addSpecimenGroupSubmit = SecuredAction { implicit request =>
    specimenGroupForm.bindFromRequest.fold(
      formWithErrors => {
        // studyId and studyName are hidden values in the form, they should always be present
        val studyId = formWithErrors("studyId").value.getOrElse("")
        val studyName = formWithErrors("studyName").value.getOrElse("")

        BadRequest(html.study.specimengroup.add(
          formWithErrors, AddFormType(), studyId, studyName, addBreadcrumbs(studyId, studyName)))
      },
      sgForm => {
        implicit val userId = new UserId(request.user.id.id)
        val studyId = sgForm.studyId
        val studyName = sgForm.studyName

        Async {
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
                Redirect(routes.SpecimenGroupController.index(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.study.specimen.group.added", sg.name))
            })
        }
      })
  }

  private def badActionRequest(
    studyId: String,
    studyName: String,
    subheading: String)(implicit request: WrappedRequest[AnyContent]) = {
    BadRequest(html.serviceError(
      Messages("biobank.study.specimen.group.error.heading"),
      subheading,
      updateBreadcrumbs(studyId, studyName)))
  }

  /**
   * If the annotation type is not in use, the the {@link actionFunc} will be invoked.
   */
  private def checkSpecimenGroupNotInUse(
    studyId: String,
    studyName: String,
    specimenGroupId: String)(actionFunc: (String, String, String) => Result)(implicit request: WrappedRequest[AnyContent]) = {
    studyService.specimenGroupInUse(studyId, specimenGroupId) match {
      case Failure(x) =>
        if (x.head.contains("study does not have specimen group")) {
          badActionRequest(studyId, studyName, Messages("biobank.study.error"))
        } else if (x.head.contains("specimen group does not exist")) {
          badActionRequest(studyId, studyName, Messages("biobank.study.specimen.group.invalid"))
        } else {
          throw new Error(x.head)
        }
      case Success(result) =>
        if (result) {
          badActionRequest(studyId, studyName, Messages("biobank.study.specimen.group.in.use.error.message"))
        } else {
          actionFunc(studyId, studyName, specimenGroupId)
        }
    }
  }

  def updateSpecimenGroup(
    studyId: String,
    studyName: String,
    specimenGroupId: String) = SecuredAction { implicit request =>

    def action(studyId: String, studyName: String, specimenGroupId: String) = {
      studyService.specimenGroupWithId(studyId, specimenGroupId) match {
        case Failure(x) => throw new Error(x.head)
        case Success(sg) =>
          val form = specimenGroupForm.fill(SpecimenGroupFormObject(
            sg.id.id, sg.version, studyId, studyName, sg.name, sg.description, sg.units,
            sg.anatomicalSourceType.toString, sg.preservationType.toString,
            sg.preservationTemperatureType.toString, sg.specimenType.toString))
          Ok(html.study.specimengroup.add(form, UpdateFormType(), studyId, studyName,
            updateBreadcrumbs(studyId, studyName)))
      }
    }

    checkSpecimenGroupNotInUse(studyId, studyName, specimenGroupId)(action)
  }

  def updateSpecimenGroupSubmit = SecuredAction { implicit request =>
    specimenGroupForm.bindFromRequest.fold(
      formWithErrors => {
        // studyId and studyName are hidden values in the form, they should always be present
        val studyId = formWithErrors("studyId").value.getOrElse("")
        val studyName = formWithErrors("studyName").value.getOrElse("")

        BadRequest(html.study.specimengroup.add(
          formWithErrors, UpdateFormType(), studyId, studyName, updateBreadcrumbs(studyId, studyName)))
      },
      sgForm => {
        implicit val userId = new UserId(request.user.id.id)
        val studyId = sgForm.studyId
        val studyName = sgForm.studyName

        Async {
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
                Redirect(routes.SpecimenGroupController.index(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.study.specimen.group.added", sg.name))
            })
        }
      })
  }

  def removeSpecimenGroup(
    studyId: String,
    studyName: String,
    specimenGroupId: String) = SecuredAction { implicit request =>

    def action(studyId: String, studyName: String, specimenGroupId: String) = {
      studyService.specimenGroupWithId(studyId, specimenGroupId) match {
        case Failure(x) =>
          throw new Error(x.head)
        case Success(sg) =>
          Ok(views.html.study.specimengroup.removeConfirm(studyId, studyName, sg,
            removeBreadcrumbs(studyId, studyName)))
      }
    }

    checkSpecimenGroupNotInUse(studyId, studyName, specimenGroupId)(action)
  }

  val specimenGroupDeleteForm = Form(
    tuple(
      "studyId" -> text,
      "studyName" -> text,
      "specimenGroupId" -> text))

  def removeSpecimenGroupSubmit = SecuredAction { implicit request =>
    specimenGroupDeleteForm.bindFromRequest.fold(
      formWithErrors =>
        throw new Error(formWithErrors.globalError.toString),
      sgForm => {
        Logger.error("******" + sgForm)
        val studyId = sgForm._1
        val studyName = sgForm._2
        val specimenGroupId = sgForm._3

        studyService.specimenGroupWithId(studyId, specimenGroupId) match {
          case Failure(x) => throw new Error(x.head)
          case Success(sg) =>
            Async {
              implicit val userId = new UserId(request.user.id.id)
              studyService.removeSpecimenGroup(RemoveSpecimenGroupCmd(
                sg.id.id, sg.versionOption, sg.studyId.id)).map(validation =>
                validation match {
                  case Success(sg) =>
                    Redirect(routes.SpecimenGroupController.index(studyId, studyName)).flashing(
                      "success" -> Messages("biobank.study.specimen.group.removed", sg.name))
                  case Failure(x) =>
                    throw new Error(x.head)
                })
            }
        }
      })
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
