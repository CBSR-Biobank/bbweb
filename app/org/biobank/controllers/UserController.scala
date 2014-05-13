package org.biobank.controllers

import org.biobank.service.ServiceComponent

import play.api._
import play.api.mvc._

import scalaz._
import Scalaz._

object UserController extends Controller  {

  private def userService = Play.current.plugin[BbwebPlugin].map(_.userService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def profile = Action { request =>
    userService.getByEmail("").fold(
      err => Forbidden,
      user => Ok(views.html.user.profile())
    )
  }

}
