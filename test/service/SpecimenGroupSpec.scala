package service

import fixture._
import infrastructure._
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
import infrastructure._
import service.commands._

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
class SpecimenGroupSpec extends StudyFixture with Tags {

  args(
    //include = "tag1",
    sequential = true) // forces all tests to be run sequentially

  val nameGenerator = new NameGenerator(classOf[SpecimenGroupSpec].getName)
  val studyName = nameGenerator.next[Study]
  val study = await(studyService.addStudy(new AddStudyCmd(studyName, Some(studyName)))) | null

  "Specimen group" can {

    "be added" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      sg1 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name)
          x.description must beSome(name)
          x.units must be(units)
          x.anatomicalSourceType must be(anatomicalSourceType)
          x.preservationType must be(preservationType)
          x.preservationTemperatureType must be(preservationTempType)
          x.specimenType must be(specimenType)
          SpecimenGroupRepository.specimenGroupWithId(study.id, x.id) must beSuccessful
          SpecimenGroupRepository.allSpecimenGroupsForStudy(study.id).size mustEqual 1
      }

      val name2 = nameGenerator.next[Study]
      val sg2 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name2, None, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      sg2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name2)
          x.description must beNone
          x.units must be(units)
          x.anatomicalSourceType must be(anatomicalSourceType)
          x.preservationType must be(preservationType)
          x.preservationTemperatureType must be(preservationTempType)
          x.specimenType must be(specimenType)
          SpecimenGroupRepository.specimenGroupWithId(study.id, x.id) must beSuccessful
          SpecimenGroupRepository.allSpecimenGroupsForStudy(study.id).size mustEqual 2
      }
    }

    "be updated" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null

      val name2 = nameGenerator.next[Study]
      val units2 = nameGenerator.next[String]
      val anatomicalSourceType2 = AnatomicalSourceType.Brain
      val preservationType2 = PreservationType.FrozenSpecimen
      val preservationTempType2 = PreservationTemperatureType.Minus180celcius
      val specimenType2 = SpecimenType.DnaBlood

      val sg2 = await(studyService.updateSpecimenGroup(
        new UpdateSpecimenGroupCmd(sg1.id.toString, sg1.versionOption, study.id.toString, name2,
          Some(name2), units2, anatomicalSourceType2, preservationType2, preservationTempType2,
          specimenType2)))

      sg2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(sg1.version + 1)
          x.name must be(name2)
          x.description must beSome(name2)
          x.units must be(units2)
          x.anatomicalSourceType must be(anatomicalSourceType2)
          x.preservationType must be(preservationType2)
          x.preservationTemperatureType must be(preservationTempType2)
          x.specimenType must be(specimenType2)
      }
    } tag ("tag1")

    "not be updated with invalid version" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null

      val name2 = nameGenerator.next[Study]
      val units2 = nameGenerator.next[String]
      val anatomicalSourceType2 = AnatomicalSourceType.Brain
      val preservationType2 = PreservationType.FrozenSpecimen
      val preservationTempType2 = PreservationTemperatureType.Minus180celcius
      val specimenType2 = SpecimenType.DnaBlood
      val versionOption = Some(sg1.version + 1)

      val sg2 = await(studyService.updateSpecimenGroup(
        new UpdateSpecimenGroupCmd(sg1.id.toString, versionOption, study.id.toString, name2,
          None, units2, anatomicalSourceType2, preservationType2, preservationTempType2,
          specimenType2)))
      sg2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }

    "not be added if name already exists" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null
      SpecimenGroupRepository.specimenGroupWithId(study.id, sg1.id) must beSuccessful

      val sg2 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))
      sg2 must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }
    }

    "not be updated to name that already exists" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null
      SpecimenGroupRepository.specimenGroupWithId(study.id, sg1.id) must beSuccessful

      val name2 = nameGenerator.next[Study]
      val units2 = nameGenerator.next[String]
      val anatomicalSourceType2 = AnatomicalSourceType.Brain
      val preservationType2 = PreservationType.FrozenSpecimen
      val preservationTempType2 = PreservationTemperatureType.Minus180celcius
      val specimenType2 = SpecimenType.DnaBlood

      val sg2 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name2, None, units2, anatomicalSourceType2,
          preservationType2, preservationTempType2, specimenType2))) | null
      SpecimenGroupRepository.specimenGroupWithId(study.id, sg2.id) must beSuccessful

      val sg3 = await(studyService.updateSpecimenGroup(
        new UpdateSpecimenGroupCmd(sg2.id.toString, sg2.versionOption, study.id.toString, name,
          None, units, anatomicalSourceType, preservationType, preservationTempType,
          specimenType)))
      sg3 must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }
    }

    "not be updated to wrong study" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null
      SpecimenGroupRepository.specimenGroupWithId(study.id, sg1.id) must beSuccessful

      val study2 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null

      val sg2 = await(studyService.updateSpecimenGroup(
        new UpdateSpecimenGroupCmd(sg1.id.toString, sg1.versionOption, study2.id.toString,
          name, Some(name), units, anatomicalSourceType, preservationType, preservationTempType,
          specimenType)))
      sg2 must beFailing.like {
        case msgs => msgs.head must contain("study does not have specimen group")
      }
    }

    "be removed" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null
      SpecimenGroupRepository.specimenGroupWithId(study.id, sg1.id) must beSuccessful

      await(studyService.removeSpecimenGroup(
        new RemoveSpecimenGroupCmd(sg1.id.toString, sg1.versionOption, study.id.toString)))
      SpecimenGroupRepository.specimenGroupWithId(study.id, sg1.id) must beFailing
    }

    "not be removed with invalid version" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null
      SpecimenGroupRepository.specimenGroupWithId(study.id, sg1.id) must beSuccessful

      val versionOption = Some(sg1.version + 1)
      val sg2 = await(studyService.removeSpecimenGroup(
        new RemoveSpecimenGroupCmd(sg1.id.toString, versionOption, study.id.toString)))
      sg2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }
  }
}