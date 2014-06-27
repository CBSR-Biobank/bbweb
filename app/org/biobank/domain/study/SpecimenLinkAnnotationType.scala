package org.biobank.domain.study

import org.biobank.domain.{ AnnotationTypeId, DomainValidation }
import org.biobank.domain.validation.StudyAnnotationTypeValidationHelper
import org.biobank.domain.AnnotationValueType._

import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._

/** Used to add custom annotations to processing specimens. The study can define multiple
  * annotation types on processed specimens to store different types of data.
  */
case class SpecimenLinkAnnotationType private (
  studyId: StudyId,
  id: AnnotationTypeId,
  version: Long = -1,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String],
  valueType: AnnotationValueType,
  maxValueCount: Option[Int],
  options: Option[Seq[String]])
  extends StudyAnnotationType {

  override def toString: String =
    s"""|SpecimenLinkAnnotationType:{
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
        }""".stripMargin

  def update(
    expectedVersion: Option[Long],
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Seq[String]] = None): DomainValidation[SpecimenLinkAnnotationType] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      validatedAnnotationType <- SpecimenLinkAnnotationType.create(
        studyId, id, version, addedDate, name, description, valueType, maxValueCount, options)
      newItem <- validatedAnnotationType.copy(lastUpdateDate = Some(dateTime)).success
    } yield newItem
  }

}

object SpecimenLinkAnnotationType extends StudyAnnotationTypeValidationHelper {

  def create(
    studyId: StudyId,
    id: AnnotationTypeId,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Seq[String]]): DomainValidation[SpecimenLinkAnnotationType] = {
    (validateId(studyId) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateNonEmpty(name, "name is null or empty") |@|
      validateNonEmptyOption(description, "description is null or empty") |@|
      validateMaxValueCount(maxValueCount) |@|
      validateOptions(options)) {
        SpecimenLinkAnnotationType(_, _, _, dateTime, None, _, _, valueType, _, _)
      }
  }

}
