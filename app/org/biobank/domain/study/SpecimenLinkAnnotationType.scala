package org.biobank.domain.study

import org.biobank.domain.{ AnnotationTypeId, DomainValidation }
import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure.JsonUtils._
import org.biobank.infrastructure.EnumUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._

/** Used to add custom annotations to processing specimens. The study can define multiple
  * annotation types on processed specimens to store different types of data.
  */
case class SpecimenLinkAnnotationType (
  studyId: StudyId,
  id: AnnotationTypeId,
  version: Long = -1,
  timeAdded: DateTime,
  timeModified: Option[DateTime],
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
        |  timeAdded: $timeAdded,
        |  timeModified: $timeModified,
        |  name: $name,
        |  description: $description,
        |  valueType: $valueType,
        |  maxValueCount: $maxValueCount,
        |  options: { $options }
        }""".stripMargin

  def update(
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Seq[String]] = None): DomainValidation[SpecimenLinkAnnotationType] = {
    SpecimenLinkAnnotationType.create(
      this.studyId, this.id, this.version, this.timeAdded, name, description, valueType, maxValueCount, options)
  }

}

object SpecimenLinkAnnotationType extends StudyAnnotationTypeValidations {
  import org.biobank.domain.CommonValidations._

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
      validateString(name, NameRequired) |@|
      validateNonEmptyOption(description, NonEmptyDescription) |@|
      validateMaxValueCount(maxValueCount) |@|
      validateOptions(options)) {
        SpecimenLinkAnnotationType(_, _, _, dateTime, None, _, _, valueType, _, _)
      }
  }

  implicit val annotationValueTypeWrites: Writes[AnnotationValueType] = enumWrites

  implicit val specimenLinkAnnotationTypeWrites: Writes[SpecimenLinkAnnotationType] = (
      (__ \ "studyId").write[StudyId] and
      (__ \ "id").write[AnnotationTypeId] and
      (__ \ "version").write[Long] and
      (__ \ "timeAdded").write[DateTime] and
      (__ \ "timeModified").write[Option[DateTime]] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").write[Option[Int]] and
      (__ \ "options").write[Option[Seq[String]]]
  )(unlift(org.biobank.domain.study.SpecimenLinkAnnotationType.unapply))

}
