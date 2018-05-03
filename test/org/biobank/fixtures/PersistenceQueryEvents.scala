package org.biobank.fixtures

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{Materializer, ActorMaterializer}
import akka.persistence.query._
import akka.stream.scaladsl.Source
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl._
import org.slf4j.LoggerFactory
import akka.persistence.inmemory.query.scaladsl.InMemoryReadJournal

trait PresistenceQueryEvents {

  private val log = LoggerFactory.getLogger(this.getClass)

  implicit val system: ActorSystem

  private implicit val mat: Materializer = ActorMaterializer()(system)

  val readJournal: InMemoryReadJournal =
    PersistenceQuery(system).readJournalFor[InMemoryReadJournal](InMemoryReadJournal.Identifier)

  def logEvents(persistenceId: String): Unit = {
    val source: Source[EventEnvelope, NotUsed] =
      readJournal.currentEventsByPersistenceId(persistenceId, 0L, Long.MaxValue)

    source.runForeach { envelope =>
      log.info(s"-----------> HERE event: {}", envelope.event)
    }
    ()
  }

  // see https://groups.google.com/forum/#!topic/akka-user/l8jbOczKR3k
  def logAllPersistenceIds(): Unit = {
    readJournal
      .currentPersistenceIds()
      .runForeach { persistenceId =>
        logEvents(persistenceId)
      }
    ()
  }

}
