package org.biobank.service.study

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.infrastructure.event.StudyEvents._

import org.slf4j.LoggerFactory
import akka.pattern.ask
import org.scalatest.Tag
import org.scalatest.BeforeAndAfterEach
import org.joda.time.DateTime
import scalaz._
import Scalaz._

/**
  * Tests for actor ParticipantAnnotationTypeProcessorSpec. These are written using ScalaTest.
  *
  * To run tagged tests, use this command:
  *   ParticipantAnnotationTypeProcessorSpec -- -n 1
  */
class ParticipantAnnotationTypeProcessorSpec extends TestFixture {
  import org.biobank.TestUtils._

  private val log = LoggerFactory.getLogger(this.getClass)

  val studyRepository = inject [StudyRepository]

  val collectionEventTypeRepository = inject [CollectionEventTypeRepository]

  val participantAnnotationTypeRepository = inject [ParticipantAnnotationTypeRepository]

  val studiesProcessor = injectActorRef [StudiesProcessor] ("studies")

  val nameGenerator = new NameGenerator(this.getClass)

  var disabledStudy: DisabledStudy = null

  // create the study to be used for each tests*
  override def beforeEach: Unit = {
    disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)
    ()
  }

  "A study processor" can {

    "add a participant annotation type" in {
      val annotType = factory.createParticipantAnnotationType

      val cmd = AddParticipantAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.name, annotType.description, annotType.valueType,
        annotType.maxValueCount, annotType.options, false)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (annotType.studyId.id)

        val addedEvent = event.getParticipantAnnotationTypeAdded
        addedEvent must have (
          'name          (Some(annotType.name)),
          'description   (annotType.description),
          'valueType     (Some(annotType.valueType.toString)),
          'maxValueCount (annotType.maxValueCount)
        )

        addedEvent.options must have size annotType.options.size
        annotType.options.map { item =>
          addedEvent.options must contain (item)
        }

        participantAnnotationTypeRepository.allForStudy(disabledStudy.id) must have size 1
        participantAnnotationTypeRepository.withId(
          disabledStudy.id,
          AnnotationTypeId(addedEvent.getAnnotationTypeId))
        .mustSucceed { at =>
          at.version mustBe(0)
          checkTimeStamps(at, DateTime.now, None)
        }
      }
    }

    "not add a participant annotation type to a study that does not exist" in {
      val study2 = factory.createDisabledStudy
      val annotType = factory.createParticipantAnnotationType

      val cmd = AddParticipantAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.name, annotType.description, annotType.valueType,
        annotType.maxValueCount, annotType.options, false)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustFail s"invalid study id: ${study2.id.id}"
    }

    "not add a participant annotation type if the name already exists" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val cmd = AddParticipantAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.name, annotType.description, annotType.valueType,
        annotType.maxValueCount, annotType.options, true)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v  mustFail "name already exists"
    }

    "update a participant annotation type" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val annotType2 = factory.createParticipantAnnotationType

      val cmd = UpdateParticipantAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.id.id, annotType.version, annotType2.name,
        annotType2.description, annotType2.valueType, annotType2.maxValueCount, annotType2.options)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (annotType.studyId.id)

        val updatedEvent = event.getParticipantAnnotationTypeUpdated
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

        participantAnnotationTypeRepository.allForStudy(disabledStudy.id) must have size 1
        participantAnnotationTypeRepository.withId(
          disabledStudy.id,
          AnnotationTypeId(updatedEvent.getAnnotationTypeId))
        .mustSucceed { at =>
          at.version mustBe(1)
          checkTimeStamps(at, annotType.timeAdded, DateTime.now)
        }
      }
    }

    "not update a participant annotation type to name that already exists" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val annotType2 = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType2)

      val dupliacteName = annotType.name

      val cmd = UpdateParticipantAnnotationTypeCmd(
        None, annotType2.studyId.id, annotType2.id.id, annotType2.version, dupliacteName,
        annotType2.description, annotType2.valueType, annotType2.maxValueCount, annotType2.options)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustFail "name already exists"
    }

    "not update a participant annotation type to the wrong study" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cmd = UpdateParticipantAnnotationTypeCmd(
        None, study2.id.id, annotType.id.id, annotType.version, annotType.name,
        annotType.description, annotType.valueType, annotType.maxValueCount, annotType.options)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustFail "study does not have annotation type"
    }

    "not update a participant annotation type with an invalid version" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val cmd = UpdateParticipantAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.id.id, annotType.version - 1, annotType.name,
        annotType.description, annotType.valueType, annotType.maxValueCount, annotType.options)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustFail "doesn't match current version"
    }

    "remove a participant annotation type" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val cmd = RemoveParticipantAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.id.id, annotType.version)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (annotType.studyId.id)

        val removedEvent = event.getParticipantAnnotationTypeRemoved
        removedEvent.getAnnotationTypeId mustBe (annotType.id.id)
      }
    }

    "not remove a participant annotation type with invalid version" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val cmd = RemoveParticipantAnnotationTypeCmd(
        None, annotType.studyId.id, annotType.id.id, annotType.version - 1)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustFail "expected version doesn't match current version"
    }

  }

}
