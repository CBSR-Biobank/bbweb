package org.biobank.controllers

import org.biobank.dto._
import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService
import org.biobank.service.centre.CentresService

import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.Future
import play.api.Play.current
import scala.language.reflectiveCalls
import scaldi.{Injectable, Injector}

/**
  * Controller for the main page, and also the about and contact us pages.
  */
class Application(implicit inj: Injector)
    extends Controller
    with Security
    with JsonController
    with Injectable {

  implicit val authToken = inject [AuthToken]

  implicit val usersService = inject [UsersService]

  implicit val studiesService = inject [StudiesService]

  implicit val centresService = inject [CentresService]

  def index = Action {
    // does not return a JSON object, but HTML content
    Results.Ok(views.html.index())
  }

  def aggregateCounts = AuthAction(parse.empty) { (token, userId, request) =>
    Ok(AggregateCountsDto(
      studiesService.getAll.size,
      centresService.getAll.size,
      usersService.getAll.size
    ))
  }

}

