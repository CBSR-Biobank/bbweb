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

  def index = SecuredAction { implicit request =>
    // get list of studies the user has access to
    val studies = studyService.getAll
    Ok(views.html.study.index(studies, request.user))
  }

  /**
   * Add a study.
   */
  def add = SecuredAction { implicit request =>
    //Ok(views.html.study.add())
    Ok
  }

}
