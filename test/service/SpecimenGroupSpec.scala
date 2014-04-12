package service

import fixture._
import org.biobank.infrastructure._
import org.biobank.domain.{
  AnatomicalSourceType,
  AnnotationTypeId,
  AnnotationValueType,
  ConcurrencySafeEntity,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import AnnotationValueType._
import org.biobank.domain.study._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.actor._

import scalaz._
import Scalaz._

@RunWith(classOf[JUnitRunner])
class SpecimenGroupSpec {
  //
  //  args(
  //    //include = "tag1",
  //    sequential = true) // forces all tests to be run sequentially
  //
  //  val nameGenerator = new NameGenerator(classOf[SpecimenGroupSpec].getName)
  //  val studyName = nameGenerator.next[Study]
  //  val studyEvent = await(studyService.addStudy(new AddStudyCmd(studyName, Some(studyName)))) | null
  //  val studyId = StudyId(studyEvent.id)
  //
  //  "Specimen group" can {
  //
  //    "be added" in {
  //      val name = nameGenerator.next[Study]
  //      val units = nameGenerator.next[String]
  //      val anatomicalSourceType = AnatomicalSourceType.Blood
  //      val preservationType = PreservationType.FreshSpecimen
  //      val preservationTempType = PreservationTemperatureType.Minus80celcius
  //      val specimenType = SpecimenType.FilteredUrine
  //
  //      val sg1 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
  //          preservationType, preservationTempType, specimenType)))
  //
  //      sg1 must beSuccessful.like {
  //        case x =>
  //          x.name must be(name)
  //          x.description must beSome(name)
  //          x.units must be(units)
  //          x.anatomicalSourceType must be(anatomicalSourceType)
  //          x.preservationType must be(preservationType)
  //          x.preservationTemperatureType must be(preservationTempType)
  //          x.specimenType must be(specimenType)
  //          specimenGroupRepository.specimenGroupWithId(studyId, x.specimenGroupId) must beSuccessful.like {
  //            case s =>
  //              s.version must beEqualTo(x.version)
  //          }
  //          specimenGroupRepository.allSpecimenGroupsForStudy(studyId).size mustEqual 1
  //      }
  //
  //      val name2 = nameGenerator.next[Study]
  //      val sg2 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name2, None, units, anatomicalSourceType,
  //          preservationType, preservationTempType, specimenType)))
  //
  //      sg2 must beSuccessful.like {
  //        case x =>
  //          x.name must be(name2)
  //          x.description must beNone
  //          x.units must be(units)
  //          x.anatomicalSourceType must be(anatomicalSourceType)
  //          x.preservationType must be(preservationType)
  //          x.preservationTemperatureType must be(preservationTempType)
  //          x.specimenType must be(specimenType)
  //          specimenGroupRepository.specimenGroupWithId(studyId, x.specimenGroupId) must beSuccessful.like {
  //            case s =>
  //              s.version must beEqualTo(x.version)
  //          }
  //          specimenGroupRepository.allSpecimenGroupsForStudy(studyId).size mustEqual 2
  //      }
  //    }
  //
  //    "be updated" in {
  //      val name = nameGenerator.next[Study]
  //      val units = nameGenerator.next[String]
  //      val anatomicalSourceType = AnatomicalSourceType.Blood
  //      val preservationType = PreservationType.FreshSpecimen
  //      val preservationTempType = PreservationTemperatureType.Minus80celcius
  //      val specimenType = SpecimenType.FilteredUrine
  //
  //      val sg1 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
  //          preservationType, preservationTempType, specimenType))) | null
  //
  //      val name2 = nameGenerator.next[Study]
  //      val units2 = nameGenerator.next[String]
  //      val anatomicalSourceType2 = AnatomicalSourceType.Brain
  //      val preservationType2 = PreservationType.FrozenSpecimen
  //      val preservationTempType2 = PreservationTemperatureType.Minus180celcius
  //      val specimenType2 = SpecimenType.DnaBlood
  //
  //      val sg2 = await(studyService.updateSpecimenGroup(
  //        new UpdateSpecimenGroupCmd(sg1.specimenGroupId, Some(sg1.version), studyId.id, name2,
  //          Some(name2), units2, anatomicalSourceType2, preservationType2, preservationTempType2,
  //          specimenType2)))
  //
  //      sg2 must beSuccessful.like {
  //        case x =>
  //          // FIXME x.version must beEqualTo(sg1.version + 1)
  //          x.name must be(name2)
  //          x.description must beSome(name2)
  //          x.units must be(units2)
  //          x.anatomicalSourceType must be(anatomicalSourceType2)
  //          x.preservationType must be(preservationType2)
  //          x.preservationTemperatureType must be(preservationTempType2)
  //          x.specimenType must be(specimenType2)
  //          specimenGroupRepository.specimenGroupWithId(studyId, x.specimenGroupId) must beSuccessful.like {
  //            case s =>
  //              s.version must beEqualTo(x.version)
  //          }
  //      }
  //    }
  //
  //    "not be updated with invalid version" in {
  //      val name = nameGenerator.next[Study]
  //      val units = nameGenerator.next[String]
  //      val anatomicalSourceType = AnatomicalSourceType.Blood
  //      val preservationType = PreservationType.FreshSpecimen
  //      val preservationTempType = PreservationTemperatureType.Minus80celcius
  //      val specimenType = SpecimenType.FilteredUrine
  //
  //      val sg1 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
  //          preservationType, preservationTempType, specimenType))) | null
  //
  //      val name2 = nameGenerator.next[Study]
  //      val units2 = nameGenerator.next[String]
  //      val anatomicalSourceType2 = AnatomicalSourceType.Brain
  //      val preservationType2 = PreservationType.FrozenSpecimen
  //      val preservationTempType2 = PreservationTemperatureType.Minus180celcius
  //      val specimenType2 = SpecimenType.DnaBlood
  //      val versionOption = Some(1L)
  //
  //      val sg2 = await(studyService.updateSpecimenGroup(
  //        new UpdateSpecimenGroupCmd(sg1.specimenGroupId, versionOption, studyId.id, name2,
  //          None, units2, anatomicalSourceType2, preservationType2, preservationTempType2,
  //          specimenType2)))
  //      sg2 must beFailing.like {
  //        case msgs => msgs.head must contain("doesn't match current version")
  //      }
  //    }
  //
  //    "not be added if name already exists" in {
  //      val name = nameGenerator.next[Study]
  //      val units = nameGenerator.next[String]
  //      val anatomicalSourceType = AnatomicalSourceType.Blood
  //      val preservationType = PreservationType.FreshSpecimen
  //      val preservationTempType = PreservationTemperatureType.Minus80celcius
  //      val specimenType = SpecimenType.FilteredUrine
  //
  //      val sg1 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
  //          preservationType, preservationTempType, specimenType))) | null
  //      specimenGroupRepository.specimenGroupWithId(studyId, sg1.specimenGroupId) must beSuccessful
  //
  //      val sg2 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
  //          preservationType, preservationTempType, specimenType)))
  //      sg2 must beFailing.like {
  //        case msgs => msgs.head must contain("name already exists")
  //      }
  //    }
  //
  //    "not be updated to name that already exists" in {
  //      val name = nameGenerator.next[Study]
  //      val units = nameGenerator.next[String]
  //      val anatomicalSourceType = AnatomicalSourceType.Blood
  //      val preservationType = PreservationType.FreshSpecimen
  //      val preservationTempType = PreservationTemperatureType.Minus80celcius
  //      val specimenType = SpecimenType.FilteredUrine
  //
  //      val sg1 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
  //          preservationType, preservationTempType, specimenType))) | null
  //      specimenGroupRepository.specimenGroupWithId(studyId, sg1.specimenGroupId) must beSuccessful
  //
  //      val name2 = nameGenerator.next[Study]
  //      val units2 = nameGenerator.next[String]
  //      val anatomicalSourceType2 = AnatomicalSourceType.Brain
  //      val preservationType2 = PreservationType.FrozenSpecimen
  //      val preservationTempType2 = PreservationTemperatureType.Minus180celcius
  //      val specimenType2 = SpecimenType.DnaBlood
  //
  //      val sg2 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name2, None, units2, anatomicalSourceType2,
  //          preservationType2, preservationTempType2, specimenType2))) | null
  //      specimenGroupRepository.specimenGroupWithId(studyId, sg2.specimenGroupId) must beSuccessful
  //
  //      val sg3 = await(studyService.updateSpecimenGroup(
  //        new UpdateSpecimenGroupCmd(sg2.specimenGroupId, Some(sg2.version), studyId.id, name,
  //          None, units, anatomicalSourceType, preservationType, preservationTempType,
  //          specimenType)))
  //      sg3 must beFailing.like {
  //        case msgs => msgs.head must contain("name already exists")
  //      }
  //    } tag ("tag1")
  //
  //    "not be updated to wrong study" in {
  //      val name = nameGenerator.next[Study]
  //      val units = nameGenerator.next[String]
  //      val anatomicalSourceType = AnatomicalSourceType.Blood
  //      val preservationType = PreservationType.FreshSpecimen
  //      val preservationTempType = PreservationTemperatureType.Minus80celcius
  //      val specimenType = SpecimenType.FilteredUrine
  //
  //      val sg1 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
  //          preservationType, preservationTempType, specimenType))) | null
  //      specimenGroupRepository.specimenGroupWithId(studyId, sg1.specimenGroupId) must beSuccessful
  //
  //      val study2 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null
  //
  //      val sg2 = await(studyService.updateSpecimenGroup(
  //        new UpdateSpecimenGroupCmd(sg1.specimenGroupId, Some(1L), study2.id.toString,
  //          name, Some(name), units, anatomicalSourceType, preservationType, preservationTempType,
  //          specimenType)))
  //      sg2 must beFailing.like {
  //        case msgs => msgs.head must contain("study does not have specimen group")
  //      }
  //    }
  //
  //    "be removed" in {
  //      val name = nameGenerator.next[Study]
  //      val units = nameGenerator.next[String]
  //      val anatomicalSourceType = AnatomicalSourceType.Blood
  //      val preservationType = PreservationType.FreshSpecimen
  //      val preservationTempType = PreservationTemperatureType.Minus80celcius
  //      val specimenType = SpecimenType.FilteredUrine
  //
  //      val sg1 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
  //          preservationType, preservationTempType, specimenType))) | null
  //      specimenGroupRepository.specimenGroupWithId(studyId, sg1.specimenGroupId) must beSuccessful
  //
  //      await(studyService.removeSpecimenGroup(
  //        new RemoveSpecimenGroupCmd(sg1.specimenGroupId, Some(sg1.version), studyId.id)))
  //      specimenGroupRepository.specimenGroupWithId(studyId, sg1.specimenGroupId) must beFailing
  //    }
  //
  //    "not be removed with invalid version" in {
  //      val name = nameGenerator.next[Study]
  //      val units = nameGenerator.next[String]
  //      val anatomicalSourceType = AnatomicalSourceType.Blood
  //      val preservationType = PreservationType.FreshSpecimen
  //      val preservationTempType = PreservationTemperatureType.Minus80celcius
  //      val specimenType = SpecimenType.FilteredUrine
  //
  //      val sg1 = await(studyService.addSpecimenGroup(
  //        new AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
  //          preservationType, preservationTempType, specimenType))) | null
  //      specimenGroupRepository.specimenGroupWithId(studyId, sg1.specimenGroupId) must beSuccessful
  //
  //      val versionOption = Some(1L)
  //      val sg2 = await(studyService.removeSpecimenGroup(
  //        new RemoveSpecimenGroupCmd(sg1.specimenGroupId, versionOption, studyId.id)))
  //      sg2 must beFailing.like {
  //        case msgs => msgs.head must contain("doesn't match current version")
  //      }
  //    }
  //  }
}