package org.biobank.service.json

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object CollectionEventType {
  import JsonUtils._
  import StudyId._

  implicit val specimenGroupDataWrites = new Writes[CollectionEventTypeSpecimenGroupData] {
    def writes(sgData: CollectionEventTypeSpecimenGroupData) = Json.obj(
      "specimenGroupId" -> sgData.specimenGroupId,
      "maxCount"        -> sgData.maxCount,
      "amount"          -> sgData.amount
    )
  }

  implicit val annotationTypeDataWrites = new Writes[CollectionEventTypeAnnotationTypeData] {
    def writes(atData: CollectionEventTypeAnnotationTypeData) = Json.obj(
      "annotationTypeId" -> atData.annotationTypeId,
      "required" -> atData.required
    )
  }

  implicit val collectionEventTypeWrites = new Writes[CollectionEventType] {
    def writes(cet: CollectionEventType) = Json.obj(
      "studyId"            -> cet.studyId,
      "id"                 -> cet.id.id,
      "version"            -> cet.version,
      "addedDate"          -> cet.addedDate,
      "lastUpdateDate"     -> cet.lastUpdateDate,
      "name"               -> cet.name,
      "description"        -> cet.description,
      "recurring"          -> cet.recurring,
      "specimenGroupData"  -> cet.specimenGroupData,
      "annotationTypeData" -> cet.annotationTypeData
    )
  }

  implicit val specimenGroupDataReads: Reads[CollectionEventTypeSpecimenGroupData]= (
    (__ \ "specimenGroupId").read[String](minLength[String](2)) and
      (__ \ "maxCount").read[Int] and
      (__ \ "amount").readNullable[BigDecimal]
    )(CollectionEventTypeSpecimenGroupData)

  implicit val annotationTypeDataReads: Reads[CollectionEventTypeAnnotationTypeData] = (
    (__ \ "annotationTypeId").read[String](minLength[String](2)) and
      (__ \ "required").read[Boolean]
  )(CollectionEventTypeAnnotationTypeData)

  implicit val addCollectionEventTypeCmdReads: Reads[AddCollectionEventTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "AddCollectionEventTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String](minLength[String](2)) and
      (__ \ "recurring").read[Boolean] and
      (__ \ "specimenGroupData").read[List[CollectionEventTypeSpecimenGroupData]] and
      (__ \ "annotationTypeData").read[List[CollectionEventTypeAnnotationTypeData]]
  )((studyId, name, description, recurring, specimenGroupData, annotationTypeData) =>
    AddCollectionEventTypeCmd(studyId, name, description, recurring, specimenGroupData, annotationTypeData))

  implicit val updateCollectionEventTypeCmdReads: Reads[UpdateCollectionEventTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "UpdateCollectionEventTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String](minLength[String](2)) and
      (__ \ "recurring").read[Boolean] and
      (__ \ "specimenGroupData").read[List[CollectionEventTypeSpecimenGroupData]] and
      (__ \ "annotationTypeData").read[List[CollectionEventTypeAnnotationTypeData]]
  )((studyId, id, expectedVersion, name, description, recurring, specimenGroupData, annotationTypeData) =>
    UpdateCollectionEventTypeCmd(studyId, id, expectedVersion, name, description, recurring, specimenGroupData, annotationTypeData))

  implicit val removeCollectionEventTypeCmdReads: Reads[RemoveCollectionEventTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "RemoveCollectionEventTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )((studyId, id, expectedVersion) => RemoveCollectionEventTypeCmd(studyId, id, expectedVersion))
}
