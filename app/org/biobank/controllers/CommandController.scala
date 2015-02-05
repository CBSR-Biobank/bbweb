package org.biobank.controllers

import org.biobank.service.users.UsersService
import org.biobank.domain.{ DomainValidation }
import org.biobank.domain.user.UserId
import org.biobank.service.AuthToken
import org.biobank.infrastructure.command.Commands._

import scala.concurrent.Future
import org.slf4j.LoggerFactory
import play.Logger
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._
import play.mvc.Http
import play.api.libs.concurrent.Execution.Implicits._

trait CommandController extends Controller with Security {

  implicit val authToken: AuthToken

  implicit val usersService: UsersService

  def commandAction[A, T <: Command]
    (func: T => UserId => Future[Result])
    (implicit reads: Reads[T]) = {
    AuthActionAsync(parse.json) { (token, userId, request) =>
        val cmdResult = request.body.validate[T]
        cmdResult.fold(
          errors => {
            Future.successful(
              BadRequest(Json.obj("status" ->"error", "message" -> JsError.toFlatJson(errors))))
          },
          cmd => {
            Logger.debug(s"commandAction: $cmd")
            func(cmd)(userId)
          }
        )
    }
  }

  def commandAction[A, T <: Command](numFields: Integer)
    (func: T => UserId => Future[Result])
    (implicit reads: Reads[T]) = {
    AuthActionAsync(parse.json) { (token, userId, request) =>
      var jsonObj = request.body.as[JsObject]
      Logger.debug(s"commandAction: $jsonObj")
      if (jsonObj.keys.size == numFields) {
        val cmdResult = request.body.validate[T]
        cmdResult.fold(
          errors => {
            Future.successful(
              BadRequest(Json.obj("status" ->"error", "message" -> JsError.toFlatJson(errors))))
          },
          cmd => {
            Logger.debug(s"commandAction: $cmd")
            func(cmd)(userId)
          }
        )
      } else {
        Future.successful(
          BadRequest(Json.obj(
            "status" ->"error",
            "message" -> s"Invalid JSON object - missing attributes: expected $numFields, got ${jsonObj.keys.size}")))
      }
    }
  }

}

/**
  *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
  */
trait JsonController extends Controller {

  import scala.language.reflectiveCalls

  val Log = LoggerFactory.getLogger(this.getClass)

  def errorReplyJson(message: String) = Json.obj("status" -> "error", "message" -> message)

  override val BadRequest = new Status(Http.Status.BAD_REQUEST) {
    def apply(message: String): Result = Results.BadRequest(errorReplyJson(message))
  }

  override val Forbidden = new Status(Http.Status.FORBIDDEN) {
    def apply(message: String): Result = Results.Forbidden(errorReplyJson(message))
  }

  override val NotFound = new Status(Http.Status.NOT_FOUND) {
    def apply(message: String): Result = Results.NotFound(errorReplyJson(message))
  }

  override val Ok = new Status(Http.Status.OK) {

    def apply[T](obj: T)(implicit writes: Writes[T]): Result =
      Results.Ok(Json.obj("status" ->"success", "data" -> Json.toJson(obj)))

  }

  protected def domainValidationReply[T]
    (validation: DomainValidation[T])
    (implicit writes: Writes[T])
      : Result = {
    validation.fold(
      err => {
        val errMsgs = err.list.mkString(", ")
        if ("does not exist".r.findAllIn(errMsgs).length > 0) {
          NotFound(errMsgs)
        } else if (errMsgs.contains("already exists")) {
          Forbidden(errMsgs)
        } else {
          BadRequest(errMsgs)
        }
      },
      event => {
        Log.debug(s"domainValidationReply: $event")
        Ok(event)
      }
    )
  }

  protected def domainValidationReply[T]
    (future: Future[DomainValidation[T]])
    (implicit writes: Writes[T])
      : Future[Result] = {
    future.map { validation => domainValidationReply(validation)
    }
  }

}
