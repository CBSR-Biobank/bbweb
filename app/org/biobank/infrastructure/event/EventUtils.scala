package org.biobank.infrastructure.event

import org.biobank.infrastructure.command.SpecimenCommands.SpecimenInfo
import org.biobank.infrastructure.event.CommonEvents. {
  AnnotationType => EventAnnotationType
}
import org.biobank.infrastructure.event.CollectionEventTypeEvents._
import org.biobank.infrastructure.event.SpecimenEvents._
import org.biobank.domain.{
  AnnotationType,
  AnnotationValueType,
  AnatomicalSourceType,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import org.biobank.infrastructure.event.ShipmentSpecimenEvents._
import org.biobank.domain.study.CollectionSpecimenSpec
import org.biobank.domain.participants.SpecimenId
import org.biobank.domain.centre.ShipmentSpecimen
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

  def annotationToEvent(annotation: org.biobank.domain.Annotation)
      : org.biobank.infrastructure.event.CommonEvents.Annotation = {
    org.biobank.infrastructure.event.CommonEvents.Annotation().update(
      _.annotationTypeId    := annotation.annotationTypeId,
      _.optionalStringValue := annotation.stringValue,
      _.optionalNumberValue := annotation.numberValue,
      _.selectedValues      := annotation.selectedValues.toSeq
    )
  }

  def annotationFromEvent(event: org.biobank.infrastructure.event.CommonEvents.Annotation)
      : org.biobank.domain.Annotation = {
    org.biobank.domain.Annotation(
      annotationTypeId = event.getAnnotationTypeId,
      stringValue      = event.stringValue,
      numberValue      = event.numberValue,
      selectedValues   = event.selectedValues.toSet
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
      _.amount                      := specimenSpec.amount.doubleValue
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
      amount                      = event.getAmount
    )
  }

  def specimenInfoToEvent(id: SpecimenId, specimenInfo: SpecimenInfo): SpecimenEvent.Added.SpecimenInfo = {
    SpecimenEvent.Added.SpecimenInfo().update(
      _.id             := id.id,
      _.inventoryId    := specimenInfo.inventoryId,
      _.specimenSpecId := specimenInfo.specimenSpecId,
      _.timeCreated    := ISODateTimeFormatter.print(specimenInfo.timeCreated),
      _.locationId     := specimenInfo.locationId,
      _.amount         := specimenInfo.amount.doubleValue
    )
  }

  def shipmentSpecimenInfoToEvent(shipmentSpecimen: ShipmentSpecimen)
      : ShipmentSpecimenEvent.ShipmentSpecimenInfo = {
    ShipmentSpecimenEvent.ShipmentSpecimenInfo().update(
      _.shipmentSpecimenId := shipmentSpecimen.id.id,
      _.version            := shipmentSpecimen.version
    )
  }

  lazy val ISODateTimeFormatter    = ISODateTimeFormat.dateTime.withZone(DateTimeZone.UTC)
  lazy val ISODateTimeParser       = ISODateTimeFormat.dateTimeParser

}
