package org.biobank.service.study

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
import org.biobank.infrastructure.command.StudyCommands._

import akka.pattern.ask
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfterEach

/**
  * Tests for actor SpecimenGroupProcessor. These are written using ScalaTest.
  *
  */
class SpecimenGroupProcessorSpec extends TestFixture {

  import org.biobank.TestUtils._

  val nameGenerator = new NameGenerator(this.getClass)

  var disabledStudy: DisabledStudy = null

  // create the study to be used for tests
  override def beforeEach: Unit = {
    disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)
    ()
  }


  "A study processor" can {

    "add a specimen group" in {
      val sg = factory.createSpecimenGroup

      var cmd = AddSpecimenGroupCmd(
        None, disabledStudy.id.id, sg.name, sg.description, sg.units,
        sg.anatomicalSourceType, sg.preservationType, sg.preservationTemperatureType, sg.specimenType)

      var v = ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (sg.studyId.id)

        val addedEvent = event.getSpecimenGroupAdded
        addedEvent must have (
          'name                        (Some(sg.name)),
          'description                 (sg.description),
          'units                       (Some(sg.units)),
          'anatomicalSourceType        (Some(sg.anatomicalSourceType.toString)),
          'preservationType            (Some(sg.preservationType.toString)),
          'preservationTemperatureType (Some(sg.preservationTemperatureType.toString)),
          'specimenType                (Some(sg.specimenType.toString))
        )

        specimenGroupRepository.allForStudy(disabledStudy.id) must have size 1
        specimenGroupRepository.withId(
          disabledStudy.id,
          SpecimenGroupId(addedEvent.getSpecimenGroupId))
        .mustSucceed { repoSg =>
          repoSg.version mustBe (0)
          checkTimeStamps(repoSg, DateTime.now, None)
        }
      }

      val name2 = nameGenerator.next[Study]

      cmd = AddSpecimenGroupCmd(
        None, disabledStudy.id.id, name2, None, sg.units, sg.anatomicalSourceType,
        sg.preservationType, sg.preservationTemperatureType, sg.specimenType)
      v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (sg.studyId.id)

        val addedEvent = event.getSpecimenGroupAdded
        addedEvent must have (
          'name                        (Some(name2)),
          'description                 (None),
          'units                       (Some(sg.units)),
          'anatomicalSourceType        (Some(sg.anatomicalSourceType.toString)),
          'preservationType            (Some(sg.preservationType.toString)),
          'preservationTemperatureType (Some(sg.preservationTemperatureType.toString)),
          'specimenType                (Some(sg.specimenType.toString))
        )

        specimenGroupRepository.allForStudy(disabledStudy.id) must have size 2
        specimenGroupRepository.withId(
          disabledStudy.id,
          SpecimenGroupId(addedEvent.getSpecimenGroupId))
        .mustSucceed { repoSg  =>
          repoSg.version mustBe (0)
          checkTimeStamps(repoSg, DateTime.now, None)
        }
      }
    }

    "not add a specimen group to a study that does not exist" in {
      val study2 = factory.createDisabledStudy
      val sg = factory.createSpecimenGroup

      var cmd = AddSpecimenGroupCmd(
        None, study2.id.id, sg.name, sg.description, sg.units,
        sg.anatomicalSourceType, sg.preservationType, sg.preservationTemperatureType, sg.specimenType)

      val v = ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFail s"invalid study id: ${study2.id.id}"
    }

    "update a specimen group" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val sg2 = factory.createSpecimenGroup

      val cmd = new UpdateSpecimenGroupCmd(
        None, disabledStudy.id.id, sg.id.id, sg.version,
        sg2.name, sg2.description, sg2.units, sg2.anatomicalSourceType, sg2.preservationType,
        sg2.preservationTemperatureType, sg2.specimenType)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (sg.studyId.id)

        val addedEvent = event.getSpecimenGroupUpdated
        addedEvent must have (
          'specimenGroupId             (Some(sg.id.id)),
          'version                     (Some(sg.version + 1)),
          'name                        (Some(sg2.name)),
          'description                 (sg2.description),
          'units                       (Some(sg2.units)),
          'anatomicalSourceType        (Some(sg2.anatomicalSourceType.toString)),
          'preservationType            (Some(sg2.preservationType.toString)),
          'preservationTemperatureType (Some(sg2.preservationTemperatureType.toString)),
          'specimenType                (Some(sg2.specimenType.toString))
        )

        specimenGroupRepository.allForStudy(disabledStudy.id) must have size 1
        specimenGroupRepository.withId(
          disabledStudy.id,
          SpecimenGroupId(addedEvent.getSpecimenGroupId))
        .mustSucceed { repoSg =>
          repoSg.version mustBe (sg.version + 1)
          checkTimeStamps(repoSg, sg.timeAdded, DateTime.now)
        }
      }
    }

    "not update a specimen group with an invalid version" in {
      val item = factory.createSpecimenGroup
      specimenGroupRepository.put(item)

      val cmd = new UpdateSpecimenGroupCmd(
        None, disabledStudy.id.id, item.id.id, -1L, item.name,
        item.description, item.units, item.anatomicalSourceType, item.preservationType,
        item.preservationTemperatureType, item.specimenType)

      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFailContains "doesn't match current version"
    }

    "not be added if the name already exists" in {
      val item = factory.createSpecimenGroup
      specimenGroupRepository.put(item)

      val cmd = AddSpecimenGroupCmd(
        None, disabledStudy.id.id, item.name,
        item.description, item.units, item.anatomicalSourceType, item.preservationType,
        item.preservationTemperatureType, item.specimenType)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustFailContains "name already exists"
    }

    "not be updated to name that already exists" in {
      val sg1 = factory.createSpecimenGroup
      specimenGroupRepository.put(sg1)

      val sg2 = factory.createSpecimenGroup
      specimenGroupRepository.put(sg2)

      val sg3 = factory.createSpecimenGroup

      val cmd = new UpdateSpecimenGroupCmd(
        None, disabledStudy.id.id, sg2.id.id, sg2.version,
        sg1.name, sg1.description, sg1.units, sg1.anatomicalSourceType, sg1.preservationType,
        sg1.preservationTemperatureType, sg1.specimenType)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFailContains "name already exists"
    }

    "not be updated to wrong study" in {
      val item = factory.createSpecimenGroup
      specimenGroupRepository.put(item)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cmd = new UpdateSpecimenGroupCmd(
        None, study2.id.id, item.id.id, item.version, item.name,
        item.description, item.units, item.anatomicalSourceType, item.preservationType,
        item.preservationTemperatureType, item.specimenType)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFailContains "study does not have specimen group"
    }

    "can remove a specimen group" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cmd = new RemoveSpecimenGroupCmd(None, disabledStudy.id.id, sg.id.id, sg.version)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue

      v mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (sg.studyId.id)

        val removedEvent = event.getSpecimenGroupRemoved

        val v2 = specimenGroupRepository.withId(
          disabledStudy.id,
          SpecimenGroupId(removedEvent.getSpecimenGroupId))
        v2 mustFailContains "specimen group does not exist"
      }
    }

    "not remove a specimen group with an invalid version" in {
      val item = factory.createSpecimenGroup
      specimenGroupRepository.put(item)

      val cmd = new RemoveSpecimenGroupCmd(None, disabledStudy.id.id, item.id.id, item.version - 10)
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFailContains "expected version doesn't match current version"
    }
  }
}
