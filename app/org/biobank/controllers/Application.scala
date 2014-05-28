package org.biobank.controllers

import scala.language.postfixOps
import play.api._
import play.api.mvc._
import play.api.libs.json._


/**
 * Controller for the main page, and also the about and contact us pages.
 */
object Application extends Controller  {

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
        routes.javascript.UserController.user,
        routes.javascript.UserController.createUser,
        routes.javascript.UserController.updateUser,
        routes.javascript.UserController.deleteUser
        // TODO Add your routes here
      )
    ).as(JAVASCRIPT)
  }
  /**
   * Log-in a user. Pass the credentials as JSON body.
   * @return The token needed for subsequent requests
   */
  def login() = Action(parse.json) { implicit request =>
    // TODO Check credentials, log user in, return correct token
    val token = java.util.UUID.randomUUID().toString
    Ok(Json.obj("token" -> token))
  }

  /** Logs the user out, i.e. invalidated the token. */
  def logout() = Action {
    // TODO Invalidate token, remove cookie
    Ok
  }
}

