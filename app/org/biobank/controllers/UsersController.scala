package org.biobank.controllers

import org.biobank.domain.user._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEventsJson._
import org.biobank.service.users.UsersService
import org.biobank.service.study.StudiesService

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
import scalaz.Validation.FlatMap._

class UsersController(implicit inj: Injector)
    extends CommandController
    with JsonController
    with Injectable {

  implicit val usersService = inject [UsersService]

  implicit val studiesService = inject [StudiesService]

  private val PageSizeDefault = 5

  private val PageSizeMax = 20

  /** Used for obtaining the email and password from the HTTP login request */
  case class LoginCredentials(email: String, password: String)

  /** JSON reader for [[LoginCredentials]]. */
  implicit val loginCredentialsReads = Json.reads[LoginCredentials]

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
            Logger.debug(s"login: error: $err")
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
            Logger.debug(s"user logged in: ${user.email}")
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
  def authenticateUser() = AuthAction(parse.empty) { (token, userId, request) =>
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
  def logout() = AuthAction(parse.empty) { (token, userId, request) =>
    Cache.remove(token)
    Ok("user has been logged out")
      .discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))
      .withNewSession
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

  def userCount() =
    AuthAction(parse.empty) { (token, userId, request) =>
      Ok(usersService.getAll.size)
    }

  def list(
    nameFilter: String,
    emailFilter: String ,
    status: String,
    sort: String,
    page: Int,
    pageSize: Int,
    order: String) =
    AuthAction(parse.empty) { (token, userId, request) =>
      Logger.debug(s"UsersController:list: nameFilter/$nameFilter, emailFilter/$emailFilter, status/$status, sort/$sort, page/$page, pageSize/$pageSize, order/$order")

      def sortWith(sortField: String): (User, User) => Boolean = {
        sortField match {
          case "name"  => (User.compareByName _)
          case "email" => (User.compareByEmail _)
          case _       => (User.compareByStatus _)
        }
      }

      val pagedQuery = PagedQuery(sort, page, pageSize, order)
      val validation = for {
        sortField   <- pagedQuery.getSortField(Seq("name", "email", "status"))
        sortWith    <- sortWith(sortField).success
        sortOrder   <- pagedQuery.getSortOrder
        users       <- usersService.getUsers(nameFilter, emailFilter, status, sortWith, sortOrder)
        page        <- pagedQuery.getPage(PageSizeMax, users.size)
        pageSize    <- pagedQuery.getPageSize(PageSizeMax)
        results     <- PagedResults.create(users, page, pageSize)
      } yield results

      validation.fold(
        err => BadRequest(err.list.mkString),
        results =>  Ok(results)
      )
    }

  /** Retrieves the user for the given id as JSON */
  def user(id: String) = AuthAction(parse.empty) { (token, userId, request) =>
    Logger.debug(s"user: id: $id")
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
        Logger.debug(s"addUser: cmd: $cmd")
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

  def userStudies(id: String, query: Option[String], sort: Option[String], order: Option[String]) =
    AuthAction(parse.empty) { (token, userId, request) =>
      // FIXME this should return the only the studies this user has access to
      //
      // This this for now, but fix once user groups have been implemented
      val studies = studiesService.getAll.toList
      Ok(studies)
    }
}
