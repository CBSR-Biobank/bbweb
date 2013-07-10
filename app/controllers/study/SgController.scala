package controllers.study

import controllers._
import service._
import infrastructure._
import infrastructure.commands._
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

object SgController extends Controller with securesocial.core.SecureSocial {

  lazy val studyService = Global.services.studyService

  val specimenGroupForm = Form(
    mapping(
      "specimenGroupId" -> text,
      "version" -> longNumber,
      "studyId" -> text,
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "units" -> nonEmptyText,
      "anatomicalSourceType" -> nonEmptyText,
      "preservationType" -> nonEmptyText,
      "preservationTemperatureType" -> nonEmptyText,
      "specimenType" -> nonEmptyText)(SpecimenGroupFormObject.apply)(SpecimenGroupFormObject.unapply))

  def index(studyId: String, studyName: String) = SecuredAction { implicit request =>
    studyService.getSpecimenGroups(studyId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(sgSet) =>
        Ok(views.html.study.showSpecimenGroups(studyId, studyName, sgSet))
    }
  }

  /**
   * Add a specimen group.
   */
  def addSpecimenGroup(studyId: String) = SecuredAction { implicit request =>
    studyService.getStudy(studyId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(study) =>
        Ok(html.study.addSpecimenGroup(specimenGroupForm, AddFormType(), studyId, study.name))
    }
  }

  def addSpecimenGroupSubmit(studyId: String, studyName: String) = SecuredAction { implicit request =>
    specimenGroupForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.study.addSpecimenGroup(
        formWithErrors, AddFormType(), studyId, studyName)),
      sgForm => {
        Async {
          implicit val userId = new UserId(request.user.id.id)
          studyService.addSpecimenGroup(sgForm.getAddCmd).map(validation =>
            validation match {
              case Success(sg) =>
                Redirect(routes.SgController.index(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.study.specimengroup.added", sg.name))
              case Failure(x) =>
                if (x.head.contains("name already exists")) {
                  val form = specimenGroupForm.fill(sgForm).withError("name",
                    Messages("biobank.study.specimengroup.form.error.name"))
                  BadRequest(html.study.addSpecimenGroup(form, AddFormType(), studyId, studyName))
                } else {
                  throw new Error(x.head)
                }
            })
        }
      })
  }

  def updateSpecimenGroup(studyId: String, studyName: String, specimenGroupId: String) = SecuredAction { implicit request =>
    studyService.getSpecimenGroup(studyId, specimenGroupId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(sg) =>
        val form = specimenGroupForm.fill(SpecimenGroupFormObject(
          sg.id.id, sg.version, sg.studyId.id, sg.name, sg.description, sg.units,
          sg.anatomicalSourceType.toString, sg.preservationType.toString,
          sg.preservationTemperatureType.toString, sg.specimenType.toString))
        Ok(html.study.addSpecimenGroup(form, UpdateFormType(), studyId, studyName))
    }
  }

  def updateSpecimenGroupSubmit(studyId: String, studyName: String) = SecuredAction { implicit request =>
    specimenGroupForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.study.addSpecimenGroup(
        formWithErrors, AddFormType(), studyId, studyName)),
      sgForm => {
        Async {
          implicit val userId = new UserId(request.user.id.id)
          studyService.updateSpecimenGroup(sgForm.getUpdateCmd).map(validation =>
            validation match {
              case Success(sg) =>
                Redirect(routes.SgController.index(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.study.specimengroup.added", sg.name))
              case Failure(x) =>
                if (x.head.contains("name already exists")) {
                  val form = specimenGroupForm.fill(sgForm).withError("name",
                    Messages("biobank.study.specimengroup.form.error.name"))
                  BadRequest(html.study.addSpecimenGroup(form, UpdateFormType(), studyId, studyName))
                } else {
                  throw new Error(x.head)
                }
            })
        }
      })
  }

  def removeSpecimenGroupConfirm(studyId: String,
    studyName: String,
    specimenGroupId: String) = SecuredAction { implicit request =>
    studyService.getSpecimenGroup(studyId, specimenGroupId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(sg) =>
        Ok(views.html.study.removeSpecimenGroupConfirm(studyId, studyName, sg))
    }
  }

  def removeSpecimenGroup(studyId: String,
    studyName: String,
    sgId: String) = SecuredAction { implicit request =>
    studyService.getSpecimenGroup(studyId, sgId) match {
      case Failure(x) => throw new Error(x.head)
      case Success(sg) =>
        Async {
          implicit val userId = new UserId(request.user.id.id)
          studyService.removeSpecimenGroup(RemoveSpecimenGroupCmd(
            sg.id.id, sg.versionOption, sg.studyId.id)).map(validation =>
            validation match {
              case Success(sg) =>
                Redirect(routes.SgController.index(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.study.specimengroup.removed", sg.name))
              case Failure(x) =>
                throw new Error(x.head)
            })
        }
    }
  }
}

case class SpecimenGroupFormObject(
  specimenGroupId: String, version: Long, studyId: String, name: String,
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
