package org.biobank.controllers

import javax.inject._
import org.biobank.dto.AggregateCountsDto
import org.biobank.domain.access.PermissionId
import org.biobank.services.access.AccessService
import org.biobank.services.centres.CentresService
import org.biobank.services.studies.StudiesService
import org.biobank.services.users.UsersService
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

  def aggregateCounts: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val userId = request.identity.user.id
      Future {
        val counts =  for {
            studyCounts  <- {
              accessService.hasPermission(userId, PermissionId.StudyRead).flatMap { p =>
                if (p) studiesService.getStudyCount(userId)
                else 0L.successNel[String]
              }
            }
            centreCounts <- {
              accessService.hasPermission(userId, PermissionId.CentreRead).flatMap { p =>
                if (p) centresService.getCentresCount(userId)
                else 0L.successNel[String]
              }
            }
            userCounts   <- {
              accessService.hasPermission(userId, PermissionId.UserRead).flatMap { p =>
                if (p) usersService.getCountsByStatus(userId).map(c => c.total)
                else 0L.successNel[String]
              }
            }
          } yield AggregateCountsDto(studyCounts, centreCounts, userCounts)
        validationReply(counts)
      }
  }

}
