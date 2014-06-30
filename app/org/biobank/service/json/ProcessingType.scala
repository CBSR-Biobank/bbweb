package org.biobank.service.json

import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.collection.immutable.Map

object ProcessingType {
  import Study._

  implicit val processingTypeIdRead = (__ \ "id").read[String](minLength[String](2)).map( new ProcessingTypeId(_) )
  implicit val processingTypeIdWrite = Writes{ (id: ProcessingTypeId) => JsString(id.id) }

  implicit val processingTypeWrites: Writes[ProcessingType] = (
    (__ \ "studyId").write[StudyId] and
      (__ \ "id").write[ProcessingTypeId] and
      (__ \ "version").write[Long] and
      (__ \ "addedDate").write[DateTime] and
      (__ \ "lastUpdateDate").write[Option[DateTime]] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "enabled").write[Boolean]
  )(unlift(org.biobank.domain.study.ProcessingType.unapply))

  implicit val addProcessingTypeCmdReads: Reads[AddProcessingTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "enabled").read[Boolean]
  )(AddProcessingTypeCmd.apply _)

  implicit val updateProcessingTypeCmdReads: Reads[UpdateProcessingTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "enabled").read[Boolean]
  )(UpdateProcessingTypeCmd.apply _)

  implicit val removeProcessingTypeCmdReads: Reads[RemoveProcessingTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )(RemoveProcessingTypeCmd.apply _)

  implicit val processingTypeAddedEventWrites: Writes[ProcessingTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "processingTypeId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "enabled").write[Boolean]
  )(unlift(ProcessingTypeAddedEvent.unapply))

  implicit val processingTypeUpdatedEventWrites: Writes[ProcessingTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "processingTypeId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "enabled").write[Boolean]
  )(unlift(ProcessingTypeUpdatedEvent.unapply))

  implicit val collectionEventAnnotationTypeRemovedEventWriter: Writes[ProcessingTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "processingTypeId").write[String]
  )(unlift(ProcessingTypeRemovedEvent.unapply))
}
