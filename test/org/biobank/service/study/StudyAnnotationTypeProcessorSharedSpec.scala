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

/**
  * Tests for actor CeventAnnotationTypeProcessorSpec. These are written using ScalaTest.
  *
  */
trait StudyAnnotationTypeProcessorSpec[T <: StudyAnnotationType] extends TestFixture {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  var disabledStudy: DisabledStudy = null

  protected def annotationTypeRepository: StudyAnnotationTypeRepository[T]

  protected def createAnnotationType(maybeId:      Option[AnnotationTypeId] = None,
                                     maybeStudyId: Option[StudyId] = None,
                                     maybeVersion: Option[Long] = None,
                                     maybeName:    Option[String] =  None): T

  protected def addCommand(annotType: T): StudyAnnotationTypeCommand

  protected def updateCommand(annotType: T): StudyAnnotationTypeModifyCommand

  protected def removeCommand(annotType: T): StudyAnnotationTypeModifyCommand

  protected def addedEventCompare(event: StudyEvent, annotType: T): Unit

  protected def updatedEventCompare(event: StudyEvent, annotType: T): Unit

  protected def removedEventCompare(event: StudyEvent, annotType: T): Unit

  // create the study to be used for each tests
  override def beforeEach: Unit = {
    disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)
    ()
  }

  def annotationTypeBehaviour() = {

    "add an annotation type" in {
      val annotType = createAnnotationType()

      val v = ask(studiesProcessor, addCommand(annotType))
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustSucceed { event =>
        event.id must be (annotType.studyId.id)
        addedEventCompare(event, annotType)
      }
    }

    "not add an annotation type to a study that does not exist" in {
      val study2 = factory.createDisabledStudy

      val annotType = createAnnotationType()

      val v = ask(studiesProcessor, addCommand(annotType))
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFail s"invalid study id: ${study2.id.id}"
    }

    "not add an annotation type if the name already exists" in {
      val annotType = createAnnotationType()
      annotationTypeRepository.put(annotType)

      val v = ask(studiesProcessor, addCommand(annotType))
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFail "name already exists"
    }

    "update an annotation type" in {
      val annotType = createAnnotationType()
      annotationTypeRepository.put(annotType)

      val annotType2 = createAnnotationType(maybeId      = Some(annotType.id),
                                            maybeStudyId = Some(annotType.studyId),
                                            maybeVersion = Some(annotType.version))

      val v = ask(studiesProcessor, updateCommand(annotType2))
      .mapTo[DomainValidation[StudyEvent]]
      .futureValue

      v mustSucceed { event =>
        event.id must be (annotType.studyId.id)
        updatedEventCompare(event, annotType2)
      }
    }

    "not update an annotation type to name that already exists" in {
      val annotType = createAnnotationType()
      annotationTypeRepository.put(annotType)

      val annotType2 = createAnnotationType()
      annotationTypeRepository.put(annotType2)

      val annotType3 = createAnnotationType(maybeId      = Some(annotType2.id),
                                            maybeStudyId = Some(annotType2.studyId),
                                            maybeVersion = Some(annotType2.version),
                                            maybeName    = Some(annotType.name))

      val v = ask(studiesProcessor, updateCommand(annotType3))
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFail "name already exists"
    }

    "not update an annotation type to the wrong study" in {
      val annotType = createAnnotationType()
      annotationTypeRepository.put(annotType)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val annotType2 = createAnnotationType(maybeId      = Some(annotType.id),
                                            maybeStudyId = Some(study2.id),
                                            maybeVersion = Some(annotType.version))

      val v = ask(studiesProcessor, updateCommand(annotType2))
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFail "study does not have annotation type"
    }

    "not update an annotation type with an invalid version" in {
      val annotType = createAnnotationType()
      annotationTypeRepository.put(annotType)

      val annotType2 = createAnnotationType(maybeId      = Some(annotType.id),
                                            maybeStudyId = Some(annotType.studyId),
                                            maybeVersion = Some(annotType.version + 1))

      val v = ask(studiesProcessor, updateCommand(annotType2))
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustFail "doesn't match current version"
    }

    "remove an annotation type" in {
      val annotType = createAnnotationType()
      annotationTypeRepository.put(annotType)

      val v = ask(studiesProcessor, removeCommand(annotType))
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustSucceed { event =>
        event.id must be (annotType.studyId.id)
        removedEventCompare(event, annotType)
      }
    }

    "not remove an annotation type with invalid version" in {
      val annotType = createAnnotationType()
      annotationTypeRepository.put(annotType)

      val annotType2 = createAnnotationType(maybeId      = Some(annotType.id),
                                            maybeStudyId = Some(annotType.studyId),
                                            maybeVersion = Some(annotType.version + 1))

      val v = ask(studiesProcessor, removeCommand(annotType2))
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustFail "expected version doesn't match current version"
    }

  }

}
