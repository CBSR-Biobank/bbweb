package org.biobank.service.study

import org.biobank.fixture._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  ContainerTypeId,
  DomainError,
  DomainValidation
}
import org.biobank.domain.study._

import org.joda.time.DateTime
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import akka.pattern.ask
import scalaz._
import scalaz.Scalaz._

/**
  * Tests for actor SpecimenLinkTypeProcessorSpec. These are written using ScalaTest.
  *
  */
class SpecimenLinkTypeProcessorSpec extends TestFixture {
  import org.biobank.TestUtils._

  private val log = LoggerFactory.getLogger(this.getClass)

  val studyRepository = inject [StudyRepository]

  val specimenGroupRepository = inject [SpecimenGroupRepository]

  val processingTypeRepository = inject [ProcessingTypeRepository]

  val specimenLinkTypeRepository = inject [SpecimenLinkTypeRepository]

  val specimenLinkAnnotationTypeRepository = inject [SpecimenLinkAnnotationTypeRepository]

  val studiesProcessor = injectActorRef [StudiesProcessor] ("studies")

  val nameGenerator = new NameGenerator(this.getClass)

  private def askAddCommand(specimenLinkType: SpecimenLinkType)
    : DomainValidation[StudyEvent] = {
    val cmd = AddSpecimenLinkTypeCmd(None,
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
    ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyEvent]].futureValue
  }

  private def askUpdateCommand(specimenLinkType: SpecimenLinkType)
    : DomainValidation[StudyEvent] = {
    val cmd = UpdateSpecimenLinkTypeCmd(None,
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
    ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyEvent]].futureValue
  }

  private def askRemoveCommand(specimenLinkType: SpecimenLinkType)
    : DomainValidation[StudyEvent] = {
    val cmd = RemoveSpecimenLinkTypeCmd(None,
                                        specimenLinkType.processingTypeId.id,
                                        specimenLinkType.id.id,
                                        specimenLinkType.version)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyEvent]].futureValue
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

      askAddCommand(slType) mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (pt.studyId.id)

        val addedEvent = event.getSpecimenLinkTypeAdded
        addedEvent must have(
          'processingTypeId      (Some(pt.id.id)),
          'expectedInputChange   (Some(slType.expectedInputChange)),
          'expectedOutputChange  (Some(slType.expectedOutputChange)),
          'inputCount            (Some(slType.inputCount)),
          'outputCount           (Some(slType.outputCount)),
          'inputGroupId          (Some(slType.inputGroupId.id)),
          'outputGroupId         (Some(slType.outputGroupId.id)),
          'inputContainerTypeId  (slType.inputContainerTypeId),
          'outputContainerTypeId (slType.outputContainerTypeId)
        )

        addedEvent.annotationTypeData must have length (0)

        specimenLinkTypeRepository.allForProcessingType(pt.id) must have size 1
        specimenLinkTypeRepository.withId(
          pt.id,
          SpecimenLinkTypeId(addedEvent.getSpecimenLinkTypeId))
        .mustSucceed  { repoSlt =>
          repoSlt.version mustBe (0)
          checkTimeStamps(repoSlt, DateTime.now, None)
        }
      }
    }

    "not add a specimen link type with an invalid processing type" in {
      val pt = factory.createProcessingType

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)

      askAddCommand(slType) mustFail "not found"
    }

    "not add a specimen link type with the same specimen group as input and output" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slType = factory.createSpecimenLinkType
      val slType2 = slType.copy(outputGroupId = slType.inputGroupId)

      askAddCommand(slType2) mustFail "input and output specimen groups are the same"
    }

    "not add a specimen link type with a specimen group that does not exist" in {
      // test both input and output
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slType = factory.createSpecimenLinkType

      val (slType2, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups

      askAddCommand(slType2) mustFail "not found"

      // save only one to the repository
      specimenGroupRepository.put(inputSg)
      val slType3 = slType.copy(inputGroupId = inputSg.id, outputGroupId = outputSg.id)
      askAddCommand(slType3) mustFail "not found"

      val slType4 = slType.copy(inputGroupId = outputSg.id, outputGroupId = inputSg.id)
      askAddCommand(slType4) mustFail "not found"
    }

    "update a specimen link type" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)
      specimenLinkTypeRepository.put(slType)

      val slType2 = slType.copy(expectedInputChange = slType.expectedInputChange + 1)

      askUpdateCommand(slType2) mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (pt.studyId.id)

        val updatedEvent = event.getSpecimenLinkTypeUpdated
        updatedEvent must have(
          'processingTypeId      (Some(pt.id.id)),
          'version               (Some(slType.version + 1)),
          'expectedInputChange   (Some(slType2.expectedInputChange)),
          'expectedOutputChange  (Some(slType2.expectedOutputChange)),
          'inputCount            (Some(slType2.inputCount)),
          'outputCount           (Some(slType2.outputCount)),
          'inputGroupId          (Some(slType2.inputGroupId.id)),
          'outputGroupId         (Some(slType2.outputGroupId.id)),
          'inputContainerTypeId  (slType2.inputContainerTypeId),
          'outputContainerTypeId (slType2.outputContainerTypeId)
        )

        updatedEvent.annotationTypeData must have length (0)

        specimenLinkTypeRepository.allForProcessingType(pt.id) must have size 1

        specimenLinkTypeRepository.withId(
          pt.id,
          SpecimenLinkTypeId(updatedEvent.getSpecimenLinkTypeId))
        .mustSucceed { repoSlt =>
          repoSlt.version mustBe (1)
          checkTimeStamps(repoSlt, slType.timeAdded, DateTime.now)
        }
      }
    }


    "not update a specimen link type with the same specimen group as input and output" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slType = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slType)

      val slType2 = slType.copy(outputGroupId = slType.inputGroupId)
      askUpdateCommand(slType2) mustFail "input and output specimen groups are the same"

      val slType3 = slType.copy(inputGroupId = slType.outputGroupId)
      askUpdateCommand(slType3) mustFail "input and output specimen groups are the same"
    }

    "not update a specimen link type to wrong processing type" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slType = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slType)

      val pt2 = factory.createProcessingType
      processingTypeRepository.put(pt2)

      val slType2 = slType.copy(processingTypeId = pt2.id)
      askUpdateCommand(slType2) mustFail "processing type does not have specimen link type"
    }

    "not update a specimen link type with an invalid version" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slType = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slType)

      val slType2 = slType.copy(version = slType.version + 1)
      askUpdateCommand(slType2) mustFail "expected version doesn't match current version"
    }

    "remove a specimen link type" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slType = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slType)

      askRemoveCommand(slType) mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (pt.studyId.id)

        val removedEvent = event.getSpecimenLinkTypeRemoved

        val v = specimenLinkTypeRepository.withId(
          pt.id,
          SpecimenLinkTypeId(removedEvent.getSpecimenLinkTypeId))
        v mustFail "specimen link type does not exist"
      }
    }

    "not remove a specimen link type with an invalid version" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slType = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slType)

      val slType2 = slType.copy(version = slType.version -1)
      askRemoveCommand(slType2) mustFail "expected version doesn't match current version"
    }

    "not update a specimen group if it used by specimen link type" in {

      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)
      specimenLinkTypeRepository.put(slType)

      val cmd = new UpdateSpecimenGroupCmd(
        None, inputSg.studyId.id, inputSg.id.id,
        inputSg.version, inputSg.name, inputSg.description, inputSg.units,
        inputSg.anatomicalSourceType, inputSg.preservationType, inputSg.preservationTemperatureType,
        inputSg.specimenType)

      val v = ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyEvent]].futureValue
      v mustFail "specimen group is in use by specimen link type"

      val cmd2 = new UpdateSpecimenGroupCmd(
        None, outputSg.studyId.id, outputSg.id.id,
        outputSg.version, outputSg.name, outputSg.description, outputSg.units,
        outputSg.anatomicalSourceType, outputSg.preservationType, outputSg.preservationTemperatureType,
        outputSg.specimenType)

      val v2 =ask(studiesProcessor, cmd2).mapTo[DomainValidation[StudyEvent]].futureValue
      v2 mustFail "specimen group is in use by specimen link type"
    }

    "not remove a specimen group if used by specimen link type" in {
      val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
      specimenGroupRepository.put(inputSg)
      specimenGroupRepository.put(outputSg)
      specimenLinkTypeRepository.put(slType)

      val cmd = new RemoveSpecimenGroupCmd(None, inputSg.studyId.id, inputSg.id.id, inputSg.version)

      val v = ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyEvent]].futureValue
      v mustFail "specimen group is in use by specimen link type"

      val cmd2 = new RemoveSpecimenGroupCmd(None, outputSg.studyId.id, outputSg.id.id, outputSg.version)

      val v2 = ask(studiesProcessor, cmd2).mapTo[DomainValidation[StudyEvent]].futureValue
      v2 mustFail "specimen group is in use by specimen link type"
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
      askAddCommand(slType) mustFail "wrong study"

      val slType2 = slType.copy(
        inputGroupId = sg1WrongStudy.id,
        outputGroupId = sg2.id)
      askAddCommand(slType2) mustFail "wrong study"
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
      askUpdateCommand(slType) mustFail "wrong study"

      val slType3 = slType.copy(
        inputGroupId = sg1WrongStudy.id,
        outputGroupId = outputSg.id)

      askUpdateCommand(slType3) mustFail "wrong study"
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

      askAddCommand(slType2) mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (pt.studyId.id)

        val addedEvent = event.getSpecimenLinkTypeAdded
        addedEvent.annotationTypeData must have length (2)

        addedEvent.annotationTypeData(0) must have(
          'annotationTypeId (Some(slTypeAnnotationTypeData(0).annotationTypeId)),
          'required         (Some(slTypeAnnotationTypeData(0).required)))

        addedEvent.annotationTypeData(1) must have(
          'annotationTypeId (Some(slTypeAnnotationTypeData(1).annotationTypeId)),
          'required         (Some(slTypeAnnotationTypeData(1).required)))
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

      val cmd = UpdateSpecimenLinkAnnotationTypeCmd(None,
                                                    studyId         = annotationType.studyId.id,
                                                    id              = annotationType.id.id,
                                                    expectedVersion = annotationType.version,
                                                    name            = annotationType.name,
                                                    description     = annotationType.description,
                                                    valueType       = annotationType.valueType,
                                                    maxValueCount   = annotationType.maxValueCount,
                                                    options         = annotationType.options)

      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
       v mustFail "annotation type is in use by specimen link type"
    }

    "remove an annotation type from specimen link type" taggedAs(Tag("1")) in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

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

      askUpdateCommand(slType3) mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (pt.studyId.id)

        val updatedEvent = event.getSpecimenLinkTypeUpdated
        updatedEvent.annotationTypeData must have length (0)
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
        None, annotationType.studyId.id, annotationType.id.id, annotationType.version)

      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[StudyEvent]]
        .futureValue
      v mustFail "annotation type is in use by specimen link type"
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
      askUpdateCommand(slType2) mustFail "annotation type.*do not belong to study"
    }
  }
}
