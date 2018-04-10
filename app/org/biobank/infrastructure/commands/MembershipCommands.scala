package org.biobank.infrastructure.commands

import Commands._

import play.api.libs.json._
import play.api.libs.json.Reads._

object MembershipCommands {

  trait MembershipCommand extends Command with HasSessionUserId

  trait MembershipModifyCommand extends MembershipCommand with HasExpectedVersion {
    val membershipId: String
  }

  final case class AddMembershipCmd(sessionUserId: String,
                                    name:          String,
                                    description:   Option[String],
                                    userIds:       List[String],
                                    allStudies:    Boolean,
                                    studyIds:      List[String],
                                    allCentres:    Boolean,
                                    centreIds:     List[String])
      extends MembershipCommand

  final case class MembershipUpdateNameCmd(sessionUserId:   String,
                                           expectedVersion: Long,
                                           membershipId:    String,
                                           name:            String)
      extends MembershipModifyCommand

  final case class MembershipUpdateDescriptionCmd(sessionUserId:   String,
                                                  expectedVersion: Long,
                                                  membershipId:    String,
                                                  description:     Option[String])
      extends MembershipModifyCommand

  final case class MembershipAddUserCmd(sessionUserId:   String,
                                        expectedVersion: Long,
                                        membershipId:    String,
                                        userId:          String)
      extends MembershipModifyCommand

  final case class MembershipAllStudiesCmd(sessionUserId:   String,
                                           expectedVersion: Long,
                                           membershipId:    String)
      extends MembershipModifyCommand

  final case class MembershipAllCentresCmd(sessionUserId:   String,
                                           expectedVersion: Long,
                                           membershipId:    String)
      extends MembershipModifyCommand

  final case class MembershipAddStudyCmd(sessionUserId:   String,
                                         expectedVersion: Long,
                                         membershipId:    String,
                                         studyId:         String)
      extends MembershipModifyCommand

  final case class MembershipAddCentreCmd(sessionUserId:   String,
                                          expectedVersion: Long,
                                          membershipId:    String,
                                          centreId:          String)
      extends MembershipModifyCommand

  final case class MembershipRemoveUserCmd(sessionUserId:   String,
                                           expectedVersion: Long,
                                           membershipId:    String,
                                           userId:          String)
      extends MembershipModifyCommand

  final case class MembershipRemoveStudyCmd(sessionUserId:   String,
                                            expectedVersion: Long,
                                            membershipId:    String,
                                            studyId:         String)
      extends MembershipModifyCommand

  final case class MembershipRemoveCentreCmd(sessionUserId:   String,
                                             expectedVersion: Long,
                                             membershipId:    String,
                                             centreId:        String)
      extends MembershipModifyCommand

  final case class RemoveMembershipCmd(sessionUserId:   String,
                                       expectedVersion: Long,
                                       membershipId:    String)
      extends MembershipModifyCommand

  implicit val addMembershipCmdReads: Reads[AddMembershipCmd] =
    Json.reads[AddMembershipCmd]

  implicit val membershipUpdateNameCmdReads: Reads[MembershipUpdateNameCmd] =
    Json.reads[MembershipUpdateNameCmd]

  implicit val membershipUpdateDescriptionCmdReads: Reads[MembershipUpdateDescriptionCmd] =
    Json.reads[MembershipUpdateDescriptionCmd]

  implicit val membershipAddUserCmdReads: Reads[MembershipAddUserCmd] =
    Json.reads[MembershipAddUserCmd]

  implicit val membershipAllStudiesCmdReads: Reads[MembershipAllStudiesCmd] =
    Json.reads[MembershipAllStudiesCmd]

  implicit val membershipAllCentresCmdReads: Reads[MembershipAllCentresCmd] =
    Json.reads[MembershipAllCentresCmd]

  implicit val membershipAddStudyCmdReads: Reads[MembershipAddStudyCmd] =
    Json.reads[MembershipAddStudyCmd]

  implicit val membershipAddCentreCmdReads: Reads[MembershipAddCentreCmd] =
    Json.reads[MembershipAddCentreCmd]

  implicit val membershipRemoveUserCmdReads: Reads[MembershipRemoveUserCmd] =
    Json.reads[MembershipRemoveUserCmd]

  implicit val membershipRemoveStudyCmdReads: Reads[MembershipRemoveStudyCmd] =
    Json.reads[MembershipRemoveStudyCmd]

  implicit val membershipRemoveCentreCmdReads: Reads[MembershipRemoveCentreCmd] =
    Json.reads[MembershipRemoveCentreCmd]

  implicit val removeMembershipCmdReads: Reads[RemoveMembershipCmd] =
    Json.reads[RemoveMembershipCmd]

}
