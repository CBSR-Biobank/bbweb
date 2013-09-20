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
import play.api.cache.Cache
import play.api.Play.current
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.Logger
import securesocial.core.SecureSocial
import securesocial.core.SecuredRequest

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

  def validateStudy(id: String, errorHeading: String)(f: Study => Result)(implicit request: WrappedRequest[AnyContent]): Result = {
    studyService.getStudy(id) match {
      case Failure(x) =>
        if (x.head.contains("study does not exist")) {
          BadRequest(html.serviceError(
            errorHeading,
            Messages("biobank.study.error"),
            studyBreadcrumbs))
        } else {
          throw new Error(x.head)
        }
      case Success(study) => f(study)
    }
  }

  def validateStudy(id: String)(f: Study => Result)(implicit request: WrappedRequest[AnyContent]): Result = {
    validateStudy(id, Messages("biobank.study.error.heading"))(f)
  }

  def selectedStudyTab(tab: StudyTab): Unit = {
    Cache.set("study.tab", tab)
    Logger.debug("selected tab: " + Cache.get("study.tab"))
  }

  def selectedStudyTab: StudyTab = {
    Cache.getAs[StudyTab.Value]("study.tab").getOrElse(StudyTab.Summary)
  }

  def index = SecuredAction { implicit request =>
    // get list of studies the user has access to
    //
    // FIXME add paging and filtering -> see "computer-databse" Play sample app
    val studies = studyService.getAll
    Ok(views.html.study.index(studies))
  }

  // this is the entry point to the study page from external pages: i.e. study list page
  def summary(id: String) = SecuredAction { implicit request =>
    validateStudy(id)(study => {
      selectedStudyTab(StudyTab.Summary)
      Redirect(routes.StudyController.showStudy(study.id.id))
    })
  }

  // this is the entry point to the study page for sub-pages: i.e. add specimen group, add collection
  // event type
  def showStudy(id: String) = SecuredAction { implicit request =>
    validateStudy(id)(study => {
      val counts = Map(
        ("participants" -> "<i>to be implemented</i>"),
        ("collection.events" -> "<i>to be implemented</i>"),
        ("specimen.count" -> "<i>to be implemented</i>"))
      Ok(html.study.showStudy(study, counts, selectedStudyTab))
    })
  }

  // Ajax call to view the "Specimens" tab
  def summaryTab(studyId: String, studyName: String) = SecuredAction(ajaxCall = true) { implicit request =>
    validateStudy(studyId)(study => {
      Logger.debug("summaryTab: ajax call")

      // returns no content since we only want to update the cache with the selected tab
      selectedStudyTab(StudyTab.Summary)
      NoContent
    })
  }

  // Ajax call to view the "Specimens" tab
  def participantsTab(studyId: String, studyName: String) = SecuredAction(ajaxCall = true) { implicit request =>
    validateStudy(studyId)(study => {
      Logger.debug("participantsTab: ajax call")

      // returns no content since we only want to update the cache with the selected tab
      selectedStudyTab(StudyTab.Participants)
      NoContent
    })
  }

  // Ajax call to view the "Specimens" tab
  def specimensTab(studyId: String, studyName: String) = SecuredAction(ajaxCall = true) { implicit request =>
    validateStudy(studyId)(study => {
      Logger.debug("specimensTab: ajax call")

      selectedStudyTab(StudyTab.Specimens)
      val specimenGroups = studyService.specimenGroupsForStudy(studyId)
      Ok(html.study.specimenGroupList(studyId, studyName, specimenGroups))
    })
  }

  // Ajax call to view the "Collection Events" tab
  def ceventsTab(studyId: String, studyName: String) = SecuredAction(ajaxCall = true) { implicit request =>
    validateStudy(studyId)(study => {
      Logger.debug("ceventsTab: ajax call")

      selectedStudyTab(StudyTab.CollectionEvents)
      val ceventTypes = studyService.collectionEventTypesForStudy(studyId)
      val specimenGroups = studyService.specimenGroupsForStudy(studyId).map(
        x => (x.id.id, x.name, x.units)).toSeq
      val annotationTypes = studyService.collectionEventAnnotationTypesForStudy(studyId)
      Ok(html.study.ceventTypeList(studyId, studyName, ceventTypes, specimenGroups,
        annotationTypes))
    })
  }

  // Ajax call to view the "Collection Events" tab
  def peventsTab(studyId: String, studyName: String) = SecuredAction(ajaxCall = true) { implicit request =>
    validateStudy(studyId)(study => {
      Logger.debug("peventsTab: ajax call")
      selectedStudyTab(StudyTab.ProcessingEvents)
      Ok("<h4>to be completed.</h4>")
    })
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
    validateStudy(studyId)(study => {
      Logger.debug("study version: " + study.version)
      Ok(html.study.addStudy(
        studyForm.fill(StudyFormObject(studyId, study.version, study.name, study.description)),
        UpdateFormType(),
        studyId))
    })
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
                  Redirect(routes.StudyController.showStudy(study.id.id)).flashing(
                    "success" -> Messages("biobank.study.updated", study.name))
              })
          }
        }
      })
  }
}

