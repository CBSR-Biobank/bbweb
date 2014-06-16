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

object SpecimenGroup {
  import JsonUtils._
  import EnumUtils._
  import Study._

  implicit val specimenGroupIdWrite = Writes{ (id: SpecimenGroupId) => JsString(id.id) }

  implicit val anatomicalSourceTypeReads = EnumUtils.enumReads(org.biobank.domain.AnatomicalSourceType)
  implicit val preservationTypeReads = EnumUtils.enumReads(org.biobank.domain.PreservationType)
  implicit val preservationTemperatureTypeReads = EnumUtils.enumReads(org.biobank.domain.PreservationTemperatureType)
  implicit val specimenTypeReads = EnumUtils.enumReads(org.biobank.domain.SpecimenType)

  implicit val collectionEventTypeWrites: Writes[SpecimenGroup] = (
    (__ \ "studyId").write[StudyId] and
      (__ \ "id").write[SpecimenGroupId] and
      (__ \ "version").write[Long] and
      (__ \ "addedDate").write[DateTime] and
      (__ \ "lastUpdateDate").write[Option[DateTime]] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "units").write[String] and
      (__ \ "anatomicalSourceType").write[AnatomicalSourceType] and
      (__ \ "preservationType").write[PreservationType] and
      (__ \ "preservationTemperatureType").write[PreservationTemperatureType] and
      (__ \ "specimenType").write[SpecimenType]
  )(unlift(org.biobank.domain.study.SpecimenGroup.unapply))

  implicit val addSpecimenGroupCmdReads: Reads[AddSpecimenGroupCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "units").read[String](minLength[String](2)) and
      (__ \ "anatomicalSourceType").read[AnatomicalSourceType] and
      (__ \ "preservationType").read[PreservationType] and
      (__ \ "preservationTemperatureType").read[PreservationTemperatureType] and
      (__ \ "specimenType").read[SpecimenType]
  )(AddSpecimenGroupCmd.apply _)

  implicit val updateSpecimenGroupCmdReads: Reads[UpdateSpecimenGroupCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "units").read[String](minLength[String](2)) and
      (__ \ "anatomicalSourceType").read[AnatomicalSourceType] and
      (__ \ "preservationType").read[PreservationType] and
      (__ \ "preservationTemperatureType").read[PreservationTemperatureType] and
      (__ \ "specimenType").read[SpecimenType]
  )(UpdateSpecimenGroupCmd.apply _)

  implicit val removeSpecimenGroupCmdReads: Reads[RemoveSpecimenGroupCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )(RemoveSpecimenGroupCmd.apply _)
}
