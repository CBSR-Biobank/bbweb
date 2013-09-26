package service.commands

import domain.participant.ParticipantId
import domain.AnnotationValueType._

trait ParticipantCommand extends Command
trait ParticipantIdentity { val participantId: String }