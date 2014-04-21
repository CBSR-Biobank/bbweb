package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  AnatomicalSourceType,
  AnnotationTypeId,
  AnnotationValueType,
  ConcurrencySafeEntity,
  DomainError,
  DomainValidation,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import AnnotationValueType._
import org.biobank.domain.study._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._

import akka.pattern.ask
import scalaz._
import scalaz.Scalaz._

class SpecimenGroupProcessorSpec extends StudyProcessorFixture {

  val nameGenerator = new NameGenerator(this.getClass)

  var disabledStudy: DisabledStudy = null

  // create the study to be used for tests
  override def beforeAll: Unit = {
    val name = nameGenerator.next[Study]
    disabledStudy = DisabledStudy.create(studyRepository.nextIdentity, -1, name, None) | fail
    studyRepository.put(disabledStudy)
  }


  "A study processor" can {

    "add a specimen group" in {
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      var cmd = AddSpecimenGroupCmd(disabledStudy.id.id, name, description, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)

      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupAddedEvent]]
	.futureValue
      validation should be success

      validation map { event =>
	event should have (
          'name                        (name),
          'description                 (description),
          'units                       (units),
          'anatomicalSourceType        (anatomicalSourceType),
          'preservationType            (preservationType),
          'preservationTemperatureType (preservationTempType),
          'specimenType                (specimenType)
	)

        val sg = specimenGroupRepository.specimenGroupWithId(
	  disabledStudy.id, SpecimenGroupId(event.specimenGroupId)) | fail
        sg.version should be (0)
        specimenGroupRepository.allSpecimenGroupsForStudy(disabledStudy.id) should have size 1
      }

      val name2 = nameGenerator.next[Study]

     cmd = AddSpecimenGroupCmd(disabledStudy.id.id, name2, None, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)
      val validation2 = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupAddedEvent]]
	.futureValue
      validation2 should be success

      validation2 map { event =>
	event should have (
          'name                        (name2),
          'description                 (None),
          'units                       (units),
          'anatomicalSourceType        (anatomicalSourceType),
          'preservationType            (preservationType),
          'preservationTemperatureType (preservationTempType),
          'specimenType                (specimenType)
	)

        val sg = specimenGroupRepository.specimenGroupWithId(
	  disabledStudy.id, SpecimenGroupId(event.specimenGroupId)) | fail
        sg.version should be (0)
        specimenGroupRepository.allSpecimenGroupsForStudy(disabledStudy.id) should have size 2
      }
    }

    "update a specimen group" in {
      val sgId = specimenGroupRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val item = SpecimenGroup.create(disabledStudy.id, sgId, -1L, name, description, units,
	anatomicalSourceType, preservationType, preservationTempType, specimenType) | fail
      specimenGroupRepository.put(item)

      val name2 = nameGenerator.next[Study]
      val units2 = nameGenerator.next[String]
      val anatomicalSourceType2 = AnatomicalSourceType.Brain
      val preservationType2 = PreservationType.FrozenSpecimen
      val preservationTempType2 = PreservationTemperatureType.Minus180celcius
      val specimenType2 = SpecimenType.DnaBlood

      val cmd = new UpdateSpecimenGroupCmd(disabledStudy.id.id, item.id.id,
	Some(item.version), name2, None, units2, anatomicalSourceType2, preservationType2,
	preservationTempType2, specimenType2)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
	.futureValue
      validation should be success

      validation map { event =>
	event should have (
	  'studyId                     (disabledStudy.id.id),
	  'specimenGroupId             (item.id.id),
	  'version                     (item.version + 1),
          'name                        (name2),
          'description                 (None),
          'units                       (units2),
          'anatomicalSourceType        (anatomicalSourceType2),
          'preservationType            (preservationType2),
          'preservationTemperatureType (preservationTempType2),
          'specimenType                (specimenType2)
	)

        val sg = specimenGroupRepository.specimenGroupWithId(
	  disabledStudy.id, SpecimenGroupId(event.specimenGroupId)) | fail
        sg.version should be (item.version + 1)
      }
    }

    "not update a specimen group with an invalid version" in {
      val sgId = specimenGroupRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val item = SpecimenGroup.create(disabledStudy.id, sgId, -1L, name, description, units,
	anatomicalSourceType, preservationType, preservationTempType, specimenType) | fail
      specimenGroupRepository.put(item)

      val cmd = new UpdateSpecimenGroupCmd(disabledStudy.id.id, item.id.id,
	Some(-1L), name, None, units, anatomicalSourceType, preservationType,
	preservationTempType, specimenType)

      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
	.futureValue
      validation should be failure

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("doesn't match current version")
      }
    }

    "not be added if the name already exists" in {
      val sgId = specimenGroupRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val item = SpecimenGroup.create(disabledStudy.id, sgId, -1L, name, description, units,
	anatomicalSourceType, preservationType, preservationTempType, specimenType) | fail
      specimenGroupRepository.put(item)

      val cmd = AddSpecimenGroupCmd(disabledStudy.id.id, name, None, units, anatomicalSourceType,
        preservationType, preservationTempType, specimenType)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupAddedEvent]]
	.futureValue
      validation should be failure

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("name already exists")
      }
    }

    "not be updated to name that already exists" in {
      val sgId = specimenGroupRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val item = SpecimenGroup.create(disabledStudy.id, sgId, -1L, name, description, units,
	anatomicalSourceType, preservationType, preservationTempType, specimenType) | fail
      specimenGroupRepository.put(item)

      val units2 = nameGenerator.next[String]
      val anatomicalSourceType2 = AnatomicalSourceType.Brain
      val preservationType2 = PreservationType.FrozenSpecimen
      val preservationTempType2 = PreservationTemperatureType.Minus180celcius
      val specimenType2 = SpecimenType.DnaBlood

      val cmd = new UpdateSpecimenGroupCmd(disabledStudy.id.id, item.id.id,
	Some(item.version), name, None, units2, anatomicalSourceType2, preservationType2,
	preservationTempType2, specimenType2)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
	.futureValue
      validation should be failure

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("name already exists")
      }
    }

    "not be updated to wrong study" in {
      val sgId = specimenGroupRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val item = SpecimenGroup.create(disabledStudy.id, sgId, -1L, name, description, units,
	anatomicalSourceType, preservationType, preservationTempType, specimenType) | fail
      specimenGroupRepository.put(item)

      val studyName = nameGenerator.next[Study]
      val study2 = DisabledStudy.create(studyRepository.nextIdentity, -1, studyName, None) | fail
      studyRepository.put(study2)

      val cmd = new UpdateSpecimenGroupCmd(study2.id.id, item.id.id,
	Some(item.version), name, description, units, anatomicalSourceType, preservationType,
	preservationTempType, specimenType)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
	.futureValue
      validation should be failure

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("study does not have specimen group")
      }
    }

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

  }
}
