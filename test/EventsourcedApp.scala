/*
 * Copyright 2012-2013 Eligotech BV.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test

import scala.concurrent.Await
import scala.reflect.ClassTag
import org.eligosource.eventsourced.core.EventsourcingExtension
import org.eligosource.eventsourced.core.Journal
import org.eligosource.eventsourced.core.Message
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps
import com.mongodb.casbah.Imports.MongoClient
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.Props
import akka.actor.Actor
import akka.actor.TypedActor.Receiver
import org.eligosource.eventsourced.core.Eventsourced
import org.eligosource.eventsourced.core.JournalProps
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

abstract class EventsourcingSpec[T <: EventsourcingFixture[_]: ClassTag] extends Specification {
  type FixtureParam = T

  def createFixture =
    implicitly[ClassTag[T]].runtimeClass.newInstance().asInstanceOf[T]

  val fixture = createFixture
}

trait EventsourcingFixtureOps[A] { self: EventsourcingFixture[A] =>
  val queue = new LinkedBlockingQueue[A]

  //def cleanup()
  def journalProps: JournalProps =
    MongodbCasbahJournalProps(MongoClient(), "biobank-test", "bbweb")

  /*
  def dequeue[A](queue: LinkedBlockingQueue[A]): A = {
    queue.poll(timeout.duration.toMillis, TimeUnit.MILLISECONDS)
  }

  def dequeue(): A = {
    dequeue(queue)
  }

  def dequeue(p: A => Unit) {
    p(dequeue())
  }
  * 
  */

  def result[A: ClassTag](actor: ActorRef)(r: Any): A = {
    Await.result(actor.ask(r)(timeout).mapTo[A], timeout.duration)
  }
}

class EventsourcingFixture[A] extends EventsourcingFixtureOps[A] {
  implicit val timeout = Timeout(10000)
  implicit val system = ActorSystem("test")

  val journal = Journal(journalProps)
  val extension = EventsourcingExtension(system, journal)

  def shutdown() {
    system.shutdown()
    system.awaitTermination(timeout.duration)
  }
}
