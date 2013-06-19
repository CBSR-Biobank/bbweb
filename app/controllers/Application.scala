package controllers

import infrastructure._
import domain._
import service._

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import akka._
import akka.util.Timeout
import views._
import scalaz._
import Scalaz._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import securesocial.core.{ Identity, Authorization }

object Application extends Controller with securesocial.core.SecureSocial {

  lazy val userService = Global.services.userService
  lazy val studyService = Global.services.studyService

  def index = SecuredAction { implicit request =>
    Ok(views.html.index(request.user))
  }

  def about = UserAwareAction { implicit request =>
    Ok(html.about(request.user))
  }

  def contact = UserAwareAction { implicit request =>
    Ok(html.contact(request.user))
  }

}

