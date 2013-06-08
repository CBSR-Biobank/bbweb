package controllers

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import akka._
import akka.util.Timeout
import domain._
import service._
import views._
import scalaz._
import Scalaz._
import play.api.libs.concurrent.Akka
import play.api.Play.current
import java.util.concurrent.TimeUnit

object Application extends Controller {

  lazy val userService = Global.services.userService
  lazy val studyService = Global.services.studyService

  // -- Authentication

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text) verifying (
        "Invalid email or password", result => result match {
          case (email, password) =>
            Await.result(userService.authenticate(email, password), 5.seconds) match {
              case Success(user) => true
              case _ => false
            }
        }))

  /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.StudyController.index).withSession("email" -> user._1))
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out")
  }

  // -- Javascript routing

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutes")(StudyController.add)).as("text/javascript")
  }

}

/**
 * Provide security features
 */
trait Secured {

  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("email")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

  // --

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  /**
   * Check if the connected user is a member of this project.
   */
  def IsMemberOf(project: Long)(f: => String => Request[AnyContent] => Result) = IsAuthenticated { user =>
    request =>
      //      if (Study.isMember(project, user)) {
      //        f(user)(request)
      //      } else {
      //        Results.Forbidden
      //      }
      f(user)(request)

  }

}
