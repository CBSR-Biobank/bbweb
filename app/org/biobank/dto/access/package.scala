package org.biobank.dto

import play.api.libs.json._

package access {

  final case class RoleDto(id:           String,
                           version:      Long,
                           timeAdded:    String,
                           timeModified: Option[String],
                           name:         String,
                           description:  Option[String],
                           userData:     Set[EntityInfoDto],
                           parentData:   EntitySetDto,
                           childData:    EntitySetDto)

  object RoleDto {

    implicit val roleDtoWriter: Writes[RoleDto] = Json.writes[RoleDto]

  }

  final case class MembershipDto(id:           String,
                                 version:      Long,
                                 timeAdded:    String,
                                 timeModified: Option[String],
                                 name:         String,
                                 description:  Option[String],
                                 userData:     Set[EntityInfoDto],
                                 studyData:    EntitySetDto,
                                 centreData:   EntitySetDto)

  object MembershipDto {

    implicit val membershipDtoWriter: Writes[MembershipDto] = Json.writes[MembershipDto]

  }

  final case class UserMembershipDto(id:           String,
                                     version:      Long,
                                     timeAdded:    String,
                                     timeModified: Option[String],
                                     name:         String,
                                     description:  Option[String],
                                     studyData:    EntitySetDto,
                                     centreData:   EntitySetDto)

  object UserMembershipDto {

    implicit val userMembershipDtoWriter: Writes[UserMembershipDto] = Json.writes[UserMembershipDto]

  }

}
