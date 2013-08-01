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

trait TopComponent extends ServiceComponent {

  def start: Unit

}

trait TopComponentImpl extends TopComponent with ServiceComponentImpl {

  implicit val system = ActorSystem("eventsourced")
  implicit val timeout = Timeout(5 seconds)

  val MongoDbName = "biobank-web"
  val MongoCollName = "bbweb"

  val mongoClient = MongoClient()
  val mongoDB = mongoClient(MongoDbName)
  val mongoColl = mongoClient(MongoDbName)(MongoCollName)

  val journalProps = MongodbCasbahJournalProps(mongoClient, MongoDbName, MongoCollName)

  val journal = Journal(journalProps)
  val extension = EventsourcingExtension(system, journal)

  val studyProcessor = system.actorOf(Props(new StudyProcessorImpl with Emitter))
  val userProcessor = system.actorOf(Props(new UserProcessorImpl with Emitter))

  val multicastTargets = List(studyProcessor, userProcessor)

  // this is the command bus
  val commandBus = extension.processorOf(
    ProcessorProps(1, pid => new Multicast(multicastTargets, identity) with Confirm with Eventsourced { val id = pid }))

  override val studyService = new StudyServiceImpl(commandBus)
  override val userService = new UserServiceImpl(commandBus)

  def start: Unit = {
    // for debug only - password is "administrator"
    userRepository.add(User.add(UserId("admin@admin.com"), "admin", "admin@admin.com",
      "$2a$10$ErWon4hGrcvVRPa02YfaoOyqOCxvAfrrObubP7ZycS3eW/jgzOqQS",
      "bcrypt", None, None) | null)

    extension.recover()
    // wait for processor 1 to complete processing of replayed event messages
    // (ensures that recovery of externally visible state maintained by
    //  memory image is completed when awaitProcessing returns)
    extension.awaitProcessing(Set(1))
  }
}
