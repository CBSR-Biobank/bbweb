package org.biobank.infrastructure.command

import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure._
import play.api.libs.json.Reads._
import play.api.libs.json._

object StudyCommands {
  import org.biobank.infrastructure.command.Commands._

  // study commands
  trait StudyCommand extends Command with HasOptionalUserId

  trait StudyModifyCommand extends StudyCommand with HasIdentity with HasExpectedVersion

  trait StudyCommandWithStudyId extends StudyCommand with HasStudyIdentity

  trait StudyModifyCommandWithStudyId
      extends StudyCommand
      with HasStudyIdentity
      with HasIdentity
      with HasExpectedVersion

  final case class AddStudyCmd(userId:      Option[String],
                               name:        String,
                               description: Option[String])
      extends StudyCommand

  final case class UpdateStudyNameCmd(userId:          Option[String],
                                      id:              String,
                                      expectedVersion: Long,
                                      name:            String)
      extends StudyModifyCommand

  final case class UpdateStudyDescriptionCmd(userId:          Option[String],
                                             id:              String,
                                             expectedVersion: Long,
                                             description:     Option[String])
      extends StudyModifyCommand

  final case class StudyAddParticipantAnnotationTypeCmd(userId:          Option[String],
                                                        id:              String,
                                                        expectedVersion: Long,
                                                        name:            String,
                                                        description:     Option[String],
                                                        valueType:       AnnotationValueType,
                                                        maxValueCount:   Option[Int],
                                                        options:         Seq[String],
                                                        required:        Boolean)
      extends StudyModifyCommand

  final case class StudyUpdateParticipantAnnotationTypeCmd(userId:          Option[String],
                                                           id:              String,
                                                           uniqueId:        String,
                                                           expectedVersion: Long,
                                                           name:            String,
                                                           description:     Option[String],
                                                           valueType:       AnnotationValueType,
                                                           maxValueCount:   Option[Int],
                                                           options:         Seq[String],
                                                           required:        Boolean)
      extends StudyModifyCommand

  final case class UpdateStudyRemoveAnnotationTypeCmd(userId:          Option[String],
                                                      id:              String,
                                                      expectedVersion: Long,
                                                      uniqueId:        String)
      extends StudyModifyCommand

  final case class EnableStudyCmd(userId:          Option[String],
                                  id:              String,
                                  expectedVersion: Long)
      extends StudyModifyCommand

  final case class DisableStudyCmd(userId:    Option[String],
                                   id:              String,
                                   expectedVersion: Long)
      extends StudyModifyCommand

  final case class RetireStudyCmd(userId:    Option[String],
                                  id: String,
                                  expectedVersion: Long)
      extends StudyModifyCommand

  final case class UnretireStudyCmd(userId:    Option[String],
                                    id: String,
                                    expectedVersion: Long)
      extends StudyModifyCommand


  // study annotation type commands
  trait StudyAnnotationTypeCommand extends StudyCommandWithStudyId

  trait StudyAnnotationTypeModifyCommand
      extends StudyCommandWithStudyId
      with HasIdentity
      with HasExpectedVersion

  // specimen link annotation type
  final case class AddSpecimenLinkAnnotationTypeCmd(
    userId:    Option[String],
    studyId:       String,
    name:          String,
    description:   Option[String],
    valueType:     AnnotationValueType,
    maxValueCount: Option[Int],
    options:       Seq[String],
    required:      Boolean)
      extends StudyAnnotationTypeCommand

  final case class UpdateSpecimenLinkAnnotationTypeCmd(
    userId:    Option[String],
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
    userId:    Option[String],
    id:              String,
    expectedVersion: Long)

  // processing type commands
  trait ProcessingTypeCommand extends StudyCommandWithStudyId

  trait ProcessingTypeModifyCommand
      extends ProcessingTypeCommand
      with HasIdentity
      with HasExpectedVersion

  final case class AddProcessingTypeCmd(
    userId:    Option[String],
    studyId:     String,
    name:        String,
    description: Option[String],
    enabled:     Boolean)
      extends ProcessingTypeCommand

  final case class UpdateProcessingTypeCmd(
    userId:    Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    name:            String,
    description:     Option[String],
    enabled:         Boolean)
      extends ProcessingTypeModifyCommand

  final case class RemoveProcessingTypeCmd(
    userId:    Option[String],
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
    userId:    Option[String],
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
    userId:    Option[String],
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

  final case class RemoveSpecimenLinkTypeCmd(userId:           Option[String],
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
