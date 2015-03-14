package org.biobank.domain.study

import org.biobank.domain.{ AnnotationTypeId, DomainValidation }
import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

/** Used to add custom annotations to collection events. The study can define multiple
  * annotation types on collection events to store different types of data.
  */
case class CollectionEventAnnotationType(studyId:       StudyId,
                                         id:            AnnotationTypeId,
                                         version:       Long,
                                         timeAdded:     DateTime,
                                         timeModified:  Option[DateTime],
                                         name:          String,
                                         description:   Option[String],
                                         valueType:     AnnotationValueType,
                                         maxValueCount: Option[Int],
                                         options:       Seq[String])
    extends StudyAnnotationType
    with StudyAnnotationTypeValidations {

  override def toString: String =
    s"""|CollectionEventAnnotationType:{
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
        }""".stripMargin

  def update(name:          String,
             description:   Option[String],
             valueType:     AnnotationValueType,
             maxValueCount: Option[Int] = None,
             options:       Seq[String] = Seq.empty)
      : DomainValidation[CollectionEventAnnotationType] = {
    val v = CollectionEventAnnotationType.create(this.studyId,
                                                 this.id,
                                                 this.version,
                                                 this.timeAdded,
                                                 name,
                                                 description,
                                                 valueType,
                                                 maxValueCount,
                                                 options)
    v.map(_.copy(timeModified =  Some(DateTime.now)))
  }
}


object CollectionEventAnnotationType extends StudyAnnotationTypeValidations {
  import org.biobank.domain.CommonValidations._

  def create(studyId:       StudyId,
             id:            AnnotationTypeId,
             version:       Long,
             dateTime:      DateTime,
             name:          String,
             description:   Option[String],
             valueType:     AnnotationValueType,
             maxValueCount: Option[Int] = None,
             options:       Seq[String] = Seq.empty)
      : DomainValidation[CollectionEventAnnotationType] = {
    (validateId(studyId, StudyIdRequired) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateString(name, NameRequired) |@|
      validateNonEmptyOption(description, NonEmptyDescription) |@|
      validateMaxValueCount(maxValueCount) |@|
      validateOptions(options) |@|
      validateSelectParams(valueType, maxValueCount, options)) {
      case (p1, p2, p3, p4, p5, p6, p7, p8) =>
        // p8 not used to create a ParticipantAnnotationType
        CollectionEventAnnotationType(p1, p2, p3, dateTime, None, p4, p5, valueType, p6, p7)
      }
  }

  implicit val collectionEventAnnotationTypeWrites = Json.writes[CollectionEventAnnotationType]

}
