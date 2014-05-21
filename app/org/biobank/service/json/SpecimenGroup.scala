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

object SpecimenGroup {
  import JsonUtils._
  import StudyId._

  implicit val specimenGroupWrites = new Writes[SpecimenGroup] {
    def writes(sg: SpecimenGroup) = Json.obj(
      "studyId"                     -> sg.studyId,
      "id"                          -> sg.id.id,
      "version"                     -> sg.version,
      "addedDate"                   -> sg.addedDate,
      "lastUpdateDate"              -> sg.lastUpdateDate,
      "name"                        -> sg.name,
      "description"                 -> sg.description,
      "units"                       -> sg.units,
      "anatomicalSourceType"        -> sg.anatomicalSourceType.toString,
      "preservationType"            -> sg.preservationType.toString,
      "preservationTemperatureType" -> sg.preservationTemperatureType.toString,
      "specimenType"                -> sg.specimenType.toString
    )
  }

  implicit val anatomicalSourceTypeReads = EnumUtils.enumReads(org.biobank.domain.AnatomicalSourceType)
  implicit val preservationTypeReads = EnumUtils.enumReads(org.biobank.domain.PreservationType)
  implicit val preservationTemperatureTypeReads = EnumUtils.enumReads(org.biobank.domain.PreservationTemperatureType)
  implicit val specimenTypeReads = EnumUtils.enumReads(org.biobank.domain.SpecimenType)

  implicit val addSpecimenGroupCmdReads: Reads[AddSpecimenGroupCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "AddSpecimenGroupCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String](minLength[String](2)) and
      (__ \ "units").read[String](minLength[String](2)) and
      (__ \ "anatomicalSourceType").read[AnatomicalSourceType] and
      (__ \ "preservationType").read[PreservationType] and
      (__ \ "preservationTemperatureType").read[PreservationTemperatureType] and
      (__ \ "specimenType").read[SpecimenType]
  )((studyId, name, description, units, anatomicalSourceType, preservationType,
    preservationTemperatureType, specimenType) =>
    AddSpecimenGroupCmd(studyId, name, description, units, anatomicalSourceType, preservationType,
    preservationTemperatureType, specimenType))

  implicit val updateSpecimenGroupCmdReads: Reads[UpdateSpecimenGroupCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "UpdateSpecimenGroupCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String](minLength[String](2)) and
      (__ \ "units").read[String](minLength[String](2)) and
      (__ \ "anatomicalSourceType").read[AnatomicalSourceType] and
      (__ \ "preservationType").read[PreservationType] and
      (__ \ "preservationTemperatureType").read[PreservationTemperatureType] and
      (__ \ "specimenType").read[SpecimenType]
  )((studyId, id, expectedVersion, name, description, units, anatomicalSourceType, preservationType,
    preservationTemperatureType, specimenType) =>
    UpdateSpecimenGroupCmd(studyId, id, expectedVersion, name, description, units, anatomicalSourceType,
      preservationType, preservationTemperatureType, specimenType))

  implicit val removeSpecimenGroupCmdReads: Reads[RemoveSpecimenGroupCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "RemoveSpecimenGroupCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )((studyId, id, expectedVersion) => RemoveSpecimenGroupCmd(studyId, id, expectedVersion))
}
