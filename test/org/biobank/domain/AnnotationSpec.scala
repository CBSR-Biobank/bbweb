package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._

class AnnotationSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val nameGenerator = new NameGenerator(this.getClass)

  describe("can be created") {

    it("when a string value is given") {
      val annotationTypeId = nameGenerator.next[String]
      val stringValue      = Some(nameGenerator.next[String])
      val numberValue      = None
      val selectedValues: Set[String] = Set.empty

      val v = Annotation.create(annotationTypeId = annotationTypeId,
                                stringValue            = stringValue,
                                numberValue            = numberValue,
                                selectedValues         = selectedValues)
      v mustSucceed { annotation =>
        annotation must have (
          'annotationTypeId (annotationTypeId),
          'stringValue            (stringValue),
          'numberValue            (numberValue),
          'selectedValues         (selectedValues)
        )
        ()
      }
    }

    it("when a number value is given") {
      val annotationTypeId = nameGenerator.next[String]
      val stringValue      = None
      val numberValue      = Some("1.01")
      val selectedValues: Set[String] = Set.empty

      val v = Annotation.create(annotationTypeId = annotationTypeId,
                                stringValue      = stringValue,
                                numberValue      = numberValue,
                                selectedValues   = selectedValues)
      v mustSucceed { annotation =>
        annotation must have (
          'annotationTypeId  (annotationTypeId),
          'stringValue       (stringValue),
          'numberValue       (numberValue),
          'selectedValues    (selectedValues)
        )
        ()
      }
    }

    it("when a selected value is given") {
      val annotationTypeId = nameGenerator.next[String]
      val stringValue      = None
      val numberValue      = None
      val selectedValues   = Set(annotationTypeId, nameGenerator.next[String])

      val v = Annotation.create(annotationTypeId = annotationTypeId,
                                stringValue      = stringValue,
                                numberValue      = numberValue,
                                selectedValues   = selectedValues)
      v mustSucceed { annotation =>
        annotation must have (
          'annotationTypeId  (annotationTypeId),
          'stringValue       (stringValue),
          'numberValue       (numberValue),
          'selectedValues    (selectedValues)
        )
        ()
      }
    }

    it("when number value is empty") {
      val annotationTypeId = nameGenerator.next[String]
      val stringValue      = None
      val numberValue      = Some("")
      val selectedValues   = Set.empty[String]

      val v = Annotation.create(annotationTypeId = annotationTypeId,
                                stringValue      = stringValue,
                                numberValue      = numberValue,
                                selectedValues   = selectedValues)
      v mustSucceed { annotation =>
        annotation must have (
          'annotationTypeId  (annotationTypeId),
          'stringValue       (stringValue),
          'numberValue       (numberValue),
          'selectedValues    (selectedValues)
        )
        ()
      }
    }

  }

  describe("not be created") {

    it("annotation type id is empty") {
      val v = Annotation.create(annotationTypeId = "",
                                stringValue      = Some(nameGenerator.next[String]),
                                numberValue      = None,
                                selectedValues   = Set.empty)
      v mustFail "AnnotationTypeIdRequired"
    }

    it("string value is empty") {
      val v = Annotation.create(annotationTypeId = nameGenerator.next[String],
                                stringValue      = Some(""),
                                numberValue      = None,
                                selectedValues   = Set.empty)
      v mustFail "NonEmptyStringOption"
    }

    it("number value is not a number string") {
      val v = Annotation.create(annotationTypeId = nameGenerator.next[String],
                                stringValue      = None,
                                numberValue      = Some(nameGenerator.next[String]),
                                selectedValues   = Set.empty)
      v mustFail "InvalidNumberString"
    }

    it("the value in selected value is empty") {
      val annotationTypeId = nameGenerator.next[String]
      val v = Annotation.create(annotationTypeId = annotationTypeId,
                                stringValue      = None,
                                numberValue      = None,
                                selectedValues   = Set(annotationTypeId, ""))
      v mustFail "NonEmptyString"
    }

  }

}
