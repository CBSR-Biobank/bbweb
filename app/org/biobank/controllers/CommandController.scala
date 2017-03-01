package org.biobank.controllers

import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.Commands._
import org.biobank.service.users.UsersService
import org.biobank.service.{AuthToken, ServiceValidation}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.Future

@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
trait CommandController extends Controller with Security {

  val action: BbwebAction

  implicit val authToken: AuthToken

  implicit val usersService: UsersService

  def commandFromJson(json: JsValue, jsonExtra: JsValue, userId: UserId): JsObject = {
    val result = Json.obj("userId" -> userId.id) ++ json.as[JsObject]
    if (jsonExtra == JsNull) result
    else result ++ jsonExtra.as[JsObject]
  }

  def commandAction[T <: Command](additionalJson: JsValue)(func: T => Result)
                   (implicit reads: Reads[T]): Action[JsValue] =
    action(parse.json) { implicit request =>
      commandFromJson(request.body, additionalJson, request.authInfo.userId).validate[T].fold(
        errors => BadRequest(Json.obj("status" ->"error", "message" -> JsError.toJson(errors))),
        cmd => func(cmd)
      )
    }

  def commandAction[T <: Command](func: T => Result)(implicit reads: Reads[T]): Action[JsValue] =
    commandAction(JsNull)(func)

  def commandActionAsync[T <: Command](additionalJson: JsValue)
                        (func: T => Future[Result])
                        (implicit reads: Reads[T]):
      Action[JsValue] =
    action.async(parse.json) { implicit request =>
      commandFromJson(request.body, additionalJson, request.authInfo.userId).validate[T].fold(
        errors => Future.successful(BadRequest(Json.obj("status" ->"error",
                                                        "message" -> JsError.toJson(errors)))),
        cmd => func(cmd)
      )
    }

  def commandActionAsync[T <: Command](func: T => Future[Result])(implicit reads: Reads[T]):
      Action[JsValue] =
    commandActionAsync(JsNull)(func)

}

/**
 *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
 */
@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
trait JsonController extends Controller {

  def errorReplyJson(message: String): JsValue = Json.obj("status" -> "error", "message" -> message)

  def BadRequest(message:String): Result = Results.BadRequest(errorReplyJson(message))

  def Forbidden(message: String): Result = Results.Forbidden(errorReplyJson(message))

  def NotFound(message: String): Result = Results.NotFound(errorReplyJson(message))

  def Ok[T](data: T)(implicit writes: Writes[T]): Result =
    Results.Ok(Json.obj("status" ->"success", "data" -> Json.toJson[T](data)))

  protected def validationReply[T](validation: ServiceValidation[T])
                               (implicit writes: Writes[T]): Result = {
    validation.fold(
      err => {
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
        Ok(reply)
      }
    )
  }

  protected def validationReply[T](future: Future[ServiceValidation[T]])
                               (implicit writes: Writes[T]): Future[Result] =
    future.map { validation => validationReply(validation) }

}
