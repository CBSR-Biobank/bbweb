package org.biobank

import java.time.OffsetDateTime
import org.biobank.domain.{EntityState, Location}
import org.biobank.domain.access.RoleId._
import org.biobank.domain.centre.Shipment
import org.biobank.dto.access.UserMembershipDto
import org.biobank.service.centres.CentreLocationInfo
import play.api.libs.json._

package dto {

  final case class EntityInfoDto(id: String, name: String)

  object EntityInfoDto {

    implicit val entityInfoDtoFormat: Format[EntityInfoDto] = Json.format[EntityInfoDto]

  }

  final case class EntitySetDto(allEntities: Boolean, entityData: Set[EntityInfoDto])

  object EntitySetDto {

    implicit val entitySetInfoDtoWriter: Writes[EntitySetDto] = Json.writes[EntitySetDto]

  }

  final case class NameDto(id: String, name: String)

  object NameDto {
    def compareByName(a: NameDto, b: NameDto): Boolean = (a.name compareToIgnoreCase b.name) < 0

    implicit val nameDtoWriter: Writes[NameDto] = Json.writes[NameDto]
  }

  final case class NameAndStateDto(id: String, name: String, state: String)

  object NameAndStateDto {
    def compareByName(a: NameAndStateDto, b: NameAndStateDto): Boolean = (a.name compareToIgnoreCase b.name) < 0

    implicit val nameAndStateDtoWriter: Writes[NameAndStateDto] = Json.writes[NameAndStateDto]
  }

  final case class CentreDto(id:           String,
                             version:      Long,
                             timeAdded:    String,
                             timeModified: Option[String],
                             state:        String,
                             name:         String,
                             description:  Option[String],
                             studyNames:   Set[NameAndStateDto],
                             locations:    Set[Location])

  object CentreDto {

    implicit val centreDtoWriter: Writes[CentreDto] = Json.writes[CentreDto]

  }

  final case class AggregateCountsDto(studies: Long, centres: Long, users: Long)

  object AggregateCountsDto {

    implicit val aggregateCountsDtoWriter: Writes[AggregateCountsDto] = Json.writes[AggregateCountsDto]

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
                           membership:   Option[UserMembershipDto])

  object UserDto {

    implicit val userDtoWriter: Writes[UserDto] = Json.writes[UserDto]

  }

  final case class SpecimenDto(id:                       String,
                               state:                    EntityState,
                               inventoryId:              String,
                               collectionEventId:        String,
                               specimenDescriptionId:    String,
                               specimenDescriptionName:  String,
                               specimenDescriptionUnits: String,
                               version:                  Long,
                               timeAdded:                OffsetDateTime,
                               timeModified:             Option[OffsetDateTime],
                               originLocationInfo:       CentreLocationInfo,
                               locationInfo:             CentreLocationInfo,
                               containerId:              Option[String],
                               positionId:               Option[String],
                               timeCreated:              OffsetDateTime,
                               amount:                   BigDecimal,
                               units:                    String,
                               isDefaultAmount:          Boolean)

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
