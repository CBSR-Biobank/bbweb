package org.biobank.infrastructure.event

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.biobank.infrastructure.command.StudyCommands.StudyCommand
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.study._

object StudyEventsUtil {

  /**
   * Creates an event with the userId for the user that issued the command, and the current date and time.
   */
  def createStudyEvent(id: StudyId, command: StudyCommand): StudyEventOld =
    StudyEventOld(id.id).update(
      _.optionalSessionUserId := command.sessionUserId,
      _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))

}
