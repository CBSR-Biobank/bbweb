package org.biobank.domain.study

import org.biobank.domain.{ AnnotationTypeId, DomainValidation }
import org.biobank.domain.validation.StudyAnnotationTypeValidationHelper
import org.biobank.domain.AnnotationValueType._

import scalaz._
import scalaz.Scalaz._

case class ParticipantAnnotationType private (
   studyId: StudyId,
   id: AnnotationTypeId,
   version: Long,
   name: String,
   description: Option[String],
   valueType: AnnotationValueType,
   maxValueCount: Option[Int],
   options: Option[Map[String, String]],
   required: Boolean)
  extends StudyAnnotationType {

  override def toString: String =
    s"""|ParticipantAnnotationTypex: {
        |  id: %s,
        |  version: %d,
        |  studyId: %s,
        |  name: %s,
        |  description: %s,
        |  valueType: %s,
        |  maxValueCount: %d,
        |  options: %s,
        |  required: %b
        |}""".stripMargin

}

object ParticipantAnnotationType extends StudyAnnotationTypeValidationHelper {

  /**
    *  Validates each item in the map and returns all failures.
    */
  def validateSpecimenGroupData(
    options: Map[String, String]): DomainValidation[Map[String, String]] = {

    def validateOtionItem(
      item: (String, String)): DomainValidation[(String, String)] = {
      (validateNonEmpty(item._1, "option key is null or empty").toValidationNel |@|
	validateNonEmpty(item._2, "option value is null or empty").toValidationNel) {
        (_, _)
      }
    }

    // FIXME convert to list?

    options.map(validateOtionItem).sequenceU
  }

  def create(
    studyId: StudyId,
    id: AnnotationTypeId,
    version: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]],
    required: Boolean): DomainValidation[ParticipantAnnotationType] = {
    (validateId(studyId).toValidationNel |@|
      validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty(name, "name is null or empty").toValidationNel |@|
      validateNonEmptyOption(description, "description is null or empty").toValidationNel |@|
      validateMaxValueCount(maxValueCount).toValidationNel) {
        ParticipantAnnotationType(_, _, _, _, _, valueType, _, options, required)
      }
  }

}
