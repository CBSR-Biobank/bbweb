import test._
import fixture._
import domain._
import domain.study._
import infrastructure.commands._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import org.specs2.specification.BeforeExample
import org.specs2.scalaz.ValidationMatchers._
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.execute.Result
import akka.actor._
import akka.util.Timeout
import org.eligosource.eventsourced.core._

import scalaz._
import Scalaz._

@RunWith(classOf[JUnitRunner])
class StudyAnnotationTypeSpec extends StudyFixture {
  sequential // forces all tests to be run sequentially

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

    "be removed" in {
      val name = nameGenerator.next[AnnotationType]
      val valueType = AnnotationValueType.String
      val maxValueCount = 0

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          valueType, maxValueCount, Map.empty[String, String]))) | null

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
  }

}