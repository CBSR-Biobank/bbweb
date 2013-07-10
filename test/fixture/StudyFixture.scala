package fixture

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

abstract class StudyFixture extends AppFixture {

  val nameGenerator: NameGenerator

  val studyRepository = new StudyReadWriteRepository(v => v.id)
  val specimenGroupRepository = new SpecimenGroupReadWriteRepository(v => v.id)
  val collectionEventTypeRepository = new CollectionEventTypeReadWriteRepository(v => v.id)

  val annotationTypeRepo = new CollectionEventAnnotationTypeReadWriteRepository(v => v.id)

  // specimen group -> collection event type repository
  val sg2cetRepo =
    new SpecimenGroupCollectionEventTypeReadWriteRepository(v => v.id)

  // annotation type -> collection type event repository
  val at2cetRepo =
    new CollectionEventTypeAnnotationTypeReadWriteRepository(v => v.id)

  val studyProcessor = extension.processorOf(Props(
    new StudyProcessor(
      studyRepository,
      specimenGroupRepository,
      collectionEventTypeRepository,
      annotationTypeRepo,
      sg2cetRepo,
      at2cetRepo) with Emitter with Eventsourced { val id = 1 }))

  val studyService = new StudyService(studyRepository, specimenGroupRepository,
    collectionEventTypeRepository, annotationTypeRepo, studyProcessor)
}