package org.biobank.domain.study

import org.biobank.domain.{ AnnotationTypeId, DomainValidation }
import org.biobank.domain.validator.StudyValidator
import org.biobank.domain.AnnotationValueType._

import scalaz._
import scalaz.Scalaz._

case class ParticipantAnnotationType private (
   id: AnnotationTypeId,
   version: Long,
   studyId: StudyId,
   name: String,
   description: Option[String],
   valueType: AnnotationValueType,
   maxValueCount: Option[Int],
   options: Option[Map[String, String]],
   required: Boolean)
  extends StudyAnnotationType {

  override def toString: String =
    s"""ParticipantAnnotationType: {
        | id: %s,
        | version: %d,
        | studyId: %s,
        | name: %s,
        | description: %s,
        | valueType: %s,
        | maxValueCount: %d,
        | options: %s,
        | required: %b
        }""".stripMargin

}

object ParticipantAnnotationType extends StudyValidator {

  def validateId(id: AnnotationTypeId): Validation[String, AnnotationTypeId] = {
    validateStringId(id.toString) match {
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

  def create(
    id: AnnotationTypeId,
    version: Long,
    studyId: StudyId,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]],
    required: Boolean): DomainValidation[ParticipantAnnotationType] = {
    (validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateId(studyId).toValidationNel |@|
      validateNonEmpty("name", name).toValidationNel |@|
      validateNonEmptyOption("description", description).toValidationNel |@|
      validateMaxValueCount(maxValueCount).toValidationNel) {
        ParticipantAnnotationType(_, _, _, _, _, valueType, _, options, required)
      }
  }

}
