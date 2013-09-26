package service

import fixture._
import domain._
import domain.study._
import service.commands._
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
class ParticipantAnnotationTypeSpec extends Specification with NoTimeConversions with Tags with StudyFixture {
  args(
    //include = "tag1",
    sequential = true) // forces all tests to be run sequentially

  val nameGenerator = new NameGenerator(classOf[ParticipantAnnotationTypeSpec].getSimpleName)
  val studyName = nameGenerator.next[Study]
  val study = await(studyService.addStudy(new AddStudyCmd(studyName, Some(studyName)))) | null

  "Participant annotation type" can {

    "be added" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(study.id.toString, name, Some(name), valueType)))

      at1 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name)
          x.description must beSome(name)
          x.valueType must beEqualTo(valueType)
          x.maxValueCount must beNone
          x.options must beNone
          participantAnnotationTypeRepository.annotationTypeWithId(
            study.id, x.id) must beSuccessful
          participantAnnotationTypeRepository.allAnnotationTypesForStudy(
            study.id).size mustEqual 1
      }

      val name2 = nameGenerator.next[AnnotationType]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))

      val at2 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(study.id.toString, name2, None,
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
            study.id, x.id) must beSuccessful
          participantAnnotationTypeRepository.allAnnotationTypesForStudy(
            study.id).size mustEqual 2
      }
    } tag ("tag1")

    "not be added if name already exists" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text
      val maxValueCount = None

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(study.id.toString, name, Some(name),
          valueType, maxValueCount, None))) | null

      participantAnnotationTypeRepository.annotationTypeWithId(
        study.id, at1.id) must beSuccessful

      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))

      val at2 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(study.id.toString, name, Some(name),
          valueType2, maxValueCount2, options)))

      at2 must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }
    }

    "be updated" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(study.id.toString, name, Some(name),
          valueType))) | null

      val name2 = nameGenerator.next[Study]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))

      val at2 = await(studyService.updateParticipantAnnotationType(
        new UpdateParticipantAnnotationTypeCmd(
          at1.id.toString, at1.versionOption, study.id.toString, name2, None, valueType2,
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
            study.id, x.id) must beSuccessful
      }
    }

    "not be updated to name that already exists" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text
      val maxValueCount = None

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(study.id.toString, name, Some(name),
          valueType, maxValueCount, None))) | null
      participantAnnotationTypeRepository.annotationTypeWithId(
        study.id, at1.id) must beSuccessful

      val name2 = nameGenerator.next[AnnotationType]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))

      val at2 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(study.id.toString, name2, None,
          valueType2, maxValueCount2, options))) | null

      participantAnnotationTypeRepository.annotationTypeWithId(
        study.id, at2.id) must beSuccessful

      val at3 = await(studyService.updateParticipantAnnotationType(
        new UpdateParticipantAnnotationTypeCmd(
          at2.id.toString, at2.versionOption, study.id.toString, name, Some(name), valueType,
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
        new AddParticipantAnnotationTypeCmd(study.id.toString, name, Some(name),
          valueType, None, None))) | null

      val name2 = nameGenerator.next[Study]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))

      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, None))) | null

      val at2 = await(studyService.updateParticipantAnnotationType(
        new UpdateParticipantAnnotationTypeCmd(
          at1.id.toString, at1.versionOption, study2.id.toString, name2, None, valueType2,
          maxValueCount2, options)))
      at2 must beFailing.like { case msgs => msgs.head must contain("study does not have annotation type") }
    }

    "not be updated with invalid version" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(study.id.toString, name, Some(name),
          valueType, None, None))) | null

      val name2 = nameGenerator.next[Study]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = Some(2)
      val options = Some(Map("1" -> "a", "2" -> "b"))
      val versionOption = Some(at1.version + 1)

      val at2 = await(studyService.updateParticipantAnnotationType(
        new UpdateParticipantAnnotationTypeCmd(
          at1.id.toString, versionOption, study.id.toString, name2, None, valueType2,
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
          study.id.toString, name, Some(name), valueType))) | null
      participantAnnotationTypeRepository.annotationTypeWithId(
        study.id, at1.id) must beSuccessful

      val at2 = await(studyService.removeParticipantAnnotationType(
        new RemoveParticipantAnnotationTypeCmd(
          at1.id.toString, at1.versionOption, study.id.toString)))

      at2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(at1.version)
          x.name must be(name)
          x.description must beSome(name)
          x.valueType must beEqualTo(valueType)
          x.maxValueCount must beNone
          x.options must beNone
          participantAnnotationTypeRepository.annotationTypeWithId(
            study.id, x.id) must beFailing
      }
    }

    "not be removed with invalid version" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.Text

      val at1 = await(studyService.addParticipantAnnotationType(
        new AddParticipantAnnotationTypeCmd(
          study.id.toString, name, Some(name), valueType))) | null
      participantAnnotationTypeRepository.annotationTypeWithId(
        study.id, at1.id) must beSuccessful

      val versionOption = Some(at1.version + 1)
      val at2 = await(studyService.removeParticipantAnnotationType(
        new RemoveParticipantAnnotationTypeCmd(
          at1.id.toString, versionOption, study.id.toString)))
      at2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }
  }

}
