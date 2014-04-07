package fixture

import infrastructure._
import service._
import domain._
import domain.study._

import play.api.Mode
import play.api.Mode._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import akka.actor._
import akka.persistence.View
import akka.util.Timeout
import org.specs2.mutable._
import org.specs2.time._

import scalaz._
import Scalaz._

/**
 * Used to test the study service.
 */
trait StudyCommandFixture
  extends Specification
  with NoTimeConversions
  with Tags
  with TestComponentImpl {

  private val studyProcessor = system.actorOf(Props[StudyProcessorImpl], "studyproc")

  override val studyService = new StudyServiceImpl(studyProcessor)
}