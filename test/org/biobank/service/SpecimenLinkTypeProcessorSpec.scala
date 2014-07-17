package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  ContainerTypeId,
  DomainError,
  DomainValidation
}
import org.biobank.domain.study._

import org.scalatest.Tag
import org.slf4j.LoggerFactory
import akka.pattern.ask
import scalaz._
import scalaz.Scalaz._

class SpecimenLinkTypeProcessorSpec extends StudiesProcessorFixture {

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
      specimenLinkType.inputGroupId.id,
      specimenLinkType.outputGroupId.id,
      specimenLinkType.inputContainerTypeId.map(_.id),
      specimenLinkType.outputContainerTypeId.map(_.id),
      specimenLinkType.annotationTypeData)
    val validation = ask(studiesProcessor, cmd).mapTo[DomainValidation[SpecimenLinkTypeAddedEvent]]
      .futureValue
    resultFunc(validation)
  }

  private def askUpdateCommand(
    specimenLinkType: SpecimenLinkType)(
    resultFunc: DomainValidation[SpecimenLinkTypeUpdatedEvent] => Unit): Unit = {
    val cmd = UpdateSpecimenLinkTypeCmd(
      specimenLinkType.processingTypeId.id,
      specimenLinkType.id.id,
      specimenLinkType.version,
      specimenLinkType.expectedInputChange,
      specimenLinkType.expectedOutputChange,
      specimenLinkType.inputCount,
      specimenLinkType.outputCount,
      specimenLinkType.inputGroupId.id,
      specimenLinkType.outputGroupId.id,
      specimenLinkType.inputContainerTypeId.map(_.id),
      specimenLinkType.outputContainerTypeId.map(_.id),
      specimenLinkType.annotationTypeData)
    val validation = ask(studiesProcessor, cmd).mapTo[DomainValidation[SpecimenLinkTypeUpdatedEvent]]
      .futureValue
    resultFunc(validation)
  }

  private def askRemoveCommand(
    specimenLinkType: SpecimenLinkType)(
    resultFunc: DomainValidation[SpecimenLinkTypeRemovedEvent] => Unit): Unit = {
    val cmd = RemoveSpecimenLinkTypeCmd(
      specimenLinkType.processingTypeId.id,
      specimenLinkType.id.id,
      specimenLinkType.version)
    val validation = ask(studiesProcessor, cmd)
      .mapTo[DomainValidation[SpecimenLinkTypeRemovedEvent]]
      .futureValue
    resultFunc(validation)
  }

  "A study processor" can {

    "add a specimen link type" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)

      askAddCommand(slType){ validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[SpecimenLinkTypeAddedEvent]
          event should have(
            'processingTypeId      (pt.id.id),
            'expectedInputChange   (slType.expectedInputChange),
            'expectedOutputChange  (slType.expectedOutputChange),
            'inputCount            (slType.inputCount),
            'outputCount           (slType.outputCount),
            'inputGroupId          (slType.inputGroupId),
            'outputGroupId         (slType.outputGroupId),
            'inputContainerTypeId  (slType.inputContainerTypeId),
            'outputContainerTypeId (slType.outputContainerTypeId),
            'annotationTypeData    (slType.annotationTypeData)
          )

          val slType2 = specimenLinkTypeRepository.withId(
            pt.id, SpecimenLinkTypeId(event.specimenLinkTypeId)) | fail
          slType2.version should be (0)
          specimenLinkTypeRepository.allForProcessingType(pt.id) should have size 1
        }
      }
    }

    "not add a specimen link type with an invalid processing type" in {
      val pt = factory.createProcessingType

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)

      askAddCommand(slType){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("not found")
        }
      }
    }

    "not add a specimen link type with the same specimen group as input and output" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slType = factory.createSpecimenLinkType
      val slType2 = slType.copy(outputGroupId = slType.inputGroupId)

      askAddCommand(slType2){ validation =>
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

      val slType = factory.createSpecimenLinkType

      val (slType2, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups

      askAddCommand(slType2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("not found")
        }
      }

      // save only one to the repository
      specimenGroupRepository.put(inputSg)
      val slType3 = slType.copy(inputGroupId = inputSg.id, outputGroupId = outputSg.id)

      askAddCommand(slType3){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("not found")
        }
      }

      val slType4 = slType.copy(inputGroupId = outputSg.id, outputGroupId = inputSg.id)
      askAddCommand(slType4){ validation =>
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

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)
      specimenLinkTypeRepository.put(slType)

      val slType2 = slType.copy(expectedInputChange = slType.expectedInputChange + 1)

      askUpdateCommand(slType2){ validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[SpecimenLinkTypeUpdatedEvent]
          event should have(
            'processingTypeId      (pt.id.id),
            'version               (slType.version + 1),
            'expectedInputChange   (slType2.expectedInputChange),
            'expectedOutputChange  (slType2.expectedOutputChange),
            'inputCount            (slType2.inputCount),
            'outputCount           (slType2.outputCount),
            'inputGroupId          (slType2.inputGroupId),
            'outputGroupId         (slType2.outputGroupId),
            'inputContainerTypeId  (slType2.inputContainerTypeId),
            'outputContainerTypeId (slType2.outputContainerTypeId),
            'annotationTypeData    (slType2.annotationTypeData)
          )

          val slTypeRepo = specimenLinkTypeRepository.withId(
            pt.id, SpecimenLinkTypeId(event.specimenLinkTypeId)) | fail
          slTypeRepo.version should be (slType.version + 1)
          specimenLinkTypeRepository.allForProcessingType(pt.id) should have size 1
        }
      }
    }


    "not update a specimen link type with the same specimen group as input and output" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slType = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slType)

      val slType2 = slType.copy(outputGroupId = slType.inputGroupId)

      askUpdateCommand(slType2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("input and output specimen groups are the same")
        }
      }

      val slType3 = slType.copy(inputGroupId = slType.outputGroupId)

      askUpdateCommand(slType3){ validation =>
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

      val slType = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slType)

      val pt2 = factory.createProcessingType
      processingTypeRepository.put(pt2)

      val slType2 = slType.copy(processingTypeId = pt2.id)

      askUpdateCommand(slType2){ validation =>
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

      val slType = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slType)

      val slType2 = slType.copy(version = slType.version + 1)

      askUpdateCommand(slType2){ validation =>
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

      val slType = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slType)

      askRemoveCommand(slType){ validation =>
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

      val slType = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slType)

      val slType2 = slType.copy(version = slType.version -1)

      askRemoveCommand(slType2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("version mismatch")
        }
      }
    }

    "not update a specimen group if it used by specimen link type" in {

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)
      specimenLinkTypeRepository.put(slType)

      val cmd = new UpdateSpecimenGroupCmd(inputSg.studyId.id, inputSg.id.id,
        inputSg.version, inputSg.name, inputSg.description, inputSg.units,
        inputSg.anatomicalSourceType, inputSg.preservationType, inputSg.preservationTemperatureType,
        inputSg.specimenType)
      val validation = ask(studiesProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
        .futureValue
      validation should be('failure)

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group is in use by specimen link type")
      }

      val cmd2 = new UpdateSpecimenGroupCmd(outputSg.studyId.id, outputSg.id.id,
        outputSg.version, outputSg.name, outputSg.description, outputSg.units,
        outputSg.anatomicalSourceType, outputSg.preservationType, outputSg.preservationTemperatureType,
        outputSg.specimenType)
      val validation2 = ask(studiesProcessor, cmd2).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
        .futureValue
      validation2 should be('failure)

      validation2.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group is in use by specimen link type")
      }
    }

    "not remove a specimen group if used by specimen link type" in {
      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)
      specimenLinkTypeRepository.put(slType)

      val cmd = new RemoveSpecimenGroupCmd(inputSg.studyId.id, inputSg.id.id, inputSg.version)
      val validation = ask(studiesProcessor, cmd).mapTo[DomainValidation[SpecimenGroupRemovedEvent]]
        .futureValue
      validation should be('failure)

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group is in use by specimen link type")
      }

      val cmd2 = new RemoveSpecimenGroupCmd(outputSg.studyId.id, outputSg.id.id, outputSg.version)
      val validation2 = ask(studiesProcessor, cmd2).mapTo[DomainValidation[SpecimenGroupRemovedEvent]]
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

      val slType = factory.createSpecimenLinkType.copy(
        inputGroupId = sg1.id,
        outputGroupId = sg2WrongStudy.id)

      askAddCommand(slType){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("wrong study")
        }
      }

      val slType2 = slType.copy(
        inputGroupId = sg1WrongStudy.id,
        outputGroupId = sg2.id)

      askAddCommand(slType2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("wrong study")
        }
      }
    }

    "not update a specimen link type with a specimen group from a different study" in {
      // test both input and output
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val study2 = factory.createDisabledStudy

      val sg1WrongStudy = factory.createSpecimenGroup
      specimenGroupRepository.put(sg1WrongStudy)

      val sg2WrongStudy = factory.createSpecimenGroup
      specimenGroupRepository.put(sg2WrongStudy)

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)
      specimenLinkTypeRepository.put(slType)

      val slType2 = slType.copy(
        inputGroupId = inputSg.id,
        outputGroupId = sg2WrongStudy.id)
      askUpdateCommand(slType){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("wrong study")
        }
      }

      val slType3 = slType.copy(
        inputGroupId = sg1WrongStudy.id,
        outputGroupId = outputSg.id)

      askUpdateCommand(slType3){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("wrong study")
        }
      }
    }

    "add an annotation type to a specimen link type" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val annotationType = factory.createSpecimenLinkAnnotationType
      specimenLinkAnnotationTypeRepository.put(annotationType)

      val slTypeAnnotationTypeData = List(
        factory.createSpecimenLinkTypeAnnotationTypeData,
        factory.createSpecimenLinkTypeAnnotationTypeData)

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)
      val slType2 = slType.copy(annotationTypeData = slTypeAnnotationTypeData)

      askAddCommand(slType2){ validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[SpecimenLinkTypeAddedEvent]
          event.annotationTypeData should have length (2)

          event.annotationTypeData(0) should have(
            'annotationTypeId (slTypeAnnotationTypeData(0).annotationTypeId),
            'required (slTypeAnnotationTypeData(0).required))

          event.annotationTypeData(1) should have(
            'annotationTypeId (slTypeAnnotationTypeData(1).annotationTypeId),
            'required (slTypeAnnotationTypeData(1).required))
        }
      }
    }

    "not update an annotation type if used by specimen link type" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val annotationType = factory.createSpecimenLinkAnnotationType
      specimenLinkAnnotationTypeRepository.put(annotationType)

      val slTypeAnnotationTypeData = List(factory.createSpecimenLinkTypeAnnotationTypeData)

      val slType = factory.createSpecimenLinkType.copy(
        annotationTypeData = slTypeAnnotationTypeData)
      specimenLinkTypeRepository.put(slType)

      val cmd = UpdateSpecimenLinkAnnotationTypeCmd(
        annotationType.studyId.id, annotationType.id.id, annotationType.version,
        annotationType.name, annotationType.description, annotationType.valueType)
      val validation = ask(studiesProcessor, cmd)
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

      val slTypeAnnotationTypeData = List(factory.createSpecimenLinkTypeAnnotationTypeData)

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)
      val slType2 = slType.copy(annotationTypeData = slTypeAnnotationTypeData)
      specimenLinkTypeRepository.put(slType2)

      val slType3 = slType2.copy(annotationTypeData = List.empty)

      askUpdateCommand(slType3){ validation =>
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

      val slTypeAnnotationTypeData = List(factory.createSpecimenLinkTypeAnnotationTypeData)

      val slType = factory.createSpecimenLinkType.copy(
        annotationTypeData = slTypeAnnotationTypeData)
      specimenLinkTypeRepository.put(slType)

      val cmd = RemoveSpecimenLinkAnnotationTypeCmd(
        annotationType.studyId.id, annotationType.id.id, annotationType.version)
      val validation = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("annotation type is in use by specimen link type")
      }
    }

    "not add an annotation type if it is in wrong study" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)
      specimenLinkTypeRepository.put(slType)

      factory.createDisabledStudy

      val annotationType = factory.createSpecimenLinkAnnotationType
      specimenLinkAnnotationTypeRepository.put(annotationType)

      val slTypeAnnotationTypeData = List(
        factory.createSpecimenLinkTypeAnnotationTypeData,
        factory.createSpecimenLinkTypeAnnotationTypeData)

      val slType2 = slType.copy(annotationTypeData = slTypeAnnotationTypeData)

      askUpdateCommand(slType2){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("annotation type(s) do not belong to study")
        }
      }
    }
  }
}
