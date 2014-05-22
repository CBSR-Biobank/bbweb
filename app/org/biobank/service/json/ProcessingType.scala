package org.biobank.service.json

import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
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
  import StudyId._

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
    (__ \ "type").read[String](Reads.verifying[String](_ == "AddProcessingTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String](minLength[String](2)) and
      (__ \ "enabled").read[Boolean]
  )((studyId, name, description, enabled) => AddProcessingTypeCmd(studyId, name, description, enabled))

  implicit val updateProcessingTypeCmdReads: Reads[UpdateProcessingTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "UpdateProcessingTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String](minLength[String](2)) and
      (__ \ "enabled").read[Boolean]
  )((studyId, id, expectedVersion, name, description, enabled) =>
    UpdateProcessingTypeCmd(studyId, id, expectedVersion, name, description, enabled))

  implicit val removeProcessingTypeCmdReads: Reads[RemoveProcessingTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "RemoveProcessingTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )((studyId, id, expectedVersion) => RemoveProcessingTypeCmd(studyId, id, expectedVersion))
}
