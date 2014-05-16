package org.biobank.domain.study

import org.biobank.domain.{ DomainSpec, ContainerTypeId }
import org.biobank.infrastructure._
import org.biobank.fixture.NameGenerator

import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

class SpecimenLinkTypeSpec extends DomainSpec {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A specimen link type" can {

    "be created" in {
      val processingType = factory.defaultProcessingType
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup
      val id = specimenLinkTypeRepository.nextIdentity
      val expectedInputChange = BigDecimal(1.0)
      val expectedOutputChange = BigDecimal(1.0)
      val inputCount = 1
      val outputCount = 1

      val disabledStudy = factory.defaultDisabledStudy

      val validation = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation should be ('success)
      validation map { slt =>
        slt should have (
          'processingTypeId (processingType.id),
          'id (id),
          'expectedInputChange (expectedInputChange),
          'expectedOutputChange (expectedOutputChange),
          'inputCount (inputCount),
          'outputCount (outputCount),
          'inputGroupId (inputSpecimenGroup.id),
          'outputGroupId (outputSpecimenGroup.id),
          'inputContainerTypeId (None),
          'outputContainerTypeId (None)
        )

        slt.annotationTypeData should have length (0)

        (slt.addedDate to DateTime.now).millis should be < 100L
        slt.lastUpdateDate should be (None)
      }
    }

    "be update" in {
      val slt = factory.createSpecimenLinkType

      val processingType = factory.defaultProcessingType
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup

      val expectedInputChange = BigDecimal(5.0)
      val expectedOutputChange = BigDecimal(5.0)
      val inputCount = 10
      val outputCount = 10

      val disabledStudy = factory.defaultDisabledStudy

      val validation = slt.update(
        slt.versionOption, org.joda.time.DateTime.now, expectedInputChange, expectedOutputChange,
        inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation should be ('success)
      validation map { slt2 =>
        slt2 should have (
          'processingTypeId (slt.processingTypeId),
          'id (slt.id),
          'version (slt.version + 1),
          'expectedInputChange (expectedInputChange),
          'expectedOutputChange (expectedOutputChange),
          'inputCount (inputCount),
          'outputCount (outputCount),
          'inputGroupId (inputSpecimenGroup.id),
          'outputGroupId (outputSpecimenGroup.id),
          'inputContainerTypeId (None),
          'outputContainerTypeId (None)
        )

        slt.annotationTypeData should have length (0)

        slt2.addedDate should be (slt.addedDate)
        val updateDate = slt2.lastUpdateDate | fail

        (updateDate to DateTime.now).millis should be < 100L
      }
    }

  }

  "A specimen link type" should {

    "not be created with an empty processing type id" in {
      val processingTypeId = ProcessingTypeId("")
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup
      val id = specimenLinkTypeRepository.nextIdentity
      val expectedInputChange = BigDecimal(1.0)
      val expectedOutputChange = BigDecimal(1.0)
      val inputCount = 1
      val outputCount = 1

      val disabledStudy = factory.defaultDisabledStudy

      val validation = SpecimenLinkType.create(
        processingTypeId, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation should be('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("id is null or empty"))
      }
    }

    "not be created with an empty id" in {
      val processingType = factory.defaultProcessingType
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup
      val id = SpecimenLinkTypeId("")
      val expectedInputChange = BigDecimal(1.0)
      val expectedOutputChange = BigDecimal(1.0)
      val inputCount = 1
      val outputCount = 1

      val disabledStudy = factory.defaultDisabledStudy

      val validation = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation should be('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("id is null or empty"))
      }
    }

    "not be created with an invalid specimen group ids" in {
      val processingType = factory.defaultProcessingType
      var specimenGroupIdIn: SpecimenGroupId = SpecimenGroupId("")
      var specimenGroupIdOut: SpecimenGroupId = specimenGroupRepository.nextIdentity
      val id = specimenLinkTypeRepository.nextIdentity
      val expectedInputChange = BigDecimal(1.0)
      val expectedOutputChange = BigDecimal(1.0)
      val inputCount = 1
      val outputCount = 1

      val disabledStudy = factory.defaultDisabledStudy

      val validation = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, specimenGroupIdIn, specimenGroupIdOut,
        annotationTypeData = List.empty)
      validation should be('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("id is null or empty"))
      }

      specimenGroupIdIn = specimenGroupRepository.nextIdentity
      specimenGroupIdOut = SpecimenGroupId("")

      val validation2 = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, specimenGroupIdIn, specimenGroupIdOut,
        annotationTypeData = List.empty)
      validation2 should be('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("id is null or empty"))
      }
    }

    "not be created with an invalid version" in {
      val processingType = factory.defaultProcessingType
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup
      val id = specimenLinkTypeRepository.nextIdentity
      val expectedInputChange = BigDecimal(1.0)
      val expectedOutputChange = BigDecimal(1.0)
      val inputCount = 1
      val outputCount = 1

      val disabledStudy = factory.defaultDisabledStudy

      val validation = SpecimenLinkType.create(
        processingType.id, id, -2L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation should be('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("invalid version value: -2"))
      }
    }

    "not be created with an invalid expected input / output change" in {
      val processingType = factory.defaultProcessingType
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup
      val id = specimenLinkTypeRepository.nextIdentity
      var expectedInputChange: BigDecimal = BigDecimal(-1.0)
      var expectedOutputChange: BigDecimal = BigDecimal(1.0)
      val inputCount = 1
      val outputCount = 1

      val disabledStudy = factory.defaultDisabledStudy

      val validation = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation should be('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("expected input change is not a positive number"))
      }

      expectedInputChange = BigDecimal(1.0)
      expectedOutputChange = BigDecimal(-1.0)

      val validation2 = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation2 should be('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("expected output change is not a positive number"))
      }
    }

    "not be created with an invalid input / output count" in {
      val processingType = factory.defaultProcessingType
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup
      val id = specimenLinkTypeRepository.nextIdentity
      val expectedInputChange = BigDecimal(1.0)
      val expectedOutputChange = BigDecimal(1.0)
      var inputCount: Int = -1
      var outputCount: Int = 1

      val disabledStudy = factory.defaultDisabledStudy

      val validation = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation should be('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("input count is not a positive number"))
      }

      inputCount = 1
      outputCount = -1
      val validation2 = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation2 should be('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("output count is not a positive number"))
      }
    }

    "not be created with invalid container types" in {
      val processingType = factory.defaultProcessingType
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup
      val id = specimenLinkTypeRepository.nextIdentity
      val expectedInputChange = BigDecimal(1.0)
      val expectedOutputChange = BigDecimal(1.0)
      val inputCount = 1
      val outputCount = 1
      var containerTypeIdIn: Option[ContainerTypeId] = Some(ContainerTypeId(""))
      var containerTypeIdOut: Option[ContainerTypeId] = Some(ContainerTypeId("xyz"))

      val disabledStudy = factory.defaultDisabledStudy

      val validation = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        containerTypeIdIn, containerTypeIdOut, annotationTypeData = List.empty)
      validation should be('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("id is null or empty"))
      }

      containerTypeIdIn = Some(ContainerTypeId("abc"))
      containerTypeIdOut = Some(ContainerTypeId(""))
      val validation2 = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        containerTypeIdIn, containerTypeIdOut, annotationTypeData = List.empty)
      validation2 should be('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("id is null or empty"))
      }
    }

    "have more than one validation fail" in {
      val processingType = factory.defaultProcessingType
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup
      val id = SpecimenLinkTypeId("")
      val expectedInputChange = BigDecimal(1.0)
      val expectedOutputChange = BigDecimal(1.0)
      val inputCount = 1
      val outputCount = 1

      val disabledStudy = factory.defaultDisabledStudy

      val validation = SpecimenLinkType.create(
        processingType.id, id, -2L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should have length 2
          err.list(0) should be ("id is null or empty")
          err.list(1) should be ("invalid version value: -2")
      }
    }

    "not be created with an invalid annotation type id" in {
      val processingType = factory.defaultProcessingType
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup
      val id = specimenLinkTypeRepository.nextIdentity
      val expectedInputChange = BigDecimal(1.0)
      val expectedOutputChange = BigDecimal(1.0)
      val inputCount = 1
      val outputCount = 1

      val disabledStudy = factory.defaultDisabledStudy
      val annotationTypeData = List(SpecimenLinkTypeAnnotationTypeData("", false))

      val validation = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = annotationTypeData)
      validation should be('failure)
      validation.swap.map { err =>
        err.list should have length 1
        err.list(0) should include ("id is null or empty")
      }
    }

  }

}
