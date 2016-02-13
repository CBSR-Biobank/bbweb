package org.biobank.domain.study

import org.biobank.domain.{
  AnnotationType,
  AnnotationTypeSetSharedSpec,
  DomainSpec,
  DomainValidation
}
import org.biobank.fixture.NameGenerator

import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import scalaz.Scalaz._

class CollectionEventTypeSpec extends DomainSpec
    with AnnotationTypeSetSharedSpec[CollectionEventType] {
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
                               ceventType.specimenSpecs,
                               ceventType.annotationTypes)
  }

  "A collection event type" can {

    "be created" in {
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

        cet.specimenSpecs must have size 0
        cet.annotationTypes must have size 0
        checkTimeStamps(cet, DateTime.now, None)
      }
    }

    "have it's name updated" in {
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

        updatedCet.specimenSpecs must have size 0
        updatedCet.annotationTypes must have size 0
        checkTimeStamps(updatedCet, DateTime.now, DateTime.now)
      }
    }

    "have it's description updated" in {
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

        updatedCet.specimenSpecs must have size 0
        updatedCet.annotationTypes must have size 0
        checkTimeStamps(updatedCet, DateTime.now, DateTime.now)
      }
    }

    "have it's recurring field updated" in {
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

          updatedCet.specimenSpecs must have size 0
          updatedCet.annotationTypes must have size 0
          checkTimeStamps(updatedCet, DateTime.now, DateTime.now)
        }
      }
    }

  }

  "A collection event type" must {

    "not be created with an empty study id" in {
      val ceventType = factory.createCollectionEventType.copy(studyId = StudyId(""))
      createFrom(ceventType) mustFail "StudyIdRequired"
    }

    "not be created with an empty id" in {
      val ceventType = factory.createCollectionEventType.copy(id = CollectionEventTypeId(""))
      createFrom(ceventType) mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val ceventType = factory.createCollectionEventType.copy(version = -2L)
      createFrom(ceventType) mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      var ceventType = factory.createCollectionEventType.copy(name = null)
      createFrom(ceventType) mustFail "NameRequired"

      ceventType = factory.createCollectionEventType.copy(name = "")
      createFrom(ceventType) mustFail "NameRequired"
    }

    "not be created with an empty description option" in {
      var ceventType = factory.createCollectionEventType.copy(description = Some(null))
      createFrom(ceventType) mustFail "InvalidDescription"

      ceventType = factory.createCollectionEventType.copy(description = Some(""))
      createFrom(ceventType) mustFail "InvalidDescription"
    }

    "have more than one validation fail" in {
      val ceventType = factory.createCollectionEventType.copy(version = -2L, name = "")
      createFrom(ceventType) mustFail ("InvalidVersion", "NameRequired")
    }

  }

  "A collection event type's specimen spec set" must {

    "add a specimen spec" in {
      val cet = factory.createCollectionEventType.copy(specimenSpecs = Set.empty)
      val specimenSpec = factory.createCollectionSpecimenSpec

      cet.withSpecimenSpec(specimenSpec) mustSucceed { updatedCet =>
        updatedCet mustBe a[CollectionEventType]

        updatedCet must have (
          'studyId     (cet.studyId),
          'id          (cet.id),
          'version     (cet.version + 1),
          'name        (cet.name),
          'description (cet.description),
          'recurring   (cet.recurring)
        )

        updatedCet.specimenSpecs must have size 1
        updatedCet.annotationTypes must have size 0
        checkTimeStamps(updatedCet, DateTime.now, DateTime.now)
      }
    }

    "replace a specimen spec" in {
      val specimenSpec = factory.createCollectionSpecimenSpec
      val specimenSpec2 = factory.createCollectionSpecimenSpec.copy(uniqueId = specimenSpec.uniqueId)
      val cet = factory.createCollectionEventType.copy(specimenSpecs = Set(specimenSpec))

      cet.withSpecimenSpec(specimenSpec2) mustSucceed { updatedCet =>
        updatedCet mustBe a[CollectionEventType]

        updatedCet must have (
          'studyId     (cet.studyId),
          'id          (cet.id),
          'version     (cet.version + 1),
          'name        (cet.name),
          'description (cet.description),
          'recurring   (cet.recurring)
        )

        updatedCet.specimenSpecs must have size 1
        updatedCet.annotationTypes must have size 0
        checkTimeStamps(updatedCet, DateTime.now, DateTime.now)
      }
    }

    "remove a specimen spec" in {
      val specimenSpec = factory.createCollectionSpecimenSpec
      val cet = factory.createCollectionEventType.copy(specimenSpecs = Set(specimenSpec))

      cet.removeSpecimenSpec(specimenSpec.uniqueId) mustSucceed { updatedCet =>
        updatedCet mustBe a[CollectionEventType]

        updatedCet must have (
          'studyId     (cet.studyId),
          'id          (cet.id),
          'version     (cet.version + 1),
          'name        (cet.name),
          'description (cet.description),
          'recurring   (cet.recurring)
        )

        updatedCet.specimenSpecs must have size 0
        updatedCet.annotationTypes must have size 0
        checkTimeStamps(updatedCet, DateTime.now, DateTime.now)
      }
    }

    "not allow adding a specimen spec with a duplicate name" in {
      val specimenSpec = factory.createCollectionSpecimenSpec
      val specimenSpec2 = factory.createCollectionSpecimenSpec.copy(name = specimenSpec.name)
      val cet = factory.createCollectionEventType.copy(specimenSpecs = Set(specimenSpec))

      cet.withSpecimenSpec(specimenSpec2) mustFail "specimen spec name already used.*"
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

  override def removeAnnotationType(entity: CollectionEventType, uniqueId: String)
      : DomainValidation[CollectionEventType] = {
    entity.removeAnnotationType(uniqueId)
  }

  "A collection event type's annotation type set" must {

    annotationTypeSetSharedBehaviour

  }

}
