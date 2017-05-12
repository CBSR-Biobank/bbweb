package org.biobank.domain

import org.biobank.fixture.NameGenerator
import org.scalatest.FunSpec

trait AnnotationTypeSetSharedSpec[T <: ConcurrencySafeEntity[_]] { this: FunSpec =>
  import org.biobank.TestUtils._

  val factory: Factory

  protected val nameGenerator: NameGenerator

  protected def createEntity(): T

  protected def getAnnotationTypeSet(entity: T): Set[AnnotationType]

  protected def addAnnotationType(entity: T, annotationType: AnnotationType): DomainValidation[T]

  protected def removeAnnotationType(entity: T, annotationTypeId: String): DomainValidation[T]

  def annotationTypeSetSharedBehaviour() = {

    it("add an annotation type") {
      val entity = createEntity
      val annotationTypeCount = getAnnotationTypeSet(entity).size
      val annotationType = factory.createAnnotationType

      addAnnotationType(entity, annotationType) mustSucceed { entity =>
        getAnnotationTypeSet(entity).size mustBe (annotationTypeCount + 1)
        getAnnotationTypeSet(entity) must contain (annotationType)
        ()
      }
    }

    it("replace an annotation type") {
      val entity = createEntity
      val annotationType = factory.createAnnotationType
      addAnnotationType(entity, annotationType) mustSucceed { entity =>
        getAnnotationTypeSet(entity) must contain (annotationType)

        val at2 = annotationType.copy(uniqueId = annotationType.uniqueId)
        addAnnotationType(entity, at2) mustSucceed { e =>
          getAnnotationTypeSet(e) must contain (at2)
          ()
        }
      }
    }

    it("remove an annotation type") {
      val entity = createEntity
      val annotationType = factory.createAnnotationType

      addAnnotationType(entity, annotationType) mustSucceed { entity =>
        getAnnotationTypeSet(entity) must contain (annotationType)
        removeAnnotationType(entity, annotationType.uniqueId) mustSucceed { e =>
          getAnnotationTypeSet(e) must not contain (annotationType)
          ()
        }
      }
    }

    it("not allow adding an annotation type with a duplicate name") {
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
