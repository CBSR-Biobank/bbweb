package controllers.study

import controllers._
import service._
import infrastructure._
import service.commands._
import service.{ ServiceComponent, TopComponentImpl }
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
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.Logger
import securesocial.core.SecureSocial

import scalaz._
import scalaz.Scalaz._

object StudyTab extends Enumeration {
  type StudyTab = Value
  val Summary = Value("tab-summary")
  val Participants = Value("tab-participants")
  val Specimens = Value("tab-specimens")
  val CollectionEvents = Value("tab-collection-events")
  val ProcessingEvents = Value("tab-processing-events")
}

import StudyTab._

case class StudyFormObject(
  studyId: String, version: Long, name: String, description: Option[String]) {

  def getAddCmd: AddStudyCmd = {
    AddStudyCmd(name, description)
  }

  def getUpdateCmd: UpdateStudyCmd = {
    UpdateStudyCmd(studyId, some(version), name, description)
  }
}

object StudyController extends Controller with SecureSocial {

  private lazy val studyService = WebComponent.studyService

  private val studyForm = Form(
    mapping(
      "studyId" -> text,
      "version" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> optional(text))(StudyFormObject.apply)(StudyFormObject.unapply))

  private def studyBreadcrumbs = {
    Map((Messages("biobank.study.plural") -> null))
  }

  def index = SecuredAction { implicit request =>
    // get list of studies the user has access to
    //
    // FIXME add paging and filtering -> see "computer-databse" Play sample app
    val studies = studyService.getAll
    Ok(views.html.study.index(studies))
  }

  def showStudy(id: String, tab: String) = SecuredAction { implicit request =>
    studyService.getStudy(id) match {
      case Failure(x) =>
        if (x.head.contains("study does not exist")) {
          BadRequest(html.serviceError(
            Messages("biobank.study.error.heading"),
            Messages("biobank.study.error"),
            studyBreadcrumbs))
        } else {
          throw new Error(x.head)
        }
      case Success(study) =>
        val counts = Map(
          ("participants" -> "<i>to be implemented</i>"),
          ("collection.events" -> "<i>to be implemented</i>"),
          ("specimen.count" -> "<i>to be implemented</i>"))
        Ok(html.study.showStudy(study, counts, StudyTab.withName(tab)))
    }
  }

  /**
   * Add a study.
   */
  def addStudy() = SecuredAction { implicit request =>
    Ok(html.study.addStudy(studyForm, AddFormType(), ""))
  }

  def addStudySubmit() = SecuredAction { implicit request =>
    studyForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.study.addStudy(formWithErrors, AddFormType(), ""))
      },
      formObj => {
        Async {
          implicit val userId = UserId(request.user.identityId.userId)
          studyService.addStudy(formObj.getAddCmd).map(study => study match {
            case Success(study) =>
              Redirect(routes.StudyController.showStudy(study.id.id, StudyTab.Summary.toString)).flashing(
                "success" -> Messages("biobank.study.added", study.name))
            case Failure(x) =>
              if (x.head.contains("study with name already exists")) {
                val form = studyForm.fill(formObj).withError("name",
                  Messages("biobank.study.form.error.name"))
                BadRequest(html.study.addStudy(form, AddFormType(), ""))
              } else {
                throw new Error(x.head)
              }
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
        throw new Error(x.head)
    }
  }

  def updateStudySubmit(studyId: String) = SecuredAction { implicit request =>
    studyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.study.addStudy(
        formWithErrors, UpdateFormType(), studyId)), {
        case formObj => {
          Async {
            implicit val userId = UserId(request.user.identityId.userId)
            studyService.updateStudy(formObj.getUpdateCmd).map(study =>
              study match {
                case Failure(x) =>
                  if (x.head.contains("study with name already exists")) {
                    val form = studyForm.fill(formObj).withError("name",
                      Messages("biobank.study.form.error.name"))
                    BadRequest(html.study.addStudy(form, UpdateFormType(), studyId))
                  } else {
                    throw new Error(x.head)
                  }
                case Success(study) =>
                  Redirect(routes.StudyController.showStudy(study.id.id, StudyTab.Summary.toString)).flashing(
                    "success" -> Messages("biobank.study.updated", study.name))
              })
          }
        }
      })
  }
}
