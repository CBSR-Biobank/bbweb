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

  val studyName = nameGenerator.next[Study]
  val study = await(studyService.addStudy(new AddStudyCmd(studyName, studyName))) | null

  "Study annotation type" can {

    "can be added" in {
      fragmentName: String =>
        val ng = new NameGenerator(fragmentName)
        val name = ng.next[Study]
        val valueType = AnnotationValueType.String
        val maxValueCount = 0

        val cet1 = await(studyService.addCollectionEventType(
          new AddCollectionEventTypeCmd(study.id.toString, name, name, true))) | null

        val at1 = await(studyService.addCollectionEventAnnotationType(
          new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
            valueType, maxValueCount)))

        at1 must beSuccessful.like {
          case x =>
            x.version must beEqualTo(0)
            x.name must be(name)
            x.description must be(name)
            x.valueType must beEqualTo(valueType)
            x.maxValueCount must beEqualTo(maxValueCount)
            annotationTypeRepo.getMap must haveKey(x.id)
        }

        val name2 = ng.next[Study]
        val valueType2 = AnnotationValueType.Select
        val maxValueCount2 = 2

        val at2 = await(studyService.addCollectionEventAnnotationType(
          new AddCollectionEventAnnotationTypeCmd(study.id.toString, name2, name2,
            valueType2, maxValueCount2)))

        at2 must beSuccessful.like {
          case x =>
            x.version must beEqualTo(0)
            x.name must be(name)
            x.description must be(name)
            x.valueType must beEqualTo(valueType)
            x.maxValueCount must beEqualTo(maxValueCount)
            annotationTypeRepo.getMap must haveKey(x.id)
        }
    }
  }

}