package org.biobank.controllers.users

import org.biobank.infrastructure.commands.Commands._
import javax.inject.{Inject, Singleton}
import org.biobank.dto._
import org.biobank.domain.Slug
import org.biobank.domain.access.{AccessItemId, MembershipId}
import org.biobank.domain.users._
import org.biobank.controllers._
import org.biobank.infrastructure.commands.UserCommands._
import org.biobank.services.users.UsersService
import org.biobank.services.{AuthToken, PagedResults}
import play.api.Logger
import play.api.cache.SyncCacheApi
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.mvc._
import play.api.{Environment, Logger}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._

object UsersController {

  /** Used for obtaining the email and password from the HTTP login request */
  final case class LoginCredentials(email: String, password: String) extends Command

  /** JSON reader for [[LoginCredentials]]. */
  implicit val loginCredentialsFormat: Format[LoginCredentials] = Json.format[LoginCredentials]

  final case class PasswordUpdate(currentPassword: String, newPassword: String)

  implicit protected val passwordUpdatenReads: Reads[PasswordUpdate] =
    Json.reads[PasswordUpdate]

}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class UsersController @Inject() (controllerComponents: ControllerComponents,
                                 val action:           BbwebAction,
                                 val env:              Environment,
                                 val cacheApi:         SyncCacheApi,
                                 val authToken:        AuthToken,
                                 val usersService:     UsersService)
                             (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  import CommandController._
  import UsersController._
  import org.biobank.controllers.Security._

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 20

  /**
   * Used for user login. Expects the credentials in the body in JSON format.
   *
   * Sets the cookie [[controllers.Security.AuthTokenCookieKey AuthTokenCookieKey]] to have AngularJS set the
   * X-XSRF-TOKEN in the HTTP header.
   *
   * @return The token needed for subsequent requests
   */
  def login(): Action[JsValue] =
    anonymousCommandAction[LoginCredentials]{ credentials =>
      Future {
        // FIXME: what if user attempts multiple failed logins? lock the account after 3 attempts?
        // how long to lock the account?
        usersService.loginAllowed(credentials.email, credentials.password).fold(
          err => Unauthorized,
          user => {
            val token = authToken.newToken(UserId(user.id))
            log.debug(s"user logged in: ${user.email}, token: $token")
            Ok(user).withCookies(Cookie(AuthTokenCookieKey, token, None, path = "/", httpOnly = false))
          }
        )
      }
    }

  /**
   * Retrieves the user associated with the token, if it is valid.
   */
  def authenticateUser(): Action[Unit] = action(parse.empty) { implicit request =>
      usersService.getUserIfAuthorized(request.authInfo.userId, request.authInfo.userId)
        .fold(
          err  => Unauthorized,
          user =>  Ok(user)
        )
    }

  /**
   * Used for logging out a user. Invalidates the authentication token.
   *
   * Discard the cookie [[controllers.Security.AuthTokenCookieKey AuthTokenCookieKey]] to have AngularJS no
   * longer set the X-XSRF-TOKEN in HTTP header.
   */
  def logout(): Action[Unit] = action(parse.empty) { implicit request =>
      authToken.removeToken(request.authInfo.token)
      Ok("user has been logged out")
        .discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))
        .withNewSession
    }

  def userCounts(): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(Future(usersService.getCountsByStatus(request.authInfo.userId)))
    }

  def list: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[UserDto]]))
        },
        pagedQuery => {
          validationReply(usersService.getUsers(request.authInfo.userId, pagedQuery))
        }
      )
    }

  def listNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      FilterAndSortQueryHelper(request.rawQueryString).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[NameAndStateDto]]))
        },
        query => {
          validationReply(usersService.getUserNames(request.authInfo.userId, query))
        }
      )
    }

  def getBySlug(slug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      val v = usersService.getUserBySlug(request.authInfo.userId, slug)
      validationReply(v)
    }

  /**
   * Permissions not checked since anyone can register as a user.
   */
  def registerUser(): Action[JsValue] =
    anonymousCommandAction[RegisterUserCmd]{ cmd =>
      usersService.processCommand(cmd).map { validation =>
        validation.fold(
          err   => {
            val errs = err.list.toList.mkString(", ")
            if (errs.contains("exists")) {
              Forbidden("email already registered")
            } else {
              BadRequest(errs)
            }
          },
          user => Ok(user)
        )
      }
    }

  def snapshot: Action[Unit] =
    action(parse.empty) { implicit request =>
      validationReply(usersService.snapshotRequest(request.authInfo.userId).map { _ => true })
    }

  /**
   * Resets the user's password.
   *
   * Permissions not checked since anyone can request a password reset..
   */
  def passwordReset(): Action[JsValue] =
    anonymousCommandAction[ResetUserPasswordCmd]{ cmd =>
      usersService.processCommand(cmd).map { validation =>
        validation.fold(
          err   => Unauthorized,
          event => Ok("password has been reset")
        )
      }
    }

  def update(id: UserId): Action[JsValue] =
    action.async(parse.json) { request =>
      val reqJson = request.body.as[JsObject] ++ Json.obj("id"            -> id,
                                                          "sessionUserId" -> request.authInfo.userId.id)
      reqJson.validate[UpdateEntityJson].fold(
        errors =>  Future.successful(BadRequest(Json.obj("status" -> "error",
                                                         "message" -> "invalid json values"))),
        updateEntity => {
          updateEntityJsonToCommand(updateEntity).fold(
            errors => {
              val errMsgs = errors.list.toList.mkString(", ")
              Future.successful(BadRequest(Json.obj("status" -> "error", "message" -> errMsgs)))
            },
            command => validationReply(usersService.processCommand(command))
          )
        }
      )
    }

  def userStudies: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      FilterAndSortQueryHelper(request.rawQueryString).fold(
        err => {
          validationReply(Future.successful(err.failure[Seq[CentreDto]]))
        },
        query => {
          validationReply(usersService.getUserStudies(request.authInfo.userId, query))
        }
      )
    }

  def addRole(userId: UserId): Action[JsValue] =
    commandAction[UpdateUserAddRoleCmd](Json.obj("id" -> userId))(processCommand)

  def removeRole(userId: UserId, version: Long, roleId: AccessItemId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = UpdateUserRemoveRoleCmd(sessionUserId   = request.authInfo.userId.id,
                                        id              = userId.id,
                                        expectedVersion = version,
                                        roleId          = roleId.id)
      processCommand(cmd)
    }

  def addMembership(userId: UserId): Action[JsValue] =
    commandAction[UpdateUserAddMembershipCmd](Json.obj("id" -> userId))(processCommand)

  def removeMembership(userId: UserId, version: Long, membershipId: MembershipId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = UpdateUserRemoveMembershipCmd(sessionUserId   = request.authInfo.userId.id,
                                              id              = userId.id,
                                              expectedVersion = version,
                                              membershipId    = membershipId.id)
      processCommand(cmd)
    }

  private def updateEntityJsonToCommand(json: UpdateEntityJson)
      : ControllerValidation[UserModifyCommand] = {

    def jsonError(): ControllerValidation[UserModifyCommand]  = {
      ControllerError(s"could not parse value for ${json.property}: ${json.newValue}")
        .failureNel[UserModifyCommand]
    }

    json.property match {
      case "name" =>
        json.newValue.validate[String].fold(
          error => jsonError,
          newName => {
            UpdateUserNameCmd(sessionUserId   = json.sessionUserId,
                              id              = json.id,
                              expectedVersion = json.expectedVersion,
                              name            = newName).successNel[String]
          }
        )
      case "email" =>
        json.newValue.validate[String].fold(
          error => jsonError,
          newEmail => {
            UpdateUserEmailCmd(sessionUserId   = json.sessionUserId,
                               id              = json.id,
                               expectedVersion = json.expectedVersion,
                               email           = newEmail).successNel[String]
          }
        )
      case "password" =>
        json.newValue.validate[PasswordUpdate].fold(
          error => jsonError,
          passwordInfo => {
            UpdateUserPasswordCmd(sessionUserId   = json.sessionUserId,
                                  id              = json.id,
                                  expectedVersion = json.expectedVersion,
                                  currentPassword = passwordInfo.currentPassword,
                                  newPassword     = passwordInfo.newPassword).successNel[String]
          }
        )
      case "avatarUrl" =>
        json.newValue.validate[String].fold(
          error => jsonError,
          newValue => {
            val url = if (newValue.isEmpty) None else Some(newValue)
            UpdateUserAvatarUrlCmd(sessionUserId   = json.sessionUserId,
                                   id              = json.id,
                                   expectedVersion = json.expectedVersion,
                                   avatarUrl       = url).successNel[String]
          }
        )
      case "state" =>
        json.newValue.validate[String].fold(
          error => jsonError,
          stateAction => {
            stateAction match {
              case "activate" =>
                ActivateUserCmd(sessionUserId   = json.sessionUserId,
                                id              = json.id,
                                expectedVersion = json.expectedVersion).successNel[String]
              case "lock" =>
                LockUserCmd(sessionUserId   = json.sessionUserId,
                            id              = json.id,
                            expectedVersion = json.expectedVersion).successNel[String]
              case "unlock" =>
                UnlockUserCmd(sessionUserId   = json.sessionUserId,
                              id              = json.id,
                              expectedVersion = json.expectedVersion).successNel[String]
            }
          }
        )
      case _ =>
        ControllerError(s"user does not support updates to property ${json.property}")
          .failureNel[UserModifyCommand]
    }
  }

  private def processCommand(cmd: UserCommand) = {
    validationReply(usersService.processCommand(cmd))
  }

}
