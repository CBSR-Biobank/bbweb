package org.biobank.controllers

import org.biobank.infrastructure.event.Events._
import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.Commands._

import scala.concurrent.Future
import play.Logger
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import play.mvc.Http

trait CommandController extends Controller with Security {

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

/**
  *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
  */
trait JsonController extends Controller {

  import scala.language.reflectiveCalls

  def errorReplyJson(message: String) = Json.obj("status" -> "error", "message" -> message)

  override val BadRequest = new Status(Http.Status.BAD_REQUEST) {
    def apply(message: String): Result = Results.BadRequest(errorReplyJson(message))
  }

  override val NotFound = new Status(Http.Status.NOT_FOUND) {
    def apply(message: String): Result = Results.NotFound(errorReplyJson(message))
  }

  override val Ok = new Status(Http.Status.OK) {

    def apply[T](obj: T)(implicit writes: Writes[T]): Result =
      Results.Ok(Json.obj(
      "status" ->"success",
      "data" -> Json.toJson(obj)))

  }

}

