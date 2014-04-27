package org.biobank.service

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

import org.slf4j.LoggerFactory
import akka.pattern.ask
import org.scalatest.Tag
import org.scalatest.BeforeAndAfterEach
import scalaz._
import Scalaz._

class ParticipantAnnotationTypeProcessorSpec extends StudyProcessorFixture with BeforeAndAfterEach {

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  val factory = new Factory(
    nameGenerator,
    studyRepository,
    collectionEventTypeRepository,
    collectionEventAnnotationTypeRepository,
    participantAnnotationTypeRepository,
    specimenGroupRepository)

  var disabledStudy: DisabledStudy = null

  // create the study to be used for each tests*
  override def beforeEach: Unit = {
    disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)
  }

  "A study processor" can {

    "add a participant annotation type" in {
      val annotType = factory.createParticipantAnnotationType

      val cmd = AddParticipantAnnotationTypeCmd(
	annotType.studyId.id, annotType.name, annotType.description, annotType.valueType,
	annotType.maxValueCount, annotType.options)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[ParticipantAnnotationTypeAddedEvent]]
        .futureValue

      validation should be('success)
      validation map { event =>
        event shouldBe a[ParticipantAnnotationTypeAddedEvent]
        event should have(
	  'studyId (annotType.studyId.id),
          'name (annotType.name),
          'description (annotType.description),
          'valueType (annotType.valueType),
	  'maxValueCount (annotType.maxValueCount)
	)

	val options = event.options map { eventOptions =>
	  val annotTypeOptions = annotType.options | fail
	  eventOptions should have size annotTypeOptions.size
	  // verify each option
	  annotTypeOptions.map { item =>
	    eventOptions should contain (item)
	  }
	}

        val at = participantAnnotationTypeRepository.annotationTypeWithId(
          disabledStudy.id, AnnotationTypeId(event.annotationTypeId)) | fail
        at.version should be(0)
        participantAnnotationTypeRepository.allAnnotationTypesForStudy(disabledStudy.id) should have size 1
      }
    }

    "not add a participant annotation type if the name already exists" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val cmd = AddParticipantAnnotationTypeCmd(
	annotType.studyId.id, annotType.name, annotType.description, annotType.valueType,
	annotType.maxValueCount, annotType.options)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[ParticipantAnnotationTypeAddedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("name already exists")
      }
    }

    "update a participant annotation type" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val annotType2 = factory.createParticipantAnnotationType

      val cmd = UpdateParticipantAnnotationTypeCmd(
	annotType.studyId.id, annotType.id.id, annotType.versionOption, annotType2.name,
	annotType2.description, annotType2.valueType, annotType2.maxValueCount, annotType2.options)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]]
        .futureValue

      validation should be('success)
      validation map { event =>
        event shouldBe a[ParticipantAnnotationTypeUpdatedEvent]
        event should have(
	  'studyId (annotType.studyId.id),
	  'version (annotType.version + 1),
          'name (annotType2.name),
          'description (annotType2.description),
          'valueType (annotType2.valueType),
	  'maxValueCount (annotType2.maxValueCount)
	)

	val options = event.options map { eventOptions =>
	  val annotTypeOptions = annotType2.options | fail
	  eventOptions should have size annotTypeOptions.size
	  // verify each option
	  annotTypeOptions.map { item =>
	    eventOptions should contain (item)
	  }
	}

        val at = participantAnnotationTypeRepository.annotationTypeWithId(
          disabledStudy.id, AnnotationTypeId(event.annotationTypeId)) | fail
        at.version should be(1)
        participantAnnotationTypeRepository.allAnnotationTypesForStudy(disabledStudy.id) should have size 1
      }
    }

    "not update a participant annotation type to name that already exists" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val annotType2 = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType2)

      val dupliacteName = annotType.name

      val cmd = UpdateParticipantAnnotationTypeCmd(
	annotType2.studyId.id, annotType2.id.id, annotType2.versionOption, dupliacteName,
	annotType2.description, annotType2.valueType, annotType2.maxValueCount, annotType2.options)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("name already exists")
      }
    }

    "not update a participant annotation type to the wrong study" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cmd = UpdateParticipantAnnotationTypeCmd(
	study2.id.id, annotType.id.id, annotType.versionOption, annotType.name,
	annotType.description, annotType.valueType, annotType.maxValueCount, annotType.options)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("study does not have annotation type") }
    }

    "not update a participant annotation type with an invalid version" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val cmd = UpdateParticipantAnnotationTypeCmd(
	annotType.studyId.id, annotType.id.id, Some(annotType.version - 1), annotType.name,
	annotType.description, annotType.valueType, annotType.maxValueCount, annotType.options)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("doesn't match current version")
      }
    }

    "remove a participant annotation type" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val cmd = RemoveParticipantAnnotationTypeCmd(
	annotType.studyId.id, annotType.id.id, annotType.versionOption)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[ParticipantAnnotationTypeRemovedEvent]]
        .futureValue

      validation should be('success)
      validation map { event => event shouldBe a[ParticipantAnnotationTypeRemovedEvent] }
    }

    "not remove a participant annotation type with invalid version" in {
      val annotType = factory.createParticipantAnnotationType
      participantAnnotationTypeRepository.put(annotType)

      val cmd = RemoveParticipantAnnotationTypeCmd(
	annotType.studyId.id, annotType.id.id, Some(annotType.version - 1))
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[ParticipantAnnotationTypeRemovedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("version mismatch")
      }
    }

  }

}
