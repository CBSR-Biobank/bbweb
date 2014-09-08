package org.biobank.controllers

import org.biobank.domain.{ DomainValidation, DomainError }
import org.biobank.domain.user.{ UserId, UserHelper }

import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.json._
import play.api.cache._
import play.api.Play.current
import com.typesafe.plugin.use

import scalaz._
import scalaz.Scalaz._

/**
 * Security actions that should be used by all controllers that need to protect their actions.
 * Can be composed to fine-tune access control.
 */
trait Security { self: Controller =>

  private def usersService = use[BbwebPlugin].usersService

  val AuthTokenCookieKey = "XSRF-TOKEN"
  val AuthTokenHeader = "X-XSRF-TOKEN"
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
     request.cookies.get(AuthTokenCookieKey) match {
       case None => DomainError("Invalid XSRF Token cookie").failNel
       case Some(xsrfTokenCookie) =>
         request.headers.get(AuthTokenHeader).orElse(request.getQueryString(AuthTokenUrlKey)) match {
           case None => DomainError("No token").failNel
           case Some(token) =>
             if (xsrfTokenCookie.value.equals(token)) {
               Cache.getAs[UserId](token) match {
                 case None => DomainError("invalid token").failNel
                 case Some(userId) => {
                   for {
                     user       <- usersService.getByEmail(userId.id)
                     activeUser <- UserHelper.isUserActive(user)
                     auth       <- AuthenticationInfo(token, userId).successNel
                   } yield auth
                 }
               }
             } else {
               DomainError("Token mismatch").failNel
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
      err => Unauthorized(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
      authInfo => f(authInfo.token)(authInfo.userId)(request))
  }

  /**
   * Ensures that the request has the proper authorization.
   *
   */
  def AuthActionAsync[A](p: BodyParser[A] = parse.anyContent)(
    f: String => UserId => Request[A] => Future[Result]) = Action.async(p) { implicit request =>
    validateToken(request).fold(
      err => Future.successful(Unauthorized(Json.obj(
        "status" ->"error",
        "message" -> err.list.mkString(", ")))),
      authInfo => f(authInfo.token)(authInfo.userId)(request))
  }

}
