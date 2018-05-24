package org.biobank.infrastructure.commands

import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationTemperature._
import org.biobank.domain.PreservationType._
import org.biobank.domain.SpecimenType._
import org.biobank.domain.annotations.AnnotationValueType._
import play.api.libs.json.Reads._
import play.api.libs.json._

object ProcessingTypeCommands {
  import org.biobank.infrastructure.commands.Commands._

  trait ProcessingTypeCommand extends Command with HasStudyIdentity with HasSessionUserId

  trait ProcessingTypeModifyCommand
      extends ProcessingTypeCommand
      with HasIdentity
      with HasExpectedVersion

  class SpecimenProcessingType(val id: String) extends AnyVal {
    override def toString: String = id
  }

  val specimenProcessingInput: SpecimenProcessingType  = new SpecimenProcessingType("input")
  val specimenProcessingOutput: SpecimenProcessingType = new SpecimenProcessingType("output")

  final case class SpecimenDefinition(name:                    String,
                                      description:             Option[String],
                                      units:                   String,
                                      anatomicalSourceType:    AnatomicalSourceType,
                                      preservationType:        PreservationType,
                                      preservationTemperature: PreservationTemperature,
                                      specimenType:            SpecimenType)

  final case class InputSpecimenProcessing(expectedChange:       BigDecimal,
                                     count:                Int,
                                     containerTypeId:      Option[String],
                                     definitionType:       String,
                                     entityId:             String,
                                     specimenDefinitionId: String)

  final case class OutputSpecimenProcessing(expectedChange:     BigDecimal,
                                      count:              Int,
                                      containerTypeId:    Option[String],
                                      specimenDefinition: SpecimenDefinition)

  final case class SpecimenProcessing(input: InputSpecimenProcessing, output: OutputSpecimenProcessing)

  final case class AddProcessingTypeCmd(sessionUserId:      String,
                                        studyId:            String,
                                        name:               String,
                                        description:        Option[String],
                                        enabled:            Boolean,
                                        specimenProcessing: SpecimenProcessing)
      extends ProcessingTypeCommand

  final case class UpdateNameCmd(sessionUserId:   String,
                                 studyId:         String,
                                 id:              String,
                                 expectedVersion: Long,
                                 name:            String)
      extends ProcessingTypeModifyCommand

  final case class UpdateDescriptionCmd(sessionUserId:   String,
                                        studyId:         String,
                                        id:              String,
                                        expectedVersion: Long,
                                        description:     Option[String])
      extends ProcessingTypeModifyCommand

  final case class UpdateEnabledCmd(sessionUserId:   String,
                                    studyId:         String,
                                    id:              String,
                                    expectedVersion: Long,
                                    enabled:         Boolean)
      extends ProcessingTypeModifyCommand

  final case class UpdateExpectedChangeCmd(sessionUserId:   String,
                                           studyId:         String,
                                           id:              String,
                                           expectedVersion: Long,
                                           inputType:       SpecimenProcessingType,
                                           expectedChange:  BigDecimal)
      extends ProcessingTypeModifyCommand

  final case class UpdateCountCmd(sessionUserId:   String,
                                  studyId:         String,
                                  id:              String,
                                  expectedVersion: Long,
                                  inputType:       SpecimenProcessingType,
                                  count:           Int)
      extends ProcessingTypeModifyCommand

  final case class UpdateContainerTypeCmd(sessionUserId:   String,
                                          studyId:         String,
                                          id:              String,
                                          expectedVersion: Long,
                                          inputType:       SpecimenProcessingType,
                                          containerTypeId: Option[String])
      extends ProcessingTypeModifyCommand

  final case class UpdateInputSpecimenDefinitionCmd(sessionUserId:        String,
                                                    studyId:              String,
                                                    id:                   String,
                                                    expectedVersion:      Long,
                                                    definitionType:       String,
                                                    entityId:             String,
                                                    specimenDefinitionId: String)
      extends ProcessingTypeModifyCommand

  final case class UpdateOutputSpecimenDefinitionCmd(sessionUserId:      String,
                                                     studyId:            String,
                                                     id:                 String,
                                                     expectedVersion:    Long,
                                                     specimenDefinition: SpecimenDefinition)
      extends ProcessingTypeModifyCommand

  final case class AddProcessingTypeAnnotationTypeCmd(sessionUserId:   String,
                                                      studyId:         String,
                                                      id:              String,
                                                      expectedVersion: Long,
                                                      name:            String,
                                                      description:     Option[String],
                                                      valueType:       AnnotationValueType,
                                                      maxValueCount:   Option[Int],
                                                      options:         Seq[String],
                                                      required:        Boolean)
      extends ProcessingTypeModifyCommand

  final case class UpdateProcessingTypeAnnotationTypeCmd(sessionUserId:   String,
                                                         studyId:          String,
                                                         id:               String,
                                                         expectedVersion:  Long,
                                                         annotationTypeId: String,
                                                         name:             String,
                                                         description:      Option[String],
                                                         valueType:        AnnotationValueType,
                                                         maxValueCount:    Option[Int],
                                                         options:          Seq[String],
                                                         required:         Boolean)
      extends ProcessingTypeModifyCommand

  final case class RemoveProcessingTypeAnnotationTypeCmd(sessionUserId:   String,
                                                         studyId:          String,
                                                         id:               String,
                                                         expectedVersion:  Long,
                                                         annotationTypeId: String)
      extends ProcessingTypeModifyCommand

  final case class RemoveProcessingTypeCmd(sessionUserId:   String,
                                           studyId:         String,
                                           id:              String,
                                           expectedVersion: Long)
      extends ProcessingTypeModifyCommand

  implicit val specimenDefinitionFormat: Format[SpecimenDefinition] = Json.format[SpecimenDefinition]

  implicit val inputSpecimenInfoFormat: Format[InputSpecimenProcessing] = Json.format[InputSpecimenProcessing]

  implicit val outputSpecimenInfoFormat: Format[OutputSpecimenProcessing] = Json.format[OutputSpecimenProcessing]

  implicit val specimenProcessingFormat: Format[SpecimenProcessing] = Json.format[SpecimenProcessing]


  implicit val addAddProcessingTypeCmdReads: Reads[AddProcessingTypeCmd] =
    Json.reads[AddProcessingTypeCmd]

  implicit val updateNameCmdReads: Reads[UpdateNameCmd] = Json.reads[UpdateNameCmd]

  implicit val removeProcessingTypeCmdReads: Reads[RemoveProcessingTypeCmd] =
    Json.reads[RemoveProcessingTypeCmd]

  implicit val addProcessingTypeAnnotationTypeCmdReads: Reads[AddProcessingTypeAnnotationTypeCmd] =
    Json.reads[AddProcessingTypeAnnotationTypeCmd]

  implicit val updateProcessingTypeAnnotationTypeCmdReads: Reads[UpdateProcessingTypeAnnotationTypeCmd] =
    Json.reads[UpdateProcessingTypeAnnotationTypeCmd]

  implicit val removeProcessingTypeAnnotationTypeCmdReads: Reads[RemoveProcessingTypeAnnotationTypeCmd] =
    Json.reads[RemoveProcessingTypeAnnotationTypeCmd]


}
