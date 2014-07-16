package org.biobank.service.json

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.domain._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.AnnotationValueType._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime

object CollectionEventAnnotationType {
  import JsonUtils._
  import EnumUtils._
  import Study._
  import StudyAnnotationType._

  implicit val collectionEventAnnotationTypeWrites: Writes[CollectionEventAnnotationType] = (
      (__ \ "studyId").write[StudyId] and
      (__ \ "id").write[AnnotationTypeId] and
      (__ \ "version").write[Long] and
      (__ \ "addedDate").write[DateTime] and
      (__ \ "lastUpdateDate").write[Option[DateTime]] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").write[Option[Int]] and
      (__ \ "options").write[Option[Seq[String]]]
  )(unlift(org.biobank.domain.study.CollectionEventAnnotationType.unapply))

  implicit val addCollectionEventAnnotationTypeCmdReads: Reads[AddCollectionEventAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Seq[String]]
  )(AddCollectionEventAnnotationTypeCmd.apply _)

  implicit val updateCollectionEventAnnotationTypeCmdReads: Reads[UpdateCollectionEventAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Seq[String]]
  )(UpdateCollectionEventAnnotationTypeCmd.apply _)

  implicit val removeCollectionEventAnnotationTypeCmdReads: Reads[RemoveCollectionEventAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(RemoveCollectionEventAnnotationTypeCmd.apply _)

  implicit val collectionEventAnnotationTypeAddedEventWriter: Writes[CollectionEventAnnotationTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Option[Seq[String]]]
  )(unlift(CollectionEventAnnotationTypeAddedEvent.unapply))

  implicit val collectionEventAnnotationTypeUpdatedEventWriter: Writes[CollectionEventAnnotationTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Option[Seq[String]]]
  )(unlift(CollectionEventAnnotationTypeUpdatedEvent.unapply))

  implicit val collectionEventAnnotationTypeRemovedEventWriter: Writes[CollectionEventAnnotationTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String]
  )(unlift(CollectionEventAnnotationTypeRemovedEvent.unapply))
}
