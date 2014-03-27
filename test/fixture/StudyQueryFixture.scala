package fixture

import infrastructure._
import service._
import service.study._
import domain._
import domain.study._
import query.model._

import play.api.Mode
import play.api.Mode._
import akka.actor._
import scala.concurrent._
import scala.slick.session.Database
import org.specs2.mutable._
import org.specs2.time._
import scala.slick.session.Database
import Database.threadLocalSession
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }

import scalaz._
import Scalaz._

class DummyCommandProcessor extends Processor {

  def receive = {
    case msg =>
      log.debug("received event %s" format msg)
  }

}

/**
 * Used to test the study query model.
 */
trait StudyQueryFixture
  extends Specification
  with NoTimeConversions
  with Tags
  with TestComponentImpl {

  protected val context = startEventsourced(Mode.Test)

  protected val DB = Database.forURL("jdbc:h2:mem:bbweb-test;MODE=MYSQL", driver = "org.h2.Driver")

  override protected def getEventProcessors = List(studyEventProcessor)

}