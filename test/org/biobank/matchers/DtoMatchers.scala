package org.biobank.matchers

import java.time.OffsetDateTime
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.centres._
import org.biobank.domain.participants._
import org.biobank.domain.studies._
import org.biobank.domain.users._
import org.biobank.dto._
import org.biobank.dto.access._
import org.biobank.services.centres.CentreLocationInfo
import play.api.libs.json._
import org.scalatest.Matchers._
import org.scalatest.matchers.{MatchResult, Matcher}

trait DtoMatchers {
  import JsonMatchers._
  import DateMatchers._

  def matchDtoToCentre(centre: Centre) =
    new Matcher[CentreDto] {
      def apply(left: CentreDto) = {
        val dtoStudyIds = left.studyNames.map { s => StudyId(s.id)  }

        val timeAddedMatcher =
          beTimeWithinSeconds(centre.timeAdded, 5L)(OffsetDateTime.parse(left.timeAdded))

        val timeModifiedMatcher = beOptionalTimeWithinSeconds(centre.timeModified, 5L)
          .apply(left.timeModified.map(OffsetDateTime.parse))

        val matchers = Map(
            ("id"           -> (left.id equals centre.id.id)),
            ("version"      -> (left.version equals centre.version)),
            ("timeAdded"    -> (timeAddedMatcher.matches)),
            ("timeModified" -> (timeModifiedMatcher.matches)),
            ("slug"         -> (left.slug equals centre.slug)),
            ("state"        -> (left.state equals centre.state.id)),
            ("name"         -> (left.name equals centre.name)),
            ("description"  -> (left.description equals centre.description)),
            ("studyIds"     -> (dtoStudyIds equals centre.studyIds)),
            ("locations"    -> (left.locations equals centre.locations)))

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "dto does not match entity for the following attributes: {0},\ndto: {1},\nentity: {2}",
                    "dto matches entity: dto: {1},\nentity: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, centre))
      }
    }

  private def optionalTimeWithinSeconds(expected: Option[String],
                                        actual: Option[OffsetDateTime],
                                        seconds: Long) =
    beOptionalTimeWithinSeconds(actual, seconds).apply(expected.map(OffsetDateTime.parse))

  def matchDtoToShipment(shipment: Shipment) =
    new Matcher[ShipmentDto] {
      def apply(left: ShipmentDto) = {
        val timeAddedMatcher =
          beTimeWithinSeconds(shipment.timeAdded, 5L)(OffsetDateTime.parse(left.timeAdded))

        val fromLocationInfoMatcher =
          matchCentreLocationInfo(shipment.fromCentreId, shipment.fromLocationId)
            .apply(left.fromLocationInfo)

        val toLocationInfoMatcher =
          matchCentreLocationInfo(shipment.toCentreId, shipment.toLocationId)
            .apply(left.toLocationInfo)

        val matchers = Map(
            ("id"               -> (left.id equals shipment.id.id)),
            ("version"          -> (left.version equals shipment.version)),
            ("timeAdded"        -> (timeAddedMatcher.matches)),
            ("timeModified"     -> optionalTimeWithinSeconds(left.timeModified,
                                                            shipment.timeModified,
                                                            5L).matches),
            ("state"            -> (left.state equals shipment.state.id)),
            ("courierName"      -> (left.courierName equals shipment.courierName)),
            ("trackingNumber"   -> (left.trackingNumber equals shipment.trackingNumber)),
            ("fromLocationInfo" -> (fromLocationInfoMatcher.matches)),
            ("toLocationInfo"   -> (toLocationInfoMatcher.matches)),
            ("timePacked"       -> optionalTimeWithinSeconds(left.timePacked,
                                                            shipment.timePacked,
                                                            5L).matches),
            ("timeSent"         -> optionalTimeWithinSeconds(left.timeSent,
                                                            shipment.timeSent,
                                                            5L).matches),
            ("timeReceived"     -> optionalTimeWithinSeconds(left.timeReceived,
                                                            shipment.timeReceived,
                                                            5L).matches),
            ("timeUnpacked"     -> optionalTimeWithinSeconds(left.timeUnpacked,
                                                            shipment.timeUnpacked,
                                                            5L).matches),
            ("timeCompleted"    -> optionalTimeWithinSeconds(left.timeCompleted,
                                                            shipment.timeCompleted,
                                                            5L).matches))

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "dto does not match entity for the following attributes: {0},\ndto: {1},\nentity: {2}",
                    "dto matches entity: dto: {1},\nentity: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, shipment))
      }
    }

  def matchDtoToSpecimen(specimen: Specimen) =
    new Matcher[SpecimenDto] {
      def apply(left: SpecimenDto) = {
        val timeAddedMatcher =
          beTimeWithinSeconds(specimen.timeAdded, 5L)(OffsetDateTime.parse(left.timeAdded))

        val timeCreatedMatcher =
          beTimeWithinSeconds(specimen.timeCreated, 5L)(OffsetDateTime.parse(left.timeCreated))

        val matchers = Map(
            ("id"                      -> (left.id      equals specimen.id.id)),
            ("version"                 -> (left.version equals specimen.version)),
            ("timeAdded"               -> timeAddedMatcher.matches),
            ("timeModified"            -> optionalTimeWithinSeconds(left.timeModified,
                                                                   specimen.timeModified,
                                                                   5L).matches),
            ("state"                   -> (left.state equals specimen.state.id)),
            ("inventoryId"             -> (left.inventoryId                   equals specimen.inventoryId)),
            ("specimenDefinitionId"    -> (left.specimenDefinitionId          equals specimen.specimenDefinitionId.id)),
            ("originLocationInfo"      -> (left.originLocationInfo.locationId equals specimen.originLocationId.id)),
            ("locationInfo"            -> (left.locationInfo.locationId       equals specimen.locationId.id)),
            ("containerId"             -> (left.containerId                   equals specimen.containerId)),
            ("positionId"              -> (left.positionId                    equals specimen.positionId)),
            ("timeCreated"             -> timeCreatedMatcher.matches))

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(
          nonMatching.size <= 0,
          "dto does not match specimen for the following attributes: {0},\ndto: {1},\nentity: {2}",
          "dto matches specimen: dto: {1},\nentity: {2}",
          IndexedSeq(nonMatching.mkString(", "), left, specimen))
      }
    }

  def matchDtoToShipmentSpecimen(shipmentSpecimen: ShipmentSpecimen) =
    new Matcher[ShipmentSpecimenDto] {
      def apply(left: ShipmentSpecimenDto) = {
        val timeAddedMatcher =
          beTimeWithinSeconds(shipmentSpecimen.timeAdded, 5L)(OffsetDateTime.parse(left.timeAdded))

        val matchers = Map[String, Boolean](
            ("id"                  -> (left.id equals shipmentSpecimen.id.id)),
            ("version"             -> (left.version equals shipmentSpecimen.version)),
            ("timeAdded"           -> (timeAddedMatcher.matches)),
            ("timeModified"        -> optionalTimeWithinSeconds(left.timeModified,
                                                            shipmentSpecimen.timeModified,
                                                            5L).matches),
            ("state"               -> (left.state equals shipmentSpecimen.state.toString)),
            ("shipmentId"          -> (left.shipmentId equals shipmentSpecimen.shipmentId.id)),
            ("shipmentContainerId" -> (left.shipmentContainerId equals
                                        shipmentSpecimen.shipmentContainerId.map(_.toString))),
            ("specimenId"          -> (left.specimen.id equals shipmentSpecimen.specimenId.id)))

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "dto does not match entity for the following attributes: {0},\ndto: {1},\nentity: {2}",
                    "dto matches entity: dto: {1},\nentity: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, shipmentSpecimen))
      }
    }

  def matchDtoToUser(user: User) =
    new Matcher[UserDto] {
      def apply(left: UserDto) = {
        val timeAddedMatcher =
          beTimeWithinSeconds(user.timeAdded, 5L)(OffsetDateTime.parse(left.timeAdded))

        val timeModifiedMatcher = beOptionalTimeWithinSeconds(user.timeModified, 5L)
          .apply(left.timeModified.map(OffsetDateTime.parse))

        val matchers = Map(
            ("id"           -> (left.id equals user.id.id)),
            ("version"      -> (left.version equals user.version)),
            ("timeAdded"    -> timeAddedMatcher.matches),
            ("timeModified" -> (timeModifiedMatcher.matches)),
            ("state"        -> (left.state equals user.state.id)),
            ("slug"         -> (left.slug equals user.slug)),
            ("name"         -> (left.name equals user.name)),
            ("email"        -> (left.email equals user.email)),
            ("avatarUrl"    -> (left.avatarUrl equals user.avatarUrl)))

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "dto does not match user for the following attributes: {0},\ndto: {1},\nuser: {2}",
                    "dto matches user: dto: {1},\nuser: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, user))
      }
    }

  def matchDtoToRole(role: Role) =
    new Matcher[UserRoleDto] {
      def apply(left: UserRoleDto) = {
        val matchers = Map(
            ("id"      -> (left.id equals role.id.id)),
            ("version" -> (left.version equals role.version)),
            ("slug"    -> (left.slug equals role.slug)),
            ("name"    -> (left.name equals role.name)))

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "dto does not match role for the following attributes: {0},\ndto: {1},\nrole: {2}",
                    "dto matches role: dto: {1},\nrole: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, role))
      }
    }

  def matchDtoToUserMembership(membership: UserMembership) =
    new Matcher[UserMembershipDto] {
      def apply(left: UserMembershipDto) = {
        val timeAddedMatcher =
          beTimeWithinSeconds(membership.timeAdded, 5L)(OffsetDateTime.parse(left.timeAdded))

        val timeModifiedMatcher = beOptionalTimeWithinSeconds(membership.timeModified, 5L)
          .apply(left.timeModified.map(OffsetDateTime.parse))

        val studyEntitySetMatcher = matchDtoToEntitySetDto(membership.studyData)(left.studyData)
        val centreEntitySetMatcher = matchDtoToEntitySetDto(membership.centreData)(left.centreData)

        val matchers = Map(
            ("id"           -> (left.id equals membership.id.id)),
            ("version"      -> (left.version equals membership.version)),
            ("timeAdded"    -> timeAddedMatcher.matches),
            ("timeModified" -> (timeModifiedMatcher.matches)),
            ("slug"         -> (left.slug equals membership.slug)),
            ("name"         -> (left.name equals membership.name)),
            ("description"  -> (left.description equals membership.description)),
            ("studyData"    -> (studyEntitySetMatcher.matches)),
            ("centreData"   -> (centreEntitySetMatcher.matches)))

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "dto does not match membership for the following attributes: {0},\ndto: {1},\nrole: {2}",
                    "dto matches membership: dto: {1},\nrole: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, membership))
      }
    }

  def matchDtoToEntitySetDto(entitySet: MembershipEntitySet[_]) =
    new Matcher[EntitySetDto] {
      def apply(left: EntitySetDto) = {
        val dtoEntityIds = left.entityData.map { ed => ed.id  }
        val entityIds = entitySet.ids.map { id => id.toString }

        val matchers = Map(
            ("allEntities"  -> (left.allEntities equals entitySet.allEntities)),
            ("ids"          -> (dtoEntityIds equals entityIds)))

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "dto does not match entitySet for the following attributes: {0},\ndto: {1},\nrole: {2}",
                    "dto matches entitySet: dto: {1},\nrole: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, entitySet))
      }
    }

  def matchNameAndStateDtos(dtos: Seq[NameAndStateDto]) =
    new Matcher[JsValue] {
      def apply(left: JsValue) = {
        val replyDtos = (left).validate[Seq[NameAndStateDto]]
        val validJs = jsSuccess(replyDtos)

        if (!validJs.matches) {
          validJs
        } else {
          val m: Matcher[Seq[NameAndStateDto]] = equal(dtos)
          m(replyDtos.get)
        }
      }
    }

  def matchCentreLocationInfo(centreId: CentreId, locationId: LocationId) =
    new Matcher[CentreLocationInfo] {
      def apply(left: CentreLocationInfo) = {
        val centreIdsMatch = left.centreId equals centreId.id
        if (!centreIdsMatch) {
          MatchResult(false, s"centreIds do not match: expected: $centreId, actual ${left.centreId}", "")
        } else {
          MatchResult(left.locationId equals locationId.id,
                      s"locationIds do not match: expected: $locationId, actual ${left.locationId}",
                      s"locationIds match: expected: $locationId, actual ${left.locationId}")
        }
      }
    }

}

object DtoMatchers extends DtoMatchers
