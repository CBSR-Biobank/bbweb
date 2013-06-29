package controllers

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

object StudyController extends Controller with securesocial.core.SecureSocial {

  //implicit val timeout = Timeout(10 seconds)

  lazy val userService = Global.services.userService
  lazy val studyService = Global.services.studyService

  val studyForm = Form(
    mapping(
      "studyId" -> text,
      "version" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> optional(text))(StudyFormObject.apply)(StudyFormObject.unapply))

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

  def index = SecuredAction { implicit request =>
    // get list of studies the user has access to
    //
    // FIXME add paging and filtering -> see "computer-databse" Play sample app
    val studies = studyService.getAll
    Ok(views.html.study.index(studies))
  }
  /**
   * Add a study.
   */
  def addStudy = SecuredAction { implicit request =>
    Ok(html.study.addStudy(studyForm, AddFormType(), ""))
  }

  def addStudySubmit = SecuredAction { implicit request =>
    studyForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.study.addStudy(formWithErrors, AddFormType(), ""))
      },
      formObj => {
        Async {
          implicit val userId = UserId(request.user.id.id)
          studyService.addStudy(formObj.getAddCmd).map(
            study => study match {
              case Success(study) =>
                Ok(html.study.showStudy(study))
                Redirect(routes.StudyController.showStudy(study.id.id)).flashing(
                  "success" -> Messages("biobank.study.added", study.name))
              case Failure(x) =>
                BadRequest("Bad Request: " + x.head)
            })
        }
      })
  }

  /**
   * Update a study.
   */
  def updateStudy(studyId: String) = SecuredAction { implicit request =>
    studyService.getStudy(studyId) match {
      case Success(study) =>
        Logger.debug("study version: " + study.version)
        Ok(html.study.addStudy(
          studyForm.fill(StudyFormObject(studyId, study.version, study.name, study.description)),
          UpdateFormType(),
          studyId))
      case Failure(x) =>
        NotFound("Bad Request: " + x.head)
    }
  }

  def updateStudySubmit(studyId: String) = SecuredAction { implicit request =>
    studyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.study.addStudy(
        formWithErrors, UpdateFormType(), studyId)), {
        case formObj => {
          Async {
            implicit val userId = UserId(request.user.id.id)
            studyService.updateStudy(formObj.getUpdateCmd).map(study =>
              study match {
                case Failure(x) => BadRequest("Bad Request: " + x.head)
                case Success(study) =>
                  Ok(html.study.showStudy(study))
                  Redirect(routes.StudyController.showStudy(study.id.id)).flashing(
                    "success" -> Messages("biobank.study.updated", study.name))
              })
          }
        }
      })
  }

  def showStudy(id: String) = SecuredAction { implicit request =>
    studyService.getStudy(id) match {
      case Success(study) => Ok(html.study.showStudy(study))
      case Failure(x) =>
        NotFound("Bad Request: " + x.head)
    }
  }

  /**
   * Add a specimen group.
   */
  def addSpecimenGroup(studyId: String) = SecuredAction { implicit request =>
    studyService.getStudy(studyId) match {
      case Failure(x) =>
        NotFound("Bad Request: " + x.head)
      case Success(study) =>
        val form = specimenGroupForm.fill(SpecimenGroupFormObject(
          "", -1, studyId, "", None, "", "", "", "", ""))
        Ok(html.study.addSpecimenGroup(form, AddFormType(), studyId, study.name))
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
                Redirect(routes.StudyController.showSpecimenGroups(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.study.specimengroup.added", sg.name))
              case Failure(x) =>
                Logger.debug("add specimen group failed: " + x.head)
                BadRequest("Bad Request: " + x.head)
            })
        }
      })
  }

  def updateSpecimenGroup(studyId: String, studyName: String, specimenGroupId: String) = SecuredAction { implicit request =>
    studyService.getSpecimenGroup(studyId, specimenGroupId) match {
      case Failure(x) =>
        NotFound("Bad Request: " + x.head)
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
                Redirect(routes.StudyController.showSpecimenGroups(studyId, studyName)).flashing(
                  "success" -> Messages("biobank.study.specimengroup.added", sg.name))
              case Failure(x) =>
                Logger.debug("add specimen group failed: " + x.head)
                BadRequest("Bad Request: " + x.head)
            })
        }
      })
  }

  def showSpecimenGroups(studyId: String, studyName: String) = SecuredAction { implicit request =>
    studyService.getSpecimenGroups(studyId) match {
      case Failure(x) =>
        NotFound("Bad Request: " + x.head)
      case Success(sgSet) =>
        Ok(views.html.study.showSpecimenGroups(studyId, studyName, sgSet))
    }
  }
}

case class StudyFormObject(
  studyId: String, version: Long, name: String, description: Option[String]) {

  def getAddCmd: AddStudyCmd = {
    AddStudyCmd(name, description)
  }

  def getUpdateCmd: UpdateStudyCmd = {
    UpdateStudyCmd(studyId, some(version), name, description)
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
