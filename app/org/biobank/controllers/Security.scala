package org.biobank.controllers

import org.biobank.domain.user.{ UserId, UserHelper }
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService

import scala.concurrent.Future
import play.api.{ Environment, Logger, Mode }
import play.api.mvc._
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  * Security actions that should be used by all controllers that need to protect their actions.
  * Can be composed to fine-tune access control.
 */
trait Security { self: Controller =>

  val env: Environment

  val AuthTokenCookieKey = "XSRF-TOKEN"
  val AuthTokenHeader = "X-XSRF-TOKEN"
  val AuthTokenUrlKey = "auth"
  val TestAuthToken = "bbweb-test-token"

  implicit val authToken: AuthToken

  implicit val usersService: UsersService

  sealed case class AuthenticationInfo(token: String, userId: UserId)

  /*
   * Checks that the token is:
   *
   *  - present in the cookie header of the request,
   *
   *  - either in the header or in the query string,
   *
   *  - that the cookie token matches the other one
   */
  private def validRequestToken[T](request: Request[T]): ControllerValidation[String] = {
    for {
      cookieXsrfToken <- {
        request.cookies.get(AuthTokenCookieKey)
          .toSuccessNel(ControllerError("Invalid XSRF Token cookie"))
          .map(_.value)
      }
      headerXsrfToken <- {
        request.headers.get(AuthTokenHeader).orElse(request.getQueryString(AuthTokenUrlKey))
          .toSuccessNel(ControllerError("No token"))
      }
      matchingTokens <- {
        if (cookieXsrfToken == headerXsrfToken) {
          headerXsrfToken.successNel[String]
        } else {
          ControllerError(s"tokens did not match: cookie/$cookieXsrfToken, header/$headerXsrfToken")
            .failureNel[String]
        }
      }
    } yield headerXsrfToken
  }

  private def getAuthInfo(token: String)
      : ControllerValidation[AuthenticationInfo] = {
    if ((env.mode == Mode.Test) && (token == TestAuthToken)) {
      // when running in TEST mode, always allow the action if the token is the test token
      AuthenticationInfo(token, org.biobank.Global.DefaultUserId).successNel
    } else {
      for {
        userId     <- authToken.getUserId(token)
        user       <- usersService.getUser(userId.id)
        activeUser <- UserHelper.isUserActive(user)
        auth       <- AuthenticationInfo(token, userId).successNel
      } yield auth
    }
  }

  /*
   * Checks that the token is:
   *
   *  - present in the cookie header of the request,
   *
   *  - either in the header or in the query string,
   *
   *  - matches a token already stored in the play cache
   *
   * Note: there is special behaviour if the code is running in TEST mode.
   */
  private def validateToken[A](request: Request[A])
      : ControllerValidation[AuthenticationInfo] = {
    for {
      token <- validRequestToken(request)
      auth <- getAuthInfo(token)
    } yield auth
  }

  /**
    * Ensures that the request has the proper authorization.
    *
   */
  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  def AuthAction[A](p: BodyParser[A] = parse.anyContent)
                (f: (String, UserId,  Request[A]) => Result): Action[A] =
    Action(p) { implicit request =>
      validateToken(request).fold(
        err => Unauthorized(Json.obj("status"  -> "error",
                                     "message" -> err.list.toList.mkString(", "))),
        authInfo => f(authInfo.token, authInfo.userId, request))
    }

  /**
    * Ensures that the request has the proper authorization.
    *
    */
  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  def AuthActionAsync[A](p: BodyParser[A] = parse.anyContent)
                     (f: (String, UserId, Request[A]) => Future[Result]): Action[A] =
    Action.async(p) { implicit request =>
      validateToken(request).fold(
        err => {
          Logger.debug(s"AuthActionAsync: $err")
          Future.successful(
            Unauthorized(Json.obj("status" ->"error", "message" -> err.list.toList.mkString(", "))))
        },
        authInfo => f(authInfo.token, authInfo.userId, request)
      )
    }

}
