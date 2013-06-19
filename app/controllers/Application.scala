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

  def onlyGoogle = SecuredAction(WithProvider("google")) { implicit request =>
    //
    //    Note: If you had a User class and returned an instance of it from UserService, this
    //          is how you would convert Identity to your own class:
    //
    //    request.user match {
    //      case user: User => // do whatever you need with your user class
    //      case _ => // did not get a User instance, should not happen,log error/thow exception
    //    }
    Ok("You can see this because you logged in using Google")
  }

  def about = UserAwareAction { implicit request =>
    Ok(html.about(request.user))
  }

  def contact = UserAwareAction { implicit request =>
    Ok(html.contact(request.user))
  }

}

// An Authorization implementation that only authorizes uses that logged in using twitter
case class WithProvider(provider: String) extends Authorization {
  def isAuthorized(user: Identity) = {
    user.id.providerId == provider
  }
}

