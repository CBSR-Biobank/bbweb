package test

import scala.concurrent.Await
import scala.reflect.ClassTag

import org.eligosource.eventsourced.core.Eventsourced
import org.eligosource.eventsourced.core.EventsourcingExtension
import org.eligosource.eventsourced.core.InvalidProcessorIdException
import org.eligosource.eventsourced.core.Journal
import org.eligosource.eventsourced.core.Message
import org.eligosource.eventsourced.core.Receiver
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps
import org.specs2.mutable.Specification

import com.mongodb.casbah.Imports.MongoClient

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

class ProcessorSpec extends EventsourcedSpec[ProcessorSpec.Fixture] {

  "receive a timestamp message" in {
    fixture.result[Long](fixture.processor(1))(Message("foo")) must be > (0L)
  }

  "not have an id < 1" in {
    fixture.processor(0) must throwA[InvalidProcessorIdException]
    fixture.processor(-1) must throwA[InvalidProcessorIdException]
  }
}

object ProcessorSpec {

  class Fixture extends EventsourcingFixture[Long] {
    def processor(pid: Int = 1) = extension.processorOf(Props(
      new Processor with Receiver with Eventsourced { val id = pid }))
  }

  class Processor extends Actor { this: Receiver =>
    def receive = {
      case "foo" => sender ! message.timestamp
    }
  }
}
