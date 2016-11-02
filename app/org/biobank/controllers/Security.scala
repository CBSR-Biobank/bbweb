package org.biobank.controllers

import org.biobank.domain.user.{ UserId, UserHelper }
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import play.api.mvc._
import play.api.{ Environment, Mode }
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

final case class AuthenticationInfo(token: String, userId: UserId)

trait Security {

  val env: Environment

  val AuthTokenCookieKey = "XSRF-TOKEN"
  val AuthTokenHeader = "X-XSRF-TOKEN"
  val AuthTokenUrlKey = "auth"
  val TestAuthToken = "bbweb-test-token"

  val usersService: UsersService

  val authToken: AuthToken

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
        if (cookieXsrfToken == headerXsrfToken) headerXsrfToken.successNel[String]
        else ControllerError(s"tokens did not match: cookie/$cookieXsrfToken, header/$headerXsrfToken")
          .failureNel[String]
      }
    } yield headerXsrfToken
  }

  private def getAuthInfo(token: String): ControllerValidation[AuthenticationInfo] = {
    if ((env.mode == Mode.Test) && (token == TestAuthToken)) {
      // when running in TEST mode, always allow the action if the token is the test token
      AuthenticationInfo(token, org.biobank.Global.DefaultUserId).successNel
    } else {
      for {
        userId     <- authToken.getUserId(token)
        user       <- usersService.getUser(userId)
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
  def validateToken[A](request: Request[A]): ControllerValidation[AuthenticationInfo] = {
    for {
      token <- validRequestToken(request)
      auth <- getAuthInfo(token)
    } yield auth
  }

}
