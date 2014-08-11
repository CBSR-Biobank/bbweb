package org.biobank.controllers

import org.biobank.domain.user.UserId

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

  private def usersService = Play.current.plugin[BbwebPlugin].map(_.usersService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def index = Action {
    Ok(views.html.index())
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
        // TODO: token should be derived from salt
        usersService.validatePassword(loginCredentials.email, loginCredentials.password).fold(
          err => {
            BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", ")))
          },
          user => {
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
    Ok(Json.obj("status" -> "success"))
      .discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))
  }
}

