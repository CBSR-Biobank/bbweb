package org.biobank.infrastructure.command

import org.biobank.domain.annotations.Annotation
import org.biobank.infrastructure.command.Commands._
import play.api.libs.json._

object ParticipantCommands {

  trait ParticipantCommand extends Command with HasSessionUserId

  trait ParticipantModifyCommand
      extends ParticipantCommand
      with HasIdentity
      with HasExpectedVersion

  final case class AddParticipantCmd(sessionUserId: String,
                                     studyId:       String,
                                     uniqueId:      String,
                                     annotations:   List[Annotation])
      extends ParticipantCommand

  final case class UpdateParticipantUniqueIdCmd(sessionUserId:   String,
                                                id:              String,
                                                expectedVersion: Long,
                                                uniqueId:        String)
      extends ParticipantModifyCommand

  final case class ParticipantUpdateAnnotationCmd(sessionUserId:    String,
                                               id:               String,
                                               expectedVersion:  Long,
                                               annotationTypeId: String,
                                               stringValue:      Option[String],
                                               numberValue:      Option[String],
                                               selectedValues:   Set[String])
      extends ParticipantModifyCommand

  final case class ParticipantRemoveAnnotationCmd(sessionUserId:    String,
                                                  id:               String,
                                                  expectedVersion:  Long,
                                                  annotationTypeId: String)
      extends ParticipantModifyCommand

  implicit val addParticipantCmdReads: Reads[AddParticipantCmd] =
    Json.reads[AddParticipantCmd]

  implicit val updateParticipantUniqueIdCmdReads: Reads[UpdateParticipantUniqueIdCmd] =
    Json.reads[UpdateParticipantUniqueIdCmd]

  implicit val participantAddAnnotationCmdReads: Reads[ParticipantUpdateAnnotationCmd] =
    Json.reads[ParticipantUpdateAnnotationCmd]

  implicit val participantRemoveAnnotationCmdReads: Reads[ParticipantRemoveAnnotationCmd] =
    Json.reads[ParticipantRemoveAnnotationCmd]

}
