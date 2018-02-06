package org.biobank.controllers.users

import org.biobank.infrastructure.command.Commands._
import javax.inject.{Inject, Singleton}
import java.time.format.DateTimeFormatter
import org.biobank.dto._
import org.biobank.domain.access.Role
import org.biobank.domain.user._
import org.biobank.controllers._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.service.access.AccessService
import org.biobank.service.studies.StudiesService
import org.biobank.service.users.UsersService
import org.biobank.service.{AuthToken, PagedResults}
import play.api.Logger
import play.api.cache.SyncCacheApi
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.mvc._
import play.api.{Environment, Logger}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class UsersController @Inject() (controllerComponents: ControllerComponents,
                                 val action:         BbwebAction,
                                 val env:            Environment,
                                 val cacheApi:       SyncCacheApi,
                                 val authToken:      AuthToken,
                                 val accessService:  AccessService,
                                 val usersService:   UsersService,
                                 val studiesService: StudiesService)
                             (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  import org.biobank.controllers.Security._

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 20

  /** Used for obtaining the email and password from the HTTP login request */
  case class LoginCredentials(email: String, password: String) extends Command

  /** JSON reader for [[LoginCredentials]]. */
  implicit val loginCredentialsReads: Reads[LoginCredentials] = Json.reads[LoginCredentials]

  /**
   * Log-in a user. Expects the credentials in the body in JSON format.
   *
   * Set the cookie [[AuthTokenCookieKey]] to have AngularJS set the X-XSRF-TOKEN in the HTTP
   * header.
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
            val dto = userToDto(user, accessService.getUserRoles(user.id))
            val token = authToken.newToken(user.id)
            log.debug(s"user logged in: ${user.email}, token: $token")
            Ok(dto).withCookies(Cookie(AuthTokenCookieKey, token, None, path = "/", httpOnly = false))
          }
        )
      }
    }

  /**
   * Retrieves the user associated with the token, if it is valid.
   */
  def authenticateUser(): Action[Unit] = action(parse.empty) { implicit request =>
      usersService.getUserIfAuthorized(request.authInfo.userId, request.authInfo.userId)
        .map(user =>  userToDto(user, accessService.getUserRoles(user.id)))
        .fold(
          err  => Unauthorized,
          dto =>  Ok(dto)
        )
    }

  /**
   * Log-out a user. Invalidates the authentication token.
   *
   * Discard the cookie [[AuthTokenCookieKey]] to have AngularJS no longer set the
   * X-XSRF-TOKEN in HTTP header.
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
      validationReply(
        Future {
          for {
            pagedQuery <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            users      <- usersService.getUsers(request.authInfo.userId, pagedQuery.filter, pagedQuery.sort)
            validPage  <- pagedQuery.validPage(users.size)
            dtos       <- {
              users.map(user => userToDto(user, accessService.getUserRoles(user.id))).successNel[String]
            }
            results    <- PagedResults.create(dtos, pagedQuery.page, pagedQuery.limit)
          } yield results
        }
      )
    }

  def listNames: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            filterAndSort <- FilterAndSortQuery.create(request.rawQueryString)
            users         <- usersService.getUsers(request.authInfo.userId,
                                                   filterAndSort.filter,
                                                   filterAndSort.sort)
          } yield {
            users.map(user => NameAndStateDto(user.id.id, user.slug, user.name, user.state.id))
          }
        }
      )
    }

  def getBySlug(slug: String): Action[Unit] =
    action(parse.empty) { implicit request =>
      val v = usersService.getUserBySlug(request.authInfo.userId, slug)
        .map(user =>  userToDto(user, accessService.getUserRoles(user.id)))
      validationReply(v)
    }

  def registerUser(): Action[JsValue] =
    anonymousCommandAction[RegisterUserCmd]{ cmd =>
      Logger.debug(s"registerUser: cmd: $cmd")
      val future = usersService.register(cmd)
      future.map { validation =>
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

  /** Resets the user's password.
   */
  def passwordReset(): Action[JsValue] =
    anonymousCommandAction[ResetUserPasswordCmd]{ cmd =>
      val future = usersService.resetPassword(cmd)
      future.map { validation =>
        validation.fold(
          err   => Unauthorized,
          event => Ok("password has been reset")
        )
      }
    }

  def updateName(id: UserId): Action[JsValue] =
    commandAction[UpdateUserNameCmd](Json.obj("id" -> id))(processCommand)

  def updateEmail(id: UserId): Action[JsValue] =
    commandAction[UpdateUserEmailCmd](Json.obj("id" -> id))(processCommand)

  def updatePassword(id: UserId): Action[JsValue] =
    commandAction[UpdateUserPasswordCmd](Json.obj("id" -> id))(processCommand)

  def updateAvatarUrl(id: UserId): Action[JsValue] =
    commandAction[UpdateUserAvatarUrlCmd](Json.obj("id" -> id))(processCommand)

  def activateUser(id: UserId): Action[JsValue] =
    commandAction[ActivateUserCmd](Json.obj("id" -> id))(processCommand)

  def lockUser(id: UserId): Action[JsValue] =
    commandAction[LockUserCmd](Json.obj("id" -> id))(processCommand)

  def unlockUser(id: UserId): Action[JsValue] =
    commandAction[UnlockUserCmd](Json.obj("id" -> id))(processCommand)

  def userStudies: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(
        Future {
          for {
            pagedQuery <- PagedQuery.create(request.rawQueryString, PageSizeMax)
            studies    <- studiesService.getStudies(request.authInfo.userId,
                                                    pagedQuery.filter,
                                                    pagedQuery.sort)
            results    <- PagedResults.create(studies, pagedQuery.page, pagedQuery.limit)
          } yield results
        }
      )
    }

  private def processCommand(cmd: UserCommand) = {
    validationReply(usersService.processCommand(cmd))
  }

  private def userToDto(user: User, roles: Set[Role]): UserDto = {
    UserDto(id           = user.id.id,
            version      = user.version,
            timeAdded    = user.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            timeModified = user.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
            state        = user.state,
            slug         = user.slug,
            name         = user.name,
            email        = user.email,
            avatarUrl    = user.avatarUrl,
            roleData     = roles.map(r => EntityInfoDto(r.id.id, r.slug, r.name)),
            membership   = None)
  }
}
