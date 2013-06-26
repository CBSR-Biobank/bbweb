package controllers

import service._

import scala.concurrent._
import scala.concurrent.duration._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import securesocial.core.{ Identity, Authorization }

import scalaz._
import Scalaz._

object UserController extends Controller with securesocial.core.SecureSocial {

  lazy val userService = Global.services.userService

  def profile = SecuredAction { implicit request =>
    userService.getByEmail(request.user.email.getOrElse("")) match {
      case Some(user) =>
        Ok(views.html.user.profile())
      case _ => Forbidden
    }
  }

}
