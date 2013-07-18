package fixture

import infrastructure._
import service._
import domain._
import domain.study._

import scala.util.{ Try, Success, Failure }
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.stm.Ref
import org.eligosource.eventsourced.core._
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps
import com.mongodb.casbah.Imports._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import akka.testkit._
import java.util.concurrent.TimeUnit
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import org.slf4j.LoggerFactory

/* A tiny class that can be used as a Specs2 'context'. */
abstract class AkkaTestkitSupport extends TestKit(ActorSystem())
  with After
  with ImplicitSender {
  // make sure we shut down the actor system after all tests have run
  def after = {
    system.shutdown()
    //system.awaitTermination(timeout.duration)
  }
}

abstract class AppFixture extends Specification with NoTimeConversions {

  private val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout = Timeout(5 seconds)
  implicit val system = ActorSystem("test")

  val mongoDbName = "biobank-test"
  val mongoCollName = "bbweb"

  val mongoClient = MongoClient()
  val mongoDB = mongoClient(mongoDbName)
  val mongoColl = mongoClient(mongoDbName)(mongoCollName)

  val nameGenerator: NameGenerator

  implicit val adminUserId = new UserId("admin@admin.com")

  def journalProps: JournalProps =
    MongodbCasbahJournalProps(mongoClient, mongoDbName, mongoCollName)

  lazy val journal = Journal(journalProps)
  lazy val extension = EventsourcingExtension(system, journal)

  def boot {
    // delete the journal contents
    mongoColl.remove(MongoDBObject.empty)

    val userProcessor = extension.processorOf(Props(
      new UserProcessor() with Emitter with Eventsourced { val id = 2 }))

    // for debug only - password is "administrator"
    UserRepository.add(User.add(adminUserId, "admin", "admin@admin.com",
      "$2a$10$ErWon4hGrcvVRPa02YfaoOyqOCxvAfrrObubP7ZycS3eW/jgzOqQS",
      "bcrypt", None, None) | null)

    extension.recover()
    // wait for processor 1 to complete processing of replayed event messages
    // (ensures that recovery of externally visible state maintained by
    //  studiesRef is completed when awaitProcessing returns)
    extension.awaitProcessing(Set(1))

    log.debug("system initialized")
  }

  def await[T](f: Future[DomainValidation[T]]): DomainValidation[T] = {
    // use blocking for now so that tests can be run in parallel
    val r = Await.result(f, timeout.duration)
    r
  }

  step {
    boot
  }
}

