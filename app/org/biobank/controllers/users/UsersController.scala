package org.biobank.controllers.users

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.{Inject, Singleton}
import org.biobank.dto._
import org.biobank.domain.Slug
import org.biobank.domain.access.{AccessItemId, MembershipId}
import org.biobank.domain.users._
import org.biobank.controllers._
import org.biobank.infrastructure.commands.Commands._
import org.biobank.infrastructure.commands.UserCommands._
import org.biobank.services.users.UsersService
import org.biobank.services.{AuthToken, PagedResults}
import org.biobank.utils.auth.DefaultEnv
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache.SyncCacheApi
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.mvc._
import play.api.{Environment, Logger}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object UsersController {

  /** Used for obtaining the email and password from the HTTP login request */
  final case class LoginCredentials(email: String, password: String) extends Command

  final case class Token(user: UserDto, token: String, expiresOn: DateTime)

  /** JSON reader for [[LoginCredentials]]. */
  implicit val loginCredentialsFormat: Format[LoginCredentials] = Json.format[LoginCredentials]

  final case class PasswordUpdate(currentPassword: String, newPassword: String)

  implicit protected val passwordUpdatenReads: Reads[PasswordUpdate] =
    Json.reads[PasswordUpdate]

  implicit val jodaDateReads: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit val jodaDateWrites: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit val tokenFormat: Format[Token] = Json.format[Token]

}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class UsersController @Inject() (controllerComponents: ControllerComponents,
                                 val action:           BbwebAction,
                                 val env:              Environment,
                                 val cacheApi:         SyncCacheApi,
                                 val authToken:        AuthToken,
                                 val usersService:     UsersService,
                                 silhouette:           Silhouette[DefaultEnv])
                             (implicit val ec: ExecutionContext)
    extends CommandController(controllerComponents) {

  import CommandController._
  import UsersController._

  val log: Logger = Logger(this.getClass)

  private val PageSizeMax = 20

  /**
   * Used for user login. Expects the credentials in the body in JSON format.
   *
   * @return The token needed for subsequent requests
   */
  def login(): Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      // FIXME: what if user attempts multiple failed logins? lock the account after 3 attempts?
      // how long to lock the account?
      val v = for {
          credentials <- request.body.as[JsObject].validate[LoginCredentials].asOpt.toSuccessNel("bad credentials")
          user <- usersService.loginAllowed(credentials.email, credentials.password)
        } yield {
          val loginInfo = LoginInfo(CredentialsProvider.ID, user.id)
          for {
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(
              token,
              Ok(Json.toJson(Token(user, token, authenticator.expirationDateTime))))

          } yield {
            log.debug(s"user logged in: ${user.email}, token: $token")
            result
          }
        }

      v.fold(
        err => Future.successful(play.api.mvc.Results.Unauthorized),
        result => result
      )
    }

  /**
   * Retrieves the user associated with the token, if it is valid.
   */
  def authenticateUser(): Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
      Future {
        val userId = request.identity.user.id
        usersService.getUserIfAuthorized(userId, userId)
          .fold(
            err => Unauthorized,
            user => Ok(user)
          )
      }
    }

  /**
   * Used for logging out a user. Invalidates the authentication token.
   */
  def logout(): Action[AnyContent] = silhouette.SecuredAction.async { implicit request =>
      silhouette.env.authenticatorService.discard(request.authenticator, Ok)
    }

  def userCounts(): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      validationReply(Future(usersService.getCountsByStatus(request.identity.user.id)))
    }

  def list: Action[Unit] =
    action.async(parse.empty) { implicit request =>
      PagedQueryHelper(request.rawQueryString, PageSizeMax).fold(
        err => {
          validationReply(Future.successful(err.failure[PagedResults[UserDto]]))
        },
        pagedQuery => {
          validationReply(usersService.getUsers(request.identity.user.id, pagedQuery))
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
          validationReply(usersService.getUserNames(request.identity.user.id, query))
        }
      )
    }

  def getBySlug(slug: Slug): Action[Unit] =
    action(parse.empty) { implicit request =>
      val v = usersService.getUserBySlug(request.identity.user.id, slug)
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
      validationReply(usersService.snapshotRequest(request.identity.user.id).map { _ => true })
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
                                                          "sessionUserId" -> request.identity.user.id.id)
      reqJson.validate[UpdateEntityJson].fold(
        errors =>  Future.successful(BadRequest(Json.obj("status" -> "error",
                                                         "message" -> s"invalid json values: $errors"))),
        updateEntity => {
          updateEntityJsonToCommand(updateEntity).fold(
            errors => {
              val message = (JsError.toJson(errors) \ "obj" \ 0 \ "msg" \ 0).as[String]
              Future.successful(BadRequest(Json.obj("status" -> "error", "message" -> message)))
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
          validationReply(usersService.getUserStudies(request.identity.user.id, query))
        }
      )
    }

  def addRole(userId: UserId): Action[JsValue] =
    commandAction[UpdateUserAddRoleCmd](Json.obj("id" -> userId))(processCommand)

  def removeRole(userId: UserId, version: Long, roleId: AccessItemId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = UpdateUserRemoveRoleCmd(sessionUserId   = request.identity.user.id.id,
                                        id              = userId.id,
                                        expectedVersion = version,
                                        roleId          = roleId.id)
      processCommand(cmd)
    }

  def addMembership(userId: UserId): Action[JsValue] =
    commandAction[UpdateUserAddMembershipCmd](Json.obj("id" -> userId))(processCommand)

  def removeMembership(userId: UserId, version: Long, membershipId: MembershipId): Action[Unit] =
    action.async(parse.empty) { implicit request =>
      val cmd = UpdateUserRemoveMembershipCmd(sessionUserId   = request.identity.user.id.id,
                                              id              = userId.id,
                                              expectedVersion = version,
                                              membershipId    = membershipId.id)
      processCommand(cmd)
    }

  private def updateEntityJsonToCommand(json: UpdateEntityJson): JsResult[UserModifyCommand] = {
    json.property match {
      case "name" =>
        json.newValue
          .validate[String].map { newName =>
          UpdateUserNameCmd(sessionUserId   = json.sessionUserId,
                            id              = json.id,
                            expectedVersion = json.expectedVersion,
                            name            = newName)
        }
      case "email" =>
        json.newValue.validate[String].map { newEmail =>
          UpdateUserEmailCmd(sessionUserId   = json.sessionUserId,
                             id              = json.id,
                             expectedVersion = json.expectedVersion,
                             email           = newEmail)
        }
      case "password" =>
        json.newValue.validate[PasswordUpdate].map { passwordInfo =>
          UpdateUserPasswordCmd(sessionUserId   = json.sessionUserId,
                                id              = json.id,
                                expectedVersion = json.expectedVersion,
                                currentPassword = passwordInfo.currentPassword,
                                newPassword     = passwordInfo.newPassword)
        }
      case "avatarUrl" =>
        json.newValue.validate[String].map { newValue =>
          val url = if (newValue.isEmpty) None else Some(newValue)
          UpdateUserAvatarUrlCmd(sessionUserId   = json.sessionUserId,
                                 id              = json.id,
                                 expectedVersion = json.expectedVersion,
                                 avatarUrl       = url)
        }
      case "state" =>
        json.newValue.validate[String].map { stateAction =>
          stateAction match {
            case "activate" =>
              ActivateUserCmd(sessionUserId   = json.sessionUserId,
                              id              = json.id,
                              expectedVersion = json.expectedVersion)
            case "lock" =>
              LockUserCmd(sessionUserId   = json.sessionUserId,
                          id              = json.id,
                          expectedVersion = json.expectedVersion)
            case "unlock" =>
              UnlockUserCmd(sessionUserId   = json.sessionUserId,
                            id              = json.id,
                            expectedVersion = json.expectedVersion)
          }
        }
      case _ =>
        JsError(JsonValidationError(s"user does not support updates to property ${json.property}"))    }
  }

  private def processCommand(cmd: UserCommand) = {
    validationReply(usersService.processCommand(cmd))
  }

}
