package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.AnnotationValueType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import scala.collection.immutable

/**
 * Events used by the Study Aggregate.
 */
object StudyEvents {

  sealed trait StudyEvent

  case class StudyAddedEvent(
    id: String,
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

  case class StudyRetiredEvent(
    id: String,
    version: Long)
    extends StudyEvent

  case class StudyUnretiredEvent(
    id: String,
    version: Long)
    extends StudyEvent

  // specimen group events
  trait SpecimenGroupEvent extends StudyEvent

  case class SpecimenGroupAddedEvent(
    studyId: String,
    specimenGroupId: String,
    version: Long,
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
    version: Long,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroup],
    annotationTypeData: List[CollectionEventTypeAnnotationType])
    extends StudyEvent

  case class CollectionEventTypeUpdatedEvent(
    studyId: String,
    collectionEventTypeId: String,
    version: Long,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroup],
    annotationTypeData: List[CollectionEventTypeAnnotationType])
    extends StudyEvent

  case class CollectionEventTypeRemovedEvent(
    studyId: String,
    collectionEventTypeId: String)
    extends StudyEvent

  case class SpecimenGroupAddedToCollectionEventTypeEvent(
    studyId: String,
    sg2cetId: String,
    collectionEventTypeId: String,
    specimenGroupId: String,
    maxCount: Int,
    amount: BigDecimal)
    extends StudyEvent

  case class SpecimenGroupRemovedFromCollectionEventTypeEvent(
    studyId: String,
    sg2cetId: String,
    collectionEventTypeId: String,
    specimenGroupId: String)
    extends StudyEvent

  case class CollectionEventAnnotationTypeAddedEvent(
    studyId: String,
    annotationTypeId: String,
    version: Long,
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
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]])
    extends StudyEvent

  case class CollectionEventAnnotationTypeRemovedEvent(
    studyId: String,
    annotationTypeId: String)

  case class AnnotationTypeAddedToCollectionEventTypeEvent(
    studyId: String,
    collectionEventTypeAnnotationTypeId: String,
    collectionEventTypeId: String,
    annotationTypeId: String)
    extends StudyEvent

  case class AnnotationTypeRemovedFromCollectionEventTypeEvent(
    studyId: String,
    collectionEventTypeAnnotationTypeId: String,
    collectionEventTypeId: String,
    annotationTypeId: String)
    extends StudyEvent

  // participant annotation types

  case class ParticipantAnnotationTypeAddedEvent(
    studyId: String,
    annotationTypeId: String,
    version: Long,
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

  // specimen link annotation types

  case class SpecimenLinkAnnotationTypeAddedEvent(
    studyId: String,
    annotationTypeId: String,
    version: Long,
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
