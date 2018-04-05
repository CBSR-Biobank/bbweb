package org.biobank.infrastructure.event

import java.time.format.DateTimeFormatter
import org.biobank.infrastructure.command.SpecimenCommands.SpecimenInfo
import org.biobank.infrastructure.event.CommonEvents.{AnnotationType => EventAnnotationType}
import org.biobank.infrastructure.event.CollectionEventTypeEvents._
import org.biobank.infrastructure.event.SpecimenEvents._
import org.biobank.domain._
import org.biobank.domain.study.{CollectionSpecimenDescription, SpecimenDescriptionId}
import org.biobank.domain.participants.SpecimenId
import org.biobank.domain.centre.ShipmentSpecimen
import org.biobank.infrastructure.event.ShipmentSpecimenEvents._

object EventUtils {

  def annotationTypeToEvent(annotationType: AnnotationType): EventAnnotationType = {
    EventAnnotationType().update(
      _.id                    := annotationType.id.id,
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
      id            = AnnotationTypeId(event.getId),
      slug          = Slug(event.getName),
      name          = event.getName,
      description   = event.description,
      valueType     = AnnotationValueType.withName(event.getValueType),
      maxValueCount = event.maxValueCount,
      options       = event.options,
      required      = event.getRequired
    )
  }

  def annotationToEvent(annotation: org.biobank.domain.annotations.Annotation)
      : org.biobank.infrastructure.event.CommonEvents.Annotation = {
    org.biobank.infrastructure.event.CommonEvents.Annotation().update(
      _.annotationTypeId    := annotation.annotationTypeId.id,
      _.optionalStringValue := annotation.stringValue,
      _.optionalNumberValue := annotation.numberValue,
      _.selectedValues      := annotation.selectedValues.toSeq
    )
  }

  def annotationFromEvent(event: org.biobank.infrastructure.event.CommonEvents.Annotation)
      : org.biobank.domain.annotations.Annotation = {
    org.biobank.domain.annotations.Annotation(
      annotationTypeId = AnnotationTypeId(event.getAnnotationTypeId),
      stringValue      = event.stringValue,
      numberValue      = event.numberValue,
      selectedValues   = event.selectedValues.toSet
    )
  }

  def specimenDescriptionToEvent(specimenDesc: CollectionSpecimenDescription):
      CollectionEventTypeEvent.SpecimenDescription = {
    CollectionEventTypeEvent.SpecimenDescription().update(
      _.id                          := specimenDesc.id.id,
      _.name                        := specimenDesc.name,
      _.optionalDescription         := specimenDesc.description,
      _.units                       := specimenDesc.units,
      _.anatomicalSourceType        := specimenDesc.anatomicalSourceType.toString,
      _.preservationType            := specimenDesc.preservationType.toString,
      _.preservationTemperature := specimenDesc.preservationTemperature.toString,
      _.specimenType                := specimenDesc.specimenType.toString,
      _.maxCount                    := specimenDesc.maxCount,
      _.amount                      := specimenDesc.amount.doubleValue
    )
  }

  def specimenDescriptionFromEvent(event: CollectionEventTypeEvent.SpecimenDescription)
      : CollectionSpecimenDescription = {
    CollectionSpecimenDescription(
      id                      = SpecimenDescriptionId(event.getId),
      slug                    = Slug(event.getName),
      name                    = event.getName,
      description             = event.description,
      units                   = event.getUnits,
      anatomicalSourceType    = AnatomicalSourceType.withName(event.getAnatomicalSourceType),
      preservationType        = PreservationType.withName(event.getPreservationType),
      preservationTemperature = PreservationTemperature.withName(event.getPreservationTemperature),
      specimenType            = SpecimenType.withName(event.getSpecimenType),
      maxCount                = event.getMaxCount,
      amount                  = event.getAmount
    )
  }

  def specimenInfoToEvent(id: SpecimenId, specimenInfo: SpecimenInfo): SpecimenEvent.Added.SpecimenInfo = {
    SpecimenEvent.Added.SpecimenInfo().update(
      _.id                    := id.id,
      _.inventoryId           := specimenInfo.inventoryId,
      _.specimenDescriptionId := specimenInfo.specimenDescriptionId,
      _.timeCreated           := specimenInfo.timeCreated.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.locationId            := specimenInfo.locationId,
      _.amount                := specimenInfo.amount.doubleValue
    )
  }

  def shipmentSpecimenInfoToEvent(shipmentSpecimen: ShipmentSpecimen)
      : ShipmentSpecimenEvent.ShipmentSpecimenInfo = {
    ShipmentSpecimenEvent.ShipmentSpecimenInfo().update(
      _.shipmentSpecimenId := shipmentSpecimen.id.id,
      _.version            := shipmentSpecimen.version
    )
  }

}
