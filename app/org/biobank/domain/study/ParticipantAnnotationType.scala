package org.biobank.domain.study

import org.biobank.domain.{ AnnotationTypeId, DomainValidation }
import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure.JsonUtils._

import org.joda.time.DateTime
import play.api.libs.json._
import com.github.nscala_time.time.Imports._
import scalaz.Scalaz._

/** Used to add custom annotations to participants. The study can define multiple
  * annotation types on participants to store different types of data.
  */
case class ParticipantAnnotationType(studyId:       StudyId,
                                     id:            AnnotationTypeId,
                                     version:       Long,
                                     timeAdded:     DateTime,
                                     timeModified:  Option[DateTime],
                                     name:          String,
                                     description:   Option[String],
                                     valueType:     AnnotationValueType,
                                     maxValueCount: Option[Int],
                                     options:       Seq[String],
                                     required:      Boolean)
    extends StudyAnnotationType
    with StudyAnnotationTypeValidations {

  def update(name:          String,
             description:   Option[String],
             valueType:     AnnotationValueType,
             maxValueCount: Option[Int] = None,
             options:       Seq[String] = Seq.empty,
             required:      Boolean = false)
      : DomainValidation[ParticipantAnnotationType] = {
    val v = ParticipantAnnotationType.create(this.studyId,
                                             this.id,
                                             this.version,
                                             name,
                                             description,
                                             valueType,
                                             maxValueCount,
                                             options,
                                             required)
    v.map(_.copy(timeModified = Some(DateTime.now)))
  }

  override def toString: String =
    s"""|ParticipantAnnotationTypex: {
        |  studyId:       $studyId,
        |  id:            $id,
        |  version:       $version,
        |  timeAdded:     $timeAdded,
        |  timeModified:  $timeModified,
        |  name:          $name,
        |  description:   $description,
        |  valueType:     $valueType,
        |  maxValueCount: $maxValueCount,
        |  options:       { $options }
        |  required:      $required
        |}""".stripMargin

}

object ParticipantAnnotationType extends StudyAnnotationTypeValidations {
  import org.biobank.domain.CommonValidations._

  def create(studyId:       StudyId,
             id:            AnnotationTypeId,
             version:       Long,
             name:          String,
             description:   Option[String],
             valueType:     AnnotationValueType,
             maxValueCount: Option[Int],
             options:       Seq[String],
             required:      Boolean)
      : DomainValidation[ParticipantAnnotationType] = {
    (validateId(studyId, StudyIdRequired) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateString(name, NameRequired) |@|
      validateNonEmptyOption(description, InvalidDescription) |@|
      validateMaxValueCount(maxValueCount) |@|
      validateOptions(options) |@|
      validateSelectParams(valueType, maxValueCount, options)) {
      case (p1, p2, p3, p4, p5, p6, p7, p8) =>
        // p8 not used to create a ParticipantAnnotationType
        ParticipantAnnotationType(p1, p2, p3, DateTime.now, None, p4, p5, valueType, p6, p7, required)
    }
  }

  implicit val participantAnnotationTypeWrites = Json.writes[ParticipantAnnotationType]

}
