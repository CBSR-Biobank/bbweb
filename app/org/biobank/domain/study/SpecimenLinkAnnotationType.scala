package org.biobank.domain.study

import org.biobank.domain.{ AnnotationTypeId, DomainValidation }
import org.biobank.domain.validation.StudyAnnotationTypeValidationHelper
import org.biobank.domain.AnnotationValueType._

import scalaz._
import scalaz.Scalaz._

/** Used to add custom annotations to processing specimens. The study can define multiple
  * annotation types on processed specimens to store different types of data.
  */
case class SpecimenLinkAnnotationType private (
  studyId: StudyId,
  id: AnnotationTypeId,
  version: Long = -1,
  name: String,
  description: Option[String],
  valueType: AnnotationValueType,
  maxValueCount: Option[Int],
  options: Option[Map[String, String]])
  extends StudyAnnotationType {

  override def toString: String =
    s"""|SpecimenLinkAnnotationType:{
        |  studyId: $studyId,
        |  id: $id,
        |  version: $version,
        |  name: $name,
        |  description: $description,
        |  valueType: $valueType,
        |  maxValueCount: $maxValueCount,
        |  options: { $options }
        }""".stripMargin

  def update(
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None): DomainValidation[SpecimenLinkAnnotationType] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      updatedAnnotationType <- SpecimenLinkAnnotationType.create(studyId, id, version,
	name, description, valueType, maxValueCount, options)
    } yield updatedAnnotationType
  }

}

object SpecimenLinkAnnotationType extends StudyAnnotationTypeValidationHelper {

  def create(
    studyId: StudyId,
    id: AnnotationTypeId,
    version: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]]): DomainValidation[SpecimenLinkAnnotationType] = {
    (validateId(studyId).toValidationNel |@|
      validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty(name, "name is null or empty").toValidationNel |@|
      validateNonEmptyOption(description, "description is null or empty").toValidationNel |@|
      validateMaxValueCount(maxValueCount).toValidationNel |@|
      validateOptions(options)) {
        SpecimenLinkAnnotationType(_, _, _, _, _, valueType, _, _)
      }
  }

}
