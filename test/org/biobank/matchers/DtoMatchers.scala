package org.biobank.matchers

import java.time.OffsetDateTime
import org.biobank.domain.access._
import org.biobank.domain.centres._
import org.biobank.domain.studies._
import org.biobank.domain.users._
import org.biobank.dto._
import org.biobank.dto.access._
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

}

object DtoMatchers extends DtoMatchers
