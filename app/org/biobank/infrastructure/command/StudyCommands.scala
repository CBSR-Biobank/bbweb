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
    id:              String,
    expectedVersion: Long,
    name:            String,
    description:     Option[String] = None)
      extends StudyModifyCommand

  case class EnableStudyCmd(
    id:              String,
    expectedVersion: Long)
      extends StudyModifyCommand

  case class DisableStudyCmd(
    id:              String,
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
    studyId:                     String,
    name:                        String,
    description:                 Option[String],
    units:                       String,
    anatomicalSourceType:        AnatomicalSourceType,
    preservationType:            PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType:                SpecimenType)
      extends SpecimenGroupCommand

  case class UpdateSpecimenGroupCmd(
    studyId:                     String,
    id:                          String,
    expectedVersion:             Long,
    name:                        String,
    description:                 Option[String],
    units:                       String,
    anatomicalSourceType:        AnatomicalSourceType,
    preservationType:            PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType:                SpecimenType)
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
    studyId:            String,
    name:               String,
    description:        Option[String],
    recurring:          Boolean,
    specimenGroupData:  List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
      extends CollectionEventTypeCommand

  case class UpdateCollectionEventTypeCmd(
    studyId:            String,
    id:                 String,
    expectedVersion:    Long,
    name:               String,
    description:        Option[String],
    recurring:          Boolean,
    specimenGroupData:  List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
      extends CollectionEventTypeModifyCommand

  case class RemoveCollectionEventTypeCmd(
    studyId:         String,
    id:              String,
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
    studyId:       String,
    name:          String,
    description:   Option[String],
    valueType:     AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options:       Seq[String])
      extends StudyAnnotationTypeCommand

  case class UpdateCollectionEventAnnotationTypeCmd(
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    name:            String,
    description:     Option[String],
    valueType:       AnnotationValueType,
    maxValueCount:   Option[Int],
    options:         Seq[String])
      extends StudyAnnotationTypeModifyCommand

  case class RemoveCollectionEventAnnotationTypeCmd(
    studyId:         String,
    id:              String,
    expectedVersion: Long)
      extends StudyAnnotationTypeModifyCommand

  // participant annotation type
  case class AddParticipantAnnotationTypeCmd(
    studyId:       String,
    name:          String,
    description:   Option[String],
    valueType:     AnnotationValueType,
    maxValueCount: Option[Int],
    options:       Seq[String],
    required:      Boolean = false)
      extends StudyAnnotationTypeCommand

  case class UpdateParticipantAnnotationTypeCmd(
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    name:            String,
    description:     Option[String],
    valueType:       AnnotationValueType,
    maxValueCount:   Option[Int] = None,
    options:         Seq[String],
    required:        Boolean = false)
      extends StudyAnnotationTypeModifyCommand

  case class RemoveParticipantAnnotationTypeCmd(
    studyId:         String,
    id:              String,
    expectedVersion: Long)
      extends StudyAnnotationTypeModifyCommand

  // specimen link annotation type
  case class AddSpecimenLinkAnnotationTypeCmd(
    studyId:       String,
    name:          String,
    description:   Option[String],
    valueType:     AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options:       Seq[String])
      extends StudyAnnotationTypeCommand

  case class UpdateSpecimenLinkAnnotationTypeCmd(
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    name:            String,
    description:     Option[String],
    valueType:       AnnotationValueType,
    maxValueCount:   Option[Int],
    options:         Seq[String])
      extends StudyAnnotationTypeModifyCommand

  case class RemoveSpecimenLinkAnnotationTypeCmd(
    studyId:         String,
    id:              String,
    expectedVersion: Long)
      extends StudyAnnotationTypeModifyCommand

  // processing type commands
  trait ProcessingTypeCommand extends StudyCommandWithStudyId

  trait ProcessingTypeModifyCommand
      extends ProcessingTypeCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddProcessingTypeCmd(
    studyId:     String,
    name:        String,
    description: Option[String],
    enabled:     Boolean)
      extends ProcessingTypeCommand

  case class UpdateProcessingTypeCmd(
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    name:            String,
    description:     Option[String],
    enabled:         Boolean)
      extends ProcessingTypeModifyCommand

  case class RemoveProcessingTypeCmd(
    studyId:         String,
    id:              String,
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
    processingTypeId:      String,
    expectedInputChange:   BigDecimal,
    expectedOutputChange:  BigDecimal,
    inputCount:            Int,
    outputCount:           Int,
    inputGroupId:          String,
    outputGroupId:         String,
    inputContainerTypeId:  Option[String],
    outputContainerTypeId: Option[String],
    annotationTypeData:    List[SpecimenLinkTypeAnnotationTypeData])
      extends SpecimenLinkTypeCommand

  case class UpdateSpecimenLinkTypeCmd(
    processingTypeId:      String,
    id:                    String,
    expectedVersion:       Long,
    expectedInputChange:   BigDecimal,
    expectedOutputChange:  BigDecimal,
    inputCount:            Int,
    outputCount:           Int,
    inputGroupId:          String,
    outputGroupId:         String,
    inputContainerTypeId:  Option[String],
    outputContainerTypeId: Option[String],
    annotationTypeData:    List[SpecimenLinkTypeAnnotationTypeData])
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
    studyId:     String,
    uniqueId:    String,
    annotations: List[ParticipantAnnotation])
      extends ParticipantCommand

  case class UpdateParticipantCmd(
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    uniqueId:        String,
    annotations:     List[ParticipantAnnotation])
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

  implicit val addStudyCmdReads = Json.reads[AddStudyCmd]
  implicit val updateStudyCmdReads = Json.reads[UpdateStudyCmd]
  implicit val enableStudyCmdReads = Json.reads[EnableStudyCmd]
  implicit val disableStudyCmdReads = Json.reads[DisableStudyCmd]
  implicit val retireStudyCmdReads = Json.reads[RetireStudyCmd]
  implicit val unretireStudyCmdReads = Json.reads[UnretireStudyCmd]
  implicit val addParticipantAnnotationTypeCmdReads = Json.reads[AddParticipantAnnotationTypeCmd]
  implicit val updateParticipantAnnotationTypeCmdReads = Json.reads[UpdateParticipantAnnotationTypeCmd]
  implicit val addSpecimenGroupCmdReads = Json.reads[AddSpecimenGroupCmd]
  implicit val updateSpecimenGroupCmdReads = Json.reads[UpdateSpecimenGroupCmd]
  implicit val removeSpecimenGroupCmdReads = Json.reads[RemoveSpecimenGroupCmd]
  implicit val addCollectionEventAnnotationTypeCmdReads = Json.reads[AddCollectionEventAnnotationTypeCmd]
  implicit val updateCollectionEventAnnotationTypeCmdReads = Json.reads[UpdateCollectionEventAnnotationTypeCmd]
  implicit val removeCollectionEventAnnotationTypeCmdReads = Json.reads[RemoveCollectionEventAnnotationTypeCmd]
  implicit val addCollectionEventTypeCmdReads = Json.reads[AddCollectionEventTypeCmd]
  implicit val updateCollectionEventTypeCmdReads = Json.reads[UpdateCollectionEventTypeCmd]
  implicit val removeCollectionEventTypeCmdReads = Json.reads[RemoveCollectionEventTypeCmd]
  implicit val addProcessingTypeCmdReads = Json.reads[AddProcessingTypeCmd]
  implicit val updateProcessingTypeCmdReads = Json.reads[UpdateProcessingTypeCmd]
  implicit val removeProcessingTypeCmdReads = Json.reads[RemoveProcessingTypeCmd]
  implicit val addSpecimenLinkAnnotationTypeCmdReads = Json.reads[AddSpecimenLinkAnnotationTypeCmd]
  implicit val updateSpecimenLinkAnnotationTypeCmdReads = Json.reads[UpdateSpecimenLinkAnnotationTypeCmd]
  implicit val removeSpecimenLinkAnnotationTypeCmdReads = Json.reads[RemoveSpecimenLinkAnnotationTypeCmd]
  implicit val addSpecimenLinkTypeCmdReads = Json.reads[AddSpecimenLinkTypeCmd]
  implicit val updateSpecimenLinkTypeCmdReads = Json.reads[UpdateSpecimenLinkTypeCmd]
  implicit val removeSpecimenLinkTypeCmdReads = Json.reads[RemoveSpecimenLinkTypeCmd]
  implicit val addParticipantCmdReads = Json.reads[AddParticipantCmd]
  implicit val updateParticipantCmdReads = Json.reads[UpdateParticipantCmd]

}
