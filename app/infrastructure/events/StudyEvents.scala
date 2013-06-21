package infrastructure.events

import domain.study.StudyId
import domain.study.SpecimenGroupId
import domain.study.CollectionEventTypeId
import domain.AnnotationTypeId
import domain.UserId
import domain.AnatomicalSourceType._
import domain.AnnotationValueType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._

// study events
//
// FIXME: need a base class here
case class StudyAddedEvent(id: StudyId, name: String, description: Option[String])
case class StudyUpdatedEvent(id: StudyId, name: String, description: Option[String])
case class StudyEnabledEvent(id: StudyId)
case class StudyDisabledEvent(id: StudyId)

// specimen group events
case class StudySpecimenGroupAddedEvent(studyId: StudyId, specimenGroupId: SpecimenGroupId,
  name: String, description: Option[String], units: String, anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
case class StudySpecimenGroupUpdatedEvent(studyId: StudyId, specimenGroupId: SpecimenGroupId,
  name: String, description: Option[String], units: String, anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
case class StudySpecimenGroupRemovedEvent(studyId: StudyId, specimenGroupId: SpecimenGroupId)

// collection event events
case class CollectionEventTypeAddedEvent(
  studyId: StudyId, collectionEventTypeId: CollectionEventTypeId,
  name: String, description: Option[String], recurring: Boolean)

case class CollectionEventTypeUpdatedEvent(
  studyId: StudyId, collectionEventTypeId: CollectionEventTypeId,
  name: String, description: Option[String], recurring: Boolean)

case class CollectionEventTypeRemovedEvent(
  studyId: StudyId, collectionEventTypeId: CollectionEventTypeId)

case class SpecimenGroupAddedToCollectionEventTypeEvent(
  studyId: StudyId, sg2cetId: String, collectionEventTypeId: CollectionEventTypeId,
  specimenGroupId: SpecimenGroupId, ount: Int, amount: BigDecimal)

case class SpecimenGroupRemovedFromCollectionEventTypeEvent(
  studyId: StudyId, sg2cetId: String,
  collectionEventTypeId: CollectionEventTypeId, specimenGroupId: SpecimenGroupId)

case class CollectionEventAnnotationTypeAddedEvent(
  studyId: StudyId, annotationTypeId: AnnotationTypeId,
  name: String, description: Option[String], valueType: AnnotationValueType,
  maxValueCount: Option[Int], options: Option[Map[String, String]])

case class CollectionEventAnnotationTypeUpdatedEvent(
  studyId: StudyId, annotationTypeId: AnnotationTypeId,
  name: String, description: Option[String], valueType: AnnotationValueType,
  maxValueCount: Option[Int], options: Option[Map[String, String]])

case class CollectionEventAnnotationTypeRemovedEvent(
  studyId: StudyId, annotationTypeId: AnnotationTypeId)

case class AnnotationTypeAddedToCollectionEventTypeEvent(
  studyId: StudyId, collectionEventTypeAnnotationTypeId: String,
  collectionEventTypeId: CollectionEventTypeId, annotationTypeId: AnnotationTypeId)

case class AnnotationTypeRemovedFromCollectionEventTypeEvent(
  studyId: StudyId, collectionEventTypeAnnotationTypeId: String,
  collectionEventTypeId: CollectionEventTypeId, annotationTypeId: AnnotationTypeId)

