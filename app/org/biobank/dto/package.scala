package org.biobank

import play.api.libs.json._

package dto {

  case class AggregateCountsDto(studies: Int, centres: Int, users: Int)

  object AggregateCountsDto {

    implicit val aggregateCountsDtoWriter = Json.writes[AggregateCountsDto]

  }

  case class StudyNameDto(id: String, name: String, status: String)

  object StudyNameDto {
    def compareByName(a: StudyNameDto, b: StudyNameDto) = (a.name compareToIgnoreCase b.name) < 0

    implicit val studyNameDtoWriter = Json.writes[StudyNameDto]
  }

  case class StudyCountsByStatus(total: Long, disabledCount: Long, enabledCount: Long, retiredCount: Long)

  object StudyCountsByStatus {

    implicit val studyCountsByStatusWriter = Json.writes[StudyCountsByStatus]
  }

  case class CentreCountsByStatus(total: Long, disabledCount: Long, enabledCount: Long)

  object CentreCountsByStatus {

    implicit val studyCountsByStatusWriter = Json.writes[CentreCountsByStatus]
  }

  case class CollectionDto(
    collectionEventTypes: List[org.biobank.domain.study.CollectionEventType],
    collectionEventAnnotationTypes: List[org.biobank.domain.study.CollectionEventAnnotationType],
    collectionEventAnnotationTypesInUse: List[String],
    specimenGroups: List[org.biobank.domain.study.SpecimenGroup])

  object CollectionDto {
    implicit val collectionDtoWriter = Json.writes[CollectionDto]
  }

  case class ProcessingDto(
    processingTypes: List[org.biobank.domain.study.ProcessingType],
    specimenLinkTypes: List[org.biobank.domain.study.SpecimenLinkType],
    specimenLinkAnnotationTypes: List[org.biobank.domain.study.SpecimenLinkAnnotationType],
    specimenGroups: List[org.biobank.domain.study.SpecimenGroup])

  object ProcessingDto {

    implicit val processingDtoWriter = Json.writes[ProcessingDto]

  }


}
