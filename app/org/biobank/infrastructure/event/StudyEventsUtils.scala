package org.biobank.infrastructure.event

import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.infrastructure._
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
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object StudyEventsUtil {

  def convertAnnotationTypeDataToEvent[T <: AnnotationTypeData](annotTypeData: List[T])
      : Seq[CollectionEventTypeAddedEvent.AnnotationTypeData] = {
    annotTypeData.map { atd =>
      CollectionEventTypeAddedEvent.AnnotationTypeData(
        annotationTypeId = atd.annotationTypeId, required = Some(atd.required))
    }
  }

  def convertCollectionEventTypeAnnotationTypeDataFromEvent
    (annotTypeData: Seq[CollectionEventTypeAddedEvent.AnnotationTypeData])
      : List[CollectionEventTypeAnnotationTypeData] = {
    annotTypeData.map { atd =>
      CollectionEventTypeAnnotationTypeData(
        annotationTypeId = atd.annotationTypeId, required = atd.getRequired)
    } toList
  }

  def convertSpecimenLinkTypeAnnotationTypeDataFromEvent
    (annotTypeData: Seq[CollectionEventTypeAddedEvent.AnnotationTypeData])
      : List[SpecimenLinkTypeAnnotationTypeData] = {
    annotTypeData.map { atd =>
      SpecimenLinkTypeAnnotationTypeData(
        annotationTypeId = atd.annotationTypeId, required = atd.getRequired)
    } toList
  }

}

object StudyEventsJson {
  import org.biobank.infrastructure.event.StudyEvents._

  implicit val studyAddedEventWriter: Writes[StudyAddedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String]
  )(unlift(StudyAddedEvent.unapply))

  implicit val studyUpdatedEventWriter: Writes[StudyUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String]
  )(unlift(StudyUpdatedEvent.unapply))

  implicit val studyEnabledEventWrites: Writes[StudyEnabledEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long]
  )(unlift(StudyEnabledEvent.unapply))

  implicit val studyDisabledEventWrites: Writes[StudyDisabledEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long]
  )(unlift(StudyDisabledEvent.unapply))

  implicit val studyRetiredEventWrites: Writes[StudyRetiredEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long]
  )(unlift(StudyRetiredEvent.unapply))

  implicit val studyUnretiredEventWrites: Writes[StudyUnretiredEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long]
  )(unlift(StudyUnretiredEvent.unapply))

  implicit val collectionEventAnnotationTypeAddedEventWriter: Writes[CollectionEventAnnotationTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").writeNullable[String] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Seq[String]]
  )(unlift(CollectionEventAnnotationTypeAddedEvent.unapply))

  implicit val collectionEventAnnotationTypeUpdatedEventWriter: Writes[CollectionEventAnnotationTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").writeNullable[String] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Seq[String]]
  )(unlift(CollectionEventAnnotationTypeUpdatedEvent.unapply))

  implicit val collectionEventAnnotationTypeRemovedEventWriter: Writes[CollectionEventAnnotationTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String]
  )(unlift(CollectionEventAnnotationTypeRemovedEvent.unapply))

  implicit val participantAnnotationTypeAddedEventWriter: Writes[ParticipantAnnotationTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").writeNullable[String] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Seq[String]] and
      (__ \ "required").writeNullable[Boolean]
  )(unlift(ParticipantAnnotationTypeAddedEvent.unapply))

  implicit val participantAnnotationTypeUpdatedEventWriter: Writes[ParticipantAnnotationTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").writeNullable[String] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Seq[String]] and
      (__ \ "required").writeNullable[Boolean]
  )(unlift(ParticipantAnnotationTypeUpdatedEvent.unapply))

  implicit val participantAnnotationTypeRemovedEventWriter: Writes[ParticipantAnnotationTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String]
  )(unlift(ParticipantAnnotationTypeRemovedEvent.unapply))

  implicit val specimenLinkAnnotationTypeAddedEventWrites: Writes[SpecimenLinkAnnotationTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").writeNullable[String] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Seq[String]]
  )(unlift(SpecimenLinkAnnotationTypeAddedEvent.unapply))

  implicit val specimenLinkAnnotationTypeUpdatedEventWrites: Writes[SpecimenLinkAnnotationTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "valueType").writeNullable[String] and
      (__ \ "maxValueCount").writeNullable[Int] and
      (__ \ "options").write[Seq[String]]
  )(unlift(SpecimenLinkAnnotationTypeUpdatedEvent.unapply))

  implicit val specimenLinkAnnotationTypeRemovedEventWrites: Writes[SpecimenLinkAnnotationTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "annotationTypeId").write[String]
  )(unlift(SpecimenLinkAnnotationTypeRemovedEvent.unapply))

  implicit val specimenGroupDataWrites: Writes[CollectionEventTypeAddedEvent.SpecimenGroupData] = (
    (__ \ "specimenGroupId").write[String] and
      (__ \ "maxCount").writeNullable[Int] and
      (__ \ "amount").writeNullable[Double]
  )(unlift(CollectionEventTypeAddedEvent.SpecimenGroupData.unapply))

  implicit val annotationTypeDataWrites: Writes[CollectionEventTypeAddedEvent.AnnotationTypeData] = (
    (__ \ "annotationTypeId").write[String] and
      (__ \ "required").writeNullable[Boolean]
  )(unlift(CollectionEventTypeAddedEvent.AnnotationTypeData.unapply))

  implicit val collectionEventTypeAddedEventWriter: Writes[CollectionEventTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "collectionEventTypeId").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "recurring").writeNullable[Boolean] and
      (__ \ "specimenGroupData").write[Seq[CollectionEventTypeAddedEvent.SpecimenGroupData]] and
      (__ \ "annotationTypeData").write[Seq[CollectionEventTypeAddedEvent.AnnotationTypeData]]
  )(unlift(CollectionEventTypeAddedEvent.unapply))

  implicit val collectionEventTypeUpdatedEventWriter: Writes[CollectionEventTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "collectionEventTypeId").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "recurring").writeNullable[Boolean] and
      (__ \ "specimenGroupData").write[Seq[CollectionEventTypeAddedEvent.SpecimenGroupData]] and
      (__ \ "annotationTypeData").write[Seq[CollectionEventTypeAddedEvent.AnnotationTypeData]]
  )(unlift(CollectionEventTypeUpdatedEvent.unapply))

  implicit val collectionEventTypeRemovedEventWriter: Writes[CollectionEventTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "collectionEventTypeId").write[String]
  )(unlift(CollectionEventTypeRemovedEvent.unapply))

  implicit val specimenGroupAddedEventWrites: Writes[SpecimenGroupAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "specimenGroupId").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "units").writeNullable[String] and
      (__ \ "anatomicalSourceType").writeNullable[String] and
      (__ \ "preservationType").writeNullable[String] and
      (__ \ "preservationTemperatureType").writeNullable[String] and
      (__ \ "specimenType").writeNullable[String]
  )(unlift(SpecimenGroupAddedEvent.unapply))

  implicit val specimenGroupUpdatedEventWrites: Writes[SpecimenGroupUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "specimenGroupId").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "units").writeNullable[String] and
      (__ \ "anatomicalSourceType").writeNullable[String] and
      (__ \ "preservationType").writeNullable[String] and
      (__ \ "preservationTemperatureType").writeNullable[String] and
      (__ \ "specimenType").writeNullable[String]
  )(unlift(SpecimenGroupUpdatedEvent.unapply))

  implicit val specimenGroupRemovedEventWriter: Writes[SpecimenGroupRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "specimenGroupId").write[String]
  )(unlift(SpecimenGroupRemovedEvent.unapply))

  implicit val processingTypeAddedEventWrites: Writes[ProcessingTypeAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "processingTypeId").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "enabled").writeNullable[Boolean]
  )(unlift(ProcessingTypeAddedEvent.unapply))

  implicit val processingTypeUpdatedEventWrites: Writes[ProcessingTypeUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "processingTypeId").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String] and
      (__ \ "enabled").writeNullable[Boolean]
  )(unlift(ProcessingTypeUpdatedEvent.unapply))

  implicit val processingTypeRemovedEventWriter: Writes[ProcessingTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "processingTypeId").write[String]
  )(unlift(ProcessingTypeRemovedEvent.unapply))


  implicit val specimenLinkTypeAddedEventWrites: Writes[SpecimenLinkTypeAddedEvent] = (
    (__ \ "processingTypeId").write[String] and
      (__ \ "specimenLinkTypeId").write[String] and
      (__ \ "expectedInputChange").writeNullable[Double] and
      (__ \ "expectedOutputChange").writeNullable[Double] and
      (__ \ "inputCount").writeNullable[Int] and
      (__ \ "outputCount").writeNullable[Int] and
      (__ \ "inputGroupId").writeNullable[String] and
      (__ \ "outputGroupId").writeNullable[String] and
      (__ \ "inputContainerTypeId").writeNullable[String] and
      (__ \ "outputContainerTypeId").writeNullable[String] and
      (__ \ "annotationTypeData").write[Seq[CollectionEventTypeAddedEvent.AnnotationTypeData]]
  )(unlift(SpecimenLinkTypeAddedEvent.unapply))

  implicit val specimenLinkTypeUpdatedEventWrites: Writes[SpecimenLinkTypeUpdatedEvent] = (
    (__ \ "processingTypeId").write[String] and
      (__ \ "specimenLinkTypeId").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "expectedInputChange").writeNullable[Double] and
      (__ \ "expectedOutputChange").writeNullable[Double] and
      (__ \ "inputCount").writeNullable[Int] and
      (__ \ "outputCount").writeNullable[Int] and
      (__ \ "inputGroupId").writeNullable[String] and
      (__ \ "outputGroupId").writeNullable[String] and
      (__ \ "inputContainerTypeId").writeNullable[String] and
      (__ \ "outputContainerTypeId").writeNullable[String] and
      (__ \ "annotationTypeData").write[Seq[CollectionEventTypeAddedEvent.AnnotationTypeData]]
  )(unlift(SpecimenLinkTypeUpdatedEvent.unapply))

  implicit val specimenLinkTypeRemovedEventWriter: Writes[SpecimenLinkTypeRemovedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "specimenLinkTypeId").write[String]
  )(unlift(SpecimenLinkTypeRemovedEvent.unapply))

  implicit val participantAnnotationEventWriter: Writes[ParticipantAddedEvent.ParticipantAnnotation] = (
    (__ \ "annotationTypeId").write[String] and
      (__ \ "stringValue").writeNullable[String] and
      (__ \ "numberValue").writeNullable[String] and
      (__ \ "selectedValues").write[Seq[String]]
  )(unlift(ParticipantAddedEvent.ParticipantAnnotation.unapply))

  implicit val participantAddedEventWriter: Writes[ParticipantAddedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "participantId").write[String] and
      (__ \ "uniqueId").writeNullable[String] and
      (__ \ "annotations").write[Seq[ParticipantAddedEvent.ParticipantAnnotation]]
  )(unlift(ParticipantAddedEvent.unapply))

  implicit val participantUpdatedEventWriter: Writes[ParticipantUpdatedEvent] = (
    (__ \ "studyId").write[String] and
      (__ \ "participantId").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "uniqueId").writeNullable[String] and
      (__ \ "annotations").write[Seq[ParticipantAddedEvent.ParticipantAnnotation]]
  )(unlift(ParticipantUpdatedEvent.unapply))

}
