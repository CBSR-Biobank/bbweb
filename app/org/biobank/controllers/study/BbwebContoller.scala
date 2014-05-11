package org.biobank.controllers

import org.biobank.infrastructure.command.Commands._

import scala.concurrent.Future
import play.Logger
import play.api.mvc._
import play.api.libs.json._
import securesocial.core.SecureSocial
import securesocial.core.SecuredRequest

trait BbwebController extends SecureSocial {

  protected def doCommand[T <: Command](
    func: T => Future[Result])(
    implicit reads: Reads[T]) = Action.async(BodyParsers.parse.json) { request =>
    val cmdResult = request.body.validate(reads)
    cmdResult.fold(
      errors => {
        Future.successful(
          BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors))))
      },
      cmd => {
        Logger.info(s"$cmd")
        func(cmd)
      }
    )
  }

}
