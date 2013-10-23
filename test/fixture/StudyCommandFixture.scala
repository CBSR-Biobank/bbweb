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
import akka.util.Timeout
import org.eligosource.eventsourced.core._
import org.specs2.mutable._
import org.specs2.time._

import scalaz._
import Scalaz._

class DummyEventProcessor extends Actor with ActorLogging {

  def receive = {
    case msg =>
      log.debug("received event %s" format msg)
  }

}

/**
 * Used to test the study service.
 */
trait StudyCommandFixture
  extends Specification
  with NoTimeConversions
  with Tags
  with TestComponentImpl {

  val context = startEventsourced(Mode.Test)

  override protected def getCommandProcessors =
    List(system.actorOf(Props(new StudyProcessorImpl with Emitter), "study"))

  override protected def getEventProcessors =
    List(system.actorOf(Props(new DummyEventProcessor with Receiver), "dummyevent"))

}