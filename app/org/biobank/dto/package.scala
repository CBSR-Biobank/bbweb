package org.biobank

import java.time.OffsetDateTime
import org.biobank.domain.EntityState
import org.biobank.domain.access.RoleId._
import org.biobank.domain.centre.Shipment
import play.api.libs.json._

package dto {

  final case class AggregateCountsDto(studies: Long, centres: Long, users: Long)

  object AggregateCountsDto {

    implicit val aggregateCountsDtoWriter: Writes[AggregateCountsDto] = Json.writes[AggregateCountsDto]

  }

  final case class MembershipInfoDto(all: Boolean, names: Set[String])

  object MembershipInfoDto {

    implicit val membershipInfoDtoWriter: Writes[MembershipInfoDto] = Json.writes[MembershipInfoDto]

  }

  final case class MembershipDto(id:           String,
                                 version:      Long,
                                 studyInfo:    MembershipInfoDto,
                                 centreInfo:   MembershipInfoDto)

  object MembershipDto {

    implicit val membershipDtoWriter: Writes[MembershipDto] = Json.writes[MembershipDto]

  }

  final case class UserDto(id:           String,
                           version:      Long,
                           timeAdded:    OffsetDateTime,
                           timeModified: Option[OffsetDateTime],
                           state:        EntityState,
                           name:         String,
                           email:        String,
                           avatarUrl:    Option[String],
                           roles:        Set[RoleId],
                           membership:   Option[MembershipDto])

  object UserDto {

    implicit val userDtoWriter: Writes[UserDto] = Json.writes[UserDto]

  }

  final case class NameDto(id: String, name: String, state: String)

  object NameDto {
    def compareByName(a: NameDto, b: NameDto): Boolean = (a.name compareToIgnoreCase b.name) < 0

    implicit val nameDtoWriter: Writes[NameDto] = Json.writes[NameDto]
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

  final case class SpecimenDto(id:                      String,
                               state:                   EntityState,
                               inventoryId:             String,
                               collectionEventId:       String,
                               specimenDescriptionId:   String,
                               specimenDescriptionName: String,
                               version:                 Long,
                               timeAdded:               OffsetDateTime,
                               timeModified:            Option[OffsetDateTime],
                               originLocationInfo:      CentreLocationInfo,
                               locationInfo:            CentreLocationInfo,
                               containerId:             Option[String],
                               positionId:              Option[String],
                               timeCreated:             OffsetDateTime,
                               amount:                  BigDecimal,
                               units:                   String)

  object SpecimenDto {

    implicit val specimenDtoWriter: Writes[SpecimenDto] = Json.writes[SpecimenDto]

  }

  final case class ShipmentDto(id:               String,
                               version:          Long,
                               timeAdded:        OffsetDateTime,
                               timeModified:     Option[OffsetDateTime],
                               state:            String,
                               courierName:      String,
                               trackingNumber:   String,
                               fromLocationInfo: CentreLocationInfo,
                               toLocationInfo:   CentreLocationInfo,
                               timePacked:       Option[OffsetDateTime],
                               timeSent:         Option[OffsetDateTime],
                               timeReceived:     Option[OffsetDateTime],
                               timeUnpacked:     Option[OffsetDateTime],
                               timeCompleted:    Option[OffsetDateTime],
                               specimenCount:    Int,
                               containerCount:   Int)

  object ShipmentDto {

    val sort2Compare: Map[String, (ShipmentDto, ShipmentDto) => Boolean] =
      Map[String, (ShipmentDto, ShipmentDto) => Boolean](
        "courierName"      -> compareByCourier,
        "trackingNumber"   -> compareByTrackingNumber,
        "state"            -> compareByState,
        "fromLocationName" -> compareByFromLocation,
        "toLocationName"   -> compareByToLocation)


    def create(shipment:         Shipment,
               fromLocationInfo: CentreLocationInfo,
               toLocationInfo:   CentreLocationInfo,
               specimenCount:    Int,
               containerCount:   Int): ShipmentDto =
      ShipmentDto(id               = shipment.id.id,
                  version          = shipment.version,
                  timeAdded        = shipment.timeAdded,
                  timeModified     = shipment.timeModified,
                  courierName      = shipment.courierName,
                  trackingNumber   = shipment.trackingNumber,
                  fromLocationInfo = fromLocationInfo,
                  toLocationInfo   = toLocationInfo,
                  timePacked       = shipment.timePacked,
                  timeSent         = shipment.timeSent,
                  timeReceived     = shipment.timeReceived,
                  timeUnpacked     = shipment.timeUnpacked,
                  timeCompleted    = shipment.timeCompleted,
                  specimenCount    = specimenCount,
                  containerCount   = containerCount,
                  state            = shipment.state.id)

    def compareByCourier(a: ShipmentDto, b: ShipmentDto): Boolean =
      (a.courierName compareToIgnoreCase b.courierName) < 0

    def compareByTrackingNumber(a: ShipmentDto, b: ShipmentDto): Boolean =
      (a.trackingNumber compareToIgnoreCase b.trackingNumber) < 0

    def compareByState(a: ShipmentDto, b: ShipmentDto): Boolean =
      (a.state.toString compareToIgnoreCase b.state.toString) < 0

    def compareByFromLocation(a: ShipmentDto, b: ShipmentDto): Boolean =
      (a.fromLocationInfo.name compareToIgnoreCase b.fromLocationInfo.name) < 0

    def compareByToLocation(a: ShipmentDto, b: ShipmentDto): Boolean =
      (a.toLocationInfo.name compareToIgnoreCase b.toLocationInfo.name) < 0

    implicit val shipmentDtoWriter: Writes[ShipmentDto] = Json.writes[ShipmentDto]

  }

  final case class ShipmentSpecimenDto(id:                  String,
                                       version:             Long,
                                       timeAdded:           OffsetDateTime,
                                       timeModified:        Option[OffsetDateTime],
                                       state:               String,
                                       shipmentId:          String,
                                       shipmentContainerId: Option[String],
                                       specimen:            SpecimenDto)

  object ShipmentSpecimenDto {

    val sort2Compare: Map[String, (ShipmentSpecimenDto, ShipmentSpecimenDto) => Boolean] =
      Map[String, (ShipmentSpecimenDto, ShipmentSpecimenDto) => Boolean](
        "inventoryId" -> compareByInventoryId,
        "state"       -> compareByState,
        "specName"    -> compareBySpecName,
        "timeCreated" -> compareByTimeCreated)

    def compareByInventoryId(a: ShipmentSpecimenDto, b: ShipmentSpecimenDto): Boolean =
      (a.specimen.inventoryId compareTo b.specimen.inventoryId) < 0

    def compareByState(a: ShipmentSpecimenDto, b: ShipmentSpecimenDto): Boolean =
      (a.state compareTo b.state) < 0

    def compareBySpecName(a: ShipmentSpecimenDto, b: ShipmentSpecimenDto): Boolean =
      (a.specimen.specimenDescriptionName compareTo b.specimen.specimenDescriptionName) < 0

    def compareByTimeCreated(a: ShipmentSpecimenDto, b: ShipmentSpecimenDto): Boolean =
      (a.specimen.timeCreated compareTo b.specimen.timeCreated) < 0

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
