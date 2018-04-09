package org.biobank.infrastructure.commands

import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationTemperature._
import org.biobank.domain.PreservationType._
import org.biobank.domain.SpecimenType._
import play.api.libs.json.Reads._
import play.api.libs.json._

object ProcessingTypeCommands {
  import org.biobank.infrastructure.commands.Commands._

  trait ProcessingTypeCommand extends Command with HasStudyIdentity with HasSessionUserId

  trait ProcessingTypeModifyCommand
      extends ProcessingTypeCommand
      with HasIdentity
      with HasExpectedVersion

  trait AddProcessingTypeCmd extends ProcessingTypeCommand {
    val sessionUserId:         String
    val studyId:               String
    val name:                  String
    val description:           Option[String]
    val enabled:               Boolean
    val expectedInputChange:   BigDecimal
    val expectedOutputChange:  BigDecimal
    val inputCount:            Int
    val outputCount:           Int
    val inputContainerTypeId:  Option[String]
    val outputContainerTypeId: Option[String]
  }

  final case class SpecimenDefinition(name:                    String,
                                      description:             Option[String],
                                      units:                   String,
                                      anatomicalSourceType:    AnatomicalSourceType,
                                      preservationType:        PreservationType,
                                      preservationTemperature: PreservationTemperature,
                                      specimenType:            SpecimenType)

  final case class AddCollectedProcessingTypeCmd(sessionUserId:             String,
                                                 studyId:                   String,
                                                 name:                      String,
                                                 description:               Option[String],
                                                 enabled:                   Boolean,
                                                 expectedInputChange:       BigDecimal,
                                                 expectedOutputChange:      BigDecimal,
                                                 inputCount:                Int,
                                                 outputCount:               Int,
                                                 inputContainerTypeId:      Option[String],
                                                 outputContainerTypeId:     Option[String],
                                                 collectionEventTypeId:     String,
                                                 inputSpecimenDefinitionId: String,
                                                 outputSpecimenDefinition:  SpecimenDefinition)
      extends AddProcessingTypeCmd

  final case class AddProcessedProcessingTypeCmd(sessionUserId:            String,
                                                 studyId:                  String,
                                                 name:                     String,
                                                 description:              Option[String],
                                                 enabled:                  Boolean,
                                                 expectedInputChange:      BigDecimal,
                                                 expectedOutputChange:     BigDecimal,
                                                 inputCount:               Int,
                                                 outputCount:              Int,
                                                 inputContainerTypeId:     Option[String],
                                                 outputContainerTypeId:    Option[String],
                                                 processingTypeId:         String,
                                                 inputSpecimenDefintionId: String,
                                                 outputSpecimenDefinition: SpecimenDefinition)
      extends AddProcessingTypeCmd

  final case class UpdateProcessingTypeCmd(sessionUserId:   String,
                                           studyId:         String,
                                           id:              String,
                                           expectedVersion: Long,
                                           name:            String,
                                           description:     Option[String],
                                           enabled:         Boolean)
      extends ProcessingTypeModifyCommand

  final case class RemoveProcessingTypeCmd(sessionUserId:   String,
                                           studyId:         String,
                                           id:              String,
                                           expectedVersion: Long)
      extends ProcessingTypeModifyCommand

  /*
  final case class AddSpecimenLinkTypeCmd(
    sessionUserId:    Option[String],
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

  final case class UpdateSpecimenLinkTypeCmd(
    sessionUserId:    Option[String],
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

  final case class RemoveSpecimenLinkTypeCmd(sessionUserId:    Option[String],
                                             processingTypeId: String,
                                             id:               String,
                                             expectedVersion:  Long)
      extends SpecimenLinkTypeModifyCommand

  // specimen link annotation type
  final case class AddSpecimenLinkAnnotationTypeCmd(
    sessionUserId:    Option[String],
    studyId:       String,
    name:          String,
    description:   Option[String],
    valueType:     AnnotationValueType,
    maxValueCount: Option[Int],
    options:       Seq[String],
    required:      Boolean)
      extends StudyAnnotationTypeCommand

  final case class UpdateSpecimenLinkAnnotationTypeCmd(
    sessionUserId:    Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    name:            String,
    description:     Option[String],
    valueType:       AnnotationValueType,
    maxValueCount:   Option[Int],
    options:         Seq[String],
    required:      Boolean)
      extends StudyAnnotationTypeModifyCommand

  final case class RemoveSpecimenLinkAnnotationTypeCmd(
    sessionUserId:    Option[String],
    id:              String,
    expectedVersion: Long)
   */

  implicit val specimenDefinitionReads: Reads[SpecimenDefinition] =
    Json.reads[SpecimenDefinition]

  implicit val addAddCollectedProcessingTypeCmdReads: Reads[AddCollectedProcessingTypeCmd] =
    Json.reads[AddCollectedProcessingTypeCmd]

  implicit val addAddProcessedProcessingTypeCmdReads: Reads[AddProcessedProcessingTypeCmd] =
    Json.reads[AddProcessedProcessingTypeCmd]

  implicit val updateProcessingTypeCmdReads: Reads[UpdateProcessingTypeCmd] =
    Json.reads[UpdateProcessingTypeCmd]

  implicit val removeProcessingTypeCmdReads: Reads[RemoveProcessingTypeCmd] =
    Json.reads[RemoveProcessingTypeCmd]

  /*
  implicit val addSpecimenLinkAnnotationTypeCmdReads: Reads[AddSpecimenLinkAnnotationTypeCmd] =
    Json.reads[AddSpecimenLinkAnnotationTypeCmd]

  implicit val updateSpecimenLinkAnnotationTypeCmdReads: Reads[UpdateSpecimenLinkAnnotationTypeCmd] =
    Json.reads[UpdateSpecimenLinkAnnotationTypeCmd]

  implicit val removeSpecimenLinkAnnotationTypeCmdReads: Reads[RemoveSpecimenLinkAnnotationTypeCmd] =
    Json.reads[RemoveSpecimenLinkAnnotationTypeCmd]

  implicit val addSpecimenLinkTypeCmdReads: Reads[AddSpecimenLinkTypeCmd] =
    Json.reads[AddSpecimenLinkTypeCmd]

  implicit val updateSpecimenLinkTypeCmdReads: Reads[UpdateSpecimenLinkTypeCmd] =
    Json.reads[UpdateSpecimenLinkTypeCmd]

  implicit val removeSpecimenLinkTypeCmdReads: Reads[RemoveSpecimenLinkTypeCmd] =
    Json.reads[RemoveSpecimenLinkTypeCmd]
   */

}
