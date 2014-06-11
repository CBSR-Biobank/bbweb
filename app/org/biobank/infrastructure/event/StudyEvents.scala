package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.domain.ContainerTypeId
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.AnnotationValueType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import org.joda.time.DateTime

/**
 * Events used by the Study Aggregate.
 */
object StudyEvents {

  sealed trait StudyEvent

  case class StudyAddedEvent(
    id: String,
    dateTime: DateTime,
    name: String,
    description: Option[String])
    extends StudyEvent

  case class StudyUpdatedEvent(
    id: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String])
    extends StudyEvent

  case class StudyEnabledEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
    extends StudyEvent

  case class StudyDisabledEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
    extends StudyEvent

  case class StudyRetiredEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
    extends StudyEvent

  case class StudyUnretiredEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
    extends StudyEvent

  // specimen group events
  trait SpecimenGroupEvent extends StudyEvent

  case class SpecimenGroupAddedEvent(
    studyId: String,
    specimenGroupId: String,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType)
    extends SpecimenGroupEvent

  case class SpecimenGroupUpdatedEvent(
    studyId: String,
    specimenGroupId: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType)
    extends SpecimenGroupEvent

  case class SpecimenGroupRemovedEvent(
    studyId: String,
    specimenGroupId: String)
    extends SpecimenGroupEvent

  // collection event events
  case class CollectionEventTypeAddedEvent(
    studyId: String,
    collectionEventTypeId: String,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
    extends StudyEvent

  case class CollectionEventTypeUpdatedEvent(
    studyId: String,
    collectionEventTypeId: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
    extends StudyEvent

  case class CollectionEventTypeRemovedEvent(
    studyId: String,
    collectionEventTypeId: String)
    extends StudyEvent

  case class CollectionEventAnnotationTypeAddedEvent(
    studyId: String,
    annotationTypeId: String,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class CollectionEventAnnotationTypeUpdatedEvent(
    studyId: String,
    annotationTypeId: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class CollectionEventAnnotationTypeRemovedEvent(
    studyId: String,
    annotationTypeId: String)

  // participant annotation types

  case class ParticipantAnnotationTypeAddedEvent(
    studyId: String,
    annotationTypeId: String,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]],
    required: Boolean = false)
    extends StudyEvent

  case class ParticipantAnnotationTypeUpdatedEvent(
    studyId: String,
    annotationTypeId: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]],
    required: Boolean = false)
    extends StudyEvent

  case class ParticipantAnnotationTypeRemovedEvent(
    studyId: String,
    annotationTypeId: String)
    extends StudyEvent

  // procesing type events
  case class ProcessingTypeAddedEvent(
    studyId: String,
    processingTypeId: String,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    enabled: Boolean)
    extends StudyEvent

  case class ProcessingTypeUpdatedEvent(
    studyId: String,
    processingTypeId: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    enabled: Boolean)
    extends StudyEvent

  case class ProcessingTypeRemovedEvent(
    studyId: String,
    processingTypeId: String)
    extends StudyEvent

  // specimen link type
  case class SpecimenLinkTypeAddedEvent(
    processingTypeId: String,
    specimenLinkTypeId: String,
    dateTime: DateTime,
    expectedInputChange: BigDecimal,
    expectedOutputChange: BigDecimal,
    inputCount: Int,
    outputCount: Int,
    inputGroupId: SpecimenGroupId,
    outputGroupId: SpecimenGroupId,
    inputContainerTypeId: Option[ContainerTypeId],
    outputContainerTypeId: Option[ContainerTypeId],
    annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData])
    extends StudyEvent

  case class SpecimenLinkTypeUpdatedEvent(
    processingTypeId: String,
    specimenLinkTypeId: String,
    version: Long,
    dateTime: DateTime,
    expectedInputChange: BigDecimal,
    expectedOutputChange: BigDecimal,
    inputCount: Int,
    outputCount: Int,
    inputGroupId: SpecimenGroupId,
    outputGroupId: SpecimenGroupId,
    inputContainerTypeId: Option[ContainerTypeId],
    outputContainerTypeId: Option[ContainerTypeId],
    annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData])
    extends StudyEvent

  case class SpecimenLinkTypeRemovedEvent(
    studyId: String,
    specimenLinkTypeId: String)
    extends StudyEvent

  // specimen link annotation types
  case class SpecimenLinkAnnotationTypeAddedEvent(
    studyId: String,
    annotationTypeId: String,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class SpecimenLinkAnnotationTypeUpdatedEvent(
    studyId: String,
    annotationTypeId: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class SpecimenLinkAnnotationTypeRemovedEvent(
    studyId: String,
    annotationTypeId: String)
    extends StudyEvent

}
