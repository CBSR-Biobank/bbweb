package org.biobank.domain.validation

import org.biobank.domain.study.{
  ProcessingTypeId,
  SpecimenGroupId,
  StudyId
}
import org.biobank.domain.{ AnnotationTypeId, DomainValidation }
import org.biobank.infrastructure.AnnotationTypeData

import scalaz._
import scalaz.Scalaz._

trait StudyValidationHelper extends ValidationHelper {

  protected def validateId(id: StudyId): Validation[String, StudyId] = {
    validateStringId(id.toString, "study id is null or empty") match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }

  protected def validateId(id: SpecimenGroupId): Validation[String, SpecimenGroupId] = {
    validateStringId(id.toString, "specimen group id is null or empty") match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }

  protected def validateId(id: ProcessingTypeId): Validation[String, ProcessingTypeId] = {
    validateStringId(id.toString, "processing type id is null or empty") match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }
}

trait StudyAnnotationTypeValidationHelper extends StudyValidationHelper {

    def validateId(id: AnnotationTypeId): Validation[String, AnnotationTypeId] = {
    validateStringId(id.toString, "annotation type id is null or empty") match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }

  def validateMaxValueCount(option: Option[Int]): Validation[String, Option[Int]] =
    option match {
      case Some(n) =>
	if (n > -1) option.success else s"max value count is not a positive number".failure
      case None =>
        none.success
    }

  /**
    *  Validates each item in the map and returns all failures.
    */
  def validateOptions(
    options: Option[Map[String, String]]): ValidationNel[String, Option[Map[String, String]]] = {

    def validateOtionItem(
      item: (String, String)): ValidationNel[String, (String, String)] = {
      (validateNonEmpty(item._1, "option key is null or empty").toValidationNel |@|
	validateNonEmpty(item._2, "option value is null or empty").toValidationNel) {
        (_, _)
      }
    }

    options match {
      case Some(optionsMap) =>
	optionsMap.toList.map(validateOtionItem).sequenceU match {
	  case Success(list) => Some(list.toMap).success
	  case Failure(err) => err.fail
	}
      case None => none.success
    }
  }

  /**
    *  Validates each item in the list and returns all failures if they exist.
    */
  protected def validateAnnotationTypeData[T <: AnnotationTypeData](
    annotationTypeData: List[T]): Validation[String, List[T]] = {

    def validateAnnotationTypeItem(annotationTypeItem: T): Validation[String, T] = {
      validateStringId(
	annotationTypeItem.annotationTypeId,
	"annotation type id is null or empty") match {
	case Success(id) => annotationTypeItem.success
	case Failure(err) => err.fail
      }
    }

    annotationTypeData.map(validateAnnotationTypeItem).sequenceU
  }
}
