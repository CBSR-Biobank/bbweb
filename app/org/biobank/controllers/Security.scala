package org.biobank.controllers

import org.biobank.domain.{ DomainValidation, DomainError, UserId }

import scala.concurrent.Future
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.cache._

import scalaz._
import scalaz.Scalaz._

/**
 * Security actions that should be used by all controllers that need to protect their actions.
 * Can be composed to fine-tune access control.
 */
trait Security { self: Controller =>

  implicit val app: play.api.Application = play.api.Play.current

  val AuthTokenHeader = "X-XSRF-TOKEN"
  val AuthTokenCookieKey = "XSRF-TOKEN"
  val AuthTokenUrlKey = "auth"

  sealed case class AuthenticationInfo(token: String, userId: UserId)

  /*
   * Checks that the token is:
   *
   *  - present in the cookie header of the request,
   *
   *  - either in the header or in the query string,
   *
   *  - matches a token already stored in the play cache
   */
  private def validateToken[A](request: Request[A]): DomainValidation[AuthenticationInfo] = {
    val xsrfTokenCookieOption = request.cookies.get(AuthTokenCookieKey)
    val headerTokenOption = request.headers.get(AuthTokenHeader).orElse(request.getQueryString(AuthTokenUrlKey))

    if (xsrfTokenCookieOption == None) {
      DomainError("Invalid XSRF Token cookie").failNel
    } else if (headerTokenOption == None) {
      DomainError("No Token").failNel
    } else {
      val token = headerTokenOption.get
      val userIdOption = Cache.getAs[UserId](token)

      if (userIdOption == None) {
        DomainError("Token not found in cache").failNel
      } else {
        val xsrfTokenCookie = xsrfTokenCookieOption.get
        val userId = userIdOption.get

        if (xsrfTokenCookie.value.equals(token)) {
          AuthenticationInfo(token, userId).successNel
        } else {
          DomainError("Token does not match cookie").failNel
        }
      }
    }
  }

  /**
   * Ensures that the request has the proper authorization.
   *
   */
  def AuthAction[A](p: BodyParser[A] = parse.anyContent)(
    f: String => UserId => Request[A] => Result): Action[A] = Action(p) { implicit request =>
    validateToken(request).fold(
      err => Unauthorized(Json.obj("message" -> err.list.mkString(", "))),
      authInfo => f(authInfo.token)(authInfo.userId)(request))
  }

  /**
   * Ensures that the request has the proper authorization.
   *
   */
  def AuthActionAsync[A](p: BodyParser[A] = parse.anyContent)(
    f: String => UserId => Request[A] => Future[Result]) = Action.async(p) { implicit request =>
    validateToken(request).fold(
      err => Future.successful(Unauthorized(Json.obj("message" -> err.list.mkString(", ")))),
      authInfo => f(authInfo.token)(authInfo.userId)(request))
  }

}
