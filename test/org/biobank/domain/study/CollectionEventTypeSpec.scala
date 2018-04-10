package org.biobank.domain.studies

import java.time.OffsetDateTime
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.fixture.NameGenerator
import org.slf4j.LoggerFactory
import scalaz.Scalaz._

class CollectionEventTypeSpec extends DomainSpec with AnnotationTypeSetSharedSpec[CollectionEventType] {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(ceventType: CollectionEventType): DomainValidation[CollectionEventType] = {
    CollectionEventType.create(ceventType.studyId,
                               ceventType.id,
                               ceventType.version,
                               ceventType.name,
                               ceventType.description,
                               ceventType.recurring,
                               ceventType.specimenDescriptions,
                               ceventType.annotationTypes)
  }

  describe("A collection event type can") {

    it("be created") {
      val ceventType = factory.createCollectionEventType
      createFrom(ceventType) mustSucceed { cet =>
        cet mustBe a[CollectionEventType]

        cet must have (
          'studyId     (ceventType.studyId),
          'id          (ceventType.id),
          'version     (ceventType.version),
          'name        (ceventType.name),
          'description (ceventType.description),
          'recurring   (ceventType.recurring)
        )

        cet.specimenDescriptions must have size 0
        cet.annotationTypes must have size 0
        checkTimeStamps(cet, OffsetDateTime.now, None)
      }
    }

    it("have it's name updated") {
      val cet = factory.createCollectionEventType
      val name = nameGenerator.next[CollectionEventType]

      cet.withName(name) mustSucceed { updatedCet =>
        updatedCet mustBe a[CollectionEventType]

        updatedCet must have (
          'studyId     (cet.studyId),
          'id          (cet.id),
          'version     (cet.version + 1),
          'name        (name),
          'description (cet.description),
          'recurring   (cet.recurring)
        )

        updatedCet.specimenDescriptions must have size 0
        updatedCet.annotationTypes must have size 0
        checkTimeStamps(updatedCet, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("have it's description updated") {
      val cet = factory.createCollectionEventType
      val description = Some(nameGenerator.next[CollectionEventType])

      cet.withDescription(description) mustSucceed { updatedCet =>
        updatedCet mustBe a[CollectionEventType]

        updatedCet must have (
          'studyId     (cet.studyId),
          'id          (cet.id),
          'version     (cet.version + 1),
          'name        (cet.name),
          'description (description),
          'recurring   (cet.recurring)
        )

        updatedCet.specimenDescriptions must have size 0
        updatedCet.annotationTypes must have size 0
        checkTimeStamps(updatedCet, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("have it's recurring field updated") {
      val cet = factory.createCollectionEventType

      List(true, false).foreach { recurring =>
        cet.withRecurring(recurring) mustSucceed { updatedCet =>
          updatedCet mustBe a[CollectionEventType]

          updatedCet must have (
            'studyId     (cet.studyId),
            'id          (cet.id),
            'version     (cet.version + 1),
            'name        (cet.name),
            'description (cet.description),
            'recurring   (recurring)
          )

          updatedCet.specimenDescriptions must have size 0
          updatedCet.annotationTypes must have size 0
          checkTimeStamps(updatedCet, OffsetDateTime.now, OffsetDateTime.now)
        }
      }
    }

  }

  describe("A collection event type") {

    it("not be created with an empty study id") {
      val ceventType = factory.createCollectionEventType.copy(studyId = StudyId(""))
      createFrom(ceventType) mustFail "StudyIdRequired"
    }

    it("not be created with an empty id") {
      val ceventType = factory.createCollectionEventType.copy(id = CollectionEventTypeId(""))
      createFrom(ceventType) mustFail "IdRequired"
    }

    it("not be created with an invalid version") {
      val ceventType = factory.createCollectionEventType.copy(version = -2L)
      createFrom(ceventType) mustFail "InvalidVersion"
    }

    it("not be created with an null or empty name") {
      var ceventType = factory.createCollectionEventType.copy(name = null)
      createFrom(ceventType) mustFail "NameRequired"

      ceventType = factory.createCollectionEventType.copy(name = "")
      createFrom(ceventType) mustFail "NameRequired"
    }

    it("not be created with an empty description option") {
      var ceventType = factory.createCollectionEventType.copy(description = Some(null))
      createFrom(ceventType) mustFail "InvalidDescription"

      ceventType = factory.createCollectionEventType.copy(description = Some(""))
      createFrom(ceventType) mustFail "InvalidDescription"
    }

    it("have more than one validation fail") {
      val ceventType = factory.createCollectionEventType.copy(version = -2L, name = "")
      createFrom(ceventType) mustFail ("InvalidVersion", "NameRequired")
    }

  }

  describe("A collection event type's specimen spec set") {

    it("add a specimen spec") {
      val cet = factory.createCollectionEventType.copy(specimenDescriptions = Set.empty)
      val specimenDescription = factory.createCollectionSpecimenDescription

      cet.withSpecimenDescription(specimenDescription) mustSucceed { updatedCet =>
        updatedCet mustBe a[CollectionEventType]

        updatedCet must have (
          'studyId     (cet.studyId),
          'id          (cet.id),
          'version     (cet.version + 1),
          'name        (cet.name),
          'description (cet.description),
          'recurring   (cet.recurring)
        )

        updatedCet.specimenDescriptions must have size 1
        updatedCet.annotationTypes must have size 0
        checkTimeStamps(updatedCet, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("replace a specimen spec") {
      val specimenDescription = factory.createCollectionSpecimenDescription
      val specimenDescription2 = factory.createCollectionSpecimenDescription.copy(id = specimenDescription.id)
      val cet = factory.createCollectionEventType.copy(specimenDescriptions = Set(specimenDescription))

      cet.withSpecimenDescription(specimenDescription2) mustSucceed { updatedCet =>
        updatedCet mustBe a[CollectionEventType]

        updatedCet must have (
          'studyId     (cet.studyId),
          'id          (cet.id),
          'version     (cet.version + 1),
          'name        (cet.name),
          'description (cet.description),
          'recurring   (cet.recurring)
        )

        updatedCet.specimenDescriptions must have size 1
        updatedCet.annotationTypes must have size 0
        checkTimeStamps(updatedCet, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("remove a specimen spec") {
      val specimenDescription = factory.createCollectionSpecimenDescription
      val cet = factory.createCollectionEventType.copy(specimenDescriptions = Set(specimenDescription))

      cet.removeSpecimenDescription(specimenDescription.id) mustSucceed { updatedCet =>
        updatedCet mustBe a[CollectionEventType]

        updatedCet must have (
          'studyId     (cet.studyId),
          'id          (cet.id),
          'version     (cet.version + 1),
          'name        (cet.name),
          'description (cet.description),
          'recurring   (cet.recurring)
        )

        updatedCet.specimenDescriptions must have size 0
        updatedCet.annotationTypes must have size 0
        checkTimeStamps(updatedCet, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("not allow adding a specimen spec with a duplicate name") {
      val specimenDescription = factory.createCollectionSpecimenDescription
      val specimenDescription2 = factory.createCollectionSpecimenDescription.copy(name = specimenDescription.name)
      val cet = factory.createCollectionEventType.copy(specimenDescriptions = Set(specimenDescription))

      cet.withSpecimenDescription(specimenDescription2) mustFail "specimen spec name already used.*"
    }
  }

  override def createEntity(): CollectionEventType = {
    factory.createCollectionEventType.copy(annotationTypes = Set.empty)
  }

  override def getAnnotationTypeSet(entity: CollectionEventType): Set[AnnotationType] = {
    entity.annotationTypes
  }

  override def addAnnotationType(entity:         CollectionEventType,
                                 annotationType: AnnotationType)
      : DomainValidation[CollectionEventType] = {
    entity.withAnnotationType(annotationType)
  }

  override def removeAnnotationType(entity: CollectionEventType, id: AnnotationTypeId)
      : DomainValidation[CollectionEventType] = {
    entity.removeAnnotationType(id)
  }

  describe("A collection event type's annotation type set") {

    annotationTypeSetSharedBehaviour

  }

}
