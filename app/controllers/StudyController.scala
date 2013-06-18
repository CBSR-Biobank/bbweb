package controllers

import service._
import infrastructure.commands._
import domain._
import domain.study._
import views._

import scala.concurrent._
import scala.concurrent.duration._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import securesocial.core.{ Identity, Authorization }

import scalaz._
import Scalaz._

object StudyController extends Controller with securesocial.core.SecureSocial {

  lazy val userService = Global.services.userService
  lazy val studyService = Global.services.studyService

  /**
   * Display the dashboard.
   */
  def index = UserAwareAction { implicit request =>
    request.user match {
      case Some(user) =>
        user.fullName
        userService.getByEmail(user.email.getOrElse("")) match {
          case Some(email) =>
            Ok(html.index(user))
          case _ => Forbidden
        }
      case _ => Forbidden
    }
  }

  /**
   * Add a study.
   */
  def add = UserAwareAction { implicit request =>
    request.user match {
      case Some(user) =>
        Ok(views.html.index(user))
      case _ => Forbidden
    }
  }

}
