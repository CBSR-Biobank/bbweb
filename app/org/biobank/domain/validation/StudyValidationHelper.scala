package org.biobank.domain.validation

import org.biobank.domain.study.{
  ProcessingTypeId,
  SpecimenGroupId,
  StudyId
}
import org.biobank.domain.{ AnnotationTypeId, DomainValidation, DomainError }
import org.biobank.infrastructure.AnnotationTypeData

import scalaz._
import scalaz.Scalaz._

trait StudyValidationHelper extends ValidationHelper {

}

trait StudyAnnotationTypeValidationHelper extends StudyValidationHelper {

  def validateMaxValueCount(option: Option[Int]): DomainValidation[Option[Int]] =
    option match {
      case Some(n) =>
        if (n > -1) option.success else s"max value count is not a positive number".failNel
      case None =>
        none.success
    }

  /**
    *  Validates each item in the map and returns all failures.
    */
  def validateOptions(
    options: Option[Seq[String]]): DomainValidation[Option[Seq[String]]] = {

      options match {
        case Some(optionsSeq) =>
          if (optionsSeq.distinct.size == optionsSeq.size) {
            optionsSeq.toList.map(validateNonEmpty(_, "option is empty or null")).sequenceU.fold(
              err => err.fail,
              list => Some(list.toSeq).success
            )
          } else {
            DomainError("duplicate items in options").failNel
          }
        case None => none.success
      }
  }

  /**
    *  Validates each item in the list and returns all failures if they exist.
    */
  protected def validateAnnotationTypeData[T <: AnnotationTypeData](
    annotationTypeData: List[T]): DomainValidation[List[T]] = {

    def validateAnnotationTypeItem(annotationTypeItem: T): DomainValidation[T] = {
      validateStringId(annotationTypeItem.annotationTypeId, "annotation type id is null or empty").fold(
        err => err.fail,
        id => annotationTypeItem.success
      )
    }

    annotationTypeData.map(validateAnnotationTypeItem).sequenceU
  }
}
