package org.biobank.domain.study

import org.biobank.domain.{ DomainSpec, ContainerTypeId }
import org.biobank.infrastructure._
import org.biobank.fixture.NameGenerator

import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

class SpecimenLinkTypeSpec extends DomainSpec {
  import org.biobank.TestUtils._

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
      validation map { slType =>
        slType should have (
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

        slType.annotationTypeData should have length (0)

        (slType.timeAdded to DateTime.now).millis should be < 200L
        slType.timeModified should be (None)
      }
    }

    "be update" in {
      val slType = factory.createSpecimenLinkType

      val processingType = factory.defaultProcessingType
      val inputSpecimenGroup = factory.createSpecimenGroup
      val outputSpecimenGroup = factory.createSpecimenGroup

      val expectedInputChange = BigDecimal(5.0)
      val expectedOutputChange = BigDecimal(5.0)
      val inputCount = 10
      val outputCount = 10

      val disabledStudy = factory.defaultDisabledStudy

      val validation = slType.update(
        expectedInputChange, expectedOutputChange, inputCount, outputCount,
        inputSpecimenGroup.id, outputSpecimenGroup.id, annotationTypeData = List.empty)
      validation should be ('success)
      validation map { slType2 =>
        slType2 should have (
          'processingTypeId (slType.processingTypeId),
          'id (slType.id),
          'version (slType.version + 1),
          'expectedInputChange (expectedInputChange),
          'expectedOutputChange (expectedOutputChange),
          'inputCount (inputCount),
          'outputCount (outputCount),
          'inputGroupId (inputSpecimenGroup.id),
          'outputGroupId (outputSpecimenGroup.id),
          'inputContainerTypeId (None),
          'outputContainerTypeId (None)
        )

        slType.annotationTypeData should have length (0)

        slType2.timeAdded should be (slType.timeAdded)
        slType2.timeModified should be (None)
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
          err.list should (have length 1 and contain("ProcessingTypeIdRequired"))
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
          err.list should (have length 1 and contain("IdRequired"))
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
          err.list should (have length 1 and contain("SpecimenGroupIdRequired"))
      }

      specimenGroupIdIn = specimenGroupRepository.nextIdentity
      specimenGroupIdOut = SpecimenGroupId("")

      val validation2 = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, specimenGroupIdIn, specimenGroupIdOut,
        annotationTypeData = List.empty)
      validation2 should be('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("SpecimenGroupIdRequired"))
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
          err.list should (have length 1 and contain("InvalidVersion"))
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
          err.list should (have length 1 and contain("InvalidPositiveNumber"))
      }

      expectedInputChange = BigDecimal(1.0)
      expectedOutputChange = BigDecimal(-1.0)

      val validation2 = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation2 should be('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("InvalidPositiveNumber"))
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
          err.list should (have length 1 and contain("InvalidPositiveNumber"))
      }

      inputCount = 1
      outputCount = -1
      val validation2 = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        annotationTypeData = List.empty)
      validation2 should be('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("InvalidPositiveNumber"))
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
      validation shouldFail "ContainerTypeIdRequired"

      containerTypeIdIn = Some(ContainerTypeId("abc"))
      containerTypeIdOut = Some(ContainerTypeId(""))
      val validation2 = SpecimenLinkType.create(
        processingType.id, id, -1L, org.joda.time.DateTime.now, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputSpecimenGroup.id, outputSpecimenGroup.id,
        containerTypeIdIn, containerTypeIdOut, annotationTypeData = List.empty)
      validation2 shouldFail "ContainerTypeIdRequired"
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
          err.list(0) should be ("IdRequired")
          err.list(1) should be ("InvalidVersion")
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
        err.list(0) should include ("IdRequired")
      }
    }

  }

}
