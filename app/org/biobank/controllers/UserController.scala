package org.biobank.controllers

import org.biobank.service.ServiceComponent

import play.api._
import play.api.mvc._
import play.api.libs.json._

import scalaz._
import Scalaz._

object UserController extends Controller  {

  private def userService = Play.current.plugin[BbwebPlugin].map(_.userService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  /** Retrieves the user for the given id as JSON */
  def user(id: String) = Action(parse.empty) { request =>
    // TODO Find user and convert to JSON
    Logger.info(s"user: $id")
    Ok(Json.obj("firstName" -> "John", "lastName" -> "Smith", "age" -> 42))
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
  def deleteUser(id: String) = Action(parse.empty) { request =>
    // TODO Implement User creation, typically via request.body.validate[User]
    Ok
  }

}
