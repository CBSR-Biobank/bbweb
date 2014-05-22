package org.biobank.service.json

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.AnnotationValueType._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object CollectionEventAnnotationType {
  import JsonUtils._
  import StudyId._

  implicit val CollectionEventAnnotationTypeWrites = new Writes[CollectionEventAnnotationType] {
    def writes(annotType: CollectionEventAnnotationType) = Json.obj(
      "studyId"        -> annotType.studyId,
      "id"             -> annotType.id.id,
      "version"        -> annotType.version,
      "addedDate"      -> annotType.addedDate,
      "lastUpdateDate" -> annotType.lastUpdateDate,
      "name"           -> annotType.name,
      "description"    -> annotType.description,
      "valueType"      -> annotType.valueType.toString,
      "maxValueCount"  -> annotType.maxValueCount,
      "options"        -> annotType.options
    )
  }

  implicit val annotationValueTypeReads = EnumUtils.enumReads(org.biobank.domain.AnnotationValueType)

  implicit val addCollectionEventAnnotationTypeCmdReads: Reads[AddCollectionEventAnnotationTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "AddCollectionEventAnnotationTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String](minLength[String](2)) and
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
      (__ \ "description").readNullable[String](minLength[String](2)) and
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
