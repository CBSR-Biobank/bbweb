package org.biobank.controllers

import org.biobank.domain.UserId
import org.biobank.infrastructure.command.Commands._

import scala.concurrent.Future
import play.Logger
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._

trait BbwebController extends Controller with Security {

  def CommandAction[A, T <: Command](func: T => UserId => Future[Result])(implicit reads: Reads[T]) = {
    HasTokenFuture(parse.json) { token => userId => implicit request =>
      val cmdResult = request.body.validate[T]
      cmdResult.fold(
        errors => {
          Future.successful(
            BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors))))
        },
        cmd => {
          Logger.info(s"CommandAction: $cmd")
          func(cmd)(userId)
        }
      )
    }
  }
}
