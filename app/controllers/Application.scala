package controllers

import scala.language.postfixOps
import play.api.mvc._
import views._

object Application extends Controller with securesocial.core.SecureSocial {

  lazy val userService = Global.services.userService
  lazy val studyService = Global.services.studyService

  def index = SecuredAction { implicit request =>
    Ok(html.index(request))
  }

  def about = UserAwareAction { implicit request =>
    Ok(html.about())
  }

  def contact = UserAwareAction { implicit request =>
    Ok(html.contact())
  }

}

