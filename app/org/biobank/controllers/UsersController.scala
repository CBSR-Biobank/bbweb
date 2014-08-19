package org.biobank.controllers

import org.biobank.infrastructure.command.UserCommands._
import org.biobank.service.json.User._
import org.biobank.service.json.Events._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.{ Logger, Play }
//import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._

import scalaz._
import Scalaz._

object UsersController extends BbwebController {

  private def usersService = Play.current.plugin[BbwebPlugin].map(_.usersService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Ok(Json.toJson(usersService.getAll.toList))
  }

  /** Retrieves the user associated with the token, if it is valid.
    */
  def authenticateUser() = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    usersService.getByEmail(userId.id).fold(
      err  => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
      user => Ok(Json.toJson(user))
    )
  }

  /** Retrieves the user for the given id as JSON */
  def user(id: String) = AuthAction(parse.empty) { token => implicit userId => implicit request =>
    Logger.info(s"user: id: $id")
    usersService.getByEmail(id).fold(
      err => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
      user => Ok(Json.toJson(user))
    )
  }

  def addUser() = Action.async(parse.json) { implicit request =>
    request.body.validate[RegisterUserCmd].fold(
      errors => {
        Future.successful(
          BadRequest(Json.obj("status" ->"error", "message" -> JsError.toFlatJson(errors))))
      },
      cmd => {
        Logger.info(s"addUser: cmd: $cmd")
        val future = usersService.register(cmd)
        future.map { validation =>
          validation.fold(
            err   => {
              val errs = err.list.mkString(", ")
              if (errs.contains("")) {
                Forbidden(Json.obj("status" ->"error", "message" -> "already registered"))
              } else {
                BadRequest(Json.obj("status" ->"error", "message" -> errs))
              }
            },
            event => Ok(eventToJsonReply(event))
          )
        }
      }
    )
  }

  def activateUser(id: String) =  CommandAction { cmd: ActivateUserCmd => implicit userId =>
    val future = usersService.activate(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def updateUser(id: String) =  CommandAction { cmd: UpdateUserCmd => implicit userId =>
    val future = usersService.update(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def lockUser(id: String) =  CommandAction { cmd: LockUserCmd => implicit userId =>
    val future = usersService.lock(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def unlockUser(id: String) =  CommandAction { cmd: UnlockUserCmd => implicit userId =>
    Logger.info(s"unlockUser")
    val future = usersService.unlock(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def removeUser(id: String, ver: Long) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = RemoveUserCmd(id, ver)
    val future = usersService.remove(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

  def resetPassword(id: String) = AuthActionAsync(parse.empty) { token => implicit userId => implicit request =>
    val cmd = ResetUserPasswordCmd(id)
    val future = usersService.resetPassword(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"error", "message" -> err.list.mkString(", "))),
        event => Ok(eventToJsonReply(event))
      )
    }
  }

}
