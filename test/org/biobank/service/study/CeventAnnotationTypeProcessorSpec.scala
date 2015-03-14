package org.biobank.service.study

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

import akka.pattern._
import org.slf4j.LoggerFactory
import org.scalatest.Tag
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

/**
  * Tests for actor CeventAnnotationTypeProcessorSpec. These are written using ScalaTest.
  *
  */
class CeventAnnotationTypeProcessorSpec extends TestFixture {
  import org.biobank.TestUtils._

  private val log = LoggerFactory.getLogger(this.getClass)

  val studyRepository = inject [StudyRepository]

  val collectionEventAnnotationTypeRepository = inject [CollectionEventAnnotationTypeRepository]

  val studiesProcessor = injectActorRef [StudiesProcessor] ("studies")

  val nameGenerator = new NameGenerator(this.getClass)

  var disabledStudy: DisabledStudy = null

  // create the study to be used for each tests
  override def beforeEach: Unit = {
    disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)
    ()
  }

  "A study processor" can {

    "add a cevent annotation type" in {
      val annotType = factory.createCollectionEventAnnotationType

      val cmd = AddCollectionEventAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.name, annotType.description, annotType.valueType,
        annotType.maxValueCount, annotType.options)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustSucceed { event =>
        event.id must be (annotType.studyId.id)

        val addedEvent = event.getCollectionEventAnnotationTypeAdded

        addedEvent must have(
          'name          (Some(annotType.name)),
          'description   (annotType.description),
          'valueType     (Some(annotType.valueType.toString)),
          'maxValueCount (annotType.maxValueCount)
        )

        addedEvent.options must have size annotType.options.size
        annotType.options.map { item =>
          addedEvent.options must contain (item)
        }

        collectionEventAnnotationTypeRepository.allForStudy(disabledStudy.id) must have size 1
        collectionEventAnnotationTypeRepository.withId(
          disabledStudy.id, AnnotationTypeId(addedEvent.getAnnotationTypeId)) mustSucceed { at =>
          at.version mustBe(0)
          checkTimeStamps(at, DateTime.now, None)
        }
      }
    }

    "not add a cevent annotation type to a study that does not exist" in {
      val study2 = factory.createDisabledStudy

      val annotType = factory.createCollectionEventAnnotationType

      val cmd = AddCollectionEventAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.name, annotType.description, annotType.valueType,
        annotType.maxValueCount, annotType.options)

      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFail s"invalid study id: ${study2.id.id}"
    }

    "not add a cevent annotation type if the name already exists" in {
      val annotType = factory.createCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotType)

      val cmd = AddCollectionEventAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.name, annotType.description, annotType.valueType,
        annotType.maxValueCount, annotType.options)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFail "name already exists"
    }

    "update a cevent annotation type" in {
      val annotType = factory.createCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotType)

      val annotType2 = factory.createCollectionEventAnnotationType

      val cmd = UpdateCollectionEventAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.id.id, annotType.version, annotType2.name,
        annotType2.description, annotType2.valueType, annotType2.maxValueCount, annotType2.options)
      val v = ask(studiesProcessor, cmd)
          .mapTo[DomainValidation[StudyEvent]]
      .futureValue

      v mustSucceed { event =>
        event.id must be (annotType.studyId.id)

        val updatedEvent = event.getCollectionEventAnnotationTypeUpdated
        updatedEvent must have(
          'version       (Some(annotType.version + 1)),
          'name          (Some(annotType2.name)),
          'description   (annotType2.description),
          'valueType     (Some(annotType2.valueType.toString)),
          'maxValueCount (annotType2.maxValueCount)
        )

        updatedEvent.options must have size annotType2.options.size
        // verify each option
        annotType2.options.map { item =>
          updatedEvent.options must contain (item)
        }

        collectionEventAnnotationTypeRepository.allForStudy(disabledStudy.id) must have size 1
        collectionEventAnnotationTypeRepository.withId(
          disabledStudy.id, AnnotationTypeId(updatedEvent.getAnnotationTypeId)) mustSucceed { at =>
          at.version mustBe(1)
          checkTimeStamps(at, annotType.timeAdded, DateTime.now)
        }
      }
    }

    "not update a cevent annotation type to name that already exists" in {
      val annotType = factory.createCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotType)

      val annotType2 = factory.createCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotType2)

      val dupliacteName = annotType.name

      val cmd = UpdateCollectionEventAnnotationTypeCmd(
        None, annotType2.studyId.id, annotType2.id.id, annotType2.version, dupliacteName,
        annotType2.description, annotType2.valueType, annotType2.maxValueCount, annotType2.options)

      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFail "name already exists"
    }

    "not update a cevent annotation type to the wrong study" in {
      val annotType = factory.createCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotType)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cmd = UpdateCollectionEventAnnotationTypeCmd(
        None, study2.id.id, annotType.id.id, annotType.version, annotType.name,
        annotType.description, annotType.valueType, annotType.maxValueCount, annotType.options)

      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFail "study does not have annotation type"
    }

    "not update a cevent annotation type with an invalid version" in {
      val annotType = factory.createCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotType)

      val cmd = UpdateCollectionEventAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.id.id, annotType.version - 1, annotType.name,
        annotType.description, annotType.valueType, annotType.maxValueCount, annotType.options)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustFail "doesn't match current version"
    }

    "remove a cevent annotation type" in {
      val annotType = factory.createCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotType)

      val cmd = RemoveCollectionEventAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.id.id, annotType.version)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustSucceed { event =>
        event.id must be (annotType.studyId.id)

        val removedEvent = event.getCollectionEventAnnotationTypeRemoved
        removedEvent.annotationTypeId mustBe (Some(annotType.id.id))
      }
    }

    "not remove a cevent annotation type with invalid version" in {
      val annotType = factory.createCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotType)

      val cmd = RemoveCollectionEventAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.id.id, annotType.version - 1)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustFail "expected version doesn't match current version"
    }

  }

}
