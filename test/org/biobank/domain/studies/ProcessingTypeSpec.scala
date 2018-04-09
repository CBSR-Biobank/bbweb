package org.biobank.domain.studies

import java.time.OffsetDateTime
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.containers._
import org.biobank.fixture.NameGenerator
import org.biobank.TestUtils
import org.slf4j.LoggerFactory
import scala.language.reflectiveCalls
import org.scalatest.prop.TableDrivenPropertyChecks._

trait ProcessingTypeSpecCommon {

  val factory: Factory

  def collectedSpecimenDerivationFixtures() = {
    new {
      val collectedSpecimenDefinition = factory.createCollectionSpecimenDefinition
      val collectionEventType = factory.createCollectionEventType.copy(
          specimenDefinitions = Set(collectedSpecimenDefinition))
      val processingSpecimenDefinition = factory.createProcessingSpecimenDefinition
      val specimenDerivation = CollectedSpecimenDerivation(collectionEventType.id,
                                                           collectedSpecimenDefinition.id,
                                                           processingSpecimenDefinition)
      val processingType = factory.createProcessingType.copy(specimenDerivation = specimenDerivation)
      val study = factory.defaultDisabledStudy
    }
  }

  def processedSpecimenDerivationFixtures() = {
    val f = collectedSpecimenDerivationFixtures
    new {
      val inputProcessingType = f.processingType
      val inputSpecimenDefinition = f.processingSpecimenDefinition
      val outputSpecimenDefinition = factory.createProcessingSpecimenDefinition
      val specimenDerivation = ProcessedSpecimenDerivation(inputProcessingType.id,
                                                           inputSpecimenDefinition.id,
                                                           outputSpecimenDefinition)
      val outputProcessingType = factory.createProcessingType.copy(specimenDerivation = specimenDerivation)
      val study = factory.defaultDisabledStudy
    }
  }

}

class ProcessingTypeSpec
    extends DomainSpec
    with ProcessingTypeSpecCommon
    with AnnotationTypeSetSharedSpec[ProcessingType] {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(processingType: ProcessingType): DomainValidation[ProcessingType] =
    ProcessingType.create(studyId               = processingType.studyId,
                          id                    = processingType.id,
                          version               = processingType.version,
                          name                  = processingType.name,
                          description           = processingType.description,
                          enabled               = processingType.enabled,
                          expectedInputChange   = processingType.expectedInputChange,
                          expectedOutputChange  = processingType.expectedOutputChange,
                          inputCount            = processingType.inputCount,
                          outputCount           = processingType.outputCount,
                          specimenDerivation    = processingType.specimenDerivation,
                          inputContainerTypeId  = processingType.inputContainerTypeId,
                          outputContainerTypeId = processingType.outputContainerTypeId,
                          annotationTypes       = processingType.annotationTypes)

  def compare(expected: ProcessingType, actual: ProcessingType) = {
    actual mustBe a [ProcessingType]
    actual must have (
      'studyId               (expected.studyId),
      'version               (expected.version),
      'name                  (expected.name),
      'description           (expected.description),
      'enabled               (expected.enabled),
      'expectedInputChange   (expected.expectedInputChange),
      'expectedOutputChange  (expected.expectedOutputChange),
      'inputCount            (expected.inputCount),
      'outputCount           (expected.outputCount),
      'specimenDerivation    (expected.specimenDerivation),
      'inputContainerTypeId  (expected.inputContainerTypeId),
      'outputContainerTypeId (expected.outputContainerTypeId)
    )

  }


  describe("A processing type can") {

    describe("be created") {

      it("with a collected specimen derivation") {
        val f = collectedSpecimenDerivationFixtures
        f.processingType.annotationTypes must have size 0
        createFrom(f.processingType) mustSucceed { pt =>
          compare(f.processingType, pt)

          pt.annotationTypes must have size f.processingType.annotationTypes.size.toLong
          TestUtils.checkTimeStamps(pt.timeAdded, OffsetDateTime.now)
          pt.timeModified mustBe (f.processingType.timeModified)
        }
      }

      it("with a processed specimen derivation") {
        val f = processedSpecimenDerivationFixtures
        f.outputProcessingType.annotationTypes must have size 0
        createFrom(f.outputProcessingType) mustSucceed { pt =>
          compare(f.outputProcessingType, pt)

          pt.annotationTypes must have size f.outputProcessingType.annotationTypes.size.toLong
          TestUtils.checkTimeStamps(pt.timeAdded, OffsetDateTime.now)
          pt.timeModified mustBe (f.outputProcessingType.timeModified)
        }
      }
    }

    it("have it's name updated") {
      val f = collectedSpecimenDerivationFixtures
      val name = nameGenerator.next[CollectionEventType]

      f.processingType.withName(name) mustSucceed { pt =>
        compare(f.processingType.copy(name    = name,
                                      version = f.processingType.version + 1),
                pt)

        pt.annotationTypes must have size f.processingType.annotationTypes.size.toLong
        checkTimeStamps(pt, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("have it's description updated") {
      val f = collectedSpecimenDerivationFixtures
      val description = Some(nameGenerator.next[CollectionEventType])

      f.processingType.withDescription(description) mustSucceed { pt =>
        compare(f.processingType.copy(description = description,
                                      version = f.processingType.version + 1),
                pt)

        pt.annotationTypes must have size f.processingType.annotationTypes.size.toLong
        checkTimeStamps(pt, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("be enabled and disabled") {
      val f = collectedSpecimenDerivationFixtures

      val statusTable = Table(("processing type status", "label"),
                              (true, "enabled"),
                              (false, "disabled"))

      forAll (statusTable) { (status, label) =>
        info(label)
        val pt = if (status) f.processingType.enable() else f.processingType.disable()

        compare(f.processingType.copy(enabled = status,
                                      version = f.processingType.version + 1),
                pt)

        pt.annotationTypes must have size f.processingType.annotationTypes.size.toLong
        checkTimeStamps(pt, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("have it's expected input change updated") {
      val f = collectedSpecimenDerivationFixtures
      val change = f.processingType.expectedInputChange + 1

      f.processingType.withExpectedInputChange(change) mustSucceed { pt =>
        compare(f.processingType.copy(expectedInputChange = change,
                                      version             = f.processingType.version + 1),
                pt)

        pt.annotationTypes must have size f.processingType.annotationTypes.size.toLong
        checkTimeStamps(pt, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("have it's expected output change updated") {
      val f = collectedSpecimenDerivationFixtures
      val change = f.processingType.expectedOutputChange + 1

      f.processingType.withExpectedOutputChange(change) mustSucceed { pt =>
        compare(f.processingType.copy(expectedOutputChange = change,
                                      version              = f.processingType.version + 1),
                pt)

        pt.annotationTypes must have size f.processingType.annotationTypes.size.toLong
        checkTimeStamps(pt, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("have it's input count updated") {
      val f = collectedSpecimenDerivationFixtures
      val count = f.processingType.inputCount + 1

      f.processingType.withInputCount(count) mustSucceed { pt =>
        compare(f.processingType.copy(inputCount = count,
                                      version    = f.processingType.version + 1),
                pt)

        pt.annotationTypes must have size f.processingType.annotationTypes.size.toLong
        checkTimeStamps(pt, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("have it's output count updated") {
      val f = collectedSpecimenDerivationFixtures
      val count = f.processingType.outputCount + 1

      f.processingType.withOutputCount(count) mustSucceed { pt =>
        compare(f.processingType.copy(outputCount = count,
                                      version    = f.processingType.version + 1),
                pt)

        pt.annotationTypes must have size f.processingType.annotationTypes.size.toLong
        checkTimeStamps(pt, OffsetDateTime.now, OffsetDateTime.now)
      }
    }


    describe("not be created") {

      it("with an empty study id") {
        val f = collectedSpecimenDerivationFixtures
        val processingType = f.processingType.copy(studyId = StudyId(""))
        createFrom(processingType) mustFail "StudyIdRequired"
      }


      it("not be created with an empty id") {
        val f = collectedSpecimenDerivationFixtures
        val processingType = f.processingType.copy(id = ProcessingTypeId(""))
        createFrom(processingType) mustFail "IdRequired"
      }

      it("not be created with an invalid version") {
        val f = collectedSpecimenDerivationFixtures
        val processingType = f.processingType.copy(version = -2L)
        createFrom(processingType) mustFail "InvalidVersion"
      }

      it("not be created with an invalid name") {
        val f = collectedSpecimenDerivationFixtures

        val invalidNameTable = Table(("invalid name", "label"),
                                     ("", "empty"),
                                     (null, "null"))
        forAll (invalidNameTable) { (invalidName, label) =>
          info(label)
          val processingType = f.processingType.copy(name = invalidName)
          createFrom(processingType) mustFail "NameRequired"
        }
      }

      it("not be created with an invalid description") {
        val f = collectedSpecimenDerivationFixtures

        val invalidDescriptionTable = Table(("invalid description", "label"),
                                            (Some(""), "empty"),
                                            (Some(null), "null"))
        forAll (invalidDescriptionTable) { (invalidDescription, label) =>
          info(label)
          val processingType = f.processingType.copy(description = invalidDescription)
          createFrom(processingType) mustFail "InvalidDescription"
        }
      }

      it("not be created with an invalid expected input change") {
        val f = collectedSpecimenDerivationFixtures
        val processingType = f.processingType.copy(expectedInputChange = -1.0)
        createFrom(processingType) mustFail "InvalidPositiveNumber"
      }

      it("not be created with an invalid expected output change") {
        val f = collectedSpecimenDerivationFixtures
        val processingType = f.processingType.copy(expectedOutputChange = -1.0)
        createFrom(processingType) mustFail "InvalidPositiveNumber"
      }

      it("not be created with an invalid input count") {
        val f = collectedSpecimenDerivationFixtures
        val processingType = f.processingType.copy(inputCount = -1)
        createFrom(processingType) mustFail "InvalidPositiveNumber"
      }

      it("not be created with an invalid output count") {
        val f = collectedSpecimenDerivationFixtures
        val processingType = f.processingType.copy(outputCount = -1)
        createFrom(processingType) mustFail "InvalidPositiveNumber"
      }

      it("not be created with an invalid input container id") {
        val f = collectedSpecimenDerivationFixtures
        val processingType = f.processingType.copy(inputContainerTypeId = Some(ContainerTypeId("")))
        createFrom(processingType) mustFail "ContainerTypeIdRequired"
      }

      it("not be created with an invalid output container id") {
        val f = collectedSpecimenDerivationFixtures
        val processingType = f.processingType.copy(outputContainerTypeId = Some(ContainerTypeId("")))
        createFrom(processingType) mustFail "ContainerTypeIdRequired"
      }

      describe("not be created with an invalid specimen derivation") {

        it("when it is for a collected specimen") {
          val f = collectedSpecimenDerivationFixtures

          val invalidSpecimenDerivationTable =
            Table(("invalid specimen derivation", "error", "label"),
                  (CollectedSpecimenDerivation(CollectionEventTypeId(""),
                                               f.collectedSpecimenDefinition.id,
                                               f.processingSpecimenDefinition),
                   "CollectionEventTypeIdRequired",
                   "invalid collection event type id"),
                  (CollectedSpecimenDerivation(f.collectionEventType.id,
                                               SpecimenDefinitionId(""),
                                               f.processingSpecimenDefinition),
                   "SpecimenDefinitionIdRequired",
                   "invalid specimen definition id"))

          forAll (invalidSpecimenDerivationTable) { (invalidSpecimenDerivation, error, label) =>
            info(label)
            val processingType = f.processingType.copy(specimenDerivation = invalidSpecimenDerivation)
            createFrom(processingType) mustFail error
          }
        }

        it("when it is for a processed specimen") {
          val f = processedSpecimenDerivationFixtures

          val invalidSpecimenDerivationTable =
            Table(("invalid specimen derivation", "error", "label"),
                  (ProcessedSpecimenDerivation(ProcessingTypeId(""),
                                               f.inputSpecimenDefinition.id,
                                               f.outputSpecimenDefinition),
                   "ProcessingTypeIdRequired",
                   "invalid processing type id"),
                  (ProcessedSpecimenDerivation(f.inputProcessingType.id,
                                               SpecimenDefinitionId(""),
                                               f.outputSpecimenDefinition),
                   "SpecimenDefinitionIdRequired",
                   "invalid specimen definition id"))

          forAll (invalidSpecimenDerivationTable) { (invalidSpecimenDerivation, error, label) =>
            info(label)
            val processingType = f.outputProcessingType.copy(
                specimenDerivation = invalidSpecimenDerivation)
            createFrom(processingType) mustFail error
          }
        }
      }

      it("have more than one validation fail") {
        val f = collectedSpecimenDerivationFixtures
        val processingType = f.processingType.copy(version = -2L,
                                                   description = Some(""))
        createFrom(processingType).mustFail("InvalidVersion", "InvalidDescription")
      }

    }

  }

  describe("A processing type's annotation type set can") {

    annotationTypeSetSharedBehaviour

  }

  override def createEntity(): ProcessingType = {
    factory.createProcessingType.copy(annotationTypes = Set.empty)
  }

  override def getAnnotationTypeSet(entity: ProcessingType): Set[AnnotationType] = {
    entity.annotationTypes
  }

  override def addAnnotationType(entity: ProcessingType, annotationType: AnnotationType)
      : DomainValidation[ProcessingType] = {
    entity.withAnnotationType(annotationType)
  }

  override def removeAnnotationType(entity: ProcessingType, id: AnnotationTypeId)
      : DomainValidation[ProcessingType] = {
    entity.removeAnnotationType(id)
  }

}
