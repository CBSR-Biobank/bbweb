package org.biobank.service.study

import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

import akka.actor._
import org.slf4j.LoggerFactory
import org.scalatest.Tag
import org.joda.time.DateTime
import scalaz.Scalaz._
import akka.testkit.{ TestActors, TestKit, ImplicitSender }

/**
  * Tests for actor CeventAnnotationTypeProcessorSpec. These are written using ScalaTest.
  *
  */
class CeventAnnotationTypeProcessorSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with StudyAnnotationTypeProcessorSpec[CollectionEventAnnotationType] {

  import org.biobank.TestUtils._

  override val system = _system

  override def annotationTypeRepository = collectionEventAnnotationTypeRepository

  override def createAnnotationType(maybeId:      Option[AnnotationTypeId] = None,
                                    maybeStudyId: Option[StudyId] = None,
                                    maybeVersion: Option[Long] = None,
                                    maybeName :   Option[String] = None) = {
    val annotType = factory.createCollectionEventAnnotationType
    annotType.copy(
      id      = maybeId.getOrElse(annotType.id),
      studyId = maybeStudyId.getOrElse(annotType.studyId),
      version = maybeVersion.getOrElse(annotType.version),
      name    = maybeName.getOrElse(annotType.name))
  }

  protected def addCommand(annotType: CollectionEventAnnotationType) =
    AddCollectionEventAnnotationTypeCmd(userId        = None,
                                    studyId       = annotType.studyId.id,
                                    name          = annotType.name,
                                    description   = annotType.description,
                                    valueType     = annotType.valueType,
                                    maxValueCount = annotType.maxValueCount,
                                    options       = annotType.options)

  protected def updateCommand(annotType: CollectionEventAnnotationType) =
    UpdateCollectionEventAnnotationTypeCmd(userId          = None,
                                       studyId         = annotType.studyId.id,
                                       id              = annotType.id.id,
                                       expectedVersion = annotType.version,
                                       name            = annotType.name,
                                       description     = annotType.description,
                                       valueType       = annotType.valueType,
                                       maxValueCount   = annotType.maxValueCount,
                                       options         = annotType.options)

  protected def removeCommand(annotType: CollectionEventAnnotationType) =
    RemoveCollectionEventAnnotationTypeCmd(userId          = None,
                                       studyId         = annotType.studyId.id,
                                       id              = annotType.id.id,
                                       expectedVersion = annotType.version)

  protected def addedEventCompare(event: StudyEvent,
                                  annotType: CollectionEventAnnotationType) = {
    event.eventType.isCollectionEventAnnotationTypeAdded must be (true)

    val addedEvent = event.getCollectionEventAnnotationTypeAdded

    addedEvent must have(
      'name          (Some(annotType.name)),
      'description   (annotType.description),
      'valueType     (Some(annotType.valueType.toString)),
      'maxValueCount (annotType.maxValueCount)
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
                                    annotType: CollectionEventAnnotationType) = {
    event.eventType.isCollectionEventAnnotationTypeUpdated must be (true)

    val updatedEvent = event.getCollectionEventAnnotationTypeUpdated

    updatedEvent must have(
      'version       (Some(annotType.version + 1)),
      'name          (Some(annotType.name)),
      'description   (annotType.description),
      'valueType     (Some(annotType.valueType.toString)),
      'maxValueCount (annotType.maxValueCount)
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
                                    annotType: CollectionEventAnnotationType) = {
    event.eventType.isCollectionEventAnnotationTypeRemoved must be (true)

    val removedEvent = event.getCollectionEventAnnotationTypeRemoved
    removedEvent.annotationTypeId mustBe (Some(annotType.id.id))
  }

  "A collection event annotation type processor" must {

    annotationTypeBehaviour

  }

}

