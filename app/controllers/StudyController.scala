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

  def index = SecuredAction { implicit request =>
    // get list of studies the user has access to
    val studies = studyService.getAll
    Ok(views.html.study.index(studies))
  }

  val addForm = Form(
    tuple(
      "name" -> nonEmptyText,
      "description" -> optional(text)))

  /**
   * Add a study.
   */
  def add = SecuredAction { implicit request =>
    Ok(html.study.add(addForm))
  }

  def addSubmit = SecuredAction { implicit request =>
    addForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.study.add(formWithErrors)),
      {
        case (name, description) =>
          implicit val userId = UserId(request.user.id.id)
          Async {
            studyService.addStudy(AddStudyCmd(name, description)).map(
              study => study match {
                case Success(study) => Ok(html.study.show(study))
                case Failure(x) => BadRequest("Bad Request: " + x.head)
              })
          }
      })
  }

  def show(id: String) = SecuredAction { implicit request =>
    studyService.getStudy(id) match {
      case Success(study) => Ok(html.study.show(study))
      case Failure(x) =>
        NotFound("Bad Request: " + x.head)
    }
  }

  val specimenGroupForm = Form(
    mapping(
      "studyId" -> nonEmptyText,
      "studyName" -> nonEmptyText,
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "units" -> nonEmptyText,
      "anatomicalSourceType" -> nonEmptyText,
      "preservationType" -> nonEmptyText,
      "preservationTemperatureType" -> nonEmptyText,
      "specimenType" -> nonEmptyText)(SpecimenGroupFormObject.apply)(SpecimenGroupFormObject.unapply))

  /**
   * Add a specimen group.
   */
  def addSpecimenGroup(id: String) = SecuredAction { implicit request =>
    studyService.getStudy(id) match {
      case Failure(x) =>
        NotFound("Bad Request: " + x.head)
      case Success(study) =>
        val form = specimenGroupForm.fill(SpecimenGroupFormObject(
          id, study.name, "", None, "", "", "", "", ""))
        Ok(html.study.specimenGroupAdd(form, id, study.name))
    }
  }

  def addSpecimenGroupSubmit = SecuredAction { implicit request =>
    specimenGroupForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.study.specimenGroupAdd(
        formWithErrors, formWithErrors.get.studyId, formWithErrors.get.studyName)), {
        case sgForm =>
          Logger.debug("sgForm:" + sgForm)
          Async {
            implicit val userId = new UserId(request.user.id.id)
            studyService.addSpecimenGroup(sgForm.getCmd).map(
              sg => sg match {
                case Success(sg) =>
                  val study = studyService.getStudy(sg.studyId.id) | null
                  Ok(html.study.specimenGroupsShow(study.id.id, study.name,
                    studyService.getSpecimenGroups(sg.studyId.id)))
                case Failure(x) => BadRequest("Bad Request: " + x.head)
              })
          }
      })
  }

  def showSpecimenGroups(studyId: String) = SecuredAction { implicit request =>
    // get list of studies the user has access to
    studyService.getStudy(studyId) match {
      case Failure(x) =>
        NotFound("Bad Request: " + x.head)
      case Success(study) =>
        Ok(views.html.study.specimenGroupsShow(studyId, study.name,
          studyService.getSpecimenGroups(studyId)))
    }
  }
}

case class SpecimenGroupFormObject(
  studyId: String, studyName: String, name: String, description: Option[String], units: String,
  anatomicalSourceType: String, preservationType: String, preservationTemperatureType: String,
  specimenType: String) {

  def getCmd: AddSpecimenGroupCmd = {
    val asType = AnatomicalSourceType.withName(anatomicalSourceType)
    val pType = PreservationType.withName(preservationType)
    val pTempType = PreservationTemperatureType.withName(preservationTemperatureType)
    val spcType = SpecimenType.withName(specimenType)
    AddSpecimenGroupCmd(studyId, name, description, units, asType, pType, pTempType, spcType)
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
