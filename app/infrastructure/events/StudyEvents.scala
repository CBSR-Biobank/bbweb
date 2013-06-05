package infrastructure.events

import domain.study.SpecimenTypeId
import domain.study.StudyId
import domain.study.SpecimenGroupId
import domain.UserId
import domain.AnatomicalSourceType._
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
  studyId: String, collectionEventId: String, specimenGroupId: String)
case class SpecimenGroupRemovedFromCollectionEventTypeEvent(
  studyId: String, collectionEventId: String, specimenGroupId: String)
case class AnnotationAddedToCollectionEventTypeEvent(
  studyId: String, collectionEventId: String, collectionEventAnnotationTypeId: String)
case class AnnotationRemovedFromCollectionEventTypeEvent(
  studyId: String, collectionEventId: String, collectionEventAnnotationTypeId: String)