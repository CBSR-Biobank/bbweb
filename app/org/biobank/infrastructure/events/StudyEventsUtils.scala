package org.biobank.infrastructure.events

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.biobank.infrastructure.commands.StudyCommands.StudyCommand
import org.biobank.infrastructure.events.StudyEvents._
import org.biobank.domain.studies._

object StudyEventsUtil {

  /**
   * Creates an event with the userId for the user that issued the command, and the current date and time.
   */
  def createStudyEvent(id: StudyId, command: StudyCommand): StudyEventOld =
    StudyEventOld(id.id).update(
      _.optionalSessionUserId := command.sessionUserId,
      _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))

}
