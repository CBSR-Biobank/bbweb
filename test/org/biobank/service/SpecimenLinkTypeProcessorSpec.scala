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

  private def askRemoveCommand(
    specimenLinkType: SpecimenLinkType)(
    resultFunc: DomainValidation[SpecimenLinkTypeRemovedEvent] => Unit): Unit = {
    val cmd = RemoveSpecimenLinkTypeCmd(
      specimenLinkType.processingTypeId.id,
      specimenLinkType.id.id,
      specimenLinkType.versionOption)
    val validation = ask(studyProcessor, cmd)
      .mapTo[DomainValidation[SpecimenLinkTypeRemovedEvent]]
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


    "not update a specimen link type with the same specimen group as input and output" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slt)

      val slt2 = slt.copy(outputGroupId = slt.inputGroupId)

      askUpdateCommand(slt2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("input and output specimen groups are the same")
        }
      }

      val slt3 = slt.copy(inputGroupId = slt.outputGroupId)

      askUpdateCommand(slt3){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("input and output specimen groups are the same")
        }
      }
    }

    "not update a specimen link type to wrong processing type" in {
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
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slt)

      val slt2 = slt.copy(version = slt.version - 1)

      askUpdateCommand(slt2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("expected version doesn't match current version")
        }
      }
    }

    "remove a specimen link type" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slt)

      askRemoveCommand(slt){ validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[SpecimenLinkTypeRemovedEvent]

          val v = specimenLinkTypeRepository.withId(
            pt.id, SpecimenLinkTypeId(event.specimenLinkTypeId))
          v should be ('failure)
          v.swap map { err =>
            err.list should have length 1
            err.list.head should include("specimen link type does not exist")
          }
        }
      }
    }

    "not remove a specimen link type with an invalid version" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slt)

      val slt2 = slt.copy(version = slt.version -1)

      askRemoveCommand(slt2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("version mismatch")
        }
      }
    }

    "not update a specimen group if it used by specimen link type" in {
      val inputSg = factory.createSpecimenGroup
      specimenGroupRepository.put(inputSg)

      val outputSg = factory.createSpecimenGroup
      specimenGroupRepository.put(outputSg)

      val slt = factory.createSpecimenLinkType.copy(
        inputGroupId = inputSg.id,
        outputGroupId = outputSg.id
      )
      specimenLinkTypeRepository.put(slt)

      val cmd = new UpdateSpecimenGroupCmd(inputSg.studyId.id, inputSg.id.id,
        inputSg.versionOption, inputSg.name, inputSg.description, inputSg.units,
        inputSg.anatomicalSourceType, inputSg.preservationType, inputSg.preservationTemperatureType,
        inputSg.specimenType)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
        .futureValue
      validation should be('failure)

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group is in use by specimen link type")
      }

      val cmd2 = new UpdateSpecimenGroupCmd(outputSg.studyId.id, outputSg.id.id,
        outputSg.versionOption, outputSg.name, outputSg.description, outputSg.units,
        outputSg.anatomicalSourceType, outputSg.preservationType, outputSg.preservationTemperatureType,
        outputSg.specimenType)
      val validation2 = ask(studyProcessor, cmd2).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
        .futureValue
      validation2 should be('failure)

      validation2.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group is in use by specimen link type")
      }
    }

    "not remove a specimen group if used by specimen link type" in {
      val inputSg = factory.createSpecimenGroup
      specimenGroupRepository.put(inputSg)

      val outputSg = factory.createSpecimenGroup
      specimenGroupRepository.put(outputSg)

      val slt = factory.createSpecimenLinkType.copy(
        inputGroupId = inputSg.id,
        outputGroupId = outputSg.id
      )
      specimenLinkTypeRepository.put(slt)

      val cmd = new RemoveSpecimenGroupCmd(inputSg.studyId.id, inputSg.id.id, inputSg.versionOption)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupRemovedEvent]]
        .futureValue
      validation should be('failure)

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group is in use by specimen link type")
      }

      val cmd2 = new RemoveSpecimenGroupCmd(outputSg.studyId.id, outputSg.id.id, outputSg.versionOption)
      val validation2 = ask(studyProcessor, cmd2).mapTo[DomainValidation[SpecimenGroupRemovedEvent]]
        .futureValue
      validation2 should be('failure)

      validation2.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group is in use by specimen link type")
      }
    }

    "not add a specimen group from a different study" in {
      // test both input and output
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val sg1 = factory.createSpecimenGroup
      specimenGroupRepository.put(sg1)
      val sg2 = factory.createSpecimenGroup
      specimenGroupRepository.put(sg2)

      factory.createDisabledStudy

      val sg1WrongStudy = factory.createSpecimenGroup
      specimenGroupRepository.put(sg1WrongStudy)
      val sg2WrongStudy = factory.createSpecimenGroup
      specimenGroupRepository.put(sg2WrongStudy)

      val slt = factory.createSpecimenLinkType.copy(
        inputGroupId = sg1.id,
        outputGroupId = sg2WrongStudy.id)

      askAddCommand(slt){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("wrong study")
        }
      }

      val slt2 = slt.copy(
        inputGroupId = sg1WrongStudy.id,
        outputGroupId = sg2.id)

      askAddCommand(slt2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("wrong study")
        }
      }
    }

    "not update a specimen link type with a specimen group from a different study" in {
      // test both input and output
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val sg1 = factory.createSpecimenGroup
      specimenGroupRepository.put(sg1)
      val sg2 = factory.createSpecimenGroup
      specimenGroupRepository.put(sg2)

      factory.createDisabledStudy

      val sg1WrongStudy = factory.createSpecimenGroup
      specimenGroupRepository.put(sg1WrongStudy)
      val sg2WrongStudy = factory.createSpecimenGroup
      specimenGroupRepository.put(sg2WrongStudy)

      val slt = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slt)

      val slt2 = slt.copy(
        inputGroupId = sg1.id,
        outputGroupId = sg2WrongStudy.id)

      askUpdateCommand(slt){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("wrong study")
        }
      }

      val slt3 = slt.copy(
        inputGroupId = sg1WrongStudy.id,
        outputGroupId = sg2.id)

      askUpdateCommand(slt3){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("wrong study")
        }
      }
    }

    "add an annotation type to a specimen link type" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val annotationType = factory.createSpecimenLinkAnnotationType
      specimenLinkAnnotationTypeRepository.put(annotationType)

      val sltAnnotationTypeData = List(
        factory.createSpecimenLinkTypeAnnotationTypeData,
        factory.createSpecimenLinkTypeAnnotationTypeData)

      val slt = factory.createSpecimenLinkType.copy(
        annotationTypeData = sltAnnotationTypeData)

      askAddCommand(slt){ validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[SpecimenLinkTypeAddedEvent]
          event.annotationTypeData should have length (2)

          event.annotationTypeData(0) should have(
            'annotationTypeId (sltAnnotationTypeData(0).annotationTypeId),
            'required (sltAnnotationTypeData(0).required))

          event.annotationTypeData(1) should have(
            'annotationTypeId (sltAnnotationTypeData(1).annotationTypeId),
            'required (sltAnnotationTypeData(1).required))
        }
      }
    }

    "not update an annotation type if used by specimen link type" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val annotationType = factory.createSpecimenLinkAnnotationType
      specimenLinkAnnotationTypeRepository.put(annotationType)

      val sltAnnotationTypeData = List(factory.createSpecimenLinkTypeAnnotationTypeData)

      val slt = factory.createSpecimenLinkType.copy(
        annotationTypeData = sltAnnotationTypeData)
      specimenLinkTypeRepository.put(slt)

      val cmd = UpdateSpecimenLinkAnnotationTypeCmd(
        annotationType.studyId.id, annotationType.id.id, annotationType.versionOption,
        annotationType.name, annotationType.description, annotationType.valueType)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("annotation type is in use by specimen link type")
      }
    }

    "remove an annotation type from specimen link type" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val annotationType = factory.createSpecimenLinkAnnotationType
      specimenLinkAnnotationTypeRepository.put(annotationType)

      val sltAnnotationTypeData = List(factory.createSpecimenLinkTypeAnnotationTypeData)

      val slt = factory.createSpecimenLinkType.copy(
        annotationTypeData = sltAnnotationTypeData)
      specimenLinkTypeRepository.put(slt)
      specimenLinkTypeRepository.put(slt)

      val slt2 = slt.copy(annotationTypeData = List.empty)

      askUpdateCommand(slt2){ validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[SpecimenLinkTypeUpdatedEvent]
          event.annotationTypeData should have length (0)
        }
      }
    }

    "not remove an annotation type if it is used by specimen link type" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val annotationType = factory.createSpecimenLinkAnnotationType
      specimenLinkAnnotationTypeRepository.put(annotationType)

      val sltAnnotationTypeData = List(factory.createSpecimenLinkTypeAnnotationTypeData)

      val slt = factory.createSpecimenLinkType.copy(
        annotationTypeData = sltAnnotationTypeData)
      specimenLinkTypeRepository.put(slt)

      val cmd = RemoveSpecimenLinkAnnotationTypeCmd(
        annotationType.studyId.id, annotationType.id.id, annotationType.versionOption)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("annotation type is in use by specimen link type")
      }
    }

    "not add an annotation type if it is in wrong study" taggedAs(Tag("single")) in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slt)

      factory.createDisabledStudy

      val annotationType = factory.createSpecimenLinkAnnotationType
      specimenLinkAnnotationTypeRepository.put(annotationType)

      val sltAnnotationTypeData = List(
        factory.createSpecimenLinkTypeAnnotationTypeData,
        factory.createSpecimenLinkTypeAnnotationTypeData)

      val slt2 = slt.copy(annotationTypeData = sltAnnotationTypeData)

      askUpdateCommand(slt2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("annotation type(s) do not belong to study")
        }
      }
    }
  }
}
