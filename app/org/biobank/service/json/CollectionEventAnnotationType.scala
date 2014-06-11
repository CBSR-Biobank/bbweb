package org.biobank.service.json

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.domain._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.AnnotationValueType._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.collection.immutable.Map

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
      (__ \ "options").write[Option[Map[String, String]]]
  )(unlift(org.biobank.domain.study.CollectionEventAnnotationType.unapply))

  implicit val addCollectionEventAnnotationTypeCmdReads: Reads[AddCollectionEventAnnotationTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "AddCollectionEventAnnotationTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Map[String, String]]
  )((studyId, name, description, valueType, maxValueCount, options) =>
    AddCollectionEventAnnotationTypeCmd(studyId, name, description, valueType, maxValueCount, options))

  implicit val updateCollectionEventAnnotationTypeCmdReads: Reads[UpdateCollectionEventAnnotationTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "UpdateCollectionEventAnnotationTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Map[String, String]]
  )((studyId, id, expectedVersion, name, description, valueType, maxValueCount, options) =>
    UpdateCollectionEventAnnotationTypeCmd(studyId, id, expectedVersion, name, description, valueType, maxValueCount, options))

  implicit val removeCollectionEventAnnotationTypeCmdReads: Reads[RemoveCollectionEventAnnotationTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "RemoveCollectionEventAnnotationTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )((studyId, id, expectedVersion) => RemoveCollectionEventAnnotationTypeCmd(studyId, id, expectedVersion))
}
