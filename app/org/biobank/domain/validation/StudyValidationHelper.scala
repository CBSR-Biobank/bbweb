package org.biobank.domain.validation

import org.biobank.domain.study.StudyId
import org.biobank.domain.{ AnnotationTypeId, DomainValidation }

import scalaz._
import scalaz.Scalaz._

trait StudyValidationHelper extends ValidationHelper {

  protected def validateId(id: StudyId): Validation[String, StudyId] = {
    validateStringId(id.toString, "study id is null or empty") match {
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
    options: Option[Map[String, String]]): DomainValidation[Option[Map[String, String]]] = {

    def validateOtionItem(
      item: (String, String)): DomainValidation[(String, String)] = {
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
}
