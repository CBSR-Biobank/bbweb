package org.biobank.domain

import play.api.libs.json._
import play.api.libs.json.Reads._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * This is a value type.
 *
 */
case class Annotation(annotationTypeUniqueId: String,
                      stringValue:            Option[String],
                      numberValue:            Option[String], // FIXME: should we use java.lang.Number
                      selectedValues:         Set[String])

object Annotation {
  import org.biobank.domain.CommonValidations._

  case object AnnotationTypeUniqueIdRequired extends ValidationKey

  implicit val annotationFormat = Json.format[Annotation]

  def create(annotationTypeUniqueId: String,
             stringValue:            Option[String],
             numberValue:            Option[String],
             selectedValues:         Set[String])
      : DomainValidation[Annotation] = {
    validate(annotationTypeUniqueId, stringValue, numberValue, selectedValues)
      .map { _ => Annotation(annotationTypeUniqueId, stringValue, numberValue, selectedValues) }
  }

  def validate(annotationTypeUniqueId: String,
               stringValue:            Option[String],
               numberValue:            Option[String],
               selectedValues:         Set[String])
      : DomainValidation[Boolean] = {

    def validateAnnotationOption(opt: String) = {
      validateString(opt, NonEmptyString)
    }

    // at least one, and only one of the three can be assigned
    def validateValues(stringValue:    Option[String],
                       numberValue:    Option[String],
                       selectedValues: Set[String])
        : DomainValidation[Boolean] = {
      if ((stringValue.isDefined && !numberValue.isDefined && selectedValues.isEmpty)
        || (!stringValue.isDefined && numberValue.isDefined && selectedValues.isEmpty)
        || (!stringValue.isDefined && !numberValue.isDefined && !selectedValues.isEmpty)) {
        true.successNel[String]
      } else if (!stringValue.isDefined && !numberValue.isDefined && selectedValues.isEmpty) {
        DomainError("at least one value must be assigned").failureNel[Boolean]
      } else {
        DomainError("cannot have multiple values assigned").failureNel[Boolean]
      }
    }

    (validateString(annotationTypeUniqueId, AnnotationTypeUniqueIdRequired) |@|
       validateNonEmptyOption(stringValue, NonEmptyStringOption) |@|
       validateNumberStringOption(numberValue) |@|
       selectedValues.toList.traverseU(validateAnnotationOption) |@|
       validateValues(stringValue, numberValue, selectedValues)) {
      case (_, _, _, _, _) => true
    }
  }

  def validate(annotation: Annotation): DomainValidation[Boolean] = {
    validate(annotation.annotationTypeUniqueId,
             annotation.stringValue,
             annotation.numberValue,
             annotation.selectedValues)
  }

  /**
   * Checks the following:
   *
   *   - no more than one annotation per annotation type
   *   - that each required annotation is present
   *   - that all annotations belong to the one annotation type.
   *
   * A DomainError is the result if these conditions fail.
   */
  def validateAnnotations(annotationTypes: Set[AnnotationType],
                          annotations:     Set[Annotation])
      : DomainValidation[Boolean]= {
    if (annotationTypes.isEmpty && annotations.isEmpty) {
      true.success
    } else {
      val annotTypeUniqueIdsAsSet = annotations.map(v => v.annotationTypeUniqueId).toSet
      for {
        hasAnnotationTypes <- {
          if (annotationTypes.isEmpty) {
            DomainError("no annotation types").failureNel
          } else {
            true.success
          }
        }
        noDuplicates <- {
          val annotAnnotTypeIdsAsList = annotations.map(v => v.annotationTypeUniqueId).toList

          if (annotTypeUniqueIdsAsSet.size != annotAnnotTypeIdsAsList.size) {
            DomainError("duplicate annotation types in annotations").failureNel
          } else {
            true.success
          }
        }
        haveRequired <- {
          val requiredAnnotTypeIds = annotationTypes
            .filter(annotationType => annotationType.required)
            .map(annotationType => annotationType.uniqueId)
            .toSet

          if (requiredAnnotTypeIds.intersect(annotTypeUniqueIdsAsSet).size != requiredAnnotTypeIds.size) {
            DomainError("missing required annotation type(s)").failureNel
          } else {
            true.success
          }
        }
        allBelong <- {
          if (annotTypeUniqueIdsAsSet.isEmpty) {
            // no annotations present
            true.success
          } else {
            val annotTypeIds = annotationTypes
              .map(annotationTypes => annotationTypes.uniqueId)
              .toSet

            val notBelonging = annotTypeIds.diff(annotTypeUniqueIdsAsSet)
            if (notBelonging.isEmpty) {
              true.success
            } else {
              DomainError("annotation(s) do not belong to annotation types: "
                + notBelonging.mkString(", ")).failureNel
            }
          }
        }
      } yield allBelong
    }
  }

}
