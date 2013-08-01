package fixture

import service._
import domain._

//import scala.util.{ Try, Success, Failure }
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
import akka.testkit._
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

trait TestComponentImpl extends TopComponent with ServiceComponentImpl {

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

  def start = {
    extension.recover()
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
