package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._

trait StudyAnnotationSpec[T <: Annotation[_]] extends DomainSpec {
  import org.biobank.TestUtils._

  val nameGenerator = new NameGenerator(this.getClass)

  protected def createAnnotation(annotationTypeId: AnnotationTypeId,
                                 stringValue:      Option[String],
                                 numberValue:      Option[String],
                                 selectedValues:   List[AnnotationOption])
      : DomainValidation[T]

  def annotationBehaviour() = {

    "can be created" when {

      "when a string value is given" in {
        val annotationTypeId = AnnotationTypeId(nameGenerator.next[String])
        val stringValue      = Some(nameGenerator.next[String])
        val numberValue      = None
        val selectedValues   = List.empty

        val v = createAnnotation(annotationTypeId = annotationTypeId,
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
        }
      }

      "when a number value is given" in {
        val annotationTypeId = AnnotationTypeId(nameGenerator.next[String])
        val stringValue      = None
        val numberValue      = Some("1.01")
        val selectedValues   = List.empty

        val v = createAnnotation(annotationTypeId = annotationTypeId,
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
        }
      }

      "when a selected value is given" in {
        val annotationTypeId = AnnotationTypeId(nameGenerator.next[String])
        val stringValue      = None
        val numberValue      = None
        val selectedValues   = List(AnnotationOption(annotationTypeId, nameGenerator.next[String]))

        val v = createAnnotation(annotationTypeId = annotationTypeId,
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
        }
      }

    }

    "not be created" when {

      "annotation type id is empty" in {
        val v = createAnnotation(annotationTypeId = AnnotationTypeId(""),
                                 stringValue      = Some(nameGenerator.next[String]),
                                 numberValue      = None,
                                 selectedValues   = List.empty)
        v mustFail "AnnotationTypeIdRequired"
      }

      "when no values are assigned" in {
        val v = createAnnotation(annotationTypeId = AnnotationTypeId(nameGenerator.next[String]),
                                 stringValue      = None,
                                 numberValue      = None,
                                 selectedValues   = List.empty)
        v mustFail "at least one value must be assigned"
      }

      "string value is empty" in {
        val v = createAnnotation(annotationTypeId = AnnotationTypeId(nameGenerator.next[String]),
                                 stringValue      = Some(""),
                                 numberValue      = None,
                                 selectedValues   = List.empty)
        v mustFail "NonEmptyStringOption"
      }

      "number value is empty" in {
        val v = createAnnotation(annotationTypeId = AnnotationTypeId(nameGenerator.next[String]),
                                 stringValue      = None,
                                 numberValue      = Some(""),
                                 selectedValues   = List.empty)
        v mustFail "InvalidNumberString"
      }

      "number value is not a number string" in {
        val v = createAnnotation(annotationTypeId = AnnotationTypeId(nameGenerator.next[String]),
                                 stringValue      = None,
                                 numberValue      = Some(nameGenerator.next[String]),
                                 selectedValues   = List.empty)
        v mustFail "InvalidNumberString"
      }

      "annotation type id in selected value does not match the annotations" in {
        val annotationTypeId = AnnotationTypeId(nameGenerator.next[String])
        val badAnnotationTypeId = AnnotationTypeId(annotationTypeId.id + nameGenerator.next[String])
        val v = createAnnotation(annotationTypeId = annotationTypeId,
                                 stringValue      = None,
                                 numberValue      = None,
                                 selectedValues   = List(
                                   AnnotationOption(badAnnotationTypeId, nameGenerator.next[String])))
        v mustFail "invalid annotation type id in selected values"
      }

      "the value in selected value is empty" in {
        val annotationTypeId = AnnotationTypeId(nameGenerator.next[String])
        val v = createAnnotation(annotationTypeId = annotationTypeId,
                                 stringValue      = None,
                                 numberValue      = None,
                                 selectedValues   = List(AnnotationOption(annotationTypeId, "")))
        v mustFail "NonEmptyString"
      }

    }

  }
}
