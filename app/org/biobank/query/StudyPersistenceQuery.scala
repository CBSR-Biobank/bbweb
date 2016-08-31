// package org.biobank.query

// import javax.inject.{ Inject, Singleton }

// import akka.actor.ActorSystem
// import akka.stream.ActorMaterializer
// import akka.stream.scaladsl.Source
// import akka.persistence.query.PersistenceQuery
// import akka.persistence.query.EventEnvelope
// import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
// import com.google.inject.ImplementedBy

// import org.slf4j.LoggerFactory

// @ImplementedBy(classOf[StudyPersistenceQueryImplementation])
// trait StudyPersistenceQuery {}

// @Singleton
// class StudyPersistenceQueryImplementation @Inject() (val actorSystem: ActorSystem)
//     extends StudyPersistenceQuery {

//   val Log = LoggerFactory.getLogger(this.getClass)

//   implicit val mat = ActorMaterializer()(actorSystem)
//   val queries = PersistenceQuery(actorSystem).readJournalFor[LeveldbReadJournal](
//     LeveldbReadJournal.Identifier)

//   val src: Source[EventEnvelope, Unit] =
//     queries.eventsByPersistenceId("studies-processor-id", 0L, Long.MaxValue)

//   val events: Source[Any, Unit] = src.map(_.event)

//   events.runForeach { event =>
//     Log.info(s"event: $event")
//   }

// }
