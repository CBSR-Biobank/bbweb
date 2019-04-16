package org.biobank.domain.participants

import org.biobank.fixtures.NameGenerator
import org.biobank.domain._
import org.biobank.domain.annotations._

class AnnotationSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(annotation: Annotation): DomainValidation[Annotation] =
    Annotation.create(annotationTypeId = annotation.annotationTypeId,
                      valueType        = annotation.valueType,
                      stringValue      = annotation.stringValue,
                      numberValue      = annotation.numberValue,
                      selectedValues   = annotation.selectedValues)

  describe("can be created") {

    it("when a string value is given") {
      val annotation = factory.createAnnotation
      createFrom(annotation) mustSucceed { reply =>
        reply must have (
          'annotationTypeId (annotation.annotationTypeId),
          'stringValue      (annotation.stringValue),
          'numberValue      (annotation.numberValue),
          'selectedValues   (annotation.selectedValues)
        )
        ()
      }
    }

    it("when a number value is given") {
      val annotation = factory.createAnnotation.copy(numberValue = Some("1.01"))
      createFrom(annotation) mustSucceed { reply =>
        reply must have (
          'annotationTypeId (annotation.annotationTypeId),
          'stringValue      (annotation.stringValue),
          'numberValue      (annotation.numberValue),
          'selectedValues   (annotation.selectedValues)
        )
        ()
      }
    }

    it("when a selected value is given") {
      val annotationType = factory.createAnnotationType
      val annotation = factory.createAnnotation
        .copy(selectedValues = Set(annotationType.id.id, nameGenerator.next[String]))
      createFrom(annotation) mustSucceed { reply =>
        reply must have (
          'annotationTypeId (annotation.annotationTypeId),
          'stringValue      (annotation.stringValue),
          'numberValue      (annotation.numberValue),
          'selectedValues   (annotation.selectedValues)
        )
        ()
      }
    }

  }

  describe("not be created") {

    it("annotation type id is empty") {
      val annotation = factory.createAnnotation.copy(annotationTypeId = AnnotationTypeId(""))
      createFrom(annotation) mustFail "AnnotationTypeIdRequired"
    }

    it("string value is empty") {
      val annotation = factory.createAnnotation.copy(stringValue = Some(""))
      createFrom(annotation) mustFail "NonEmptyString"
    }

    it("number value is not a number string") {
      val annotation = factory.createAnnotation.copy(numberValue = Some(nameGenerator.next[String]))
      createFrom(annotation) mustFail "InvalidNumberString"
    }

    it("the value in selected value is empty") {
      val annotationType = factory.createAnnotationType
      val annotation = factory.createAnnotation.copy(selectedValues = Set(annotationType.id.id, ""))
      createFrom(annotation) mustFail "NonEmptyString"
    }

  }

}
