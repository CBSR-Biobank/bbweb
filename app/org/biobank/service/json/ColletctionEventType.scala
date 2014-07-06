package org.biobank.service.json

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

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
      (__ \ "name").write[String] and
      (__ \ "maxCount").write[Int] and
      (__ \ "amount").write[Option[BigDecimal]] and
      (__ \ "units").write[String]
  )(unlift(CollectionEventTypeSpecimenGroupData.unapply))

  implicit val annotationTypeDataWrites: Writes[CollectionEventTypeAnnotationTypeData] = (
    (__ \ "annotationTypeId").write[String] and
      (__ \ "name").write[String] and
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
      (__ \ "name").read[String] and
      (__ \ "maxCount").read[Int] and
      (__ \ "amount").readNullable[BigDecimal] and
      (__ \ "units").read[String]
  )(CollectionEventTypeSpecimenGroupData)

  implicit val annotationTypeDataReads: Reads[CollectionEventTypeAnnotationTypeData] = (
    (__ \ "annotationTypeId").read[String](minLength[String](2)) and
      (__ \ "name").read[String] and
      (__ \ "required").read[Boolean]
  )(CollectionEventTypeAnnotationTypeData)

  implicit val addCollectionEventTypeCmdReads: Reads[AddCollectionEventTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "recurring").read[Boolean] and
      (__ \ "specimenGroupData").read[List[CollectionEventTypeSpecimenGroupData]] and
      (__ \ "annotationTypeData").read[List[CollectionEventTypeAnnotationTypeData]]
  )(AddCollectionEventTypeCmd.apply _)

  implicit val updateCollectionEventTypeCmdReads: Reads[UpdateCollectionEventTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "recurring").read[Boolean] and
      (__ \ "specimenGroupData").read[List[CollectionEventTypeSpecimenGroupData]] and
      (__ \ "annotationTypeData").read[List[CollectionEventTypeAnnotationTypeData]]
  )(UpdateCollectionEventTypeCmd.apply _)

  implicit val removeCollectionEventTypeCmdReads: Reads[RemoveCollectionEventTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(RemoveCollectionEventTypeCmd.apply _)

  implicit val collectionEventTypeAddedEventWriter: Writes[CollectionEventTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "collectionEventTypeId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "recurring").write[Boolean] and
      (__ \ "specimenGroupData").write[List[CollectionEventTypeSpecimenGroupData]] and
      (__ \ "annotationTypeData").write[List[CollectionEventTypeAnnotationTypeData]]
  )(unlift(CollectionEventTypeAddedEvent.unapply))

  implicit val collectionEventTypeUpdatedEventWriter: Writes[CollectionEventTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "collectionEventTypeId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "recurring").write[Boolean] and
      (__ \ "specimenGroupData").write[List[CollectionEventTypeSpecimenGroupData]] and
      (__ \ "annotationTypeData").write[List[CollectionEventTypeAnnotationTypeData]]
  )(unlift(CollectionEventTypeUpdatedEvent.unapply))

  implicit val collectionEventTypeRemovedEventWriter: Writes[CollectionEventTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "collectionEventTypeId").write[String]
  )(unlift(CollectionEventTypeRemovedEvent.unapply))
}
