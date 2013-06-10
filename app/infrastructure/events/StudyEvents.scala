package infrastructure.events

import domain.study.StudyId
import domain.study.SpecimenGroupId
import domain.UserId
import domain.AnatomicalSourceType._
import domain.AnnotationValueType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._

// study events
//
// FIXME: need a base class here
case class StudyAddedEvent(id: StudyId, name: String, description: String)
case class StudyUpdatedEvent(id: StudyId, name: String, description: String)
case class StudyEnabledEvent(id: StudyId)
case class StudyDisabledEvent(id: StudyId)

// specimen group events
case class StudySpecimenGroupAddedEvent(studyId: StudyId, specimenGroupId: SpecimenGroupId,
  name: String, description: String, units: String, anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
case class StudySpecimenGroupUpdatedEvent(studyId: StudyId, specimenGroupId: SpecimenGroupId,
  name: String, description: String, units: String, anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
case class StudySpecimenGroupRemovedEvent(studyId: StudyId, specimenGroupId: SpecimenGroupId)

// collection event events
case class CollectionEventTypeAddedEvent(
  studyId: String, name: String, description: String, recurring: Boolean)

case class CollectionEventTypeUpdatedEvent(
  studyId: String, collectionEventId: String, name: String, description: String, recurring: Boolean)

case class CollectionEventTypeRemovedEvent(studyId: String, collectionEventId: String)

case class SpecimenGroupAddedToCollectionEventTypeEvent(
  studyId: String, collectionEventId: String, specimenGroupId: String, count: Int, amount: BigDecimal)

case class SpecimenGroupRemovedFromCollectionEventTypeEvent(sg2cetId: String)

case class CollectionEventAnnotationTypeAddedEvent(studyId: String, name: String,
  description: String, valueType: AnnotationValueType, maxValueCount: Int)

case class CollectionEventAnnotationTypeUpdatedEvent(
  studyId: String, collectionEventAnnotationTypeId: String,
  name: String, description: String, valueType: AnnotationValueType, maxValueCount: Int)

case class CollectionEventAnnotationTypeRemovedEvent(
  studyId: String, collectionEventAnnotationTypeId: String)

case class AnnotationTypeAddedToCollectionEventTypeEvent(
  studyId: String, collectionEventId: String, annotationTypeId: String)

case class AnnotationTypeRemovedFromCollectionEventTypeEvent(studyId: String, cet2AtId: String)
