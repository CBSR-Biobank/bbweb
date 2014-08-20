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

/**
  * Controller for the main page, and also the about and contact us pages.
  */
object Application extends Controller with Security {

  def index = Action {
    Ok(views.html.index())
  }

  def aggregateCounts = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Ok(Json.obj(
      "status" ->"success",
      "data" -> Json.obj(
        "studies" -> use[BbwebPlugin].studiesService.getAll.size,
        "centres" -> use[BbwebPlugin].centresService.getAll.size,
        "users"   -> use[BbwebPlugin].usersService.getAll.size
      )))
  }

}

