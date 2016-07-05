package org.biobank.domain

import org.biobank.fixture.NameGenerator

trait AnnotationTypeSetSharedSpec[T <: ConcurrencySafeEntity[_]]
    extends DomainSpec {
  import org.biobank.TestUtils._

  protected val nameGenerator: NameGenerator

  protected def createEntity(): T

  protected def getAnnotationTypeSet(entity: T): Set[AnnotationType]

  protected def addAnnotationType(entity: T, annotationType: AnnotationType): DomainValidation[T]

  protected def removeAnnotationType(entity: T, annotationTypeId: String): DomainValidation[T]

  def annotationTypeSetSharedBehaviour() = {

    "add an annotation type" in {
      val entity = createEntity
      val annotationTypeCount = getAnnotationTypeSet(entity).size
      val annotationType = factory.createAnnotationType

      addAnnotationType(entity, annotationType) mustSucceed { entity =>
        getAnnotationTypeSet(entity).size mustBe (annotationTypeCount + 1)
        getAnnotationTypeSet(entity) must contain (annotationType)
      }
    }

    "replace an annotation type" in {
      val entity = createEntity
      val annotationType = factory.createAnnotationType
      addAnnotationType(entity, annotationType) mustSucceed { entity =>
        getAnnotationTypeSet(entity) must contain (annotationType)

        val at2 = annotationType.copy(uniqueId = annotationType.uniqueId)
        addAnnotationType(entity, at2) mustSucceed { e =>
          getAnnotationTypeSet(e) must contain (at2)
        }
      }
    }

    "remove an annotation type" in {
      val entity = createEntity
      val annotationType = factory.createAnnotationType

      addAnnotationType(entity, annotationType) mustSucceed { entity =>
        getAnnotationTypeSet(entity) must contain (annotationType)
        removeAnnotationType(entity, annotationType.uniqueId) mustSucceed { e =>
          getAnnotationTypeSet(e) must not contain (annotationType)
        }
      }
    }

    "not allow adding an annotation type with a duplicate name" in {
      val entity = createEntity
      val annotationType = factory.createAnnotationType
      addAnnotationType(entity, annotationType) mustSucceed { entity =>
        getAnnotationTypeSet(entity) must contain (annotationType)

        val at2 = factory.createAnnotationType.copy(name = annotationType.name)
        addAnnotationType(entity, at2) mustFail "annotation type name already used.*"
      }
    }

  }
}
