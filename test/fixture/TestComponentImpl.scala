package fixture

import service._
import domain._

import play.api.Mode
import play.api.Mode._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import org.eligosource.eventsourced.core._
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps
import com.mongodb.casbah.Imports._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.util.Timeout
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import org.slf4j.LoggerFactory
import akka.actor._

import scalaz._
import scalaz.Scalaz._

trait TestComponentImpl extends TopComponent with ServiceComponentImpl {

  private val log = LoggerFactory.getLogger(this.getClass)
  protected val nameGenerator: NameGenerator

  protected implicit val system = ActorSystem("bbweb-test")
  private implicit val timeout = Timeout(5 seconds)

  protected implicit val adminUserId = new UserId("admin@admin.com")

  private val mongoDbName = "biobank-test"
  private val mongoCollName = "bbweb"

  private val mongoClient = MongoClient()
  private val mongoDB = mongoClient(mongoDbName)
  private val mongoColl = mongoClient(mongoDbName)(mongoCollName)

  private val journal = MongodbCasbahJournalProps(mongoClient, mongoDbName, mongoCollName).createJournal
  private val extension = EventsourcingExtension(system, journal)

  // the command bus
  private val commandBusProcessors = getCommandProcessors
  private val commandProcessor = extension.processorOf(
    Props(multicast(1, commandBusProcessors)))

  // the event bus
  private val eventBusProcessors = getEventProcessors
  private val eventProcessor = extension.processorOf(ProcessorProps(3, pid => new Multicast(
    eventBusProcessors, identity) with Confirm with Eventsourced { val id = pid }))
  private val eventBusChannel = extension.channelOf(
    ReliableChannelProps(Configuration.EventBusChannelId, eventProcessor).withName("eventBus"))

  override val studyService = new StudyServiceImpl(commandProcessor)
  override val userService = null

  /**
   * Returns the list of command processors to be used in this test fixture.
   */
  protected def getCommandProcessors: List[ActorRef]

  /**
   * Returns the list of event processors to be used in this test fixture.
   */
  protected def getEventProcessors: List[ActorRef]

  def startEventsourced(appMode: Mode) = {
    // delete the journal contents
    mongoColl.remove(MongoDBObject.empty)

    extension.recover(Seq(ReplayParams(1)))
    //extension.recover

    // wait for processor 1 to complete processing of replayed event messages
    // (ensures that recovery of externally visible state maintained by
    //  studiesRef is completed when awaitProcessing returns)
    extension.awaitProcessing(Set(1))
    log.debug("system initialized")
  }

  def await[T](f: Future[DomainValidation[T]]): DomainValidation[T] = {
    // use blocking for now so that tests can be run in parallel
    Await.result(f, timeout.duration)
  }
}
