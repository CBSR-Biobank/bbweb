package org.biobank.infrastructure.event

import org.biobank.infrastructure.event.CommonEvents. {
  AnnotationType => EventAnnotationType
}
import org.biobank.infrastructure.event.CollectionEventTypeEvents._
import org.biobank.domain.{
  AnnotationType,
  AnnotationValueType,
  AnatomicalSourceType,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import org.biobank.domain.study.CollectionSpecimenSpec
import org.joda.time._
import org.joda.time.format.ISODateTimeFormat

object EventUtils {

  def annotationTypeToEvent(annotationType: AnnotationType): EventAnnotationType = {
    EventAnnotationType().update(
      _.uniqueId              := annotationType.uniqueId,
      _.name                  := annotationType.name,
      _.optionalDescription   := annotationType.description,
      _.valueType             := annotationType.valueType.toString,
      _.optionalMaxValueCount := annotationType.maxValueCount,
      _.options               := annotationType.options,
      _.required              := annotationType.required
    )
  }

  def annotationTypeFromEvent(event: EventAnnotationType): AnnotationType = {
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

  def specimenSpecToEvent(specimenSpec: CollectionSpecimenSpec): CollectionEventTypeEvent.SpecimenSpec = {
    CollectionEventTypeEvent.SpecimenSpec().update(
      _.uniqueId                    := specimenSpec.uniqueId,
      _.name                        := specimenSpec.name,
      _.optionalDescription         := specimenSpec.description,
      _.units                       := specimenSpec.units,
      _.anatomicalSourceType        := specimenSpec.anatomicalSourceType.toString,
      _.preservationType            := specimenSpec.preservationType.toString,
      _.preservationTemperatureType := specimenSpec.preservationTemperatureType.toString,
      _.specimenType                := specimenSpec.specimenType.toString,
      _.maxCount                    := specimenSpec.maxCount,
      _.optionalAmount              := specimenSpec.amount.map(_.doubleValue)
    )
  }

  def specimenSpecFromEvent(event: CollectionEventTypeEvent.SpecimenSpec): CollectionSpecimenSpec = {
    CollectionSpecimenSpec(
      uniqueId                    = event.getUniqueId,
      name                        = event.getName,
      description                 = event.description,
      units                       = event.getUnits,
      anatomicalSourceType        = AnatomicalSourceType.withName(event.getAnatomicalSourceType),
      preservationType            = PreservationType.withName(event.getPreservationType),
      preservationTemperatureType = PreservationTemperatureType.withName(event.getPreservationTemperatureType),
      specimenType                = SpecimenType.withName(event.getSpecimenType),
      maxCount                    = event.getMaxCount,
      amount                      = event.amount.map(BigDecimal(_))
    )
  }

  lazy val ISODateTimeFormatter    = ISODateTimeFormat.dateTime.withZone(DateTimeZone.UTC)
  lazy val ISODateTimeParser       = ISODateTimeFormat.dateTimeParser

}
