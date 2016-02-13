package org.biobank.infrastructure.event

import org.biobank.infrastructure.event.CommonEvents._
import org.biobank.domain.{ AnnotationType, AnnotationValueType }
import org.joda.time._
import org.joda.time.format.ISODateTimeFormat

object EventUtils {

  def annotationTypeToEvent(annotationType: AnnotationType): AnnotationTypeAdded = {
    AnnotationTypeAdded().update(
      _.uniqueId              := annotationType.uniqueId,
      _.name                  := annotationType.name,
      _.optionalDescription   := annotationType.description,
      _.valueType             := annotationType.valueType.toString,
      _.optionalMaxValueCount := annotationType.maxValueCount,
      _.options               := annotationType.options,
      _.required              := annotationType.required
    )
  }

  def annotationTypeFromEvent(event: AnnotationTypeAdded): AnnotationType = {
    AnnotationType(
      uniqueId      = event.getUniqueId,
      name          = event.getName,
      description   = event.description,
      valueType     = AnnotationValueType.withName(event.getValueType),
      maxValueCount = event.maxValueCount,
      options       = event.options,
      required      = event.getRequired
    )
  }

  lazy val ISODateTimeFormatter    = ISODateTimeFormat.dateTime.withZone(DateTimeZone.UTC)
  lazy val ISODateTimeParser       = ISODateTimeFormat.dateTimeParser

}
