package org.biobank.infrastructure.command

import org.biobank.domain.Annotation
import org.biobank.infrastructure.command.Commands._
import play.api.libs.json._

object ParticipantCommands {

  trait ParticipantCommand extends Command with HasUserId

  trait ParticipantModifyCommand
      extends ParticipantCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddParticipantCmd(userId:      String,
                               studyId:     String,
                               uniqueId:    String,
                               annotations: List[Annotation])
      extends ParticipantCommand

  case class UpdateParticipantUniqueIdCmd(userId:          String,
                                          id:              String,
                                          expectedVersion: Long,
                                          uniqueId:        String)
      extends ParticipantModifyCommand

  case class ParticipantAddAnnotationCmd(userId:           String,
                                         id:               String,
                                         expectedVersion:  Long,
                                         annotationTypeId: String,
                                         stringValue:      Option[String],
                                         numberValue:      Option[String],
                                         selectedValues:   Set[String])
      extends ParticipantModifyCommand

  case class ParticipantRemoveAnnotationCmd(userId:           String,
                                            id:               String,
                                            expectedVersion:  Long,
                                            annotationTypeId: String)
      extends ParticipantModifyCommand

  implicit val addParticipantCmdReads              = Json.reads[AddParticipantCmd]
  implicit val updateParticipantUniqueIdCmdReads   = Json.reads[UpdateParticipantUniqueIdCmd]
  implicit val participantAddAnnotationCmdReads    = Json.reads[ParticipantAddAnnotationCmd]
  implicit val participantRemoveAnnotationCmdReads = Json.reads[ParticipantRemoveAnnotationCmd]

}
