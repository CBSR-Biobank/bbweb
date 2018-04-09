package org.biobank.dto

import org.biobank.domain.Slug
import play.api.libs.json._

package access {

  final case class AccessItemNameDto(id:             String,
                                     slug:           Slug,
                                     name:           String,
                                     accessItemType: String) extends Dto


  object AccessItemNameDto {
    def compareByName(a: AccessItemNameDto, b: AccessItemNameDto): Boolean =
      (a.name compareToIgnoreCase b.name) < 0

    implicit val accessItemNameDtoWriter: Writes[AccessItemNameDto] = Json.writes[AccessItemNameDto]
  }

  final case class RoleDto(id:             String,
                           version:        Long,
                           timeAdded:      String,
                           timeModified:   Option[String],
                           accessItemType: String,
                           slug: Slug,
                           name:           String,
                           description:    Option[String],
                           userData:       Set[EntityInfoDto],
                           parentData:     Set[EntityInfoDto],
                           childData:      Set[EntityInfoDto]) extends Dto

  object RoleDto {

    implicit val roleDtoWriter: Writes[RoleDto] = Json.writes[RoleDto]

  }

  final case class UserRoleDto(id:             String,
                               version:        Long,
                               slug: Slug,
                               name:           String,
                               childData:      Set[EntityInfoDto]) extends Dto

  object UserRoleDto {

    implicit val userRoleDtoWriter: Writes[UserRoleDto] = Json.writes[UserRoleDto]

  }

  final case class MembershipDto(id:           String,
                                 version:      Long,
                                 timeAdded:    String,
                                 timeModified: Option[String],
                                 slug: Slug,
                                 name:         String,
                                 description:  Option[String],
                                 userData:     Set[EntityInfoDto],
                                 studyData:    EntitySetDto,
                                 centreData:   EntitySetDto) extends Dto

  object MembershipDto {

    implicit val membershipDtoWriter: Writes[MembershipDto] = Json.writes[MembershipDto]

  }

  final case class UserMembershipDto(id:           String,
                                     version:      Long,
                                     timeAdded:    String,
                                     timeModified: Option[String],
                                     slug: Slug,
                                     name:         String,
                                     description:  Option[String],
                                     studyData:    EntitySetDto,
                                     centreData:   EntitySetDto) extends Dto

  object UserMembershipDto {

    implicit val userMembershipDtoWriter: Writes[UserMembershipDto] = Json.writes[UserMembershipDto]

  }

}
