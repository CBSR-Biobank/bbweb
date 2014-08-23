package org.biobank.controllers

import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service.json.User._

import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.Future
import com.typesafe.plugin.use
import play.api.Play.current
import scala.language.reflectiveCalls

/**
  * Controller for the main page, and also the about and contact us pages.
  */
object Application extends Controller with Security with JsonController {

  def index = Action {
    // does not return a JSON object, but HTML content
    Results.Ok(views.html.index())
  }

  // FIXME move to DTO file
  case class AggregateCountsDto(studies: Int, centres: Int, users: Int)

  // FIXME move to DTO file
  implicit val aggregateCountsDtoWriter: Writes[AggregateCountsDto] = (
    (__ \ "studies").write[Int] and
      (__ \ "centres").write[Int] and
      (__ \ "users").write[Int]
  )(unlift(AggregateCountsDto.unapply))

  def aggregateCounts = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Ok(AggregateCountsDto(
        use[BbwebPlugin].studiesService.getAll.size,
        use[BbwebPlugin].centresService.getAll.size,
        use[BbwebPlugin].usersService.getAll.size
      ))
  }

}

