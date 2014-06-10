package org.biobank.controllers

import org.biobank.domain.UserId

import scala.language.postfixOps
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.cache._

/**
  * Controller for the main page, and also the about and contact us pages.
  */
object Application extends Controller with Security {

  def index = Action {
    Ok(views.html.index())
  }

  /**
    * Returns the JavaScript router that the client can use for "type-safe" routes.
    * @param varName The name of the global variable, defaults to `jsRoutes`
    */
  def jsRoutes(varName: String = "jsRoutes") = Cached(_ => "jsRoutes", duration = 86400) {
    Action { implicit request =>
      Ok(
        Routes.javascriptRouter(varName)(
          routes.javascript.Application.login,
          routes.javascript.Application.logout,
          org.biobank.controllers.study.routes.javascript.StudyController.list,
          org.biobank.controllers.study.routes.javascript.StudyController.query,
          routes.javascript.UserController.user,
          routes.javascript.UserController.addUser,
          routes.javascript.UserController.updateUser,
          routes.javascript.UserController.removeUser
        )
      ).as(JAVASCRIPT)
    }
  }

  /**
    * Log-in a user. Pass the credentials as JSON body.
    *
    * Set the cookie {@link AuthTokenCookieKey} to have AngularJS set X-XSRF-TOKEN in the HTTP
    * header.
    *
    * @return The token needed for subsequent requests
    */
  def login() = Action(parse.json) { implicit request =>
    // TODO Check credentials, log user in, return correct token
    val token = java.util.UUID.randomUUID().toString
     val userId = UserId("temp");
    Cache.set(token, userId)
    Ok(Json.obj("token" -> token))
      .withCookies(Cookie(AuthTokenCookieKey, token, None, httpOnly = false))
  }

  /**
    * Logs the user out.
    *
    * Discards the cookie {@link AuthTokenCookieKey} to have AngularJS no longer set the
    * X-XSRF-TOKEN in HTTP header.
    */
  def logout() = AuthAction(parse.json) { token => userId => implicit request =>
    Cache.remove(token)
    Ok.discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))
  }
}

