package org.biobank.infrastructure.command

import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure.command.Commands._

object ParticipantCommands {

  trait ParticipantCommand extends Command

  trait ParticipantIdentity { val participantId: String }

}