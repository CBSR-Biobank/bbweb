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
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps
import org.specs2.specification.BeforeExample
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions

import scalaz._
import Scalaz._

trait StudyFixture
  extends Specification
  with NoTimeConversions
  with Tags
  with TestComponentImpl {

  val context = startEventsourced(Mode.Test)

  override protected def getProcessors =
    List(system.actorOf(Props(new StudyProcessorImpl with Emitter), "study"))

}