package org.biobank.controllers

import org.biobank.domain.UserId

import scala.concurrent.Future
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.cache._

/**
 * Security actions that should be used by all controllers that need to protect their actions.
 * Can be composed to fine-tune access control.
 */
trait Security { self: Controller =>

  implicit val app: play.api.Application = play.api.Play.current

  val AuthTokenHeader = "X-XSRF-TOKEN"
  val AuthTokenCookieKey = "XSRF-TOKEN"
  val AuthTokenUrlKey = "auth"

  private def getToken[A](request: Request[A]) = {
    val maybeToken = request.headers.get(AuthTokenHeader).orElse(request.getQueryString(AuthTokenUrlKey))
    maybeToken flatMap { token =>
      Some(token)
    }
  }

  /** Checks that a token is either in the header or in the query string */
  def AuthAction[A](p: BodyParser[A] = parse.anyContent)(
    f: String => UserId => Request[A] => Result): Action[A] = {
    Action(p) { implicit request =>
      getToken(request).flatMap { token =>
        Cache.getAs[UserId](token) map { userId =>
          f(token)(userId)(request)
        }
      } getOrElse Unauthorized(Json.obj("message" -> "No Token"))
    }
  }

  /** Checks that a token is either in the header or in the query string */
  def AuthActionAsync[A](p: BodyParser[A] = parse.anyContent)(
    f: String => UserId => Request[A] => Future[Result]) = {
    Action.async(p) { request =>
      getToken(request).flatMap { token =>
        Cache.getAs[UserId](token) map { userId =>
          f(token)(userId)(request)
        }
      } getOrElse Future.successful(Unauthorized(Json.obj("message" -> "No Token")))
    }
  }
}
