package org.biobank.domain.study

import org.biobank.domain.{ AnnotationTypeId, DomainValidation }
import org.biobank.domain.validation.StudyAnnotationTypeValidationHelper
import org.biobank.domain.AnnotationValueType._

import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._

/** Used to add custom annotations to participants. The study can define multiple
  * annotation types on participants to store different types of data.
  */
case class ParticipantAnnotationType private (
   studyId: StudyId,
   id: AnnotationTypeId,
   version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
   name: String,
   description: Option[String],
   valueType: AnnotationValueType,
   maxValueCount: Option[Int],
   options: Option[Map[String, String]],
   required: Boolean)
  extends StudyAnnotationType {

  override def toString: String =
    s"""|ParticipantAnnotationTypex: {
        |  studyId: $studyId,
        |  id: $id,
        |  version: $version,
        |  addedDate: $addedDate,
        |  lastUpdateDate: $lastUpdateDate,
        |  name: $name,
        |  description: $description,
        |  valueType: $valueType,
        |  maxValueCount: $maxValueCount,
        |  options: { $options }
        |  required: $required
        |}""".stripMargin

  def update(
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None,
    required: Boolean = false): DomainValidation[ParticipantAnnotationType] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      updatedAnnotationType <- ParticipantAnnotationType.create(studyId, id, version,
        name, description, valueType, maxValueCount, options, required)
    } yield updatedAnnotationType
  }

}

object ParticipantAnnotationType extends StudyAnnotationTypeValidationHelper {

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
    (validateId(studyId) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateNonEmpty(name, "name is null or empty") |@|
      validateNonEmptyOption(description, "description is null or empty") |@|
      validateMaxValueCount(maxValueCount) |@|
      validateOptions(options)) {
        ParticipantAnnotationType(_, _, _, org.joda.time.DateTime.now, None, _, _, valueType, _, _, required)
      }
  }

}
