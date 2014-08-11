package org.biobank.controllers

import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.Commands._

import scala.concurrent.Future
import play.Logger
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._

trait BbwebController extends Controller with Security {

  def CommandAction[A, T <: Command](
    func: T => UserId => Future[Result])(implicit reads: Reads[T]) = {
    AuthActionAsync(parse.json) { token => implicit userId => implicit request =>
        val cmdResult = request.body.validate[T]
        cmdResult.fold(
          errors => {
            Future.successful(
              BadRequest(Json.obj("status" ->"error", "message" -> JsError.toFlatJson(errors))))
          },
          cmd => {
            Logger.info(s"CommandAction: $cmd")
            func(cmd)(userId)
          }
        )
    }
  }

  def CommandAction[A, T <: Command](numFields: Integer)(
    func: T => UserId => Future[Result])(implicit reads: Reads[T]) = {
    AuthActionAsync(parse.json) { token => implicit userId => implicit request =>
      if (request.body.as[JsObject].keys.size == numFields) {
        val cmdResult = request.body.validate[T]
        cmdResult.fold(
          errors => {
            Future.successful(
              BadRequest(Json.obj("status" ->"error", "message" -> JsError.toFlatJson(errors))))
          },
          cmd => {
            Logger.info(s"CommandAction: $cmd")
            func(cmd)(userId)
          }
        )
      } else {
        Future.successful(
          BadRequest(Json.obj("status" ->"error", "message" -> "Invalid JSON object")))
      }
    }
  }

}
