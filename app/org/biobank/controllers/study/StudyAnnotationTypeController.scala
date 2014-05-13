package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.{ ServiceComponent, ServiceComponentImpl }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.study._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.templates._
import play.api.i18n.Messages
import play.Logger
import akka.util.Timeout

import scalaz._
import Scalaz._

trait StudyAnnotationTypeController[A <: StudyAnnotationType] extends Controller  {

}
