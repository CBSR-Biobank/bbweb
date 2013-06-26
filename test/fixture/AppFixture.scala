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

  implicit val timeout = Timeout(5 seconds)
  implicit val system = ActorSystem("test")

  val MongoDbName = "biobank-test"
  val MongoCollName = "bbweb"

  val mongoClient = MongoClient()
  val mongoDB = mongoClient(MongoDbName)
  val mongoColl = mongoClient(MongoDbName)(MongoCollName)

  val userRepo = new UserReadWriteRepository(v => new UserId(v.email))

  // delete the journal contents
  mongoColl.remove(MongoDBObject.empty)

  def journalProps: JournalProps =
    MongodbCasbahJournalProps(mongoClient, MongoDbName, MongoCollName)

  val journal = Journal(journalProps)
  val extension = EventsourcingExtension(system, journal)

  def await[T](f: Future[DomainValidation[T]]): DomainValidation[T] = {
    // use blocking for now so that tests can be run in parallel
    Await.result(f, timeout.duration)
  }

  val userProcessor = extension.processorOf(Props(
    new UserProcessor(userRepo) with Emitter with Eventsourced { val id = 2 }))

  implicit val adminUserId = new UserId("admin@admin.com")

  // for debug only - password is "administrator"
  userRepo.updateMap(User.add(adminUserId, "admin", "admin@admin.com",
    "$2a$10$ErWon4hGrcvVRPa02YfaoOyqOCxvAfrrObubP7ZycS3eW/jgzOqQS",
    "bcrypt", None, None) | null)

  extension.recover()
  // wait for processor 1 to complete processing of replayed event messages
  // (ensures that recovery of externally visible state maintained by
  //  studiesRef is completed when awaitProcessing returns)
  extension.awaitProcessing(Set(1))
}
