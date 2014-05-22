package org.biobank.service.json

import org.biobank.domain.study._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.ContainerTypeId
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.collection.immutable.Map

object SpecimenLinkType {
  import ProcessingType._
  import SpecimenGroup._

  implicit val specimenLinkTypeIdWrite = Writes{ (id: SpecimenLinkTypeId) => JsString(id.id) }

  implicit val containerTypeIdWrite = Writes{ (id: ContainerTypeId) => JsString(id.id) }

  implicit val annotationTypeDataWrites: Writes[SpecimenLinkTypeAnnotationTypeData] = (
    (__ \ "annotationTypeId").write[String] and
      (__ \ "required").write[Boolean]
  )(unlift(SpecimenLinkTypeAnnotationTypeData.unapply))

  implicit val specimenLinkTypeWrites: Writes[SpecimenLinkType] = (
    (__ \ "processingTypeId").write[ProcessingTypeId] and
      (__ \ "id").write[SpecimenLinkTypeId] and
      (__ \ "version").write[Long] and
      (__ \ "addedDate").write[DateTime] and
      (__ \ "lastUpdateDate").write[Option[DateTime]] and
      (__ \ "expectedInputChange").write[BigDecimal] and
      (__ \ "expectedOutputChange").write[BigDecimal] and
      (__ \ "inputCount").write[Int] and
      (__ \ "outputCount").write[Int] and
      (__ \ "inputGroupId").write[SpecimenGroupId] and
      (__ \ "outputGroupId").write[SpecimenGroupId] and
      (__ \ "inputContainerTypeId").write[Option[ContainerTypeId]] and
      (__ \ "outputContainerTypeId").write[Option[ContainerTypeId]] and
      (__ \ "annotationTypeData").write[List[SpecimenLinkTypeAnnotationTypeData]]
  )(unlift(org.biobank.domain.study.SpecimenLinkType.unapply))

  implicit val annotationTypeDataReads: Reads[SpecimenLinkTypeAnnotationTypeData] = (
    (__ \ "annotationTypeId").read[String](minLength[String](2)) and
      (__ \ "required").read[Boolean]
  )(SpecimenLinkTypeAnnotationTypeData)

  implicit val addSpecimenLinkTypeCmdReads: Reads[AddSpecimenLinkTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "AddSpecimenLinkTypeCmd")) andKeep
      (__ \ "processingTypeId").read[String](minLength[String](2)) and
      (__ \ "expectedInputChange").read[BigDecimal] and
      (__ \ "expectedOutputChange").read[BigDecimal] and
      (__ \ "inputCount").read[Int] and
      (__ \ "outputCount").read[Int] and
      (__ \ "inputGroupId").read[String] and
      (__ \ "outputGroupId").read[String] and
      (__ \ "inputContainerTypeId").read[Option[String]] and
      (__ \ "outputContainerTypeId").read[Option[String]] and
      (__ \ "annotationTypeData").read[List[SpecimenLinkTypeAnnotationTypeData]]
  )((processingTypeId, expectedInputChange, expectedOutputChange, inputCount, outputCount,
    inputGroupId, outputGroupId, inputContainerTypeId, outputContainerTypeId, annotationTypeData) =>
    AddSpecimenLinkTypeCmd(processingTypeId, expectedInputChange, expectedOutputChange, inputCount,
      outputCount, inputGroupId, outputGroupId, inputContainerTypeId, outputContainerTypeId,
      annotationTypeData))

  implicit val updateSpecimenLinkTypeCmdReads: Reads[UpdateSpecimenLinkTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "UpdateSpecimenLinkTypeCmd")) andKeep
      (__ \ "processingTypeId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "expectedInputChange").read[BigDecimal] and
      (__ \ "expectedOutputChange").read[BigDecimal] and
      (__ \ "inputCount").read[Int] and
      (__ \ "outputCount").read[Int] and
      (__ \ "inputGroupId").read[String] and
      (__ \ "outputGroupId").read[String] and
      (__ \ "inputContainerTypeId").read[Option[String]] and
      (__ \ "outputContainerTypeId").read[Option[String]] and
      (__ \ "annotationTypeData").read[List[SpecimenLinkTypeAnnotationTypeData]]
  )((processingTypeId, id, expectedVersion, expectedInputChange, expectedOutputChange, inputCount,
    outputCount, inputGroupId, outputGroupId, inputContainerTypeId, outputContainerTypeId,
    annotationTypeData) =>
    UpdateSpecimenLinkTypeCmd(processingTypeId, id, expectedVersion, expectedInputChange,
      expectedOutputChange, inputCount, outputCount, inputGroupId, outputGroupId,
      inputContainerTypeId, outputContainerTypeId, annotationTypeData))

  implicit val removeSpecimenLinkTypeCmdReads: Reads[RemoveSpecimenLinkTypeCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "RemoveSpecimenLinkTypeCmd")) andKeep
      (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )((studyId, id, expectedVersion) => RemoveSpecimenLinkTypeCmd(studyId, id, expectedVersion))
}
