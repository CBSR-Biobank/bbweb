package controllers

import scala.concurrent._
import scala.concurrent.duration._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import service._
import service.commands._
import domain._
import domain.study._
import views._

import scalaz._
import Scalaz._

object StudyController extends Controller with Secured {

  lazy val userService = Global.services.userService
  lazy val studyService = Global.services.studyService

  /**
   * Display the dashboard.
   */
  def index = IsAuthenticated { username =>
    _ =>
      userService.getByEmail(username) match {
        case Some(user) => Ok(html.dashboard(user))
        case _ => Forbidden
      }
  }

  /**
   * Add a study.
   */
  def add = IsAuthenticated { username =>
    implicit request =>
      val form = Form(tuple("name" -> nonEmptyText, "description" -> nonEmptyText)).bindFromRequest.fold(
        errors => BadRequest,
        values =>
          Await.result(studyService.addStudy(new AddStudyCmd(values._1, values._2)), 5.seconds) match {
            case Success(study) => Ok(views.html.study.item(study))
            case _ =>
          })
      BadRequest
  }

}
