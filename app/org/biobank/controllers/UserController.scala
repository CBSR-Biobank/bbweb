package org.biobank.controllers

import org.biobank.infrastructure.command.UserCommands._
import org.biobank.service.json.User._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.{ Logger, Play }
//import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._

import scalaz._
import Scalaz._

object UserController extends BbwebController {

  private def userService = Play.current.plugin[BbwebPlugin].map(_.userService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = AuthAction(parse.empty) { token => userId => implicit request =>
    Ok(Json.toJson(userService.getAll.toList))
  }

  /** Retrieves the user for the given id as JSON */
  def user(id: String) = AuthAction(parse.empty) { token => userId => implicit request =>
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

  def addUser() = CommandAction { cmd: RegisterUserCmd => userId =>
    Logger.info(s"addUser: cmd: $cmd")
    val future = userService.register(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"user added: ${event.id}.") ))
      )
    }
  }

  def activateUser(id: String) =  CommandAction { cmd: ActivateUserCmd => userId =>
    val future = userService.activate(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"user activated: ${event.id}.") ))
      )
    }
  }

  def updateUser(id: String) =  CommandAction { cmd: UpdateUserCmd => userId =>
    val future = userService.update(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"user updated: ${event.id}.") ))
      )
    }
  }

  def lockUser(id: String) =  CommandAction { cmd: LockUserCmd => userId =>
    val future = userService.lock(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"user locked: ${event.id}.") ))
      )
    }
  }

  def unlockUser(id: String) =  CommandAction { cmd: UnlockUserCmd => userId =>
    Logger.info(s"unlockUser")
    val future = userService.unlock(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"user unlocked: ${event.id}.") ))
      )
    }
  }

  def removeUser(id: String) =  CommandAction { cmd: RemoveUserCmd => userId =>
    val future = userService.remove(cmd)
    future.map { validation =>
      validation.fold(
        err   => BadRequest(Json.obj("status" ->"KO", "message" -> err.list.mkString(", "))),
        event => Ok(Json.obj("status" ->"OK", "message" -> (s"user removed: ${event.id}.") ))
      )
    }
  }

}
