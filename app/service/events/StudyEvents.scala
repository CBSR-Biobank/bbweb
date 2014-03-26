package service.events

import domain.study._
import domain.AnnotationTypeId
import domain.AnatomicalSourceType._
import domain.AnnotationValueType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._

/**
 * Events used by the Study Aggregate.
 */
object StudyEvents {

  sealed trait StudyEvent

  case class StudyAddedEvent(
    id: String,
    version: Long,
    name: String,
    description: Option[String])
    extends StudyEvent

  case class StudyUpdatedEvent(
    id: String,
    version: Long,
    name: String,
    description: Option[String])
    extends StudyEvent

  case class StudyEnabledEvent(
    id: String,
    version: Long)
    extends StudyEvent

  case class StudyDisabledEvent(
    id: String,
    version: Long)
    extends StudyEvent

  // specimen group events
  case class SpecimenGroupAddedEvent(
    studyid: String,
    specimenGroupId: String,
    version: Long,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType)
    extends StudyEvent

  case class SpecimenGroupUpdatedEvent(
    studyid: String,
    specimenGroupId: String,
    version: Long,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType)

  case class SpecimenGroupRemovedEvent(
    studyid: String,
    specimenGroupId: String)
    extends StudyEvent

  // collection event events
  case class CollectionEventTypeAddedEvent(
    studyid: String,
    collectionEventTypeId: String,
    version: Long,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: Set[CollectionEventTypeSpecimenGroup],
    annotationTypeData: Set[CollectionEventTypeAnnotationType])
    extends StudyEvent

  case class CollectionEventTypeUpdatedEvent(
    studyid: String,
    collectionEventTypeId: String,
    version: Long,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: Set[CollectionEventTypeSpecimenGroup],
    annotationTypeData: Set[CollectionEventTypeAnnotationType])
    extends StudyEvent

  case class CollectionEventTypeRemovedEvent(
    studyid: String,
    collectionEventTypeId: String)
    extends StudyEvent

  case class SpecimenGroupAddedToCollectionEventTypeEvent(
    studyid: String,
    sg2cetId: String,
    collectionEventTypeId: String,
    specimenGroupId: String,
    maxCount: Int,
    amount: BigDecimal)
    extends StudyEvent

  case class SpecimenGroupRemovedFromCollectionEventTypeEvent(
    studyid: String,
    sg2cetId: String,
    collectionEventTypeId: String,
    specimenGroupId: String)
    extends StudyEvent

  case class CollectionEventAnnotationTypeAddedEvent(
    studyid: String,
    annotationTypeId: String,
    version: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class CollectionEventAnnotationTypeUpdatedEvent(
    studyid: String,
    annotationTypeId: String,
    version: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class CollectionEventAnnotationTypeRemovedEvent(
    studyid: String,
    annotationTypeId: String)

  case class AnnotationTypeAddedToCollectionEventTypeEvent(
    studyid: String,
    collectionEventTypeAnnotationTypeId: String,
    collectionEventTypeId: String,
    annotationTypeId: String)
    extends StudyEvent

  case class AnnotationTypeRemovedFromCollectionEventTypeEvent(
    studyid: String,
    collectionEventTypeAnnotationTypeId: String,
    collectionEventTypeId: String,
    annotationTypeId: String)
    extends StudyEvent

  // participant annotation types

  case class ParticipantAnnotationTypeAddedEvent(
    studyid: String,
    annotationTypeId: String,
    version: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class ParticipantAnnotationTypeUpdatedEvent(
    studyid: String,
    annotationTypeId: String,
    version: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class ParticipantAnnotationTypeRemovedEvent(
    studyid: String,
    annotationTypeId: String)
    extends StudyEvent

  // specimen link annotation types

  case class SpecimenLinkAnnotationTypeAddedEvent(
    studyid: String,
    annotationTypeId: String,
    version: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class SpecimenLinkAnnotationTypeUpdatedEvent(
    studyid: String,
    annotationTypeId: String,
    version: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class SpecimenLinkAnnotationTypeRemovedEvent(
    studyid: String,
    annotationTypeId: String)
    extends StudyEvent

}