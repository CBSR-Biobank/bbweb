package service

import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps

import org.eligosource.eventsourced.core._
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps

import com.mongodb.casbah.Imports._

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import java.util.concurrent.TimeUnit

import domain._
import domain.study._

trait AppServices {
  def studyService: StudyService
  def userService: UserService
}

object AppServices {
  def boot: AppServices = new AppServices {
    implicit val system = ActorSystem("eventsourced")
    implicit val timeout = Timeout(10 seconds)

    val MongoDbName = "biobank-web"
    val MongoCollName = "bbweb"

    val mongoClient = MongoClient()
    val mongoDB = mongoClient(MongoDbName)
    val mongoColl = mongoClient(MongoDbName)(MongoCollName)

    val journalProps = MongodbCasbahJournalProps(mongoClient, MongoDbName, MongoCollName)

    val journal = Journal(journalProps)
    val extension = EventsourcingExtension(system, journal)

    val studiesRef = Ref(Map.empty[domain.study.StudyId, Study])
    val specimenGroupsRef = Ref(Map.empty[domain.study.SpecimenGroupId, SpecimenGroup])
    val usersRef = Ref(Map.empty[domain.UserId, User])

    val studyProcessor = extension.processorOf(Props(
      new StudyProcessor(studiesRef, specimenGroupsRef) with Emitter with Eventsourced { val id = 1 }))

    val userProcessor = extension.processorOf(Props(
      new UserProcessor(usersRef) with Emitter with Eventsourced { val id = 1 }))

    val studyService = new StudyService(studiesRef, specimenGroupsRef, studyProcessor)
    val userService = new UserService(usersRef, userProcessor)

    extension.recover()
    // wait for processor 1 to complete processing of replayed event messages
    // (ensures that recovery of externally visible state maintained by
    //  studiesRef is completed when awaitProcessing returns)
    extension.awaitProcessing(Set(1))
  }
}
