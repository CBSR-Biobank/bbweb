package org.biobank.domain

import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure._

import scalaz._
import Scalaz._

/** Annotation types define an [[Annotation]].
  *
  * Annotations allow sub classes to collect custom named and defined pieces of data.
  */
trait AnnotationType
  extends ConcurrencySafeEntity[AnnotationTypeId]
    with HasUniqueName
    with HasDescriptionOption {

  /** The type of information stored by the annotation. I.e. text, number, date, or an item from a drop down
    * list. See [[AnnotationValueType]].
    */
  val valueType: AnnotationValueType


  /** When valueType is [[AnnotationValueType.Select]] (i.e. a drop down list), this is the number of items
    * allowed to be selected. If the value is 0 then any number of values can be selected.
    */
  val maxValueCount: Option[Int]


  /** When valueType is [[AnnotationValueType.Select]], these are the list of options allowed to
    * be selected.
    *
    * @todo describe why this is a map.
    */
  val options: Seq[String]
}

trait AnnotationTypeValidations {
  import org.biobank.domain.CommonValidations._

  case object MaxValueCountError extends ValidationKey

  case object OptionRequired extends ValidationKey

  case object DuplicateOptionsError extends ValidationKey

  def validateMaxValueCount(option: Option[Int]): DomainValidation[Option[Int]] = {
    option.fold {
      none[Int].successNel[String]
    } { n  =>
      if (n > -1) {
        option.successNel
      } else {
        MaxValueCountError.toString.failureNel[Option[Int]]
      }
    }
  }

  /**
    *  Validates each item in the map and returns all failures.
    */
  def validateOptions(options: Seq[String]): DomainValidation[Seq[String]] = {
      if (options.distinct.size == options.size) {
        options.toList.map(validateString(_, OptionRequired)).sequenceU.fold(
          err => err.toList.mkString(",").failureNel[Seq[String]],
          list => list.toSeq.successNel
        )
      } else {
        DuplicateOptionsError.toString.failureNel[Seq[String]]
      }
  }

  /**
    *  Validates each item in the list and returns all failures if they exist.
    */
  protected def validateAnnotationTypeData[T <: AnnotationTypeData](
    annotationTypeData: List[T]): DomainValidation[List[T]] = {

    def validateAnnotationTypeItem(annotationTypeItem: T): DomainValidation[T] = {
      validateString(annotationTypeItem.annotationTypeId, IdRequired).fold(
        err => err.failure,
        id => annotationTypeItem.success
      )
    }

    annotationTypeData.map(validateAnnotationTypeItem).sequenceU
  }

  /** If an annotation type is for a select, the following is required:
    *
    * - max value count must be 1 or 2
    * - options must be a non-empty sequence
    *
    * If an annotation type is not for a select, the following is required
    *
    * - max value count must be 0
    * - options must be None
    */
  def validateSelectParams(
    valueType: AnnotationValueType, maxValueCount: Option[Int], options: Seq[String])
      : DomainValidation[Boolean] = {
    if (valueType == AnnotationValueType.Select) {
      maxValueCount.fold {
        DomainError(s"max value count is invalid for select").failureNel[Boolean]
      } { count =>
        val countValidation = if ((count < 1) || (count > 2)) {
          DomainError(s"select annotation type with invalid maxValueCount: $count").failureNel[Boolean]
        } else {
          true.successNel[String]
        }

        val optionsValidation = if (options.isEmpty) {
          DomainError("select annotation type with no options to select").failureNel[Boolean]
        } else {
          true.successNel[String]
        }

        (countValidation |@| optionsValidation)(_ && _)
      }
    } else {
      val countValidation = maxValueCount.fold {
        true.successNel[String]
      } { count =>
        DomainError(s"max value count is invalid for non-select").failureNel[Boolean]
      }

      val optionsValidation = if (options.isEmpty) {
        true.successNel[String]
      } else {
        DomainError("non select annotation type with options to select").failureNel[Boolean]
      }

      (countValidation |@| optionsValidation)(_ && _)
    }
  }

}
