package infrastructure.command

import domain.AnnotationValueType._
import infrastructure.command.Commands._

object ParticipantCommands {

  trait ParticipantCommand extends Command

  trait ParticipantIdentity { val participantId: String }

}