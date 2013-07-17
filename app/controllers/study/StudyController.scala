package controllers.study

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
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.Logger
import scalaz._
import scalaz.Scalaz._
import controllers._

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
                Redirect(routes.StudyController.showStudy(study.id.id)).flashing(
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
            implicit val userId = UserId(request.user.id.id)
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
                  Redirect(routes.StudyController.showStudy(study.id.id)).flashing(
                    "success" -> Messages("biobank.study.updated", study.name))
              })
          }
        }
      })
  }

  def showStudy(id: String) = SecuredAction { implicit request =>
    studyService.getStudy(id) match {
      case Failure(x) => throw new Error(x.head)
      case Success(study) =>
        val counts = Map(
          ("participants" -> "<i>to be implemented</i>"),
          ("collection.events" -> "<i>to be implemented</i>"),
          ("specimen.groups" -> studyService.getSpecimenGroups(id).toOption.map(
            x => x.size.toString).getOrElse("0")),
          ("collection.event.annotation.types" -> studyService.getCollectionEventAnnotationTypes(id).toOption.map(
            x => x.size.toString).getOrElse("0")),
          ("collection.event.types" -> studyService.getCollectionEventTypes(id).toOption.map(
            x => x.size.toString).getOrElse("0")))
        Ok(html.study.showStudy(study, counts))
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

