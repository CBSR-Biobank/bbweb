package org.biobank.controllers

import javax.inject._
import org.biobank.dto.AggregateCountsDto
import org.biobank.service.AuthToken
import org.biobank.service.centres.CentresService
import org.biobank.service.studies.StudiesService
import org.biobank.service.users.UsersService
import play.api.Environment
import play.api.mvc._

/**
  * Controller for the main page, and also the about and contact us pages.
 */
@Singleton
class Application @Inject() (val action:         BbwebAction,
                             val env:            Environment,
                             val authToken:      AuthToken,
                             val usersService:   UsersService,
                             val studiesService: StudiesService,
                             val centresService: CentresService)
    extends Controller
    with Security
    with JsonController {

  def index: Action[AnyContent] = Action {
    // does not return a JSON object, but HTML content
    Results.Ok(views.html.index())
  }

  def aggregateCounts: Action[Unit] = action(parse.empty) { implicit request =>
    Ok(AggregateCountsDto(
      studiesService.getStudyCount,
      centresService.getCentresCount,
      usersService.getAll.size
    ))
  }

}
