package org.biobank.domain.study

import org.biobank.domain.{ AnnotationTypeId, DomainValidation, DomainError, ValidationKey }
import org.biobank.domain.AnnotationType
import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure.AnnotationTypeData

import scalaz._
import scalaz.Scalaz._

/**
  * StudyAnnotationTypes allow a study to collect custom named and defined pieces of data on
  * [[CollectionEvent]]s, [[ProcessingEvent]]s, and [[Participant]]s. Annotations are
  * optional and are not a requirement for specimen collection or processing.
  */
trait StudyAnnotationType extends AnnotationType with HasStudyId

trait StudyAnnotationTypeValidations {
  import org.biobank.domain.CommonValidations._

  case object StudyIdRequired extends ValidationKey

  case object MaxValueCountError extends ValidationKey

  case object OptionRequired extends ValidationKey

  case object DuplicateOptionsError extends ValidationKey

  def validateMaxValueCount(option: Option[Int]): DomainValidation[Option[Int]] = {
    option match {
      case Some(n) => if (n > -1) option.success else MaxValueCountError.failNel
      case None =>    none.success
    }
  }

  /**
    *  Validates each item in the map and returns all failures.
    */
  def validateOptions(options: Option[Seq[String]]): DomainValidation[Option[Seq[String]]] = {
    options match {
      case Some(optionsSeq) =>
        if (optionsSeq.distinct.size == optionsSeq.size) {
          optionsSeq.toList.map(validateString(_, OptionRequired)).sequenceU.fold(
            err => err.fail,
            list => Some(list.toSeq).success
          )
        } else {
          DuplicateOptionsError.failNel
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
      validateString(annotationTypeItem.annotationTypeId, IdRequired).fold(
        err => err.fail,
        id => annotationTypeItem.success
      )
    }

    annotationTypeData.map(validateAnnotationTypeItem).sequenceU
  }

}
