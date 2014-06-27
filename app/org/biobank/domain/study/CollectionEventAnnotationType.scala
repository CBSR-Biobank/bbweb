package org.biobank.domain.study

import org.biobank.domain.{ AnnotationTypeId, DomainValidation }
import org.biobank.domain.validation.StudyAnnotationTypeValidationHelper
import org.biobank.domain.AnnotationValueType._

import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

/** Used to add custom annotations to collection events. The study can define multiple
  * annotation types on collection events to store different types of data.
  */
case class CollectionEventAnnotationType private (
  studyId: StudyId,
  id: AnnotationTypeId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String],
  valueType: AnnotationValueType,
  maxValueCount: Option[Int],
  options: Option[Seq[String]])
  extends StudyAnnotationType {

  override def toString: String =
    s"""|CollectionEventAnnotationType:{
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
    options: Option[Seq[String]] = None): DomainValidation[CollectionEventAnnotationType] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      validatedAnnotationType <- CollectionEventAnnotationType.create(
        studyId, id, version, addedDate, name, description, valueType, maxValueCount, options)
      updatedAnnotationType <- validatedAnnotationType.copy(lastUpdateDate = Some(dateTime)).success
    } yield updatedAnnotationType
  }
}


object CollectionEventAnnotationType extends StudyAnnotationTypeValidationHelper {

  def create(
    studyId: StudyId,
    id: AnnotationTypeId,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Seq[String]] = None): DomainValidation[CollectionEventAnnotationType] = {
    (validateId(studyId) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateNonEmpty(name, "name is null or empty") |@|
      validateNonEmptyOption(description, "description is null or empty") |@|
      validateMaxValueCount(maxValueCount) |@|
      validateOptions(options)) {
        CollectionEventAnnotationType(_, _, _, dateTime, None, _, _, valueType, _, _)
      }
  }

}
