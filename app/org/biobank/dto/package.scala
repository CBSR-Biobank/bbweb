package org.biobank

import org.biobank.infrastructure.JsonUtils._
import org.joda.time.DateTime
import play.api.libs.json._

package dto {

  final case class AggregateCountsDto(studies: Int, centres: Int, users: Int)

  object AggregateCountsDto {

    implicit val aggregateCountsDtoWriter: Writes[AggregateCountsDto] = Json.writes[AggregateCountsDto]

  }

  final case class NameDto(id: String, name: String, status: String)

  object NameDto {
    def compareByName(a: NameDto, b: NameDto) = (a.name compareToIgnoreCase b.name) < 0

    implicit val studyNameDtoWriter: Writes[NameDto] = Json.writes[NameDto]
  }

  final case class CentreLocation(centreId:     String,
                                  locationId:   String,
                                  centreName:   String,
                                  locationName: String)

  object CentreLocation {

    implicit val centreLocationWriter: Writes[CentreLocation] = Json.writes[CentreLocation]

  }

  final case class CentreLocationInfo(centreId:   String,
                                      locationId: String,
                                      name:       String)

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  object CentreLocationInfo {

    def apply(centreId: String,
              locationId: String,
              centreName: String,
              locationName: String): CentreLocationInfo =
        CentreLocationInfo(centreId, locationId, s"$centreName: $locationName")

    implicit val centreLocationInfoWriter: Writes[CentreLocationInfo] = Json.writes[CentreLocationInfo]

  }

  final case class StudyCountsByStatus(total:         Long,
                                       disabledCount: Long,
                                       enabledCount:  Long,
                                       retiredCount:  Long)

  object StudyCountsByStatus {

    implicit val studyCountsByStatusWriter: Writes[StudyCountsByStatus] = Json.writes[StudyCountsByStatus]
  }

  final case class CentreCountsByStatus(total: Long, disabledCount: Long, enabledCount: Long)

  object CentreCountsByStatus {

    implicit val centreCountsByStatusWriter: Writes[CentreCountsByStatus] = Json.writes[CentreCountsByStatus]
  }

  final case class UserCountsByStatus(total: Long, registeredCount: Long, activeCount: Long, lockedCount: Long)

  object UserCountsByStatus {

    implicit val userCountsByStatusWriter: Writes[UserCountsByStatus] = Json.writes[UserCountsByStatus]
  }

  final case class SpecimenDto(id:                 String,
                               inventoryId:        String,
                               collectionEventId:  String,
                               specimenSpecId:     String,
                               specimenSpecName:   String,
                               version:            Long,
                               timeAdded:          DateTime,
                               timeModified:       Option[DateTime],
                               originLocationInfo: CentreLocationInfo,
                               locationInfo:       CentreLocationInfo,
                               containerId:        Option[String],
                               positionId:         Option[String],
                               timeCreated:        DateTime,
                               amount:             BigDecimal,
                               units:              String,
                               status:             String)

  object SpecimenDto {

    implicit val specimenDtoWriter: Writes[SpecimenDto] = Json.writes[SpecimenDto]

  }

  final case class ShipmentDto(id:               String,
                               version:          Long,
                               timeAdded:        DateTime,
                               timeModified:     Option[DateTime],
                               state:            String,
                               courierName:      String,
                               trackingNumber:   String,
                               fromLocationInfo: CentreLocationInfo,
                               toLocationInfo:   CentreLocationInfo,
                               timePacked:       Option[DateTime],
                               timeSent:         Option[DateTime],
                               timeReceived:     Option[DateTime],
                               timeUnpacked:     Option[DateTime])

  object ShipmentDto {

  val sort2Compare = Map[String, (ShipmentDto, ShipmentDto) => Boolean](
      "courierName"      -> compareByCourier,
      "trackingNumber"   -> compareByTrackingNumber,
      "state"            -> compareByState,
      "fromLocationName" -> compareByFromLocation,
      "toLocationName"   -> compareByToLocation)

    def compareByCourier(a: ShipmentDto, b: ShipmentDto) =
      (a.courierName compareToIgnoreCase b.courierName) < 0

    def compareByTrackingNumber(a: ShipmentDto, b: ShipmentDto) =
      (a.trackingNumber compareToIgnoreCase b.trackingNumber) < 0

    def compareByState(a: ShipmentDto, b: ShipmentDto) =
      (a.state compareToIgnoreCase b.state) < 0

    def compareByFromLocation(a: ShipmentDto, b: ShipmentDto) =
      (a.fromLocationInfo.name compareToIgnoreCase b.fromLocationInfo.name) < 0

    def compareByToLocation(a: ShipmentDto, b: ShipmentDto) =
      (a.toLocationInfo.name compareToIgnoreCase b.toLocationInfo.name) < 0

    implicit val shipmentDtooDtoWriter: Writes[ShipmentDto] = Json.writes[ShipmentDto]

  }

  final case class ShipmentSpecimenDto(id:                  String,
                                       version:             Long,
                                       timeAdded:           DateTime,
                                       timeModified:        Option[DateTime],
                                       state:               String,
                                       shipmentId:          String,
                                       shipmentContainerId: Option[String],
                                       specimen:            SpecimenDto)

  object ShipmentSpecimenDto {

  val sort2Compare = Map[String, (ShipmentSpecimenDto, ShipmentSpecimenDto) => Boolean](
      "inventoryId" -> compareByInventoryId,
      "state"       -> compareByState)

    def compareByInventoryId(a: ShipmentSpecimenDto, b: ShipmentSpecimenDto) =
      (a.specimen.inventoryId compareTo b.specimen.inventoryId) < 0

    def compareByState(a: ShipmentSpecimenDto, b: ShipmentSpecimenDto) = (a.state compareTo b.state) < 0

    implicit val shipmentSpecimenDtoWriter: Writes[ShipmentSpecimenDto] = Json.writes[ShipmentSpecimenDto]
  }

  final case class ProcessingDto(
    processingTypes:   List[org.biobank.domain.study.ProcessingType],
    specimenLinkTypes: List[org.biobank.domain.study.SpecimenLinkType],
    specimenGroups:    List[org.biobank.domain.study.SpecimenGroup])

  object ProcessingDto {

    implicit val processingDtoWriter: Writes[ProcessingDto] = Json.writes[ProcessingDto]

  }


}
