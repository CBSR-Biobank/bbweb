package org.biobank.infrastructure.event

import org.biobank.infrastructure.command.StudyCommands.StudyCommand
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.study._

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

object StudyEventsUtil {

  /**
   * Creates an event with the userId for the user that issued the command, and the current date and time.
   */
  def createStudyEvent(id: StudyId, command: StudyCommand): StudyEventOld =
    StudyEventOld(id.id).update(
      _.optionalUserId := command.userId,
      _.time           := ISODateTimeFormat.dateTime.print(DateTime.now))

}
