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
  trait StudyCommand extends Command {
    val id: String
    val expectedVersion: Long
  }

  case class AddStudyCmd(
    id: String,               // don't care
    expectedVersion: Long,    // don't care
    name: String,
    description: Option[String] = None)
      extends StudyCommand

  object AddStudyCmd {
    def apply(name: String, description: Option[String]): AddStudyCmd =
      AddStudyCmd("", 0L, name, description)
  }

  case class UpdateStudyCmd(
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String] = None)
      extends StudyCommand
      with HasIdentity
      with HasExpectedVersion

  case class EnableStudyCmd(
    id: String,
    expectedVersion: Long)
      extends StudyCommand
      with HasIdentity
      with HasExpectedVersion

  case class DisableStudyCmd(
    id: String,
    expectedVersion: Long)
      extends StudyCommand
      with HasIdentity
      with HasExpectedVersion

  case class RetireStudyCmd(
    id: String,
    expectedVersion: Long)
      extends StudyCommand
      with HasIdentity
      with HasExpectedVersion

  case class UnretireStudyCmd(
    id: String,
    expectedVersion: Long)
      extends StudyCommand
      with HasIdentity
      with HasExpectedVersion

  // specimen group commands
  trait StudyCommandWithId extends StudyCommand with HasStudyIdentity

  trait SpecimenGroupCommand extends StudyCommandWithId

  case class AddSpecimenGroupCmd(
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
      extends SpecimenGroupCommand with HasStudyIdentity

  object AddSpecimenGroupCmd{
    def apply(
    studyId: String,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType): AddSpecimenGroupCmd =
      AddSpecimenGroupCmd(studyId, "", 0L, name, description, units, anatomicalSourceType,
        preservationType, preservationTemperatureType, specimenType)
  }

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
      extends SpecimenGroupCommand
      with HasIdentity
      with HasStudyIdentity with HasExpectedVersion

  case class RemoveSpecimenGroupCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends SpecimenGroupCommand
      with HasIdentity
      with HasExpectedVersion

  // collection event type commands
  trait CollectionEventTypeCommand extends StudyCommandWithId

  case class AddCollectionEventTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
      extends CollectionEventTypeCommand with HasStudyIdentity

  object AddCollectionEventTypeCmd {
    def apply(
      studyId: String,
      name: String,
      description: Option[String],
      recurring: Boolean,
      specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
      annotationTypeData: List[CollectionEventTypeAnnotationTypeData]): AddCollectionEventTypeCmd =
      AddCollectionEventTypeCmd(
        studyId, "", 0L, name, description, recurring, specimenGroupData, annotationTypeData)
  }

  case class UpdateCollectionEventTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
      extends CollectionEventTypeCommand
      with HasIdentity
      with HasExpectedVersion

  case class RemoveCollectionEventTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends CollectionEventTypeCommand
      with HasIdentity
      with HasExpectedVersion

  // study annotation type commands
  trait StudyAnnotationTypeCommand extends StudyCommandWithId

  // collection event annotation type commands
  trait CollectionEventAnnotationTypeCommand extends StudyAnnotationTypeCommand

  case class AddCollectionEventAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Seq[String]] = None)
      extends CollectionEventAnnotationTypeCommand

  object AddCollectionEventAnnotationTypeCmd {
    def apply(
      studyId: String,
      name: String,
      description: Option[String],
      valueType: AnnotationValueType,
      maxValueCount: Option[Int],
      options: Option[Seq[String]]): AddCollectionEventAnnotationTypeCmd =
      AddCollectionEventAnnotationTypeCmd(
        studyId, "", 0L, name, description, valueType, maxValueCount, options)
  }

  case class UpdateCollectionEventAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Seq[String]] = None)
      extends CollectionEventAnnotationTypeCommand
      with HasIdentity
      with HasStudyIdentity
      with HasExpectedVersion

  case class RemoveCollectionEventAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends CollectionEventAnnotationTypeCommand
      with HasIdentity
      with HasStudyIdentity
      with HasExpectedVersion

  // participant annotation type
  trait ParticipantAnnotationTypeCommand extends StudyAnnotationTypeCommand

  case class AddParticipantAnnotationTypeCmd(
    studyId: String,
    id: String,                   // don't care
    expectedVersion: Long,        // don't care
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Seq[String]] = None,
    required: Boolean = false)
      extends ParticipantAnnotationTypeCommand
      with HasStudyIdentity

  object AddParticipantAnnotationTypeCmd {
    def apply(
      studyId: String,
      name: String,
      description: Option[String],
      valueType: AnnotationValueType,
      maxValueCount: Option[Int],
      options: Option[Seq[String]],
      required: Boolean): AddParticipantAnnotationTypeCmd =
      AddParticipantAnnotationTypeCmd(
        studyId, "", 0L, name, description, valueType, maxValueCount, options, required)
  }

  case class UpdateParticipantAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Seq[String]] = None,
    required: Boolean = false)
      extends ParticipantAnnotationTypeCommand
      with HasIdentity
      with HasStudyIdentity
      with HasExpectedVersion

  case class RemoveParticipantAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends ParticipantAnnotationTypeCommand
      with HasIdentity
      with HasExpectedVersion

  // specimen link annotation type
  trait SpecimenLinkAnnotationTypeCommand extends StudyAnnotationTypeCommand

  case class AddSpecimenLinkAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Seq[String]] = None)
      extends SpecimenLinkAnnotationTypeCommand
      with HasStudyIdentity

  object AddSpecimenLinkAnnotationTypeCmd {
    def apply(
      studyId: String,
      name: String,
      description: Option[String],
      valueType: AnnotationValueType,
      maxValueCount: Option[Int],
      options: Option[Seq[String]]): AddSpecimenLinkAnnotationTypeCmd =
      AddSpecimenLinkAnnotationTypeCmd(
        studyId, "", 0L, name, description, valueType, maxValueCount, options)
  }

  case class UpdateSpecimenLinkAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Seq[String]] = None)
      extends SpecimenLinkAnnotationTypeCommand
      with HasIdentity
      with HasStudyIdentity
      with HasExpectedVersion

  case class RemoveSpecimenLinkAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends SpecimenLinkAnnotationTypeCommand
      with HasIdentity
      with HasStudyIdentity
      with HasExpectedVersion

  // processing type commands
  trait ProcessingTypeCommand extends StudyCommandWithId

  case class AddProcessingTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    enabled: Boolean)
      extends ProcessingTypeCommand with HasStudyIdentity

  object AddProcessingTypeCmd {
    def apply(
      studyId: String,
      name: String,
      description: Option[String],
      enabled: Boolean): AddProcessingTypeCmd =
      AddProcessingTypeCmd(studyId, "", 0L, name, description, enabled)
  }

  case class UpdateProcessingTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String],
    enabled: Boolean)
      extends ProcessingTypeCommand
      with HasIdentity
      with HasExpectedVersion

  case class RemoveProcessingTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Long)
      extends ProcessingTypeCommand
      with HasIdentity
      with HasExpectedVersion

  // specimen link type commands
  trait SpecimenLinkTypeCommand extends StudyCommand with HasProcessingTypeIdentity {

    /** the id of the processing type the specimen link type belongs to. */
    val processingTypeId: String

    /** The id for the specimen link type. */
    val id: String

  }


  case class AddSpecimenLinkTypeCmd(
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
      extends SpecimenLinkTypeCommand
      with HasProcessingTypeIdentity

  object AddSpecimenLinkTypeCmd {
    def apply(
      processingTypeId: String,
      expectedInputChange: BigDecimal,
      expectedOutputChange: BigDecimal,
      inputCount: Int,
      outputCount: Int,
      inputGroupId: String,
      outputGroupId: String,
      inputContainerTypeId: Option[String],
      outputContainerTypeId: Option[String],
      annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData]): AddSpecimenLinkTypeCmd =
      AddSpecimenLinkTypeCmd(
        processingTypeId, "", 0L, expectedInputChange, expectedOutputChange, inputCount, outputCount,
        inputGroupId, outputGroupId, inputContainerTypeId, outputContainerTypeId, annotationTypeData)
  }

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
      extends SpecimenLinkTypeCommand
      with HasIdentity
      with HasExpectedVersion

  case class RemoveSpecimenLinkTypeCmd(
    processingTypeId: String,
    id: String,
    expectedVersion: Long)
      extends SpecimenLinkTypeCommand
      with HasIdentity
      with HasExpectedVersion

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
  ){ AddStudyCmd("", 0L, _, _) }

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
      (__ \ "options").readNullable[Seq[String]] and
      (__ \ "required").read[Boolean]
  ){ AddParticipantAnnotationTypeCmd(_, "", 0L, _, _, _, _, _, _) }

  implicit val updateParticipantAnnotationTypeCmdReads: Reads[UpdateParticipantAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Seq[String]] and
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
  ){ AddSpecimenGroupCmd(_, "", 0L, _, _, _, _, _, _, _) }

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
      (__ \ "options").readNullable[Seq[String]]
  ){ AddCollectionEventAnnotationTypeCmd(_, "", 0L, _, _, _, _, _) }

  implicit val updateCollectionEventAnnotationTypeCmdReads: Reads[UpdateCollectionEventAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Seq[String]]
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
  ){ AddCollectionEventTypeCmd(_, "", 0L, _, _, _, _, _) }

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
  ){ AddProcessingTypeCmd(_, "", 0L, _, _, _) }

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
      (__ \ "options").readNullable[Seq[String]]
  ){ AddSpecimenLinkAnnotationTypeCmd(_, "", 0L, _, _, _, _, _) }

  implicit val updateSpecimenLinkAnnotationTypeCmdReads: Reads[UpdateSpecimenLinkAnnotationTypeCmd] = (
    (__ \ "studyId").read[String](minLength[String](2)) and
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String] and
      (__ \ "valueType").read[AnnotationValueType] and
      (__ \ "maxValueCount").readNullable[Int] and
      (__ \ "options").readNullable[Seq[String]]
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
  ){ AddSpecimenLinkTypeCmd(_, "", 0L, _, _, _, _, _, _, _, _, _) }

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

}
