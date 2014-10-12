package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.event.Events._
import org.biobank.domain.study._
import org.biobank.domain.ContainerTypeId
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.AnnotationValueType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import org.biobank.infrastructure.JsonUtils._
import org.biobank.infrastructure.EnumUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime

/**
  * Events used by the Study Aggregate.
  */
object StudyEvents {

  sealed trait StudyEvent extends Event

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

  sealed trait StudyStatusChangedEvent extends StudyEvent {
    val id: String
    val version: Long
    val dateTime: DateTime
  }

  case class StudyEnabledEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends StudyStatusChangedEvent

  case class StudyDisabledEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends StudyStatusChangedEvent

  case class StudyRetiredEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends StudyStatusChangedEvent

  case class StudyUnretiredEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends StudyStatusChangedEvent

  trait StudyEventWithId extends StudyEvent with HasStudyIdentity

  // specimen group events
  trait SpecimenGroupEvent extends StudyEventWithId

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
      extends StudyEventWithId

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
      extends StudyEventWithId

  case class CollectionEventTypeRemovedEvent(
    studyId: String,
    collectionEventTypeId: String)
      extends StudyEventWithId

  case class CollectionEventAnnotationTypeAddedEvent(
    studyId: String,
    annotationTypeId: String,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Seq[String]])
      extends StudyEventWithId

  case class CollectionEventAnnotationTypeUpdatedEvent(
    studyId: String,
    annotationTypeId: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Seq[String]])
      extends StudyEventWithId

  case class CollectionEventAnnotationTypeRemovedEvent(
    studyId: String,
    annotationTypeId: String)
      extends StudyEventWithId

  // participant annotation types

  case class ParticipantAnnotationTypeAddedEvent(
    studyId: String,
    annotationTypeId: String,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Seq[String]],
    required: Boolean = false)
      extends StudyEventWithId

  case class ParticipantAnnotationTypeUpdatedEvent(
    studyId: String,
    annotationTypeId: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Seq[String]],
    required: Boolean = false)
      extends StudyEventWithId

  case class ParticipantAnnotationTypeRemovedEvent(
    studyId: String,
    annotationTypeId: String)
      extends StudyEventWithId

  // procesing type events
  case class ProcessingTypeAddedEvent(
    studyId: String,
    processingTypeId: String,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    enabled: Boolean)
      extends StudyEventWithId

  case class ProcessingTypeUpdatedEvent(
    studyId: String,
    processingTypeId: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    enabled: Boolean)
      extends StudyEventWithId

  case class ProcessingTypeRemovedEvent(
    studyId: String,
    processingTypeId: String)
      extends StudyEventWithId

  // specimen link type
  trait SpecimenLinkTypeEvent extends StudyEvent with HasProcessingTypeIdentity

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
      extends SpecimenLinkTypeEvent

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
      extends SpecimenLinkTypeEvent

  case class SpecimenLinkTypeRemovedEvent(
    processingTypeId: String,
    specimenLinkTypeId: String)
      extends SpecimenLinkTypeEvent

  // specimen link annotation types
  case class SpecimenLinkAnnotationTypeAddedEvent(
    studyId: String,
    annotationTypeId: String,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Seq[String]])
      extends StudyEventWithId

  case class SpecimenLinkAnnotationTypeUpdatedEvent(
    studyId: String,
    annotationTypeId: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Seq[String]])
      extends StudyEventWithId

  case class SpecimenLinkAnnotationTypeRemovedEvent(
    studyId: String,
    annotationTypeId: String)
      extends StudyEventWithId

  implicit val annotationValueTypeFormat: Format[AnnotationValueType] =
    enumFormat(org.biobank.domain.AnnotationValueType)

  implicit val studyAddedEventWriter: Writes[StudyAddedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String]
  )(unlift(StudyAddedEvent.unapply))

  implicit val studyUpdatedEventWriter: Writes[StudyUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String]
  )(unlift(StudyUpdatedEvent.unapply))

  implicit val studyStatusChangeWrites = new Writes[StudyStatusChangedEvent] {
    def writes(event: StudyStatusChangedEvent) = Json.obj(
      "id"       -> event.id,
      "version"  -> event.version,
      "dateTime" -> event.dateTime
    )
  }

  implicit val participantAnnotationTypeAddedEventWriter: Writes[ParticipantAnnotationTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Option[Seq[String]]] and
      (__ \ "required").write[Boolean]
  )(unlift(ParticipantAnnotationTypeAddedEvent.unapply))

  implicit val participantAnnotationTypeUpdatedEventWriter: Writes[ParticipantAnnotationTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Option[Seq[String]]] and
      (__ \ "required").write[Boolean]
  )(unlift(ParticipantAnnotationTypeUpdatedEvent.unapply))

  implicit val participantAnnotationTypeRemovedEventWriter: Writes[ParticipantAnnotationTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String]
  )(unlift(ParticipantAnnotationTypeRemovedEvent.unapply))

  implicit val specimenGroupAddedEventWrites: Writes[SpecimenGroupAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "specimenGroupId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "units").write[String] and
      (__ \ "anatomicalSourceType").write[AnatomicalSourceType] and
      (__ \ "preservationType").write[PreservationType] and
      (__ \ "preservationTemperatureType").write[PreservationTemperatureType] and
      (__ \ "specimenType").write[SpecimenType]
  )(unlift(SpecimenGroupAddedEvent.unapply))

  implicit val specimenGroupUpdatedEventWrites: Writes[SpecimenGroupUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "specimenGroupId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "units").write[String] and
      (__ \ "anatomicalSourceType").write[AnatomicalSourceType] and
      (__ \ "preservationType").write[PreservationType] and
      (__ \ "preservationTemperatureType").write[PreservationTemperatureType] and
      (__ \ "specimenType").write[SpecimenType]
  )(unlift(SpecimenGroupUpdatedEvent.unapply))

  implicit val specimenGroupRemovedEventWriter: Writes[SpecimenGroupRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "specimenGroupId").write[String]
  )(unlift(SpecimenGroupRemovedEvent.unapply))

  implicit val collectionEventAnnotationTypeAddedEventWriter: Writes[CollectionEventAnnotationTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Option[Seq[String]]]
  )(unlift(CollectionEventAnnotationTypeAddedEvent.unapply))

  implicit val collectionEventAnnotationTypeUpdatedEventWriter: Writes[CollectionEventAnnotationTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Option[Seq[String]]]
  )(unlift(CollectionEventAnnotationTypeUpdatedEvent.unapply))

  implicit val collectionEventAnnotationTypeRemovedEventWriter: Writes[CollectionEventAnnotationTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String]
  )(unlift(CollectionEventAnnotationTypeRemovedEvent.unapply))

  implicit val collectionEventTypeAddedEventWriter: Writes[CollectionEventTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "collectionEventTypeId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "recurring").write[Boolean] and
      (__ \ "specimenGroupData").write[List[CollectionEventTypeSpecimenGroupData]] and
      (__ \ "annotationTypeData").write[List[CollectionEventTypeAnnotationTypeData]]
  )(unlift(CollectionEventTypeAddedEvent.unapply))

  implicit val collectionEventTypeUpdatedEventWriter: Writes[CollectionEventTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "collectionEventTypeId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "recurring").write[Boolean] and
      (__ \ "specimenGroupData").write[List[CollectionEventTypeSpecimenGroupData]] and
      (__ \ "annotationTypeData").write[List[CollectionEventTypeAnnotationTypeData]]
  )(unlift(CollectionEventTypeUpdatedEvent.unapply))

  implicit val collectionEventTypeRemovedEventWriter: Writes[CollectionEventTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "collectionEventTypeId").write[String]
  )(unlift(CollectionEventTypeRemovedEvent.unapply))

  implicit val processingTypeAddedEventWrites: Writes[ProcessingTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "processingTypeId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "enabled").write[Boolean]
  )(unlift(ProcessingTypeAddedEvent.unapply))

  implicit val processingTypeUpdatedEventWrites: Writes[ProcessingTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "processingTypeId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "enabled").write[Boolean]
  )(unlift(ProcessingTypeUpdatedEvent.unapply))

  implicit val processingTypeRemovedEventWriter: Writes[ProcessingTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "processingTypeId").write[String]
  )(unlift(ProcessingTypeRemovedEvent.unapply))

  implicit val specimenLinkAnnotationTypeAddedEventWrites: Writes[SpecimenLinkAnnotationTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Option[Seq[String]]]
  )(unlift(SpecimenLinkAnnotationTypeAddedEvent.unapply))

  implicit val specimenLinkAnnotationTypeUpdatedEventWrites: Writes[SpecimenLinkAnnotationTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").write[AnnotationValueType] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Option[Seq[String]]]
  )(unlift(SpecimenLinkAnnotationTypeUpdatedEvent.unapply))

  implicit val specimenLinkAnnotationTypeRemovedEventWrites: Writes[SpecimenLinkAnnotationTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String]
  )(unlift(SpecimenLinkAnnotationTypeRemovedEvent.unapply))

  implicit val specimenLinkTypeAddedEventWrites: Writes[SpecimenLinkTypeAddedEvent] = (
    (__ \ "processingTypeId").write[String] and
      (__ \ "specimenLinkTypeId").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "expectedInputChange").write[BigDecimal] and
      (__ \ "expectedOutputChange").write[BigDecimal] and
      (__ \ "inputCount").write[Int] and
      (__ \ "outputCount").write[Int] and
      (__ \ "inputGroupId").write[SpecimenGroupId] and
      (__ \ "outputGroupId").write[SpecimenGroupId] and
      (__ \ "inputContainerTypeId").write[Option[ContainerTypeId]] and
      (__ \ "outputContainerTypeId").write[Option[ContainerTypeId]] and
      (__ \ "annotationTypeData").write[List[SpecimenLinkTypeAnnotationTypeData]]
  )(unlift(SpecimenLinkTypeAddedEvent.unapply))

  implicit val specimenLinkTypeUpdatedEventWrites: Writes[SpecimenLinkTypeUpdatedEvent] = (
    (__ \ "processingTypeId").write[String] and
      (__ \ "specimenLinkTypeId").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "expectedInputChange").write[BigDecimal] and
      (__ \ "expectedOutputChange").write[BigDecimal] and
      (__ \ "inputCount").write[Int] and
      (__ \ "outputCount").write[Int] and
      (__ \ "inputGroupId").write[SpecimenGroupId] and
      (__ \ "outputGroupId").write[SpecimenGroupId] and
      (__ \ "inputContainerTypeId").write[Option[ContainerTypeId]] and
      (__ \ "outputContainerTypeId").write[Option[ContainerTypeId]] and
      (__ \ "annotationTypeData").write[List[SpecimenLinkTypeAnnotationTypeData]]
  )(unlift(SpecimenLinkTypeUpdatedEvent.unapply))

  implicit val specimenLinkTypeRemovedEventWriter: Writes[SpecimenLinkTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "specimenLinkTypeId").write[String]
  )(unlift(SpecimenLinkTypeRemovedEvent.unapply))

}
