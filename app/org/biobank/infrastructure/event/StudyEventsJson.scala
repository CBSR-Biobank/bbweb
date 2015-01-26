package org.biobank.infrastructure.event

import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.infrastructure.JsonUtils._
import org.biobank.infrastructure.EnumUtils._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

trait StudyEventsJson {
  import org.biobank.infrastructure.event.StudyEvents._
  import CollectionEventTypeAddedEvent._
  import ParticipantAddedEvent._

  implicit val studyAddedEventWriter = Json.writes[StudyAddedEvent]
  implicit val studyUpdatedEventWriter = Json.writes[StudyUpdatedEvent]
  implicit val studyEnabledEventWrites = Json.writes[StudyEnabledEvent]
  implicit val studyDisabledEventWrites = Json.writes[StudyDisabledEvent]
  implicit val studyRetiredEventWrites = Json.writes[StudyRetiredEvent]
  implicit val studyUnretiredEventWrites = Json.writes[StudyUnretiredEvent]
  implicit val collectionEventAnnotationTypeAddedEventWriter =
    Json.writes[CollectionEventAnnotationTypeAddedEvent]
  implicit val collectionEventAnnotationTypeUpdatedEventWriter =
    Json.writes[CollectionEventAnnotationTypeUpdatedEvent]
  implicit val collectionEventAnnotationTypeRemovedEventWriter =
    Json.writes[CollectionEventAnnotationTypeRemovedEvent]
  implicit val participantAnnotationTypeAddedEventWriter =
    Json.writes[ParticipantAnnotationTypeAddedEvent]
  implicit val participantAnnotationTypeUpdatedEventWriter =
    Json.writes[ParticipantAnnotationTypeUpdatedEvent]
  implicit val participantAnnotationTypeRemovedEventWriter =
    Json.writes[ParticipantAnnotationTypeRemovedEvent]
  implicit val specimenLinkAnnotationTypeAddedEventWrites =
    Json.writes[SpecimenLinkAnnotationTypeAddedEvent]
  implicit val specimenLinkAnnotationTypeUpdatedEventWrites =
    Json.writes[SpecimenLinkAnnotationTypeUpdatedEvent]
  implicit val specimenLinkAnnotationTypeRemovedEventWrites =
    Json.writes[SpecimenLinkAnnotationTypeRemovedEvent]

  implicit val specimenGroupDataWrites = Json.writes[SpecimenGroupData]
  implicit val annotationTypeDataWrites = Json.writes[AnnotationTypeData]
  implicit val collectionEventTypeAddedEventWriter = Json.writes[CollectionEventTypeAddedEvent]
  implicit val collectionEventTypeUpdatedEventWriter = Json.writes[CollectionEventTypeUpdatedEvent]
  implicit val collectionEventTypeRemovedEventWriter = Json.writes[CollectionEventTypeRemovedEvent]
  implicit val specimenGroupAddedEventWrites = Json.writes[SpecimenGroupAddedEvent]
  implicit val specimenGroupUpdatedEventWrites = Json.writes[SpecimenGroupUpdatedEvent]
  implicit val specimenGroupRemovedEventWriter = Json.writes[SpecimenGroupRemovedEvent]
  implicit val processingTypeAddedEventWrites = Json.writes[ProcessingTypeAddedEvent]
  implicit val processingTypeUpdatedEventWrites = Json.writes[ProcessingTypeUpdatedEvent]
  implicit val processingTypeRemovedEventWriter = Json.writes[ProcessingTypeRemovedEvent]
  implicit val specimenLinkTypeAddedEventWrites = Json.writes[SpecimenLinkTypeAddedEvent]
  implicit val specimenLinkTypeUpdatedEventWrites = Json.writes[SpecimenLinkTypeUpdatedEvent]
  implicit val specimenLinkTypeRemovedEventWriter = Json.writes[SpecimenLinkTypeRemovedEvent]
  implicit val participantAnnotationEventWriter = Json.writes[ParticipantAnnotation]
  implicit val participantAddedEventWriter = Json.writes[ParticipantAddedEvent]
  implicit val participantUpdatedEventWriter = Json.writes[ParticipantUpdatedEvent]

}
