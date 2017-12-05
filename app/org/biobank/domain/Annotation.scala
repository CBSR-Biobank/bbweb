package org.biobank.domain

import org.biobank.ValidationKey
import play.api.libs.json._
import play.api.libs.json.Reads._
import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * This is a value type.
 *
 */
final case class Annotation(annotationTypeId: AnnotationTypeId,
                            stringValue:      Option[String],
                            numberValue:      Option[String], // FIXME: should we use java.lang.Number
                            selectedValues:   Set[String]) {

  override def equals(that: Any): Boolean = {
    that match {
      case that: Annotation => this.annotationTypeId == that.annotationTypeId
      case _ => false
    }
  }

  override def hashCode:Int = {
    annotationTypeId.id.toUpperCase.hashCode
  }

}

object Annotation {
  import org.biobank.domain.CommonValidations._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  case object AnnotationTypeIdRequired extends ValidationKey

  implicit val annotationFormat: Format[Annotation] = Json.format[Annotation]

  def create(annotationTypeId: AnnotationTypeId,
             stringValue:      Option[String],
             numberValue:      Option[String],
             selectedValues:   Set[String])
      : DomainValidation[Annotation] = {
    validate(annotationTypeId, stringValue, numberValue, selectedValues)
      .map { _ => Annotation(annotationTypeId, stringValue, numberValue, selectedValues) }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(annotationTypeId: AnnotationTypeId,
               stringValue:      Option[String],
               numberValue:      Option[String],
               selectedValues:   Set[String])
      : DomainValidation[Boolean] = {

    def validateAnnotationOption(opt: String) = {
      validateString(opt, NonEmptyString)
    }

    // at least one, and only one of the three can be assigned
    def validateValues(stringValue:    Option[String],
                       numberValue:    Option[String],
                       selectedValues: Set[String])
        : DomainValidation[Boolean] = {
      if ((selectedValues.isEmpty && stringValue.isDefined && numberValue.isDefined)
            || (!selectedValues.isEmpty && (stringValue.isDefined || numberValue.isDefined))) {
          DomainError("cannot have multiple values assigned").failureNel[Boolean]
      } else {
        true.successNel[String]
      }
    }

    (validateId(annotationTypeId, AnnotationTypeIdRequired) |@|
       validateNonEmptyOption(stringValue, NonEmptyStringOption) |@|
       validateNumberStringOption(numberValue) |@|
       selectedValues.toList.traverseU(validateAnnotationOption) |@|
       validateValues(stringValue, numberValue, selectedValues)) { case _ =>
        true
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(annotation: Annotation): DomainValidation[Boolean] = {
    validate(annotation.annotationTypeId,
             annotation.stringValue,
             annotation.numberValue,
             annotation.selectedValues)
  }

  /**
   * Checks the following:
   *
   *   - no more than one annotation per annotation type
   *   - that each required annotation is present
   *   - that all annotations belong to one annotation type.
   *
   * A DomainError is the result if these conditions fail.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Monad"))
  def validateAnnotations(annotationTypes: Set[AnnotationType],
                          annotations:     List[Annotation])
      : DomainValidation[Boolean]= {

    val requiredAnnotTypeIds = annotationTypes
      .filter(annotationType => annotationType.required)
      .map(annotationType => annotationType.id)
      .toSet

    if (!requiredAnnotTypeIds.isEmpty && annotations.isEmpty) {
      DomainError("missing required annotation type(s)").failureNel[Boolean]
    } else if (annotations.isEmpty && annotationTypes.isEmpty) {
      true.successNel[String]
    } else {
      val annotTypeIdsFromAnnotationsAsSet = annotations.map(v => v.annotationTypeId).toSet

      for {
        hasAnnotationTypes <- {
          if (annotationTypes.isEmpty) DomainError("no annotation types").failureNel[Boolean]
          else true.successNel[String]
        }
        noDuplicates <- {
          if (annotTypeIdsFromAnnotationsAsSet.size != annotations.size) {
            DomainError("duplicate annotations").failureNel[Boolean]
          } else {
            true.successNel[String]
          }
        }
        haveRequired <- {
          if (requiredAnnotTypeIds.intersect(annotTypeIdsFromAnnotationsAsSet).size
                != requiredAnnotTypeIds.size) {
            DomainError("missing required annotation type(s)").failureNel[Boolean]
          } else {
            true.successNel[String]
          }
        }
        allBelong <- {
          val annotTypeIds = annotationTypes.map(at => at.id).toSet
          val notBelonging = annotTypeIdsFromAnnotationsAsSet.diff(annotTypeIds)

          if (notBelonging.isEmpty) {
            true.successNel[String]
          } else {
            DomainError("annotation(s) do not belong to annotation types: "
                          + notBelonging.mkString(", ")).failureNel[Boolean]
          }
        }
      } yield allBelong
    }
  }

}
