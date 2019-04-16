package org.biobank.infrastructure.events

import java.time.format.DateTimeFormatter
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.centres.ShipmentSpecimen
import org.biobank.domain.participants.SpecimenId
import org.biobank.domain.studies.{CollectionSpecimenDefinition, SpecimenDefinitionId}
import org.biobank.infrastructure.commands.SpecimenCommands.SpecimenInfo
import org.biobank.infrastructure.events.CollectionEventTypeEvents._
import org.biobank.infrastructure.events.CommonEvents.{AnnotationType => EventAnnotationType}
import org.biobank.infrastructure.events.ShipmentSpecimenEvents._
import org.biobank.infrastructure.events.SpecimenEvents._

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
      : org.biobank.infrastructure.events.CommonEvents.Annotation = {
    org.biobank.infrastructure.events.CommonEvents.Annotation().update(
      _.annotationTypeId    := annotation.annotationTypeId.id,
      _.valueType           := annotation.valueType.toString,
      _.optionalStringValue := annotation.stringValue,
      _.optionalNumberValue := annotation.numberValue,
      _.selectedValues      := annotation.selectedValues.toSeq
    )
  }

  def annotationFromEvent(event: org.biobank.infrastructure.events.CommonEvents.Annotation)
      : org.biobank.domain.annotations.Annotation = {
    org.biobank.domain.annotations.Annotation(
      annotationTypeId = AnnotationTypeId(event.getAnnotationTypeId),
      valueType        = AnnotationValueType.withName(event.getValueType),
      stringValue      = event.stringValue,
      numberValue      = event.numberValue,
      selectedValues   = event.selectedValues.toSet
    )
  }

  def specimenDefinitionToEvent(specimenDefinition: CollectionSpecimenDefinition):
      CollectionEventTypeEvent.SpecimenDefinition = {
    CollectionEventTypeEvent.SpecimenDefinition().update(
      _.id                      := specimenDefinition.id.id,
      _.name                    := specimenDefinition.name,
      _.optionalDescription     := specimenDefinition.description,
      _.units                   := specimenDefinition.units,
      _.anatomicalSourceType    := specimenDefinition.anatomicalSourceType.toString,
      _.preservationType        := specimenDefinition.preservationType.toString,
      _.preservationTemperature := specimenDefinition.preservationTemperature.toString,
      _.specimenType            := specimenDefinition.specimenType.toString,
      _.maxCount                := specimenDefinition.maxCount,
      _.amount                  := specimenDefinition.amount.doubleValue
    )
  }

  def specimenDefinitionFromEvent(event: CollectionEventTypeEvent.SpecimenDefinition)
      : CollectionSpecimenDefinition = {
    CollectionSpecimenDefinition(
      id                      = SpecimenDefinitionId(event.getId),
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
      _.specimenDefinitionId := specimenInfo.specimenDefinitionId,
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
