package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  DomainError,
  DomainValidation
}
import org.biobank.domain.study._

import org.scalatest.Tag
import org.slf4j.LoggerFactory
import akka.pattern.ask
import scalaz._
import scalaz.Scalaz._

class SpecimenLinkTypeProcessorSpec extends StudyProcessorFixture {

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  private def askAddCommand(
    specimenLinkType: SpecimenLinkType)(
    resultFunc: DomainValidation[SpecimenLinkTypeAddedEvent] => Unit): Unit = {
    val cmd = AddSpecimenLinkTypeCmd(
      specimenLinkType.processingTypeId.id,
      specimenLinkType.expectedInputChange,
      specimenLinkType.expectedOutputChange,
      specimenLinkType.inputCount,
      specimenLinkType.outputCount,
      specimenLinkType.inputGroupId,
      specimenLinkType.outputGroupId,
      specimenLinkType.inputContainerTypeId,
      specimenLinkType.outputContainerTypeId,
      specimenLinkType.annotationTypeData)
    val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenLinkTypeAddedEvent]]
      .futureValue
    resultFunc(validation)
  }

  private def askUpdateCommand(
    specimenLinkType: SpecimenLinkType)(
    resultFunc: DomainValidation[SpecimenLinkTypeUpdatedEvent] => Unit): Unit = {
    val cmd = UpdateSpecimenLinkTypeCmd(
      specimenLinkType.processingTypeId.id,
      specimenLinkType.id.id,
      specimenLinkType.versionOption,
      specimenLinkType.expectedInputChange,
      specimenLinkType.expectedOutputChange,
      specimenLinkType.inputCount,
      specimenLinkType.outputCount,
      specimenLinkType.inputGroupId,
      specimenLinkType.outputGroupId,
      specimenLinkType.inputContainerTypeId,
      specimenLinkType.outputContainerTypeId,
      specimenLinkType.annotationTypeData)
    val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenLinkTypeUpdatedEvent]]
      .futureValue
    resultFunc(validation)
  }

  "A study processor" can {

    "add a specimen link type" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType

      askAddCommand(slt){ validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[SpecimenLinkTypeAddedEvent]
          event should have(
            'processingTypeId      (pt.id.id),
            'version               (slt.version),
            'expectedInputChange   (slt.expectedInputChange),
            'expectedOutputChange  (slt.expectedOutputChange),
            'inputCount            (slt.inputCount),
            'outputCount           (slt.outputCount),
            'inputGroupId          (slt.inputGroupId),
            'outputGroupId         (slt.outputGroupId),
            'inputContainerTypeId  (slt.inputContainerTypeId),
            'outputContainerTypeId (slt.outputContainerTypeId),
            'annotationTypeData    (slt.annotationTypeData)
          )

          val slt2 = specimenLinkTypeRepository.withId(
            pt.id, SpecimenLinkTypeId(event.specimenLinkTypeId)) | fail
          slt2.version should be (0)
          specimenLinkTypeRepository.allForProcessingType(pt.id) should have size 1
        }
      }
    }

    "not add a specimen link type with the same specimen group as input and output" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType
      val slt2 = slt.copy(outputGroupId = slt.inputGroupId)

      askAddCommand(slt2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("input and output specimen groups are the same")
        }
      }
    }

    "not add a specimen link type with a specimen group that does not exist" in {
      // test both input and output
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType

      val sg1 = factory.createSpecimenGroup
      val sg2 = factory.createSpecimenGroup

      val slt2 = slt.copy(inputGroupId = sg1.id, outputGroupId = sg2.id)

      askAddCommand(slt2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("not found")
        }
      }

      // save only one to the repository
      specimenGroupRepository.put(sg1)
      val slt3 = slt.copy(inputGroupId = sg1.id, outputGroupId = sg2.id)

      askAddCommand(slt3){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("not found")
        }
      }

      val slt4 = slt.copy(inputGroupId = sg2.id, outputGroupId = sg1.id)
      askAddCommand(slt4){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("not found")
        }
      }
    }

    "update a specimen link type" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slt)

      val slt2 = slt.copy(expectedInputChange = slt.expectedInputChange + 1)

      askUpdateCommand(slt2){ validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[SpecimenLinkTypeUpdatedEvent]
          event should have(
            'processingTypeId      (pt.id.id),
            'version               (slt.version + 1),
            'expectedInputChange   (slt2.expectedInputChange),
            'expectedOutputChange  (slt2.expectedOutputChange),
            'inputCount            (slt2.inputCount),
            'outputCount           (slt2.outputCount),
            'inputGroupId          (slt2.inputGroupId),
            'outputGroupId         (slt2.outputGroupId),
            'inputContainerTypeId  (slt2.inputContainerTypeId),
            'outputContainerTypeId (slt2.outputContainerTypeId),
            'annotationTypeData    (slt2.annotationTypeData)
          )

          val sltRepo = specimenLinkTypeRepository.withId(
            pt.id, SpecimenLinkTypeId(event.specimenLinkTypeId)) | fail
          sltRepo.version should be (slt.version + 1)
          specimenLinkTypeRepository.allForProcessingType(pt.id) should have size 1
        }
      }
    }

    "not update a specimen link type to name that already exists" in {
      ???
    }

    "not update a specimen link type to wrong processing type" taggedAs(Tag("single")) in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slt)

      val pt2 = factory.createProcessingType
      processingTypeRepository.put(pt2)

      val slt2 = slt.copy(processingTypeId = pt2.id)

      askUpdateCommand(slt2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("processing type does not have specimen link type")
        }
      }
    }

    "not update a specimen link type with an invalid version" in {
      ???
    }

    "remove a specimen link type" in {
      ???
    }

    "not remove a specimen link type  with an invalid version" in {
      ???
    }

    "add a specimen group to a specimen link type" in {
      ???
    }

    "update a specimen link type and add specimen groups" in {
      ???
    }

    "not update a specimen group if it used by specimen link type" in {
      ???
    }

    "remove a specimen group from specimen link type" in {
      ???
    }

    "not remove a specimen group if used by specimen link type" in {
      ???
    }

    "not add a specimen group from a different study" in {
      ???
    }

    "not update a specimen link type with a specimen group from a different study" in {
      ???
    }

    "add an annotation type to a specimen link" in {
      ???
    }

    "not update an annotation type if used by specimen link type" in {
      ???
    }

    "remove an annotation type from specimen link type" in {
      ???
    }

    "not remove an annotation type if it is used by specimen link type" in {
      ???
    }

    "not add an annotation type if it is in wrong study" in {
      ???
    }
  }
}

