package test

import scala.concurrent._
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.language.postfixOps

import org.eligosource.eventsourced.core._
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps

import com.mongodb.casbah.Imports._

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.Props
import akka.actor.Actor
import akka.actor.TypedActor.Receiver

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

import domain._
import domain.study._
import service._

abstract class EventsourcedSpec[T <: EventsourcingFixture[_]: ClassTag] extends Specification {
  type FixtureParam = T

  def createFixture =
    implicitly[ClassTag[T]].runtimeClass.newInstance().asInstanceOf[T]

  val fixture = createFixture
}

trait EventsourcingFixtureOps[A] { self: EventsourcingFixture[A] =>
  val queue = new LinkedBlockingQueue[A]

  val MongoDbName = "biobank-test"
  val MongoCollName = "bbweb"

  // delete the journal contents
  val mongoClient = MongoClient()
  val mongoDB = mongoClient(MongoDbName)
  val mongoColl = mongoClient(MongoDbName)(MongoCollName)
  mongoColl.remove(MongoDBObject.empty)

  def journalProps: JournalProps =
    MongodbCasbahJournalProps(mongoClient, MongoDbName, MongoCollName)

  def result[A: ClassTag](actor: ActorRef)(r: Any): A = {
    Await.result(actor.ask(r)(timeout).mapTo[A], timeout.duration)
  }
}

// TODO: this may need a better implementation
//
//  
class EventsourcingFixture[A] extends EventsourcingFixtureOps[A] {
  implicit val timeout = Timeout(10 seconds)
  implicit val system = ActorSystem("test")

  val journal = Journal(journalProps)
  val extension = EventsourcingExtension(system, journal)

  def shutdown() {
    system.shutdown()
    system.awaitTermination(timeout.duration)
  }

  extension.recover()
  // wait for processor 1 to complete processing of replayed event messages
  // (ensures that recovery of externally visible state maintained by
  //  studiesRef is completed when awaitProcessing returns)
  extension.awaitProcessing(Set(1))
}
