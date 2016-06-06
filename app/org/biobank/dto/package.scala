package org.biobank

import org.biobank.infrastructure.JsonUtils._
import org.joda.time.DateTime
import play.api.libs.json._

package dto {

  case class AggregateCountsDto(studies: Int, centres: Int, users: Int)

  object AggregateCountsDto {

    implicit val aggregateCountsDtoWriter = Json.writes[AggregateCountsDto]

  }

  case class NameDto(id: String, name: String, status: String)

  object NameDto {
    def compareByName(a: NameDto, b: NameDto) = (a.name compareToIgnoreCase b.name) < 0

    implicit val studyNameDtoWriter = Json.writes[NameDto]
  }

  case class CentreLocation(centreId: String, locationId: String, centreName: String, locationName: String)

  object CentreLocation {

    implicit val centreLocationWriter = Json.writes[CentreLocation]

  }

  case class StudyCountsByStatus(total: Long, disabledCount: Long, enabledCount: Long, retiredCount: Long)

  object StudyCountsByStatus {

    implicit val studyCountsByStatusWriter = Json.writes[StudyCountsByStatus]
  }

  case class CentreCountsByStatus(total: Long, disabledCount: Long, enabledCount: Long)

  object CentreCountsByStatus {

    implicit val centreCountsByStatusWriter = Json.writes[CentreCountsByStatus]
  }

  case class UserCountsByStatus(total: Long, registeredCount: Long, activeCount: Long, lockedCount: Long)

  object UserCountsByStatus {

    implicit val userCountsByStatusWriter = Json.writes[UserCountsByStatus]
  }

  case class SpecimenDto(id:                 String,
                         inventoryId:        String,
                         collectionEventId:  String,
                         specimenSpecId:     String,
                         specimenSpecName:   String,
                         version:            Long,
                         timeAdded:          DateTime,
                         timeModified:       Option[DateTime],
                         originLocationId:   String,
                         originLocationName: String,
                         locationId:         String,
                         locationName:       String,
                         containerId:        Option[String],
                         positionId:         Option[String],
                         timeCreated:        DateTime,
                         amount:             BigDecimal,
                         units:              String,
                         status:             String)

  object SpecimenDto {

    implicit val specimenDtoWriter = Json.writes[SpecimenDto]

  }

  case class ProcessingDto(
    processingTypes:   List[org.biobank.domain.study.ProcessingType],
    specimenLinkTypes: List[org.biobank.domain.study.SpecimenLinkType],
    specimenGroups:    List[org.biobank.domain.study.SpecimenGroup])

  object ProcessingDto {

    implicit val processingDtoWriter = Json.writes[ProcessingDto]

  }


}
