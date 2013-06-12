import test._
import fixture._
import infrastructure._
import infrastructure.commands._
import service.{ StudyService, StudyProcessor }
import domain.{
  AnatomicalSourceType,
  AnnotationTypeId,
  AnnotationValueType,
  ConcurrencySafeEntity,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import AnnotationValueType._
import domain.study._

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
class StudyServiceSpec extends StudyFixture {
  sequential // forces all tests to be run sequentially

  val nameGenerator = new NameGenerator(classOf[StudyServiceSpec].getName)

  "Study" can {

    "be added" in {
      val name = nameGenerator.next[Study]
      val study = await(studyService.addStudy(new AddStudyCmd(name, name)))
      studyRepository.getMap must not be empty
      study must beSuccessful.like {
        case s =>
          s.version must beEqualTo(0)
          s.name must be(name)
          s.description must be(name)
          studyRepository.getMap must haveKey(s.id)
      }
    }

    "be updated" in {
      val name = nameGenerator.next[Study]
      val study1 = await(studyService.addStudy(new AddStudyCmd(name, name))) | null

      val name2 = nameGenerator.next[Study]
      val study2 = await(studyService.updateStudy(
        new UpdateStudyCmd(study1.id.toString, study1.versionOption, name2, name2)))

      studyRepository.getMap must not be empty
      study2 must beSuccessful.like {
        case s =>
          studyRepository.getMap must haveKey(s.id)
          s.version must beEqualTo(study1.version + 1)
          s.name must be(name2)
          s.description must be(name2)
      }
    }

    "be enabled" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val study1 = await(studyService.addStudy(new AddStudyCmd(name, name))) | null

      await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name, name, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      await(studyService.enableStudy(
        new EnableStudyCmd(study1.id.toString, study1.versionOption)))

      studyRepository.getByKey(study1.id) must beSuccessful.like {
        case s => s must beAnInstanceOf[EnabledStudy]
      }
    }

    "be disabled" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val study1 = await(studyService.addStudy(new AddStudyCmd(name, name))) | null

      await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name, name, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      val study2 = await(studyService.enableStudy(
        new EnableStudyCmd(study1.id.toString, study1.versionOption))) | null

      studyRepository.getByKey(study2.id) must beSuccessful.like {
        case s => s must beAnInstanceOf[EnabledStudy]
      }

      await(studyService.disableStudy(
        new DisableStudyCmd(study2.id.toString, study2.versionOption)))

      studyRepository.getByKey(study1.id) must beSuccessful.like {
        case s => s must beAnInstanceOf[DisabledStudy]
      }
    }

    "not be added if same name exists" in {
      val name = nameGenerator.next[Study]
      await(studyService.addStudy(new AddStudyCmd(name, name))) must beSuccessful

      await(studyService.addStudy(new AddStudyCmd(name, name))) must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }
    }
  }
}
