package org.biobank.domain.validation

import org.biobank.domain.study.StudyId
import org.biobank.domain.AnnotationTypeId

import scalaz._
import scalaz.Scalaz._

trait StudyValidationHelper extends ValidationHelper {

  def validateId(id: StudyId): Validation[String, StudyId] = {
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
}
