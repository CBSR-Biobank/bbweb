package service

import domain._
import domain.study._
import infrastructure._

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

    val studyRepository = new ReadWriteRepository[StudyId, Study](v => v.id)
    val collectionEventTypeRepository =
      new ReadWriteRepository[CollectionEventTypeId, CollectionEventType](v => v.id)
    val sg2cetRepo =
      new ReadWriteRepository[String, SpecimenGroupCollectionEventType](v => v.id)
    val cet2atRepo =
      new ReadWriteRepository[String, CollectionEventTypeAnnotationType](v => v.id)
    val userRepo = new ReadWriteRepository[UserId, User](v => new UserId(v.email))

    val multicastTargets = List(
      system.actorOf(Props(new StudyProcessor(
        studyRepository, collectionEventTypeRepository) with Emitter)),
      system.actorOf(Props(new UserProcessor(userRepo) with Emitter)))

    // this is the commnad bus
    val multicastProcessor = extension.processorOf(
      ProcessorProps(1, pid => new Multicast(multicastTargets, identity) with Confirm with Eventsourced { val id = pid }))

    val studyService = new StudyService(multicastProcessor)
    val userService = new UserService(userRepo, multicastProcessor)

    // for debug only - password is "administrator"
    userRepo.updateMap(User.add(new UserId("admin@admin.com"), "admin", "admin@admin.com",
      "$2a$10$ErWon4hGrcvVRPa02YfaoOyqOCxvAfrrObubP7ZycS3eW/jgzOqQS",
      "bcrypt", None, None) | null)

    extension.recover()
    // wait for processor 1 to complete processing of replayed event messages
    // (ensures that recovery of externally visible state maintained by
    //  studiesRef is completed when awaitProcessing returns)
    //extension.awaitProcessing(Set(1))
  }
}
