package org.biobank.controllers

import javax.inject._
import org.biobank.dto.AggregateCountsDto
import org.biobank.service.AuthToken
import org.biobank.service.centres.CentresService
import org.biobank.service.study.StudiesService
import org.biobank.service.users.UsersService
import play.api.Environment
import play.api.mvc._
import scala.language.reflectiveCalls

/**
  * Controller for the main page, and also the about and contact us pages.
 */
@Singleton
class Application @Inject() (val env:                   Environment,
                             val authToken:             AuthToken,
                             val usersService:          UsersService,
                             val studiesService:        StudiesService,
                             val centresService:        CentresService
                             //, val studyPersistenceQuery: StudyPersistenceQuery
                             )
    extends Controller
    with Security
    with JsonController {

  def index = Action {
    // does not return a JSON object, but HTML content
    Results.Ok(views.html.index())
  }

  def aggregateCounts = AuthAction(parse.empty) { (token, userId, request) =>
    Ok(AggregateCountsDto(
      studiesService.getStudyCount,
      centresService.getAll.size,
      usersService.getAll.size
    ))
  }

}
