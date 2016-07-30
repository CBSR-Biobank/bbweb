package org.biobank.controllers

import org.biobank.service.users.UsersService
import org.biobank.service.{AuthToken, ServiceValidation}
import org.biobank.infrastructure.command.Commands._

import scala.concurrent.Future
import org.slf4j.LoggerFactory
import play.Logger
import play.api.mvc._
import play.api.libs.json._
import play.mvc.Http
import play.api.libs.concurrent.Execution.Implicits._

@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
trait CommandController extends Controller with Security {

  implicit val authToken: AuthToken

  implicit val usersService: UsersService

  def commandAction[T <: Command](additionalJson: JsObject)(func: T => Future[Result])
                   (implicit reads: Reads[T]): Action[JsValue] = {
    AuthActionAsync(parse.json) { (token, userId, request) =>
      val jsonCmd = request.body.as[JsObject] ++ additionalJson ++ Json.obj("userId" -> userId.id)
      jsonCmd.validate[T].fold(
        errors => {
          Future.successful(
            BadRequest(Json.obj("status" ->"error", "message" -> JsError.toJson(errors))))
        },
        cmd => {
          Logger.debug(s"commandAction: $cmd")
          func(cmd)
        }
      )
    }
  }

  def commandAction[T <: Command](func: T => Future[Result])
                   (implicit reads: Reads[T]): Action[JsValue] = {
    commandAction(Json.obj()) { cmd: T => func(cmd) }
  }

  def commandAction[A, T <: Command](numFields: Integer)(func: T => Future[Result])
                   (implicit reads: Reads[T]) = {
    AuthActionAsync(parse.json) { (token, userId, request) =>
      val jsonObj = request.body.as[JsObject]
      Logger.debug(s"commandAction: $jsonObj")
      if (jsonObj.keys.size == numFields) {
        val jsonWithUserId = request.body.as[JsObject] ++ Json.obj("userId" -> userId.id)
        val cmdResult = jsonWithUserId.validate[T]
        cmdResult.fold(
          errors => {
            Future.successful(
              BadRequest(Json.obj("status" ->"error", "message" -> JsError.toJson(errors))))
          },
          cmd => {
            Logger.debug(s"commandAction: $cmd")
            func(cmd)
          }
        )
      } else {
        Future.successful(
          BadRequest(
            Json.obj(
              "status" ->"error",
              "message" -> s"Invalid JSON object - missing attributes: expected $numFields, got ${jsonObj.keys.size}")))
      }
    }
  }

}

/**
 *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
 */
@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
trait JsonController extends Controller {

  import scala.language.reflectiveCalls

  val Log = LoggerFactory.getLogger(this.getClass)

  def errorReplyJson(message: String) = Json.obj("status" -> "error", "message" -> message)

  override val BadRequest = new Status(Http.Status.BAD_REQUEST) {
      @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
      def apply(message: String): Result = Results.BadRequest(errorReplyJson(message))
    }

  override val Forbidden = new Status(Http.Status.FORBIDDEN) {
      @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
      def apply(message: String): Result = Results.Forbidden(errorReplyJson(message))
    }

  override val NotFound = new Status(Http.Status.NOT_FOUND) {
      @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
      def apply(message: String): Result = Results.NotFound(errorReplyJson(message))
    }

  // Wart Remover complains about Any and Nothing here
  override val Ok = new Status(Http.Status.OK) {
      @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
      def apply[T](content: T)(implicit writes: Writes[T]): Result =
        Results.Ok(Json.obj("status" ->"success", "data" -> Json.toJson[T](content)))
    }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  protected def validationReply[T](validation: ServiceValidation[T])
                                     (implicit writes: Writes[T]): Result = {
    validation.fold(
      err => {
        Log.trace("*** ERROR ***: " + err.list.toList.mkString(", "))
        val errMsgs = err.list.toList.mkString(", ")
        if (("IdNotFound".r.findAllIn(errMsgs).length > 0)
              || ("not found".r.findAllIn(errMsgs).length > 0)
              || ("does not exist".r.findAllIn(errMsgs).length > 0)
              || ("invalid.*id".r.findAllIn(errMsgs).length > 0)) {
          NotFound(errMsgs)
        } else if (errMsgs.contains("already exists")) {
          Forbidden(errMsgs)
        } else {
          BadRequest(errMsgs)
        }
      },
      reply => {
        Log.trace(s"validationReply: $reply")
        Ok(reply)
      }
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  protected def validationReply[T](future: Future[ServiceValidation[T]])
                                     (implicit writes: Writes[T]): Future[Result] =
    future.map { validation => validationReply(validation) }

}
