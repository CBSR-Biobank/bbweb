package org.biobank.service.study

import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.infrastructure.event.StudyEvents._

import org.slf4j.LoggerFactory
import akka.actor._
import akka.pattern.ask
import org.scalatest.Tag
import org.scalatest.BeforeAndAfterEach
import org.joda.time.DateTime
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import scalaz._
import Scalaz._

/**
  * Tests for actor ParticipantAnnotationTypeProcessorSpec. These are written using ScalaTest.
  *
  * To run tagged tests, use this command:
  *   ParticipantAnnotationTypeProcessorSpec -- -n 1
  */
class ParticipantAnnotationTypeProcessorSpec(_system: ActorSystem)
    extends StudyAnnotationTypeProcessorSpec[ParticipantAnnotationType] {

  import org.biobank.TestUtils._

  override def annotationTypeRepository = participantAnnotationTypeRepository

  override def createAnnotationType(maybeId:      Option[AnnotationTypeId] = None,
                                    maybeStudyId: Option[StudyId] = None,
                                    maybeVersion: Option[Long] = None,
                                    maybeName :   Option[String] = None) = {
    val annotType = factory.createParticipantAnnotationType
    annotType.copy(
      id      = maybeId.getOrElse(annotType.id),
      studyId = maybeStudyId.getOrElse(annotType.studyId),
      version = maybeVersion.getOrElse(annotType.version),
      name    = maybeName.getOrElse(annotType.name))
  }

  protected def addCommand(annotType: ParticipantAnnotationType) =
    AddParticipantAnnotationTypeCmd(userId        = None,
                                    studyId       = annotType.studyId.id,
                                    name          = annotType.name,
                                    description   = annotType.description,
                                    valueType     = annotType.valueType,
                                    maxValueCount = annotType.maxValueCount,
                                    options       = annotType.options,
                                    required      = annotType.required)

  protected def updateCommand(annotType: ParticipantAnnotationType) =
    UpdateParticipantAnnotationTypeCmd(userId          = None,
                                       studyId         = annotType.studyId.id,
                                       id              = annotType.id.id,
                                       expectedVersion = annotType.version,
                                       name            = annotType.name,
                                       description     = annotType.description,
                                       valueType       = annotType.valueType,
                                       maxValueCount   = annotType.maxValueCount,
                                       options         = annotType.options,
                                       required        = annotType.required)

  protected def removeCommand(annotType: ParticipantAnnotationType) =
    RemoveParticipantAnnotationTypeCmd(userId          = None,
                                       studyId         = annotType.studyId.id,
                                       id              = annotType.id.id,
                                       expectedVersion = annotType.version)

  protected def addedEventCompare(event: StudyEvent,
                                  annotType: ParticipantAnnotationType) = {
    event.eventType.isParticipantAnnotationTypeAdded must be (true)

    val addedEvent = event.getParticipantAnnotationTypeAdded

    addedEvent must have(
      'name          (Some(annotType.name)),
      'description   (annotType.description),
      'valueType     (Some(annotType.valueType.toString)),
      'maxValueCount (annotType.maxValueCount),
      'required      (Some(annotType.required))
    )

    addedEvent.options must have size annotType.options.size
    annotType.options.foreach { item =>
      addedEvent.options must contain (item)
    }

    annotationTypeRepository.allForStudy(disabledStudy.id) must have size 1
    annotationTypeRepository.withId(
      disabledStudy.id, AnnotationTypeId(addedEvent.getAnnotationTypeId)) mustSucceed { at =>
      at.version mustBe(0)
      checkTimeStamps(at, DateTime.now, None)
    }
  }

  protected def updatedEventCompare(event: StudyEvent,
                                    annotType: ParticipantAnnotationType) = {
    event.eventType.isParticipantAnnotationTypeUpdated must be (true)

    val updatedEvent = event.getParticipantAnnotationTypeUpdated

    updatedEvent must have(
      'version       (Some(annotType.version + 1)),
      'name          (Some(annotType.name)),
      'description   (annotType.description),
      'valueType     (Some(annotType.valueType.toString)),
      'maxValueCount (annotType.maxValueCount),
      'required      (Some(annotType.required))
    )

    updatedEvent.options must have size annotType.options.size
    // verify each option
    annotType.options.map { item =>
      updatedEvent.options must contain (item)
    }

    annotationTypeRepository.allForStudy(disabledStudy.id) must have size 1
    annotationTypeRepository.withId(
      disabledStudy.id, AnnotationTypeId(updatedEvent.getAnnotationTypeId)) mustSucceed { at =>
      at.version mustBe(1)
      checkTimeStamps(at, annotType.timeAdded, DateTime.now)
    }
  }

  protected def removedEventCompare(event: StudyEvent,
                                    annotType: ParticipantAnnotationType) = {
    event.eventType.isParticipantAnnotationTypeRemoved must be (true)

    val removedEvent = event.getParticipantAnnotationTypeRemoved
    removedEvent.annotationTypeId mustBe (Some(annotType.id.id))
  }

  "A participant annotation type processor" must {

    annotationTypeBehaviour

  }

}
