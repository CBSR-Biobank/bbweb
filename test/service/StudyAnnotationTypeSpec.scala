package service

import test._
import fixture._
import domain._
import domain.study._
import infrastructure.commands._
import scala.concurrent._
import scala.concurrent.duration._
import org.specs2.scalaz.ValidationMatchers._
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import akka.actor._
import org.eligosource.eventsourced.core._
import scalaz._
import Scalaz._

@RunWith(classOf[JUnitRunner])
class StudyAnnotationTypeSpec extends StudyFixture with Tags {
  args(
    //include = "tag1",
    sequential = true) // forces all tests to be run sequentially

  val nameGenerator = new NameGenerator(classOf[StudyAnnotationTypeSpec].getSimpleName)
  val studyName = nameGenerator.next[Study]
  val study = await(studyService.addStudy(new AddStudyCmd(studyName, studyName))) | null

  "Collection event annotation type" can {

    "be added" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.String
      val maxValueCount = 0

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          valueType, maxValueCount, Map.empty[String, String])))

      at1 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name)
          x.description must be(name)
          x.valueType must beEqualTo(valueType)
          x.maxValueCount must beEqualTo(maxValueCount)
          annotationTypeRepo.getMap must haveKey(x.id)
      }

      val name2 = nameGenerator.next[AnnotationType]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = 2
      val options = Map("1" -> "a", "2" -> "b")

      val at2 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name2, name2,
          valueType2, maxValueCount2, options)))

      at2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name2)
          x.description must be(name2)
          x.valueType must beEqualTo(valueType2)
          x.maxValueCount must beEqualTo(maxValueCount2)
          annotationTypeRepo.getMap must haveKey(x.id)
      }
    }

    "not be added if name already exists" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.String
      val maxValueCount = 0

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          valueType, maxValueCount, Map.empty[String, String]))) | null
      annotationTypeRepo.getMap must haveKey(at1.id)

      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = 2
      val options = Map("1" -> "a", "2" -> "b")

      val at2 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          valueType2, maxValueCount2, options)))

      at2 must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }
    }

    "be updated" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.String
      val maxValueCount = 0

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          valueType, maxValueCount, Map.empty[String, String]))) | null

      val name2 = nameGenerator.next[Study]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = 2
      val options = Map("1" -> "a", "2" -> "b")

      val at2 = await(studyService.updateCollectionEventAnnotationType(
        new UpdateCollectionEventAnnotationTypeCmd(study.id.toString,
          at1.id.toString, at1.versionOption, name2, name2, valueType2,
          maxValueCount2, options)))

      at2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(at1.version + 1)
          x.name must be(name2)
          x.description must be(name2)
          x.valueType must beEqualTo(valueType2)
          x.maxValueCount must beEqualTo(maxValueCount2)
          annotationTypeRepo.getMap must haveKey(x.id)
      }
    }

    "not be updated to name that already exists" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.String
      val maxValueCount = 0

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          valueType, maxValueCount, Map.empty[String, String]))) | null
      annotationTypeRepo.getMap must haveKey(at1.id)

      val name2 = nameGenerator.next[AnnotationType]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = 2
      val options = Map("1" -> "a", "2" -> "b")

      val at2 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name2, name2,
          valueType2, maxValueCount2, options))) | null
      annotationTypeRepo.getMap must haveKey(at2.id)

      val at3 = await(studyService.updateCollectionEventAnnotationType(
        new UpdateCollectionEventAnnotationTypeCmd(study.id.toString,
          at2.id.toString, at2.versionOption, name, name, valueType,
          maxValueCount, options)))
      at3 must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }
    } tag ("tag1")

    "not be updated to wrong study" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.String
      val maxValueCount = 0

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          valueType, maxValueCount, Map.empty[String, String]))) | null

      val name2 = nameGenerator.next[Study]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = 2
      val options = Map("1" -> "a", "2" -> "b")

      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, name2))) | null

      val at2 = await(studyService.updateCollectionEventAnnotationType(
        new UpdateCollectionEventAnnotationTypeCmd(study2.id.toString,
          at1.id.toString, at1.versionOption, name2, name2, valueType2,
          maxValueCount2, options)))
      at2 must beFailing.like { case msgs => msgs.head must contain("does not belong to study") }
    }

    "not be updated with invalid version" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.String
      val maxValueCount = 0

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          valueType, maxValueCount, Map.empty[String, String]))) | null

      val name2 = nameGenerator.next[Study]
      val valueType2 = AnnotationValueType.Select
      val maxValueCount2 = 2
      val options = Map("1" -> "a", "2" -> "b")
      val versionOption = Some(at1.version + 1)

      val at2 = await(studyService.updateCollectionEventAnnotationType(
        new UpdateCollectionEventAnnotationTypeCmd(study.id.toString,
          at1.id.toString, versionOption, name2, name2, valueType2,
          maxValueCount2, options)))
      at2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }

    "be removed" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.String
      val maxValueCount = 0

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          valueType, maxValueCount, Map.empty[String, String]))) | null
      annotationTypeRepo.getMap must haveKey(at1.id)

      val at2 = await(studyService.removeCollectionEventAnnotationType(
        new RemoveCollectionEventAnnotationTypeCmd(study.id.toString,
          at1.id.toString, at1.versionOption)))

      at2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(at1.version)
          x.name must be(name)
          x.description must be(name)
          x.valueType must beEqualTo(valueType)
          x.maxValueCount must beEqualTo(maxValueCount)
          annotationTypeRepo.getMap must not haveKey (at1.id)
      }
    }

    "not be removed with invalid version" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.String
      val maxValueCount = 0

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          valueType, maxValueCount, Map.empty[String, String]))) | null
      annotationTypeRepo.getMap must haveKey(at1.id)

      val versionOption = Some(at1.version + 1)
      val at2 = await(studyService.removeCollectionEventAnnotationType(
        new RemoveCollectionEventAnnotationTypeCmd(study.id.toString,
          at1.id.toString, versionOption)))
      at2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }
  }

}