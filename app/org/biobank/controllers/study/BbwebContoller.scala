package org.biobank.controllers

import org.biobank.infrastructure.command.Commands._

import scala.concurrent.Future
import play.Logger
import play.api.mvc._
import play.api.libs.json._
import play.api.mvc.Results._

trait BbwebController {

  protected def doCommand[T <: Command](
    func: T => Future[Result])(
    implicit reads: Reads[T]) = Action.async(BodyParsers.parse.json) { request =>
    Logger.info(s"doCommand: request body: ${request.body}")
    val cmdResult = request.body.validate[T]
    cmdResult.fold(
      errors => {
        Future.successful(
          BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors))))
      },
      cmd => {
        Logger.info(s"doCommand: $cmd")
        func(cmd)
      }
    )
  }

}
