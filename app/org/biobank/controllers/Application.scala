package org.biobank.controllers

import javax.inject._
import org.biobank.dto.AggregateCountsDto
import org.biobank.domain.access.PermissionId
import org.biobank.service.access.AccessService
import org.biobank.service.centres.CentresService
import org.biobank.service.studies.StudiesService
import org.biobank.service.users.UsersService
import play.api.{Environment, Logger}
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  * Controller for the main page, and also the about and contact us pages.
 */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class Application @Inject() (controllerComponents: ControllerComponents,
                             val action:               BbwebAction,
                             val env:                  Environment,
                             val accessService:        AccessService,
                             val usersService:         UsersService,
                             val studiesService:       StudiesService,
                             val centresService:       CentresService)
                         (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  import org.biobank.domain.access.AccessItem._

  val log: Logger = Logger(this.getClass)

  def index: Action[AnyContent] = Action {
    // does not return a JSON object, but HTML content
    Results.Ok(views.html.index())
  }

  def aggregateCounts: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      Future {
        val counts =  for {
            studyCounts  <- {
              accessService.hasPermission(request.authInfo.userId, PermissionId.StudyRead).flatMap { p =>
                if (p) studiesService.getStudyCount(request.authInfo.userId)
                else 0L.successNel[String]
              }
            }
            centreCounts <- {
              accessService.hasPermission(request.authInfo.userId, PermissionId.CentreRead).flatMap { p =>
                if (p) centresService.getCentresCount(request.authInfo.userId)
                else 0L.successNel[String]
              }
            }
            userCounts   <- {
              accessService.hasPermission(request.authInfo.userId, PermissionId.UserRead).flatMap { p =>
                if (p) usersService.getCountsByStatus(request.authInfo.userId).map(c => c.total)
                else 0L.successNel[String]
              }
            }
          } yield AggregateCountsDto(studyCounts, centreCounts, userCounts)
        validationReply(counts)
      }
  }

}
