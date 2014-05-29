package org.biobank.controllers

import org.biobank.service.json.User._


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._

import scalaz._
import Scalaz._

object UserController extends Controller  {

  private def userService = Play.current.plugin[BbwebPlugin].map(_.userService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = Action(parse.empty) { request =>
    Ok(Json.toJson(userService.getAll.toList))
  }

  /** Retrieves the user for the given id as JSON */
  def user(id: String) = Action(parse.empty) { request =>
    Logger.info(s"user: $id")
    userService.getByEmail(id).fold(
      err => {
        BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", ")))
      },
      user => {
        Ok(Json.toJson(user))
      }
    )
  }

  /** Creates a user from the given JSON */
  def createUser() = Action(parse.json) { request =>
    // TODO Implement User creation, typically via request.body.validate[User]
    Ok
  }

  /** Updates the user for the given id from the JSON body */
  def updateUser(id: String) = Action(parse.json) { request =>
    // TODO Implement User creation, typically via request.body.validate[User]
    Ok
  }

  /** Deletes a user for the given id */
  def removeUser(id: String) = Action(parse.empty) { request =>
    // TODO Implement User creation, typically via request.body.validate[User]
    Ok
  }

}
