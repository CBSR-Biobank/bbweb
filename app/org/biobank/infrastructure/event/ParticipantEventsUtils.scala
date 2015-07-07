package org.biobank.infrastructure.event

import org.biobank.domain.participants._

import org.biobank.infrastructure.command.ParticipantCommands.ParticipantCommand
import org.biobank.infrastructure.event.ParticipantEvents._

import org.joda.time.DateTime

object ParticipantEventsUtil {

  /**
   * Creates an event with the userId for the user that issued the command, and the current date and time.
   */
  def createEvent(participant: Participant, command: ParticipantCommand) =
    ParticipantEvent(id     = participant.id.id,
                     userId = command.userId,
                     time   = Some(EventUtils.ISODateTimeFormatter.print(DateTime.now)))


}
