package org.biobank.controllers.centres

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import org.biobank.controllers._
import org.biobank.domain.centre.{ Centre, CentreId }
import org.biobank.domain.user.UserId
import org.biobank.dto._
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.service._
import org.biobank.service.centres.CentresService
import org.biobank.service.studies.StudiesService
import play.api.libs.json._
import play.api.mvc._
import play.api.{ Environment, Logger }
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

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
      Future(validationReply(service.getCountsByStatus(request.authInfo.userId)))
    }

  def list: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            pagedQuery <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            centres    <- service.getCentres(request.authInfo.userId, pagedQuery.filter, pagedQuery.sort)
            validPage  <- pagedQuery.validPage(centres.size)
            dtos        <- {
             centres.map(centre => centreToDto(request.authInfo.userId, centre))
                .toList.sequenceU.map(_.toSeq)
            }
            results    <- PagedResults.create(dtos, pagedQuery.page, pagedQuery.limit)
          } yield results
        }
      )
    }

  def listNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            filterAndSort <- FilterAndSortQuery.create(request.rawQueryString)
            centres       <- service.getCentres(request.authInfo.userId,
                                                filterAndSort.filter,
                                                filterAndSort.sort)
          } yield {
            centres.map(centre => NameAndStateDto(centre.id.id, centre.name, centre.state.id))
          }
        }
      )
    }

  def searchLocations(): Action[JsValue] =
    commandAction[SearchCentreLocationsCmd](JsNull){ cmd =>
      Future(validationReply(service.searchLocations(cmd)))
    }

  def getBySlug(slug: String): Action[Unit] =
    action(parse.empty) { implicit request =>
      val v = service.getCentreBySlug(request.authInfo.userId, slug).flatMap { centre =>
          centreToDto(request.authInfo.userId, centre)
        }
      validationReply(v)
    }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(service.snapshotRequest(request.authInfo.userId).map(_ => true))
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
      processCommand(RemoveStudyFromCentreCmd(request.authInfo.userId.id, centreId.id, ver, studyId))
    }

  def addLocation(id: CentreId): Action[JsValue] =
    commandAction[AddCentreLocationCmd](Json.obj("id" -> id))(processCommand)

  def updateLocation(id: CentreId, locationId: String): Action[JsValue] = {
    val json = Json.obj("id" -> id, "locationId" -> locationId)
    commandAction[UpdateCentreLocationCmd](json)(processCommand)
  }

  def removeLocation(centreId: CentreId, ver: Long, locationId: String): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      processCommand(RemoveCentreLocationCmd(request.authInfo.userId.id, centreId.id, ver, locationId))
    }

  def enable(id: CentreId): Action[JsValue] =
    commandAction[EnableCentreCmd](Json.obj("id" -> id))(processCommand)

  def disable(id: CentreId): Action[JsValue] =
    commandAction[DisableCentreCmd](Json.obj("id" -> id))(processCommand)

  private def processCommand(cmd: CentreCommand): Future[Result] = {
    val future = service.processCommand(cmd).map { validation =>
        validation.flatMap(centre => centreToDto(UserId(cmd.sessionUserId), centre))
      }
    validationReply(future)
  }

  private def centreToDto(requestUserId: UserId, centre: Centre): ControllerValidation[CentreDto] = {
    val v = centre.studyIds
      .map { id =>
        studiesService.getStudy(requestUserId, id).map { study =>
          NameAndStateDto(study.id.id, study.name, study.state.id)
        }
      }
      .toList.sequenceU

    v.map { studyNames =>
      CentreDto(id           = centre.id.id,
                version      = centre.version,
                timeAdded    = centre.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                timeModified = centre.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                state        = centre.state.id,
                slug         = centre.slug,
                name         = centre.name,
                description  = centre.description,
                studyNames   = studyNames.toSet,
                locations    = centre.locations)
    }
  }

}
