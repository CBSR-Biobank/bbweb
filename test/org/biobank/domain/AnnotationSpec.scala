package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._

class AnnotationSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val nameGenerator = new NameGenerator(this.getClass)

  "can be created" when {

    "when a string value is given" in {
      val annotationTypeUniqueId = nameGenerator.next[String]
      val stringValue      = Some(nameGenerator.next[String])
      val numberValue      = None
      val selectedValues: Set[String] = Set.empty

      val v = Annotation.create(annotationTypeUniqueId = annotationTypeUniqueId,
                                stringValue            = stringValue,
                                numberValue            = numberValue,
                                selectedValues         = selectedValues)
      v mustSucceed { annotation =>
        annotation must have (
          'annotationTypeUniqueId (annotationTypeUniqueId),
          'stringValue            (stringValue),
          'numberValue            (numberValue),
          'selectedValues         (selectedValues)
        )
      }
    }

    "when a number value is given" in {
      val annotationTypeUniqueId = nameGenerator.next[String]
      val stringValue      = None
      val numberValue      = Some("1.01")
      val selectedValues: Set[String] = Set.empty

      val v = Annotation.create(annotationTypeUniqueId = annotationTypeUniqueId,
                                stringValue      = stringValue,
                                numberValue      = numberValue,
                                selectedValues   = selectedValues)
      v mustSucceed { annotation =>
        annotation must have (
          'annotationTypeUniqueId  (annotationTypeUniqueId),
          'stringValue       (stringValue),
          'numberValue       (numberValue),
          'selectedValues    (selectedValues)
        )
        }
    }

    "when a selected value is given" in {
      val annotationTypeUniqueId = nameGenerator.next[String]
      val stringValue      = None
      val numberValue      = None
      val selectedValues   = Set(annotationTypeUniqueId, nameGenerator.next[String])

      val v = Annotation.create(annotationTypeUniqueId = annotationTypeUniqueId,
                                stringValue      = stringValue,
                                numberValue      = numberValue,
                                selectedValues   = selectedValues)
      v mustSucceed { annotation =>
        annotation must have (
          'annotationTypeUniqueId  (annotationTypeUniqueId),
          'stringValue       (stringValue),
          'numberValue       (numberValue),
          'selectedValues    (selectedValues)
        )
      }
    }

  }

  "not be created" when {

    "annotation type id is empty" in {
      val v = Annotation.create(annotationTypeUniqueId = "",
                                stringValue      = Some(nameGenerator.next[String]),
                                numberValue      = None,
                                selectedValues   = Set.empty)
      v mustFail "AnnotationTypeIdRequired"
    }

    "when no values are assigned" in {
      val v = Annotation.create(annotationTypeUniqueId = nameGenerator.next[String],
                                stringValue      = None,
                                numberValue      = None,
                                selectedValues   = Set.empty)
      v mustFail "at least one value must be assigned"
    }

    "string value is empty" in {
      val v = Annotation.create(annotationTypeUniqueId = nameGenerator.next[String],
                                stringValue      = Some(""),
                                numberValue      = None,
                                selectedValues   = Set.empty)
      v mustFail "NonEmptyStringOption"
    }

    "number value is empty" in {
      val v = Annotation.create(annotationTypeUniqueId = nameGenerator.next[String],
                         stringValue      = None,
                                numberValue      = Some(""),
                                selectedValues   = Set.empty)
      v mustFail "InvalidNumberString"
    }

    "number value is not a number string" in {
      val v = Annotation.create(annotationTypeUniqueId = nameGenerator.next[String],
                                stringValue      = None,
                                numberValue      = Some(nameGenerator.next[String]),
                                selectedValues   = Set.empty)
      v mustFail "InvalidNumberString"
    }

    "the value in selected value is empty" in {
      val annotationTypeUniqueId = nameGenerator.next[String]
      val v = Annotation.create(annotationTypeUniqueId = annotationTypeUniqueId,
                                stringValue      = None,
                                numberValue      = None,
                                selectedValues   = Set(annotationTypeUniqueId, ""))
      v mustFail "NonEmptyString"
    }

  }

}
