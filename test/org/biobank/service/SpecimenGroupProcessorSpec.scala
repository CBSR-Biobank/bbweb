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
  RepositoryComponentImpl,
  SpecimenType
}
import AnnotationValueType._
import org.biobank.domain.study._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._

import akka.pattern.ask
import org.scalatest.BeforeAndAfterEach
import scalaz._
import scalaz.Scalaz._

class SpecimenGroupProcessorSpec extends StudyProcessorFixture {

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

      var cmd = AddSpecimenGroupCmd(disabledStudy.id.id, sg.name, sg.description, sg.units,
        sg.anatomicalSourceType, sg.preservationType, sg.preservationTemperatureType, sg.specimenType)

      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupAddedEvent]]
        .futureValue
      validation should be ('success)

      validation map { event =>
        event shouldBe a[SpecimenGroupAddedEvent]
        event should have (
          'name                        (sg.name),
          'description                 (sg.description),
          'units                       (sg.units),
          'anatomicalSourceType        (sg.anatomicalSourceType),
          'preservationType            (sg.preservationType),
          'preservationTemperatureType (sg.preservationTemperatureType),
          'specimenType                (sg.specimenType)
        )

        val validation2 = specimenGroupRepository.withId(
          disabledStudy.id, SpecimenGroupId(event.specimenGroupId))
        validation2 should be ('success)
        validation2 map { sg =>
          sg.version should be (0)
          specimenGroupRepository.allForStudy(disabledStudy.id) should have size 1
        }
      }

      val name2 = nameGenerator.next[Study]

      cmd = AddSpecimenGroupCmd(disabledStudy.id.id, name2, None, sg.units, sg.anatomicalSourceType,
        sg.preservationType, sg.preservationTemperatureType, sg.specimenType)
      val validation2 = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupAddedEvent]]
        .futureValue
      validation2 should be ('success)

      validation2 map { event =>
        event shouldBe a[SpecimenGroupAddedEvent]
        event should have (
          'name                        (name2),
          'description                 (None),
          'units                       (sg.units),
          'anatomicalSourceType        (sg.anatomicalSourceType),
          'preservationType            (sg.preservationType),
          'preservationTemperatureType (sg.preservationTemperatureType),
          'specimenType                (sg.specimenType)
        )

        val validation3 = specimenGroupRepository.withId(
          disabledStudy.id, SpecimenGroupId(event.specimenGroupId))
        validation3 should be ('success)
        validation3 map { sg  =>
          sg.version should be (0)
          specimenGroupRepository.allForStudy(disabledStudy.id) should have size 2
        }
      }
    }

    "not add a specimen group to a study that does not exist" in {
      val study2 = factory.createDisabledStudy
      val sg = factory.createSpecimenGroup

      var cmd = AddSpecimenGroupCmd(study2.id.id, sg.name, sg.description, sg.units,
        sg.anatomicalSourceType, sg.preservationType, sg.preservationTemperatureType, sg.specimenType)

      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupAddedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include regex s"${study2.id.id}.*not found"
      }
    }

    "update a specimen group" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val sg2 = factory.createSpecimenGroup

      val cmd = new UpdateSpecimenGroupCmd(disabledStudy.id.id, sg.id.id, sg.version,
        sg2.name, sg2.description, sg2.units, sg2.anatomicalSourceType, sg2.preservationType,
        sg2.preservationTemperatureType, sg2.specimenType)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
        .futureValue
      validation should be ('success)

      validation map { event =>
        event shouldBe a[SpecimenGroupUpdatedEvent]
        event should have (
          'studyId                     (disabledStudy.id.id),
          'specimenGroupId             (sg.id.id),
          'version                     (sg.version + 1),
          'name                        (sg2.name),
          'description                 (sg2.description),
          'units                       (sg2.units),
          'anatomicalSourceType        (sg2.anatomicalSourceType),
          'preservationType            (sg2.preservationType),
          'preservationTemperatureType (sg2.preservationTemperatureType),
          'specimenType                (sg2.specimenType)
        )

        val sg3 = specimenGroupRepository.withId(
          disabledStudy.id, SpecimenGroupId(event.specimenGroupId)) | fail
        sg3.version should be (sg.version + 1)
      }
    }

    "not update a specimen group with an invalid version" in {
      val item = factory.createSpecimenGroup
      specimenGroupRepository.put(item)

      val cmd = new UpdateSpecimenGroupCmd(disabledStudy.id.id, item.id.id, -1L, item.name,
        item.description, item.units, item.anatomicalSourceType, item.preservationType,
        item.preservationTemperatureType, item.specimenType)

      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
        .futureValue
      validation should be ('failure)

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("doesn't match current version")
      }
    }

    "not be added if the name already exists" in {
      val item = factory.createSpecimenGroup
      specimenGroupRepository.put(item)

      val cmd = AddSpecimenGroupCmd(disabledStudy.id.id, item.name,
        item.description, item.units, item.anatomicalSourceType, item.preservationType,
        item.preservationTemperatureType, item.specimenType)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupAddedEvent]]
        .futureValue
      validation should be ('failure)

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("name already exists")
      }
    }

    "not be updated to name that already exists" in {
      val sg1 = factory.createSpecimenGroup
      specimenGroupRepository.put(sg1)

      val sg2 = factory.createSpecimenGroup
      specimenGroupRepository.put(sg2)

      val sg3 = factory.createSpecimenGroup

      val cmd = new UpdateSpecimenGroupCmd(disabledStudy.id.id, sg2.id.id, sg2.version,
        sg1.name, sg1.description, sg1.units, sg1.anatomicalSourceType, sg1.preservationType,
        sg1.preservationTemperatureType, sg1.specimenType)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
        .futureValue

      validation should be ('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("name already exists")
      }
    }

    "not be updated to wrong study" in {
      val item = factory.createSpecimenGroup
      specimenGroupRepository.put(item)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cmd = new UpdateSpecimenGroupCmd(study2.id.id, item.id.id, item.version, item.name,
        item.description, item.units, item.anatomicalSourceType, item.preservationType,
        item.preservationTemperatureType, item.specimenType)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
        .futureValue
      validation should be ('failure)

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("study does not have specimen group")
      }
    }

    "can remove a specimen group" in {
      val item = factory.createSpecimenGroup
      specimenGroupRepository.put(item)

      val cmd = new RemoveSpecimenGroupCmd(disabledStudy.id.id, item.id.id, item.version)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupRemovedEvent]]
        .futureValue

      validation should be ('success)
      validation map { event =>
        event shouldBe a[SpecimenGroupRemovedEvent]
      }
    }

    "not remove a specimen group with an invalid version" in {
      val item = factory.createSpecimenGroup
      specimenGroupRepository.put(item)

      val cmd = new RemoveSpecimenGroupCmd(disabledStudy.id.id, item.id.id, item.version - 10)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupRemovedEvent]]
        .futureValue

      validation should be ('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("version mismatch")
      }
    }
  }
}
