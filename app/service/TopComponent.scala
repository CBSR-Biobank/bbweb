package service

import domain._
import domain.study._

import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps
import org.eligosource.eventsourced.core._
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps
import com.mongodb.casbah.Imports._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.Props
import java.util.concurrent.TimeUnit

import play.api.Logger

/**
 * Uses the Scala Cake Pattern to configure the application.
 */
trait TopComponent extends ServiceComponent {

  def startEventsourced: Unit

}

/**
 * Web Application Eventsourced configuration
 *
 * ==Eventsourced config==
 *
 * Two processors and one channel. One processor handles commands and the other events. The command
 * processor, {@link commandProcessor}, is a multicast processor that forwards messages to aggregate
 * roots: {@link studyProcessor} and {@link userProcessor}.
 *
 * Aggregate root processors generate events to the {@link eventBusChannel}. All events received
 * on the event bus are confirmed by default.
 *
 * ==Recovery==
 *
 * By default, recovery is only done on the Journal associated withe the command processor to only
 * rebuild the ''In Memory Image''. To rebuild the ''query database'', the application can be run
 * with the `bbweb.query.db.load` system property set to `true` and the event processor will also
 * be recovered.
 *
 * @author Nelson Loyola
 */
trait TopComponentImpl extends TopComponent with ServiceComponentImpl {

  private implicit val system = ActorSystem("bbweb")
  private implicit val timeout = Timeout(5 seconds)

  private val MongoDbName = "biobank-web"
  private val MongoCollName = "bbweb"

  private val mongoClient = MongoClient()
  private val mongoDB = mongoClient(MongoDbName)
  private val mongoColl = mongoClient(MongoDbName)(MongoCollName)

  private val journal = MongodbCasbahJournalProps(mongoClient, MongoDbName, MongoCollName).createJournal
  private val extension = EventsourcingExtension(system, journal)

  // the command bus
  private val studyProcessor = system.actorOf(Props(new StudyProcessorImpl with Emitter))
  private val userProcessor = system.actorOf(Props(new UserProcessorImpl with Emitter))
  private val commandBusProcessors = List(studyProcessor, userProcessor)
  private val commandProcessor = extension.processorOf(Props(multicast(1, commandBusProcessors)))

  // the event bus
  private val studyEventProcessor = system.actorOf(Props(new StudyEventProcessorImpl with Receiver))
  private val eventBusProcessors = List(studyEventProcessor)
  private val eventProcessor = extension.processorOf(ProcessorProps(2, pid => new Multicast(
    eventBusProcessors, identity) with Confirm with Eventsourced { val id = pid }))
  private val eventBusChannel = extension.channelOf(ReliableChannelProps(2, eventProcessor).withName("eventBus"))

  override val studyService = new StudyServiceImpl(commandProcessor)
  override val userService = new UserServiceImpl(commandProcessor)

  /**
   * Starts the recovery stage for the Eventsourced framework.
   *
   * The
   */
  def startEventsourced: Unit = {
    // for debug only - password is "administrator"
    userRepository.add(User.add(UserId("admin@admin.com"), "admin", "admin@admin.com",
      "$2a$10$ErWon4hGrcvVRPa02YfaoOyqOCxvAfrrObubP7ZycS3eW/jgzOqQS",
      "bcrypt", None, None) | null)

    val httpPort = Option(System.getProperty("bbweb.query.db.load"))

    if (httpPort.exists(_ == "true")) {
      // recover the command bus processor and the event bus
      extension.recover(Seq(ReplayParams(1), ReplayParams(2)))
    } else {
      // only recover the command bus
      extension.recover(Seq(ReplayParams(1)))
    }

    // wait for processor 1 to complete processing of replayed event messages
    // (ensures that recovery of externally visible state maintained by
    //  memory image is completed when awaitProcessing returns)
    extension.awaitProcessing(Set(1))
  }
}
