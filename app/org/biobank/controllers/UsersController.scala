package org.biobank.controllers

import org.biobank.infrastructure.command.UserCommands._
import org.biobank.service.users.UsersService

import play.api.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import scala.concurrent.Future
import scala.language.reflectiveCalls
import scaldi.{Injectable, Injector}

import scalaz._
import Scalaz._

class UsersController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable {

  implicit val usersService = inject [UsersService]

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
        BadRequest(JsError.toFlatJson(errors))
      },
      loginCredentials => {
        // TODO: token should be derived from salt
        usersService.validatePassword(loginCredentials.email, loginCredentials.password).fold(
          err => {
            Logger.info(s"login: error: $err")
            val errStr = err.list.mkString(", ")
            // FIXME: what if user attempts multiple failed logins? lock the account after 3 attempts?
            // how long to lock the account?
            if (errStr.contains("not found") || errStr.contains("invalid password")) {
              Forbidden("invalid email or password")
            } else if (errStr.contains("not active") || errStr.contains("is locked")) {
              Forbidden(err.list.mkString(", "))
            } else {
              NotFound(err.list.mkString(", "))
            }
          },
          user => {
            Logger.info(s"user logged in: ${user.email}")
            val token = java.util.UUID.randomUUID.toString.replaceAll("-","")
            Cache.set(token, user.id)
            Ok(token).withCookies(Cookie(AuthTokenCookieKey, token, None, httpOnly = false))
          }
        )
      }
    )
  }

  /** Retrieves the user associated with the token, if it is valid.
    */
  def authenticateUser() = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    usersService.getUser(userId.id).fold(
      err  => BadRequest(err.list.mkString(", ")),
      user => Ok(user)
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
    Ok("user has been logged out").discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))
  }

  /** Resets the user's password.
    */
  def passwordReset() = Action.async(parse.json) { implicit request =>
    request.body.validate[ResetUserPasswordCmd].fold(
      errors => {
        Future.successful(BadRequest(JsError.toFlatJson(errors)))
      },
      command => {
        val future = usersService.resetPassword(command)
        future.map { validation =>
          validation.fold(
            err => {
              val errStr = err.list.mkString(", ")
              if (errStr.contains("not found")) {
                NotFound("email address not registered")
              } else if (errStr.contains("not active")) {
                Forbidden("user is not active")
              } else {
                BadRequest("email address not registered")
              }
            },
            event => Ok("password has been reset")
          )
        }
      }
    )
  }

  def list(query: Option[String], sort: Option[String], order: Option[String]) =
    AuthAction(parse.empty) { token => implicit userId => implicit request =>
      val users = usersService.getAll.toList
      Ok(users)
    }

  /** Retrieves the user for the given id as JSON */
  def user(id: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Logger.info(s"user: id: $id")
    usersService.getUser(id).fold(
      err => BadRequest(err.list.mkString(", ")),
      user => Ok(user)
    )
  }

  def registerUser() = Action.async(parse.json) { implicit request =>
    request.body.validate[RegisterUserCmd].fold(
      errors => {
        Future.successful(BadRequest(JsError.toFlatJson(errors)))
      },
      cmd => {
        Logger.info(s"addUser: cmd: $cmd")
        val future = usersService.register(cmd)
        future.map { validation =>
          validation.fold(
            err   => {
              val errs = err.list.mkString(", ")
              if (errs.contains("exists")) {
                Forbidden("already registered")
              } else {
                BadRequest(errs)
              }
            },
            event => Ok(event)
          )
        }
      }
    )
  }

  def updateName(id: String) =  commandAction { cmd: UpdateUserNameCmd => implicit userId =>
    val future = usersService.updateName(cmd)
    domainValidationReply(future)
  }

  def updateEmail(id: String) =  commandAction { cmd: UpdateUserEmailCmd => implicit userId =>
    val future = usersService.updateEmail(cmd)
    domainValidationReply(future)
  }

  def updatePassword(id: String) =  commandAction { cmd: UpdateUserPasswordCmd => implicit userId =>
    val future = usersService.updatePassword(cmd)
    domainValidationReply(future)
  }

  def updateAvatarUrl(id: String) =  commandAction { cmd: UpdateUserAvatarUrlCmd => implicit userId =>
    val future = usersService.updateAvatarUrl(cmd)
    domainValidationReply(future)
  }

  def activateUser(id: String) =  commandAction { cmd: ActivateUserCmd => implicit userId =>
      if (cmd.id != id) {
        Future.successful(BadRequest("user id mismatch"))
      } else {
        val future = usersService.activate(cmd)
        domainValidationReply(future)
      }
  }

  def lockUser(id: String) =  commandAction { cmd: LockUserCmd => implicit userId =>
      if (cmd.id != id) {
        Future.successful(BadRequest("user id mismatch"))
      } else {
        val future = usersService.lock(cmd)
        domainValidationReply(future)
      }
  }

  def unlockUser(id: String) =  commandAction { cmd: UnlockUserCmd => implicit userId =>
      if (cmd.id != id) {
        Future.successful(BadRequest("user id mismatch"))
      } else {
        val future = usersService.unlock(cmd)
        domainValidationReply(future)
      }
  }

}
