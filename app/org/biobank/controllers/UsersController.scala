package org.biobank.controllers

import org.biobank.infrastructure.command.UserCommands._
import org.biobank.service.json.User._
import org.biobank.service.json.Events._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.cache.Cache

import scalaz._
import Scalaz._

object UsersController extends CommandController {

  private def usersService = Play.current.plugin[BbwebPlugin].map(_.usersService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  /** Used for obtaining the email and password from the HTTP login request */
  case class LoginCredentials(email: String, password: String)

  /** JSON reader for [[LoginCredentials]]. */
  implicit val loginCredentialsReads = (
    (__ \ "email").read[String](minLength[String](5)) and
      (__ \ "password").read[String](minLength[String](2))
  )(LoginCredentials.apply _)

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
            val errStr = err.list.mkString(", ")
            Logger.debug(s"login: error: $err")
            if (errStr.contains("not found") || errStr.contains("invalid password")) {
              Forbidden(Json.obj("status" ->"error", "message" -> "invalid email or password"))
            } else {
              NotFound(Json.obj("status" ->"error", "message" -> err.list.mkString(", ")))
            }
          },
          user => {
            Logger.info(s"user logged in: ${user.email}")
            val token = java.util.UUID.randomUUID().toString
            Cache.set(token, user.id)
            Ok(Json.obj("status" -> "success", "token" -> token))
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
    Ok(Json.obj("status" -> "success", "message" -> "user has been logged out"))
      .discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))
  }

  /** Resets the user's password.
    */
  def passwordReset() = Action.async(parse.json) { implicit request =>
    request.body.validate[ResetUserPasswordCmd].fold(
      errors => {
        Future.successful(
          BadRequest(Json.obj("status" ->"error", "message" -> JsError.toFlatJson(errors))))
      },
      command => {
        val future = usersService.resetPassword(command)
        future.map { validation =>
          validation.fold(
            err => {
              val errStr = err.list.mkString(", ")
              if (errStr.contains("not found")) {
                NotFound(Json.obj("status" ->"error", "message" -> "email address not registered"))
              } else if (errStr.contains("user is not active")) {
                Forbidden(Json.obj("status" ->"error", "message" -> "user is not active"))
              } else {
                BadRequest(Json.obj("status" ->"error", "message" -> "email address not registered"))
              }
            },
            event => Ok(Json.obj("status" -> "success", "message" -> "password has been reset"))
          )
        }
      }
    )
  }

  def list = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Ok(Json.toJson(usersService.getAll.toList))
  }

  /** Retrieves the user associated with the token, if it is valid.
    */
  def authenticateUser() = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    usersService.getByEmail(userId.id).fold(
      err  => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
      user => Ok(Json.toJson(user))
    )
  }

  /** Retrieves the user for the given id as JSON */
  def user(id: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Logger.info(s"user: id: $id")
    usersService.getByEmail(id).fold(
      err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
      user => Ok(Json.toJson(user))
    )
  }

  def addUser() = Action.async(parse.json) { implicit request =>
    request.body.validate[RegisterUserCmd].fold(
      errors => {
        Future.successful(
          BadRequest(Json.obj("status" ->"error", "message" -> JsError.toFlatJson(errors))))
      },
      cmd => {
        Logger.info(s"addUser: cmd: $cmd")
        val future = usersService.register(cmd)
        future.map { validation =>
          validation.fold(
            err   => {
              val errs = err.list.mkString(", ")
              if (errs.contains("")) {
                Forbidden(Json.obj("status" ->"error", "message" -> "already registered"))
              } else {
                BadRequest(Json.obj("status" ->"error", "message" -> errs))
              }
            },
            event => Ok(eventToJsonReply(event))
          )
        }
      }
    )
  }

  def activateUser(id: String) =  CommandAction { cmd: ActivateUserCmd => implicit userId =>
    val future = usersService.activate(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def updateUser(id: String) =  CommandAction { cmd: UpdateUserCmd => implicit userId =>
    val future = usersService.update(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def lockUser(id: String) =  CommandAction { cmd: LockUserCmd => implicit userId =>
    val future = usersService.lock(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def unlockUser(id: String) =  CommandAction { cmd: UnlockUserCmd => implicit userId =>
    Logger.info(s"unlockUser")
    val future = usersService.unlock(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeUser(id: String, ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveUserCmd(id, ver)
    val future = usersService.remove(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def resetPassword(id: String) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = ResetUserPasswordCmd(id)
    val future = usersService.resetPassword(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

}
