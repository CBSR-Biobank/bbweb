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
    implicit val userId = new UserId(request.user.id.id)
    addForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.study.add(formWithErrors)),
      {
        case (name, description) =>
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
    tuple(
      "name" -> nonEmptyText,
      "description" -> optional(text),
      "units" -> nonEmptyText,
      "anatomicalSourceType" -> optional(text),
      "preservationType" -> optional(text),
      "preservationTemperatureType" -> optional(text),
      "specimenType" -> optional(text)))

  /**
   * Add a specimen group.
   */
  def addSpecimenGroup(id: String) = SecuredAction { implicit request =>
    studyService.getStudy(id) match {
      case Failure(x) =>
        NotFound("Bad Request: " + x.head)
      case Success(study) =>
        Ok(html.study.specimenGroupAdd(id, study.name, specimenGroupForm))
    }
  }

  def addSpecimenGroupSubmit = SecuredAction { implicit request =>
    addForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.study.add(formWithErrors)), {
        case (name, description) =>
          implicit val userId = new UserId(request.user.id.id)
          Async {
            studyService.addStudy(AddStudyCmd(name, description)).map(
              study => study match {
                case Success(study) => Ok(html.study.show(study))
                case Failure(x) => BadRequest("Bad Request: " + x.head)
              })
          }
      })
  }
}
