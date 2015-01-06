package org.biobank.infrastructure.command

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.domain.ContainerTypeId
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure.JsonUtils._
import org.biobank.infrastructure.EnumUtils._

import Commands._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object StudyCommands {

  // study commands
  trait StudyCommand extends Command

  trait StudyModifyCommand extends StudyCommand with HasIdentity with HasExpectedVersion

  trait StudyCommandWithStudyId extends StudyCommand with HasStudyIdentity

  trait StudyModifyCommandWithStudyId
      extends StudyCommand
      with HasStudyIdentity
      with HasIdentity
      with HasExpectedVersion

  case class AddStudyCmd(
    name: String,
    description: Option[String] = None)
      extends StudyCommand

  case class UpdateStudyCmd(
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String] = None)
      extends StudyModifyCommand

  case class EnableStudyCmd(
    id: String,
    expectedVersion: Long)
      extends StudyModifyCommand

  case class DisableStudyCmd(
    id: String,
    expectedVersion: Long)
      extends StudyModifyCommand

  case class RetireStudyCmd(
    id: String,
    expectedVersion: Long)
      extends StudyModifyCommand

  case class UnretireStudyCmd(
    id: String,
    expectedVersion: Long)
      extends StudyModifyCommand

  // specimen group commands

  trait SpecimenGroupCommand extends StudyCommandWithStudyId

  trait SpecimenGroupModifyCommand
      extends SpecimenGroupCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddSpecimenGroupCmd(
    studyId: String,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType)
      extends SpecimenGroupCommand

  case class UpdateSpecimenGroupCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType)
      extends SpecimenGroupModifyCommand

  case class RemoveSpecimenGroupCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends SpecimenGroupModifyCommand

  // collection event type commands
  trait CollectionEventTypeCommand extends StudyCommandWithStudyId

  trait CollectionEventTypeModifyCommand
      extends CollectionEventTypeCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddCollectionEventTypeCmd(
    studyId: String,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
      extends CollectionEventTypeCommand

  case class UpdateCollectionEventTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
      extends CollectionEventTypeModifyCommand

  case class RemoveCollectionEventTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends CollectionEventTypeModifyCommand

  // study annotation type commands
  trait StudyAnnotationTypeCommand extends StudyCommandWithStudyId

  trait StudyAnnotationTypeModifyCommand
      extends StudyCommandWithStudyId
      with HasIdentity
      with HasExpectedVersion

  // collection event annotation type commands
  case class AddCollectionEventAnnotationTypeCmd(
    studyId: String,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Seq[String])
      extends StudyAnnotationTypeCommand

  case class UpdateCollectionEventAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Seq[String])
      extends StudyAnnotationTypeModifyCommand

  case class RemoveCollectionEventAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends StudyAnnotationTypeModifyCommand

  // participant annotation type
  case class AddParticipantAnnotationTypeCmd(
    studyId: String,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Seq[String],
    required: Boolean = false)
      extends StudyAnnotationTypeCommand

  case class UpdateParticipantAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Seq[String],
    required: Boolean = false)
      extends StudyAnnotationTypeModifyCommand

  case class RemoveParticipantAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends StudyAnnotationTypeModifyCommand

  // specimen link annotation type
  case class AddSpecimenLinkAnnotationTypeCmd(
    studyId: String,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Seq[String])
      extends StudyAnnotationTypeCommand

  case class UpdateSpecimenLinkAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Seq[String])
      extends StudyAnnotationTypeModifyCommand

  case class RemoveSpecimenLinkAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends StudyAnnotationTypeModifyCommand

  // processing type commands
  trait ProcessingTypeCommand extends StudyCommandWithStudyId

  trait ProcessingTypeModifyCommand
      extends ProcessingTypeCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddProcessingTypeCmd(
    studyId: String,
    name: String,
    description: Option[String],
    enabled: Boolean)
      extends ProcessingTypeCommand

  case class UpdateProcessingTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    enabled: Boolean)
      extends ProcessingTypeModifyCommand

  case class RemoveProcessingTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends ProcessingTypeModifyCommand

  // specimen link type commands
  trait SpecimenLinkTypeCommand extends StudyCommand {

    /** the id of the processing type the specimen link type belongs to. */
    val processingTypeId: String
  }

  trait SpecimenLinkTypeModifyCommand
      extends SpecimenLinkTypeCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddSpecimenLinkTypeCmd(
    processingTypeId: String,
    expectedInputChange: BigDecimal,
    expectedOutputChange: BigDecimal,
    inputCount: Int,
    outputCount: Int,
    inputGroupId: String,
    outputGroupId: String,
    inputContainerTypeId: Option[String],
    outputContainerTypeId: Option[String],
    annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData])
      extends SpecimenLinkTypeCommand

  case class UpdateSpecimenLinkTypeCmd(
    processingTypeId: String,
    id: String,
    expectedVersion: Long,
    expectedInputChange: BigDecimal,
    expectedOutputChange: BigDecimal,
    inputCount: Int,
    outputCount: Int,
    inputGroupId: String,
    outputGroupId: String,
    inputContainerTypeId: Option[String],
    outputContainerTypeId: Option[String],
    annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData])
      extends SpecimenLinkTypeModifyCommand

  case class RemoveSpecimenLinkTypeCmd(
    processingTypeId: String,
    id: String,
    expectedVersion: Long)
      extends SpecimenLinkTypeModifyCommand

  //--

  trait ParticipantCommand extends StudyCommandWithStudyId

  trait ParticipantModifyCommand
      extends SpecimenGroupCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddParticipantCmd(
    studyId: String,
    uniqueId: String,
    annotations: List[ParticipantAnnotation])
      extends ParticipantCommand

  case class UpdateParticipantCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    uniqueId: String,
    annotations: List[ParticipantAnnotation])
      extends ParticipantModifyCommand

  //--

  implicit val annotationValueTypeFormat: Format[AnnotationValueType] =
    enumFormat(org.biobank.domain.AnnotationValueType)

  implicit val anatomicalSourceTypeFormat: Format[AnatomicalSourceType] =
    enumFormat(org.biobank.domain.AnatomicalSourceType)

  implicit val specimenTypeReads: Format[SpecimenType] =
    enumFormat(org.biobank.domain.SpecimenType)

  implicit val preservationTypeReads: Format[PreservationType] =
    enumFormat(org.biobank.domain.PreservationType)

  implicit val preservatioTempTypeReads: Format[PreservationTemperatureType] =
    enumFormat(org.biobank.domain.PreservationTemperatureType)

  implicit val addStudyCmdReads = (
    (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  ){ AddStudyCmd(_, _) }

  implicit val updateStudyCmdReads: Reads[UpdateStudyCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )(UpdateStudyCmd.apply _)

  implicit val enableStudyCmdReads: Reads[EnableStudyCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(EnableStudyCmd.apply _ )

  implicit val disableStudyCmdReads: Reads[DisableStudyCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(DisableStudyCmd.apply _)

  implicit val retireStudyCmdReads: Reads[RetireStudyCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(RetireStudyCmd.apply _)

  implicit val unretireStudyCmdReads: Reads[UnretireStudyCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(UnretireStudyCmd.apply _)

  implicit val addParticipantAnnotationTypeCmdReads: Reads[AddParticipantAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").read[Seq[String]] and
      (__ \ "required").read[Boolean]
  ){ AddParticipantAnnotationTypeCmd.apply _}

  implicit val updateParticipantAnnotationTypeCmdReads: Reads[UpdateParticipantAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").read[Seq[String]] and
      (__ \ "required").read[Boolean]
  )(UpdateParticipantAnnotationTypeCmd.apply _)

  implicit val addSpecimenGroupCmdReads: Reads[AddSpecimenGroupCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "units").read[String](minLength[String](1)) and
      (__ \ "anatomicalSourceType").read[AnatomicalSourceType] and
      (__ \ "preservationType").read[PreservationType] and
      (__ \ "preservationTemperatureType").read[PreservationTemperatureType] and
      (__ \ "specimenType").read[SpecimenType]
  ){ AddSpecimenGroupCmd.apply _ }

  implicit val updateSpecimenGroupCmdReads: Reads[UpdateSpecimenGroupCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "units").read[String](minLength[String](1)) and
      (__ \ "anatomicalSourceType").read[AnatomicalSourceType] and
      (__ \ "preservationType").read[PreservationType] and
      (__ \ "preservationTemperatureType").read[PreservationTemperatureType] and
      (__ \ "specimenType").read[SpecimenType]
  )(UpdateSpecimenGroupCmd.apply _)

  implicit val removeSpecimenGroupCmdReads: Reads[RemoveSpecimenGroupCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(RemoveSpecimenGroupCmd.apply _)

  implicit val addCollectionEventAnnotationTypeCmdReads: Reads[AddCollectionEventAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").read[Seq[String]]
  ){ AddCollectionEventAnnotationTypeCmd.apply _ }

  implicit val updateCollectionEventAnnotationTypeCmdReads: Reads[UpdateCollectionEventAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").read[Seq[String]]
  )(UpdateCollectionEventAnnotationTypeCmd.apply _)

  implicit val removeCollectionEventAnnotationTypeCmdReads: Reads[RemoveCollectionEventAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(RemoveCollectionEventAnnotationTypeCmd.apply _)

  implicit val addCollectionEventTypeCmdReads: Reads[AddCollectionEventTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "recurring").read[Boolean] and
      (__ \ "specimenGroupData").read[List[CollectionEventTypeSpecimenGroupData]] and
      (__ \ "annotationTypeData").read[List[CollectionEventTypeAnnotationTypeData]]
  ){ AddCollectionEventTypeCmd.apply _ }

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

  implicit val addProcessingTypeCmdReads: Reads[AddProcessingTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "enabled").read[Boolean]
  ){ AddProcessingTypeCmd.apply _ }

  implicit val updateProcessingTypeCmdReads: Reads[UpdateProcessingTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "enabled").read[Boolean]
  )(UpdateProcessingTypeCmd.apply _)

  implicit val removeProcessingTypeCmdReads: Reads[RemoveProcessingTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(RemoveProcessingTypeCmd.apply _)

  implicit val addSpecimenLinkAnnotationTypeCmdReads: Reads[AddSpecimenLinkAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").read[Seq[String]]
  ){ AddSpecimenLinkAnnotationTypeCmd.apply _ }

  implicit val updateSpecimenLinkAnnotationTypeCmdReads: Reads[UpdateSpecimenLinkAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").read[Seq[String]]
  )(UpdateSpecimenLinkAnnotationTypeCmd.apply _)

  implicit val removeSpecimenLinkAnnotationTypeCmdReads: Reads[RemoveSpecimenLinkAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(RemoveSpecimenLinkAnnotationTypeCmd.apply _)

  implicit val addSpecimenLinkTypeCmdReads: Reads[AddSpecimenLinkTypeCmd] = (
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
  ){ AddSpecimenLinkTypeCmd.apply _ }

  implicit val updateSpecimenLinkTypeCmdReads: Reads[UpdateSpecimenLinkTypeCmd] = (
    (__ \ "processingTypeId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "expectedInputChange").read[BigDecimal] and
      (__ \ "expectedOutputChange").read[BigDecimal] and
      (__ \ "inputCount").read[Int] and
      (__ \ "outputCount").read[Int] and
      (__ \ "inputGroupId").read[String] and
      (__ \ "outputGroupId").read[String] and
      (__ \ "inputContainerTypeId").read[Option[String]] and
      (__ \ "outputContainerTypeId").read[Option[String]] and
      (__ \ "annotationTypeData").read[List[SpecimenLinkTypeAnnotationTypeData]]
  )(UpdateSpecimenLinkTypeCmd.apply _)

  implicit val removeSpecimenLinkTypeCmdReads: Reads[RemoveSpecimenLinkTypeCmd] = (
    (__ \ "processingTypeId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(RemoveSpecimenLinkTypeCmd.apply _)

  implicit val addParticipantCmdReads: Reads[AddParticipantCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "uniqueId").read[String](minLength[String](2)) and
      (__ \ "annotations").read[List[ParticipantAnnotation]]
  ){ AddParticipantCmd.apply _ }

  implicit val updateParticipantCmdReads: Reads[UpdateParticipantCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "uniqueId").read[String](minLength[String](2)) and
      (__ \ "annotations").read[List[ParticipantAnnotation]]
  ){ UpdateParticipantCmd.apply _ }

}
