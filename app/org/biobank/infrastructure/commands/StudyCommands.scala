package org.biobank.infrastructure.commands

import org.biobank.domain.annotations.AnnotationValueType._
import org.biobank.infrastructure._
import play.api.libs.json.Reads._
import play.api.libs.json._

object StudyCommands {
  import org.biobank.infrastructure.commands.Commands._

  // study commands
  trait StudyCommand extends Command with HasOptionalSessionUserId

  trait StudyModifyCommand extends StudyCommand with HasIdentity with HasExpectedVersion

  trait StudyStateChangeCommand extends StudyModifyCommand

  trait StudyCommandWithStudyId extends StudyCommand with HasStudyIdentity

  trait StudyModifyCommandWithStudyId
      extends StudyCommand
      with HasStudyIdentity
      with HasIdentity
      with HasExpectedVersion

  final case class AddStudyCmd(sessionUserId: Option[String],
                               name:          String,
                               description:   Option[String])
      extends StudyCommand

  final case class UpdateStudyNameCmd(sessionUserId:   Option[String],
                                      id:              String,
                                      expectedVersion: Long,
                                      name:            String)
      extends StudyModifyCommand

  final case class UpdateStudyDescriptionCmd(sessionUserId:   Option[String],
                                             id:              String,
                                             expectedVersion: Long,
                                             description:     Option[String])
      extends StudyModifyCommand

  final case class StudyAddParticipantAnnotationTypeCmd(sessionUserId:   Option[String],
                                                        id:              String,
                                                        expectedVersion: Long,
                                                        name:            String,
                                                        description:     Option[String],
                                                        valueType:       AnnotationValueType,
                                                        maxValueCount:   Option[Int],
                                                        options:         Seq[String],
                                                        required:        Boolean)
      extends StudyModifyCommand

  final case class StudyUpdateParticipantAnnotationTypeCmd(sessionUserId:    Option[String],
                                                           id:               String,
                                                           annotationTypeId: String,
                                                           expectedVersion:  Long,
                                                           name:             String,
                                                           description:      Option[String],
                                                           valueType:        AnnotationValueType,
                                                           maxValueCount:    Option[Int],
                                                           options:          Seq[String],
                                                           required:         Boolean)
      extends StudyModifyCommand

  final case class UpdateStudyRemoveAnnotationTypeCmd(sessionUserId:    Option[String],
                                                      id:               String,
                                                      expectedVersion:  Long,
                                                      annotationTypeId: String)
      extends StudyModifyCommand

  final case class EnableStudyCmd(sessionUserId:   Option[String],
                                  id:              String,
                                  expectedVersion: Long)
      extends StudyStateChangeCommand

  final case class DisableStudyCmd(sessionUserId:   Option[String],
                                   id:              String,
                                   expectedVersion: Long)
      extends StudyStateChangeCommand

  final case class RetireStudyCmd(sessionUserId:   Option[String],
                                  id:              String,
                                  expectedVersion: Long)
      extends StudyStateChangeCommand

  final case class UnretireStudyCmd(sessionUserId:   Option[String],
                                    id:              String,
                                    expectedVersion: Long)
      extends StudyStateChangeCommand


  // study annotation type commands
  trait StudyAnnotationTypeCommand extends StudyCommandWithStudyId

  trait StudyAnnotationTypeModifyCommand
      extends StudyCommandWithStudyId
      with HasIdentity
      with HasExpectedVersion

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

  // processing type commands
  trait ProcessingTypeCommand extends StudyCommandWithStudyId

  trait ProcessingTypeModifyCommand
      extends ProcessingTypeCommand
      with HasIdentity
      with HasExpectedVersion

  final case class AddProcessingTypeCmd(
    sessionUserId:    Option[String],
    studyId:     String,
    name:        String,
    description: Option[String],
    enabled:     Boolean)
      extends ProcessingTypeCommand

  final case class UpdateProcessingTypeCmd(
    sessionUserId:    Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    name:            String,
    description:     Option[String],
    enabled:         Boolean)
      extends ProcessingTypeModifyCommand

  final case class RemoveProcessingTypeCmd(
    sessionUserId:    Option[String],
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

  //--

  implicit val addStudyCmdReads: Reads[AddStudyCmd] =
    Json.reads[AddStudyCmd]

  implicit val updateStudyNameCmdReads: Reads[UpdateStudyNameCmd] =
    Json.reads[UpdateStudyNameCmd]

  implicit val updateStudyDescriptionCmdReads: Reads[UpdateStudyDescriptionCmd] =
    Json.reads[UpdateStudyDescriptionCmd]

  implicit val studyAddParticipantAnnotationTypeCmdReads: Reads[StudyAddParticipantAnnotationTypeCmd] =
    Json.reads[StudyAddParticipantAnnotationTypeCmd]

  implicit val studyUpdateParticipantAnnotationTypeCmdReads: Reads[StudyUpdateParticipantAnnotationTypeCmd] =
    Json.reads[StudyUpdateParticipantAnnotationTypeCmd]

  implicit val updateStudyRemoveAnnotationTypeCmdReads: Reads[UpdateStudyRemoveAnnotationTypeCmd] =
    Json.reads[UpdateStudyRemoveAnnotationTypeCmd]

  implicit val enableStudyCmdReads: Reads[EnableStudyCmd] =
    Json.reads[EnableStudyCmd]

  implicit val disableStudyCmdReads: Reads[DisableStudyCmd] =
    Json.reads[DisableStudyCmd]

  implicit val retireStudyCmdReads: Reads[RetireStudyCmd] =
    Json.reads[RetireStudyCmd]

  implicit val unretireStudyCmdReads: Reads[UnretireStudyCmd] =
    Json.reads[UnretireStudyCmd]

  implicit val addProcessingTypeCmdReads: Reads[AddProcessingTypeCmd] =
    Json.reads[AddProcessingTypeCmd]

  implicit val updateProcessingTypeCmdReads: Reads[UpdateProcessingTypeCmd] =
    Json.reads[UpdateProcessingTypeCmd]

  implicit val removeProcessingTypeCmdReads: Reads[RemoveProcessingTypeCmd] =
    Json.reads[RemoveProcessingTypeCmd]

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

}