package org.biobank.domain.studies

import java.time.OffsetDateTime
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.containers._
import org.biobank.fixtures._
import org.slf4j.LoggerFactory
import org.scalatest.prop.TableDrivenPropertyChecks._
import shapeless._

class ProcessingTypeSpec
    extends DomainSpec
    with ProcessingTypeFixtures
    with AnnotationTypeSetSharedSpec[ProcessingType] {
  import org.biobank.TestUtils._
  import org.biobank.matchers.EntityMatchers._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(processingType: ProcessingType): DomainValidation[ProcessingType] =
    ProcessingType.create(studyId         = processingType.studyId,
                          id              = processingType.id,
                          version         = processingType.version,
                          name            = processingType.name,
                          description     = processingType.description,
                          enabled         = processingType.enabled,
                          input           = processingType.input,
                          output          = processingType.output,
                          annotationTypes = processingType.annotationTypes)

  describe("A processing type can") {

    describe("be created") {

      it("with a collected specimen definition") {
        val f = new CollectionSpecimenDefinitionFixtures
        f.processingType.annotationTypes must have size 0
        createFrom(f.processingType) mustSucceed { pt =>
          pt must matchProcessingType(f.processingType)
        }
      }

      it("with a processed specimen definition") {
        val f = new ProcessedSpecimenDefinitionFixtures
        f.outputProcessingType.annotationTypes must have size 0
        createFrom(f.outputProcessingType) mustSucceed { pt =>
          pt must matchProcessingType(f.outputProcessingType)
        }
      }
    }

    it("have it's name updated") {
      val f = new CollectionSpecimenDefinitionFixtures
      val name = nameGenerator.next[CollectionEventType]

      f.processingType.withName(name) mustSucceed { pt =>
        val updatedPt = f.processingType.copy(slug         = Slug(name),
                                              name         = name,
                                              version      = f.processingType.version + 1,
                                              timeModified = Some(OffsetDateTime.now))
        pt must matchProcessingType(updatedPt)
      }
    }

    it("have it's description updated") {
      val f = new CollectionSpecimenDefinitionFixtures
      val description = Some(nameGenerator.next[CollectionEventType])

      f.processingType.withDescription(description) mustSucceed { pt =>
        val updatedPt = f.processingType.copy(description = description,
                                              version = f.processingType.version + 1,
                                              timeModified = Some(OffsetDateTime.now))
        pt must matchProcessingType(updatedPt)
      }
    }

    it("be enabled and disabled") {
      val f = new CollectionSpecimenDefinitionFixtures

      val statusTable = Table(("processing type status", "label"),
                              (true, "enabled"),
                              (false, "disabled"))

      forAll (statusTable) { (status, label) =>
        info(label)
        val pt = if (status) f.processingType.enable() else f.processingType.disable()
        val updatedPt = f.processingType.copy(enabled = status,
                                              version = f.processingType.version + 1,
                                              timeModified = Some(OffsetDateTime.now))
        pt must matchProcessingType(updatedPt)
      }
    }

    it("have it's expected input change updated") {
      val f = new CollectionSpecimenDefinitionFixtures
      val input = f.processingType.input
        .copy(expectedChange = f.processingType.input.expectedChange + 1)

      f.processingType.withInputSpecimenProcessing(input) mustSucceed { pt =>
        val updateLens = lens[ProcessingType].version ~
          lens[ProcessingType].timeModified ~
          lens[ProcessingType].input

        val updatedPt = updateLens.set(f.processingType)(Tuple3(f.processingType.version + 1,
                                                                Some(OffsetDateTime.now),
                                                                input))
        pt must matchProcessingType(updatedPt)
      }
    }

    it("have it's expected output change updated") {
      val f = new CollectionSpecimenDefinitionFixtures
      val output = f.processingType.output
        .copy(expectedChange = f.processingType.output.expectedChange + 1)

      f.processingType.withOutputSpecimenProcessing(output) mustSucceed { pt =>
        val updateLens = lens[ProcessingType].version ~
          lens[ProcessingType].timeModified ~
          lens[ProcessingType].output

        val updatedPt = updateLens.set(f.processingType)(Tuple3(f.processingType.version + 1,
                                                                Some(OffsetDateTime.now),
                                                                output))
        pt must matchProcessingType(updatedPt)
      }
    }

    it("have it's input count updated") {
      val f = new  CollectionSpecimenDefinitionFixtures
      val input = f.processingType.input
        .copy(count = f.processingType.input.count + 1)

      f.processingType.withInputSpecimenProcessing(input) mustSucceed { pt =>
        val updateLens = lens[ProcessingType].version ~
        lens[ProcessingType].timeModified ~
        lens[ProcessingType].input

        val updatedPt = updateLens.set(f.processingType)(Tuple3(f.processingType.version + 1,
                                                                Some(OffsetDateTime.now),
                                                                input))
        pt must matchProcessingType(updatedPt)
      }
    }

    it("have it's output count updated") {
      val f = new  CollectionSpecimenDefinitionFixtures
      val output = f.processingType.output
        .copy(count = f.processingType.output.count + 1)

      f.processingType.withOutputSpecimenProcessing(output) mustSucceed { pt =>
        val updateLens = lens[ProcessingType].version ~
          lens[ProcessingType].timeModified ~
          lens[ProcessingType].output

        val updatedPt = updateLens.set(f.processingType)(Tuple3(f.processingType.version + 1,
                                                                Some(OffsetDateTime.now),
                                                                output))
        pt must matchProcessingType(updatedPt)
      }
    }


    describe("not be created") {

      it("with an empty study id") {
        val f = new CollectionSpecimenDefinitionFixtures
        val processingType = f.processingType.copy(studyId = StudyId(""))
        createFrom(processingType) mustFail "StudyIdRequired"
      }


      it("not be created with an empty id") {
        val f = new CollectionSpecimenDefinitionFixtures
        val processingType = f.processingType.copy(id = ProcessingTypeId(""))
        createFrom(processingType) mustFail "IdRequired"
      }

      it("not be created with an invalid version") {
        val f = new CollectionSpecimenDefinitionFixtures
        val processingType = f.processingType.copy(version = -2L)
        createFrom(processingType) mustFail "InvalidVersion"
      }

      it("not be created with an invalid name") {
        val f = new CollectionSpecimenDefinitionFixtures

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
        val f = new CollectionSpecimenDefinitionFixtures

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
        val f = new CollectionSpecimenDefinitionFixtures
        val changeLens = lens[ProcessingType].input.expectedChange
        val processingType = changeLens.set(f.processingType)(-1.0)
        createFrom(processingType) mustFail "InvalidPositiveNumber"
      }

      it("not be created with an invalid expected output change") {
        val f = new CollectionSpecimenDefinitionFixtures
        val changeLens = lens[ProcessingType].output.expectedChange
        val processingType = changeLens.set(f.processingType)(-1.0)
        createFrom(processingType) mustFail "InvalidPositiveNumber"
      }

      it("not be created with an invalid input count") {
        val f = new CollectionSpecimenDefinitionFixtures
        val changeLens = lens[ProcessingType].input.count
        val processingType = changeLens.set(f.processingType)(-1)
        createFrom(processingType) mustFail "InvalidPositiveNumber"
      }

      it("not be created with an invalid output count") {
        val f = new CollectionSpecimenDefinitionFixtures
        val changeLens = lens[ProcessingType].output.count
        val processingType = changeLens.set(f.processingType)(-1)
        createFrom(processingType) mustFail "InvalidPositiveNumber"
      }

      it("not be created with an invalid input container id") {
        val f = new CollectionSpecimenDefinitionFixtures
        val changeLens = lens[ProcessingType].input.containerTypeId
        val processingType = changeLens.set(f.processingType)(Some(ContainerTypeId("")))
        createFrom(processingType) mustFail "ContainerTypeIdRequired"
      }

      it("not be created with an invalid output container id") {
        val f = new CollectionSpecimenDefinitionFixtures
        val changeLens = lens[ProcessingType].output.containerTypeId
        val processingType = changeLens.set(f.processingType)(Some(ContainerTypeId("")))
        createFrom(processingType) mustFail "ContainerTypeIdRequired"
      }

      describe("not be created with an invalid input specimen definition") {

        val entityIdLens = lens[ProcessingType].input.entityId
        val specimenDefinitionIdLens = lens[ProcessingType].input.specimenDefinitionId
        val inputSpecimenInfoLens = entityIdLens ~ specimenDefinitionIdLens

        it("when it is for a collected specimen") {
          val f = new CollectionSpecimenDefinitionFixtures

          val invalidSpecimenDefinitionTable =
            Table(("processingType", "error", "label"),
                  (inputSpecimenInfoLens.set(f.processingType)(Tuple2(CollectionEventTypeId(""),
                                                                      f.collectionSpecimenDefinition.id)),
                   "CollectionEventTypeIdRequired",
                   "invalid collection event type id"),
                  (inputSpecimenInfoLens.set(f.processingType)(Tuple2(f.collectionEventType.id,
                                                                      SpecimenDefinitionId(""))),
                   "SpecimenDefinitionIdRequired",
                   "invalid specimen definition id"))

          forAll (invalidSpecimenDefinitionTable) { (processingType, error, label) =>
            info(label)
            createFrom(processingType) mustFail error
          }
        }

        it("when it is for a processed specimen") {
          val f = new ProcessedSpecimenDefinitionFixtures

          val invalidSpecimenDefinitionTable =
            Table(("invalid specimen definition", "error", "label"),
                  (inputSpecimenInfoLens.set(f.outputProcessingType)(Tuple2(ProcessingTypeId(""),
                                                                            f.inputSpecimenDefinition.id)),
                   "ProcessingTypeIdRequired",
                   "invalid processing type id"),
                  (inputSpecimenInfoLens.set(f.outputProcessingType)(Tuple2(f.inputProcessingType.id,
                                                                            SpecimenDefinitionId(""))),
                   "SpecimenDefinitionIdRequired",
                   "invalid specimen definition id"))

          forAll (invalidSpecimenDefinitionTable) { (processingType, error, label) =>
            info(label)
            createFrom(processingType) mustFail error
          }
        }
      }

      it("have more than one validation fail") {
        val f = new CollectionSpecimenDefinitionFixtures
        val processingType = f.processingType.copy(version = -2L,
                                                   description = Some(""))
        createFrom(processingType).mustFail("InvalidVersion", "InvalidDescription")
      }

    }

    it("not be created with an invalid output specimen definition") {
      val f = new CollectionSpecimenDefinitionFixtures

      val updateLens = lens[ProcessingType].output.specimenDefinition.name ~
        lens[ProcessingType].output.specimenDefinition.description

      val processingType = updateLens.set(f.processingType)(Tuple2("", Some("")))
      createFrom(processingType).mustFail("NameRequired", "InvalidDescription")
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
