package service

import fixture._
import infrastructure._
import service.commands._
import domain.{ AnatomicalSourceType, PreservationType, PreservationTemperatureType, SpecimenType }
import domain.AnnotationValueType._
import domain.study._

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
class StudyServiceSpec extends StudyFixture with Tags {
  args(
    //include = "tag1",
    sequential = true) // forces all tests to be run sequentially

  val nameGenerator = new NameGenerator(classOf[StudyServiceSpec].getName)

  "Study" can {

    "be added" in {
      val name = nameGenerator.next[Study]
      val study = await(studyService.addStudy(new AddStudyCmd(name, Some(name))))
      studyRepository.getMap must not be empty
      study must beSuccessful.like {
        case s =>
          s.version must beEqualTo(0)
          s.name must be(name)
          s.description must beSome(name)
          studyRepository.getMap must haveKey(s.id)
      }
    } tag ("tag1")

    "not be added if same name exists" in {
      val name = nameGenerator.next[Study]
      await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) must beSuccessful

      await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }
    }

    "be updated" in {
      val name = nameGenerator.next[Study]
      val study1 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null

      val name2 = nameGenerator.next[Study]
      val study2 = await(studyService.updateStudy(
        new UpdateStudyCmd(study1.id.toString, study1.versionOption, name2, None)))

      study2 must beSuccessful.like {
        case s =>
          s.version must beEqualTo(study1.version + 1)
          s.name must be(name2)
          s.description must beNone
          studyRepository.getMap must haveKey(s.id)

          // update something other than the name
          val study3 = await(studyService.updateStudy(
            new UpdateStudyCmd(study1.id.toString, Some(s.version), name2)))
          study3 must beSuccessful
      }
    }

    "not be updated to name that exists" in {
      val name = nameGenerator.next[Study]
      val study1 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null

      val name2 = nameGenerator.next[Study]
      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, Some(name2)))) | null

      val study3 = await(studyService.updateStudy(
        new UpdateStudyCmd(study2.id.toString, study2.versionOption, name, Some(name))))
      study3 must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }

    }

    "not be updated with invalid version" in {
      val name = nameGenerator.next[Study]
      val study1 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null

      val name2 = nameGenerator.next[Study]
      val versionOption = Some(study1.version + 1)
      val study2 = await(studyService.updateStudy(
        new UpdateStudyCmd(study1.id.toString, versionOption, name2, None)))

      study2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }

    "be enabled" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val study1 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null

      await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study1.id.toString, name, Some(name), true,
          Set.empty, Set.empty)))

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

      val study1 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null

      await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study1.id.toString, name, Some(name), true,
          Set.empty, Set.empty)))

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
  }
}
