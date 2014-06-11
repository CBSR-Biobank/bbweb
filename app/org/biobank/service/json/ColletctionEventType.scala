package org.biobank.service.json

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.collection.immutable.Map

object CollectionEventType {
  import JsonUtils._
  import Study._

  implicit val collectionEventTypeIdWrite = Writes{ (id: CollectionEventTypeId) => JsString(id.id) }

  implicit val specimenGroupDataWrites: Writes[CollectionEventTypeSpecimenGroupData] = (
    (__ \ "specimenGroupId").write[String] and
      (__ \ "maxCount").write[Int] and
      (__ \ "amount").write[Option[BigDecimal]]
  )(unlift(CollectionEventTypeSpecimenGroupData.unapply))

  implicit val annotationTypeDataWrites: Writes[CollectionEventTypeAnnotationTypeData] = (
    (__ \ "annotationTypeId").write[String] and
      (__ \ "required").write[Boolean]
  )(unlift(CollectionEventTypeAnnotationTypeData.unapply))

  implicit val collectionEventTypeWrites: Writes[CollectionEventType] = (
    (__ \ "studyId").write[StudyId] and
      (__ \ "id").write[CollectionEventTypeId] and
      (__ \ "version").write[Long] and
      (__ \ "addedDate").write[DateTime] and
      (__ \ "lastUpdateDate").write[Option[DateTime]] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "recurring").write[Boolean] and
      (__ \ "specimenGroupData").write[List[CollectionEventTypeSpecimenGroupData]] and
      (__ \ "annotationTypeData").write[List[CollectionEventTypeAnnotationTypeData]]
  )(unlift(org.biobank.domain.study.CollectionEventType.unapply))

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
      (__ \ "description").readNullable[String] and
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
      (__ \ "description").readNullable[String] and
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
