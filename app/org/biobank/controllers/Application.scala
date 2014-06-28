package org.biobank.controllers

import org.biobank.domain.UserId

import scala.language.postfixOps
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.cache._

/**
  * Controller for the main page, and also the about and contact us pages.
  */
object Application extends Controller with Security {

  private def userService = Play.current.plugin[BbwebPlugin].map(_.userService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def index = Action {
    Ok(views.html.index())
  }

  /**
    * Returns the JavaScript router that the client can use for "type-safe" routes.
    * @param varName The name of the global variable, defaults to `jsRoutes`
    */
  def jsRoutes(varName: String = "jsRoutes") = Action { implicit request =>
    Ok(
      Routes.javascriptRouter(varName)(
        routes.javascript.Application.login,
        routes.javascript.Application.logout,
        routes.javascript.UserController.authUser,
        routes.javascript.UserController.user,
        routes.javascript.UserController.addUser,
        routes.javascript.UserController.updateUser,
        routes.javascript.UserController.removeUser,
        org.biobank.controllers.study.routes.javascript.StudyController.list,
        org.biobank.controllers.study.routes.javascript.StudyController.query,
        org.biobank.controllers.study.routes.javascript.StudyController.add,
        org.biobank.controllers.study.routes.javascript.StudyController.update,
        org.biobank.controllers.study.routes.javascript.StudyController.enable,
        org.biobank.controllers.study.routes.javascript.StudyController.disable,
        org.biobank.controllers.study.routes.javascript.StudyController.retire,
        org.biobank.controllers.study.routes.javascript.StudyController.unretire,
        org.biobank.controllers.study.routes.javascript.StudyController.valueTypes,
        org.biobank.controllers.study.routes.javascript.StudyController.anatomicalSourceTypes,
        org.biobank.controllers.study.routes.javascript.StudyController.specimenTypes,
        org.biobank.controllers.study.routes.javascript.StudyController.preservTypes,
        org.biobank.controllers.study.routes.javascript.StudyController.preservTempTypes,
        org.biobank.controllers.study.routes.javascript.ParticipantAnnotTypeController.get,
        org.biobank.controllers.study.routes.javascript.ParticipantAnnotTypeController.addAnnotationType,
        org.biobank.controllers.study.routes.javascript.ParticipantAnnotTypeController.updateAnnotationType,
        org.biobank.controllers.study.routes.javascript.ParticipantAnnotTypeController.removeAnnotationType
      )
    ).as(JAVASCRIPT)
  }

  /** Used for obtaining the email and password from the HTTP login request */
  case class LoginCredentials(email: String, password: String)

  /** JSON reader for [[LoginCredentials]]. */
  implicit val loginCredentialsReads = (
    (__ \ "email").read[String](minLength[String](5)) and
      (__ \ "password").read[String](minLength[String](2))
  )((email, password) => LoginCredentials(email, password))

  /**
    * Log-in a user. Expects the credentials in the body in JSON format.
    *
    * Set the cookie [[AuthTokenCookieKey]] to have AngularJS set the X-XSRF-TOKEN in the HTTP
    * header.
    *
    * @return The token needed for subsequent requests
    */
  def login() = Action(parse.json) { implicit request =>
    request.body.validate[LoginCredentials].fold(
      errors => {
        BadRequest(Json.obj("status" ->"error", "message" -> JsError.toFlatJson(errors)))
      },
      loginCredentials => {
        Logger.info(s"login: $loginCredentials")
        userService.getByEmail(loginCredentials.email).fold(
          err => {
            BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", ")))
          },
          user => {
            // TODO: token should be derived from salt
            val token = java.util.UUID.randomUUID().toString
            Cache.set(token, user.id)
            Ok(Json.obj("token" -> token))
              .withCookies(Cookie(AuthTokenCookieKey, token, None, httpOnly = false))
          }
        )
      }
    )
  }

  /**
    * Log-out a user. Invalidates the authentication token.
    *
    * Discard the cookie [[AuthTokenCookieKey]] to have AngularJS no longer set the
    * X-XSRF-TOKEN in HTTP header.
    */
  def logout() = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Cache.remove(token)
    Ok.discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))
  }
}

