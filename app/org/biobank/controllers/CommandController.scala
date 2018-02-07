package org.biobank.controllers

import org.biobank.infrastructure.command.Commands._
import org.biobank.service.ServiceValidation
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

object CommandController {

  final case class UpdateEntityJson(sessionUserId:   String,
                                    id:              String,
                                    expectedVersion: Long,
                                    property:        String,
                                    newValue:        JsValue)

  implicit val updateEntityJsonReads: Reads[UpdateEntityJson] =
    Json.reads[UpdateEntityJson]
}

/**
 *  Uses [[http://labs.omniti.com/labs/jsend JSend]] format for JSon replies.
 */
@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
abstract class CommandController(controllerComponents: ControllerComponents)
    extends AbstractController(controllerComponents) {

  implicit protected val ec: ExecutionContext

  protected val action: BbwebAction

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  protected def commandAction[T <: Command](jsonExtra: JsValue)
                             (block: T => Future[Result])
                             (implicit reads: Reads[T]): Action[JsValue] =
    action.async(parse.json) { request =>
      var jsonCmd = request.body.as[JsObject] ++ Json.obj("sessionUserId" -> request.authInfo.userId.id)
      if (jsonExtra != JsNull) {
        jsonCmd = jsonCmd ++ jsonExtra.as[JsObject]
      }
      processJsonCommand(jsonCmd)(block)
    }

  /**
   * This is for actions that do not require the user to be logged in.
   */
  protected def anonymousCommandAction[T <: Command](block: T => Future[Result])
                                      (implicit reads: Reads[T]): Action[JsValue] =
    Action.async(parse.json) { request =>
      processJsonCommand(request.body.as[JsObject])(block)
    }

  protected def processJsonCommand[T <: Command](jsonCmd: JsValue)
                                (block: T => Future[Result])
                                (implicit reads: Reads[T]): Future[Result] = {
    if (log.isTraceEnabled) {
      log.trace(s"commandAction: json: $jsonCmd")
    }

    jsonCmd.validate[T].fold(
      errors => {
        val errString = errors.map(e => s"field: ${e._1}, errors: ${e._2}").toList.mkString(", ")
        Future.successful(BadRequest(Json.obj("status" -> "error", "message" -> errString)))
      },
      cmd => block(cmd)
    )
  }

  protected def errorReplyJson(message: String): JsValue =
    Json.obj("status" -> "error", "message" -> message)

  protected def BadRequest(message:String): Result = Results.BadRequest(errorReplyJson(message))

  protected def Forbidden(message: String): Result = Results.Forbidden(errorReplyJson(message))

  protected def NotFound(message: String): Result = Results.NotFound(errorReplyJson(message))

  protected def InternalServerError(message: String): Result =
    Results.InternalServerError(errorReplyJson(message))

  protected def Ok[T](data: T)(implicit writes: Writes[T]): Result =
    Results.Ok(Json.obj("status" ->"success", "data" -> Json.toJson[T](data)))

  protected def validationReply[T](validation: ServiceValidation[T])
                               (implicit writes: Writes[T]): Result = {
    validation.fold(
      err => {
        val errMsgs = err.list.toList.mkString(", ")
        if (errMsgs.contains("Unauthorized")) {
          Unauthorized
        } else if (("NotFound".r.findAllIn(errMsgs).length > 0)
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
