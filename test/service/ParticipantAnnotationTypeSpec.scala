package service

import fixture._
import domain._
import domain.study._
import service.commands.StudyCommands._
import scala.concurrent._
import scala.concurrent.duration._
import org.specs2.scalaz.ValidationMatchers._
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import akka.actor._
import org.eligosource.eventsourced.core._
import scalaz._
import Scalaz._

@RunWith(classOf[JUnitRunner])
class ParticipantAnnotationTypeSpec extends StudyCommandFixture {
  args(
    //include = "tag1",
    sequential = true) // forces all tests to be run sequentially

  val nameGenerator = new NameGenerator(classOf[ParticipantAnnotationTypeSpec].getSimpleName)
  val studyName = nameGenerator.next[Study]
  val studyEvent = await(studyService.addStudy(new AddStudyCmd(studyName, Some(studyName)))) | null
  val studyId = StudyId(studyEvent.id)

  "Participant annotation type" can {

    "be added" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(studyId.id, name, Some(name), valueType)))

      at1 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name)
          x.description must beSome(name)
          x.valueType must beEqualTo(valueType)
          x.maxValueCount must beNone
          x.options must beNone
          participantAnnotationTypeRepository.annotationTypeWithId(
            studyId, x.annotationTypeId) must beSuccessful
          participantAnnotationTypeRepository.allAnnotationTypesForStudy(
            studyId).size mustEqual 1
      }

      val name2 = nameGenerator.next[AnnotationType]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))

      val at2 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(studyId.id, name2, None,
          valueType2, maxValueCount2, options)))

      at2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name2)
          x.description must beNone
          x.valueType must beEqualTo(valueType2)
          x.maxValueCount must be(maxValueCount2)
          x.options must be(options)
          participantAnnotationTypeRepository.annotationTypeWithId(
            studyId, x.annotationTypeId) must beSuccessful
          participantAnnotationTypeRepository.allAnnotationTypesForStudy(
            studyId).size mustEqual 2
      }
    } tag ("tag1")

    "not be added if name already exists" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text
      val maxValueCount = None

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(studyId.id, name, Some(name),
          valueType, maxValueCount, None))) | null

      participantAnnotationTypeRepository.annotationTypeWithId(
        studyId, at1.annotationTypeId) must beSuccessful

      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))

      val at2 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(studyId.id, name, Some(name),
          valueType2, maxValueCount2, options)))

      at2 must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }
    }

    "be updated" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(studyId.id, name, Some(name),
          valueType))) | null

      val name2 = nameGenerator.next[Study]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))

      val at2 = await(studyService.updateParticipantAnnotationType(
        new UpdateParticipantAnnotationTypeCmd(
          at1.annotationTypeId, Some(at1.version), studyId.id, name2, None, valueType2,
          maxValueCount2, options)))

      at2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(at1.version + 1)
          x.name must be(name2)
          x.description must beNone
          x.valueType must beEqualTo(valueType2)
          x.maxValueCount must be(maxValueCount2)
          x.options must be(options)
          participantAnnotationTypeRepository.annotationTypeWithId(
            studyId, x.annotationTypeId) must beSuccessful
      }
    }

    "not be updated to name that already exists" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text
      val maxValueCount = None

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(studyId.id, name, Some(name),
          valueType, maxValueCount, None))) | null
      participantAnnotationTypeRepository.annotationTypeWithId(
        studyId, at1.annotationTypeId) must beSuccessful

      val name2 = nameGenerator.next[AnnotationType]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))

      val at2 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(studyId.id, name2, None,
          valueType2, maxValueCount2, options))) | null

      participantAnnotationTypeRepository.annotationTypeWithId(
        studyId, at2.annotationTypeId) must beSuccessful

      val at3 = await(studyService.updateParticipantAnnotationType(
        new UpdateParticipantAnnotationTypeCmd(
          at2.annotationTypeId, Some(at2.version), studyId.id, name, Some(name), valueType,
          maxValueCount, options)))
      at3 must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }
    }

    "not be updated to wrong study" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text
      val maxValueCount = 0

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(studyId.id, name, Some(name),
          valueType, None, None))) | null

      val name2 = nameGenerator.next[Study]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))

      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, None))) | null

      val at2 = await(studyService.updateParticipantAnnotationType(
        new UpdateParticipantAnnotationTypeCmd(
          at1.annotationTypeId, Some(at1.version), study2.id, name2, None, valueType2,
          maxValueCount2, options)))
      at2 must beFailing.like { case msgs => msgs.head must contain("study does not have annotation type") }
    }

    "not be updated with invalid version" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(studyId.id, name, Some(name),
          valueType, None, None))) | null

      val name2 = nameGenerator.next[Study]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))
      val versionOption = Some(at1.version + 1)

      val at2 = await(studyService.updateParticipantAnnotationType(
        new UpdateParticipantAnnotationTypeCmd(
          at1.annotationTypeId, versionOption, studyId.id, name2, None, valueType2,
          maxValueCount2, options)))
      at2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }

    "be removed" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(
          studyId.id, name, Some(name), valueType))) | null
      participantAnnotationTypeRepository.annotationTypeWithId(
        studyId, at1.annotationTypeId) must beSuccessful

      val at2 = await(studyService.removeParticipantAnnotationType(
        new RemoveParticipantAnnotationTypeCmd(
          at1.annotationTypeId, Some(at1.version), studyId.id)))

      at2 must beSuccessful.like {
        case x =>
          participantAnnotationTypeRepository.annotationTypeWithId(
            studyId, x.annotationTypeId) must beFailing
      }
    }

    "not be removed with invalid version" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(
          studyId.id, name, Some(name), valueType))) | null
      participantAnnotationTypeRepository.annotationTypeWithId(
        studyId, at1.annotationTypeId) must beSuccessful

      val versionOption = Some(at1.version + 1)
      val at2 = await(studyService.removeParticipantAnnotationType(
        new RemoveParticipantAnnotationTypeCmd(
          at1.annotationTypeId, versionOption, studyId.id)))
      at2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }
  }

}
