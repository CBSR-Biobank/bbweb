package org.biobank.domain

import scalaz.Scalaz._

/** Used to store the value for an annotation.
  *
  */
trait Annotation[T <: AnnotationType] {

  val annotationTypeId: AnnotationTypeId

  val stringValue: Option[String]

  val numberValue: Option[String] //Fixme: change to Option[java.lang.Number]

  val selectedValues: List[AnnotationOption]

}

object Annotation {
  import org.biobank.domain.CommonValidations._

  case object AnnotationTypeIdRequired extends ValidationKey

  def validate(annotationTypeId: AnnotationTypeId,
               stringValue:      Option[String],
               numberValue:      Option[String],
               selectedValues:   List[AnnotationOption])
      : DomainValidation[Boolean] = {

    def validateAnnotationOption(opt: AnnotationOption) = {
      (AnnotationOption.create(opt.annotationTypeId, opt.value) |@|
        validateAnnotationTypeIdEqual(annotationTypeId, opt.annotationTypeId)) {
        case (_, _) => true
      }
    }

    def validateAnnotationTypeIdEqual(id1: AnnotationTypeId, id2: AnnotationTypeId) = {
      if (id1 != id2) {
        DomainError("invalid annotation type id in selected values").failureNel[Boolean]
      } else {
        true.successNel[String]
      }
    }

    // at least one, and only one of the three can be assigned
    def validateValues(stringValue:    Option[String],
                       numberValue:    Option[String],
                       selectedValues: List[AnnotationOption]) = {
      if ((stringValue.isDefined && !numberValue.isDefined && selectedValues.isEmpty)
        || (!stringValue.isDefined && numberValue.isDefined && selectedValues.isEmpty)
        || (!stringValue.isDefined && !numberValue.isDefined && !selectedValues.isEmpty)) {
        true.successNel[String]
      } else if (!stringValue.isDefined && !numberValue.isDefined && selectedValues.isEmpty) {
        DomainError("at least one value must be assigned").failureNel
      } else {
        DomainError("cannot have multiple values assigned").failureNel
      }
    }

    (validateId(annotationTypeId, AnnotationTypeIdRequired) |@|
      validateNonEmptyOption(stringValue, NonEmptyStringOption) |@|
      validateNumberStringOption(numberValue) |@|
      selectedValues.traverseU(validateAnnotationOption) |@|
      validateValues(stringValue, numberValue, selectedValues)) {
      case (_, _, _, _, _) => true
    }
  }

}
