package org.biobank.controllers.centres

import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.Slug
import org.biobank.domain.centres.CentreId
import org.biobank.dto._
import org.biobank.infrastructure.commands.CentreCommands._
import org.biobank.services._
import org.biobank.services.centres.CentresService
import org.biobank.services.studies.StudiesService
import play.api.libs.json._
import play.api.mvc._
import play.api.{ Environment, Logger }
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._

/**
 *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
 */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class CentresController @Inject()(controllerComponents: ControllerComponents,
                                  val action:           BbwebAction,
                                  val env:              Environment,
                                  val service:          CentresService,
                                  val studiesService:   StudiesService)
                               (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 10

  def centreCounts(): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      Future(validationReply(service.getCountsByStatus(request.identity.user.id)))
    }

  def list: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[CentreDto]]))
        },
        pagedQuery => {
          validationReply(service.getCentres(request.identity.user.id, pagedQuery))
        }
      )
    }

  def listNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      FilterAndSortQueryHelper(request.rawQueryString).fold(
        err => {
          validationReply(Future.successful(err.failure[Seq[CentreDto]]))
        },
        query => {
          validationReply(service.getCentreNames(request.identity.user.id,
                                                 query.filter,
                                                 query.sort))
        }
      )
    }

  def searchLocations(): Action[JsValue] =
    commandAction[SearchCentreLocationsCmd](JsNull){ cmd =>
      Future(validationReply(service.searchLocations(cmd)))
    }

  def getBySlug(slug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      val v = service.getCentreBySlug(request.identity.user.id, slug)
      validationReply(v)
    }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.snapshotRequest(request.identity.user.id).map(_ => true))
    }

  def add(): Action[JsValue] = commandAction[AddCentreCmd](JsNull)(processCommand)

  def updateName(id: CentreId): Action[JsValue] =
    commandAction[UpdateCentreNameCmd](Json.obj("id" -> id))(processCommand)

  def updateDescription(id: CentreId): Action[JsValue] =
    commandAction[UpdateCentreDescriptionCmd](Json.obj("id" -> id))(processCommand)

  def addStudy(centreId: CentreId): Action[JsValue] =
    commandAction[AddStudyToCentreCmd](Json.obj("id" -> centreId))(processCommand)

  def removeStudy(centreId: CentreId, ver: Long, studyId: String): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      processCommand(RemoveStudyFromCentreCmd(request.identity.user.id.id, centreId.id, ver, studyId))
    }

  def addLocation(id: CentreId): Action[JsValue] =
    commandAction[AddCentreLocationCmd](Json.obj("id" -> id))(processCommand)

  def updateLocation(id: CentreId, locationId: String): Action[JsValue] = {
    val json = Json.obj("id" -> id, "locationId" -> locationId)
    commandAction[UpdateCentreLocationCmd](json)(processCommand)
  }

  def removeLocation(centreId: CentreId, ver: Long, locationId: String): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      processCommand(RemoveCentreLocationCmd(request.identity.user.id.id, centreId.id, ver, locationId))
    }

  def enable(id: CentreId): Action[JsValue] =
    commandAction[EnableCentreCmd](Json.obj("id" -> id))(processCommand)

  def disable(id: CentreId): Action[JsValue] =
    commandAction[DisableCentreCmd](Json.obj("id" -> id))(processCommand)

  private def processCommand(cmd: CentreCommand): Future[Result] = {
    val future = service.processCommand(cmd)
    validationReply(future)
  }

}
