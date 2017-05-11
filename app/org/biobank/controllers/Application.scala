package org.biobank.controllers

import javax.inject._
import org.biobank.dto.AggregateCountsDto
import org.biobank.service.centres.CentresService
import org.biobank.service.studies.StudiesService
import org.biobank.service.users.UsersService
import play.api.Environment
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation.FlatMap._

/**
  * Controller for the main page, and also the about and contact us pages.
 */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class Application @Inject() (val action:         BbwebAction,
                             val env:            Environment,
                             val usersService:   UsersService,
                             val studiesService: StudiesService,
                             val centresService: CentresService)
                         (implicit val ec: ExecutionContext)
    extends CommandController {

  def index: Action[AnyContent] = Action {
    // does not return a JSON object, but HTML content
    Results.Ok(views.html.index())
  }

  def aggregateCounts: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      Future {
        val v = for {
            studyCounts <- studiesService.getStudyCount(request.authInfo.userId)
            userCounts  <- usersService.getCountsByStatus(request.authInfo.userId)
          } yield AggregateCountsDto(studyCounts,
                                     centresService.getCentresCount,
                                     userCounts.total)
        validationReply(v)
      }
  }

}
