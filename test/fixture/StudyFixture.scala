package fixture

import infrastructure._
import service._
import domain._
import domain.study._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import akka.actor._
import akka.util.Timeout
import org.eligosource.eventsourced.core._
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps
import scalaz._
import Scalaz._

trait StudyFixture extends TestComponentImpl {

  val studyProcessor = system.actorOf(Props(new StudyProcessorImpl with Emitter))
  val multicastTargets = List(studyProcessor)

  val commandBus = extension.processorOf(
    ProcessorProps(1, pid => new Multicast(multicastTargets, identity) with Confirm with Eventsourced { val id = pid }))

  override val studyService = new StudyServiceImpl(commandBus)
  override val userService = null

}