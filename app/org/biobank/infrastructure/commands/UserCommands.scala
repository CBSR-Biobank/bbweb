package org.biobank.infrastructure.commands

import org.biobank.infrastructure.commands.Commands._

import play.api.libs.json._
import play.api.libs.json.Reads._

object UserCommands {

  trait UserCommand extends Command

  trait UserModifyCommand extends UserCommand with HasIdentity with HasExpectedVersion with HasSessionUserId

  trait UserStateChangeCommand extends UserModifyCommand

  trait UserAccessCommand extends UserModifyCommand

  final case class RegisterUserCmd(name:      String,
                                   email:     String,
                                   password:  String,
                                   avatarUrl: Option[String])
      extends UserCommand

  final case class UpdateUserNameCmd(sessionUserId:   String,
                                     id:              String,
                                     expectedVersion: Long,
                                     name:            String)
      extends UserModifyCommand

  final case class UpdateUserEmailCmd(sessionUserId:   String,
                                      id:              String,
                                      expectedVersion: Long,
                                      email:           String)
      extends UserModifyCommand

  final case class UpdateUserPasswordCmd(sessionUserId:   String,
                                         id:              String,
                                         expectedVersion: Long,
                                         currentPassword: String,
                                         newPassword:     String)
      extends UserModifyCommand

  final case class UpdateUserAvatarUrlCmd(sessionUserId:   String,
                                          id:              String,
                                          expectedVersion: Long,
                                          avatarUrl:       Option[String])
      extends UserModifyCommand

  final case class UpdateUserAddRoleCmd(sessionUserId:   String,
                                        id:              String,
                                        expectedVersion: Long,
                                        roleId:          String)
      extends UserAccessCommand

  final case class UpdateUserRemoveRoleCmd(sessionUserId:   String,
                                           id:              String,
                                           expectedVersion: Long,
                                           roleId:          String)
      extends UserAccessCommand

  final case class UpdateUserAddMembershipCmd(sessionUserId:   String,
                                              id:              String,
                                              expectedVersion: Long,
                                              membershipId:    String)
      extends UserAccessCommand

  final case class UpdateUserRemoveMembershipCmd(sessionUserId:   String,
                                                 id:              String,
                                                 expectedVersion: Long,
                                                 membershipId:    String)
      extends UserAccessCommand

  final case class ActivateUserCmd(sessionUserId:   String,
                                   id:              String,
                                   expectedVersion: Long)
      extends UserStateChangeCommand

  final case class LockUserCmd(sessionUserId:   String,
                               id:              String,
                               expectedVersion: Long)
      extends UserStateChangeCommand

  final case class UnlockUserCmd(sessionUserId:   String,
                                 id:              String,
                                 expectedVersion: Long)
      extends UserStateChangeCommand

  final case class ResetUserPasswordCmd(email: String)
      extends UserCommand

  implicit val registerUserCmdReads: Reads[RegisterUserCmd] =
    Json.reads[RegisterUserCmd]

  implicit val resetUserPasswordCmdReads: Reads[ResetUserPasswordCmd] = (
    (__ \ "email").read[String](minLength[String](5))
  ).map { ResetUserPasswordCmd(_) }

  implicit val updateUserAddRoleCmdReads: Reads[UpdateUserAddRoleCmd] =
    Json.reads[UpdateUserAddRoleCmd]

  implicit val updateUserRemoveRoleCmdReads: Reads[UpdateUserRemoveRoleCmd] =
    Json.reads[UpdateUserRemoveRoleCmd]

  implicit val updateUserAddMembershipCmdReads: Reads[UpdateUserAddMembershipCmd] =
    Json.reads[UpdateUserAddMembershipCmd]

  implicit val updateUserRemoveMembershipCmdReads: Reads[UpdateUserRemoveMembershipCmd] =
    Json.reads[UpdateUserRemoveMembershipCmd]

}
