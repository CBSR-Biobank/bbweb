package org.biobank.infrastructure.event

import org.biobank.infrastructure.command.StudyCommands.StudyCommand
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.infrastructure._
import org.biobank.domain.study._

import play.api.libs.json.Reads._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

object StudyEventsUtil {

  def convertAnnotationTypeDataToEvent[T <: AnnotationTypeData](annotTypeData: List[T])
      : Seq[CollectionEventTypeAddedEvent.AnnotationTypeData] = {
    annotTypeData.map { atd =>
      CollectionEventTypeAddedEvent.AnnotationTypeData(
        annotationTypeId = Some(atd.annotationTypeId),
        required = Some(atd.required))
    }
  }

  def convertCollectionEventTypeAnnotationTypeDataFromEvent
    (annotTypeData: Seq[CollectionEventTypeAddedEvent.AnnotationTypeData])
      : List[CollectionEventTypeAnnotationTypeData] = {
    annotTypeData.map { atd =>
      CollectionEventTypeAnnotationTypeData(
        annotationTypeId = atd.getAnnotationTypeId,
        required = atd.getRequired)
    } toList
  }

  def convertSpecimenLinkTypeAnnotationTypeDataFromEvent
    (annotTypeData: Seq[CollectionEventTypeAddedEvent.AnnotationTypeData])
      : List[SpecimenLinkTypeAnnotationTypeData] = {
    annotTypeData.map { atd =>
      SpecimenLinkTypeAnnotationTypeData(
        annotationTypeId = atd.getAnnotationTypeId,
        required = atd.getRequired)
    } toList
  }

  /**
   * Creates an event with the userId for the user that issued the command, and the current date and time.
   */
  def createStudyEvent(id: StudyId, command: StudyCommand) =
    StudyEvent(id     = id.id,
               userId = command.userId,
               time   = Some(ISODateTimeFormat.dateTime.print(DateTime.now)))

}
