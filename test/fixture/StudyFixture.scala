package fixture
import fixture._
import infrastructure._
import service._
import domain._
import domain.study._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import akka.actor._
import akka.util.Timeout
import org.eligosource.eventsourced.core._
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps
import scalaz._
import Scalaz._

object StudyFixture {

  class Fixture extends EventsourcingFixture {

    val studyRepository = new ReadWriteRepository[StudyId, Study](v => v.id)
    val specimenGroupRepository = new ReadWriteRepository[SpecimenGroupId, SpecimenGroup](v => v.id)
    val collectionEventTypeRepository = new ReadWriteRepository[CollectionEventTypeId, CollectionEventType](v => v.id)

    val annotationTypeRepo =
      new ReadWriteRepository[AnnotationTypeId, StudyAnnotationType](v => v.id)

    val annotationOptionRepo =
      new ReadWriteRepository[String, AnnotationOption](v => v.id)

    // specimen group -> collection event type repository
    val sg2cetRepo =
      new ReadWriteRepository[String, SpecimenGroupCollectionEventType](v => v.id)

    // annotation type -> collection type event repository
    val at2cetRepo =
      new ReadWriteRepository[String, CollectionEventTypeAnnotationType](v => v.id)

    val studyProcessor = extension.processorOf(Props(
      new StudyProcessor(
        studyRepository,
        specimenGroupRepository,
        collectionEventTypeRepository,
        annotationTypeRepo,
        annotationOptionRepo,
        sg2cetRepo,
        at2cetRepo) with Emitter with Eventsourced { val id = 1 }))

    val studyService = new StudyService(studyRepository, specimenGroupRepository,
      collectionEventTypeRepository, studyProcessor)

    def await[T](f: Future[DomainValidation[T]]) = {
      Await.result(f, timeout.duration)
    }

    def validationResult[T](f: Future[DomainValidation[T]]): T = {
      await(f) match {
        case Success(e) => e
        case Failure(msg) => throw new Error("validation failed: " + msg)
      }
    }
  }

}