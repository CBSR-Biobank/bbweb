package org.biobank.infrastructure.command

import Commands._

import play.api.libs.json._
import play.api.libs.json.Reads._

object AccessCommands {

  trait AccessCommand extends Command with HasSessionUserId

  trait AccessModifyCommand extends AccessCommand with HasExpectedVersion

  trait RoleModifyCommand extends AccessModifyCommand {
    val roleId: String
    val expectedVersion: Long
  }

  final case class AddUserToRoleCmd(sessionUserId:   String,
                                    userId:          String,
                                    roleId:          String,
                                    expectedVersion: Long)
      extends RoleModifyCommand

  // ROLES

  trait RoleCommand extends AccessCommand

  final case class AddRoleCmd(sessionUserId: String,
                              name:          String,
                              description:   Option[String],
                              userIds:       List[String],
                              parentIds:     List[String],
                              childrenIds:   List[String])
      extends RoleCommand

  final case class RoleUpdateNameCmd(sessionUserId:   String,
                                     expectedVersion: Long,
                                     roleId:          String,
                                     name:            String)
      extends RoleModifyCommand

  final case class RoleUpdateDescriptionCmd(sessionUserId:   String,
                                            expectedVersion: Long,
                                            roleId:          String,
                                            description:     Option[String])
      extends RoleModifyCommand

  final case class RoleAddUserCmd(sessionUserId:   String,
                                  expectedVersion: Long,
                                  roleId:          String,
                                  userId:          String)
      extends RoleModifyCommand

  final case class RoleAddParentCmd(sessionUserId:   String,
                                    expectedVersion: Long,
                                    roleId:          String,
                                    parentRoleId:    String)
      extends RoleModifyCommand

  final case class RoleAddChildCmd(sessionUserId:   String,
                                   expectedVersion: Long,
                                   roleId:          String,
                                   childRoleId:     String)
      extends RoleModifyCommand

  final case class RoleRemoveUserCmd(sessionUserId:   String,
                                     expectedVersion: Long,
                                     roleId:          String,
                                     userId:          String)
      extends RoleModifyCommand

  final case class RoleRemoveParentCmd(sessionUserId:   String,
                                       expectedVersion: Long,
                                       roleId:          String,
                                       parentRoleId:    String)
      extends RoleModifyCommand

  final case class RoleRemoveChildCmd(sessionUserId:   String,
                                       expectedVersion: Long,
                                       roleId:          String,
                                       childRoleId:     String)
      extends RoleModifyCommand

  final case class RemoveRoleCmd(sessionUserId:   String,
                                 expectedVersion: Long,
                                 roleId:          String)
      extends RoleModifyCommand

  implicit val adduserToRoleCmdReads: Reads[AddUserToRoleCmd] =
    Json.reads[AddUserToRoleCmd]

  implicit val addRoleCmdReads: Reads[AddRoleCmd] = Json.reads[AddRoleCmd]

  implicit val roleUpdateNameCmdReads: Reads[RoleUpdateNameCmd] = Json.reads[RoleUpdateNameCmd]

  implicit val roleUpdateDescriptionCmdReads: Reads[RoleUpdateDescriptionCmd] =
    Json.reads[RoleUpdateDescriptionCmd]

  implicit val roleAddUserCmdReads: Reads[RoleAddUserCmd] = Json.reads[RoleAddUserCmd]

  implicit val roleAddParentCmdReads: Reads[RoleAddParentCmd] = Json.reads[RoleAddParentCmd]

  implicit val roleAddChildCmdReads: Reads[RoleAddChildCmd] = Json.reads[RoleAddChildCmd]

  implicit val roleRemoveUserCmdReads: Reads[RoleRemoveUserCmd] = Json.reads[RoleRemoveUserCmd]

  implicit val roleRemoveParentCmdReads: Reads[RoleRemoveParentCmd] = Json.reads[RoleRemoveParentCmd]

  implicit val roleRemoveChildCmdReads: Reads[RoleRemoveChildCmd] = Json.reads[RoleRemoveChildCmd]

  implicit val removeRoleCmdReads: Reads[RemoveRoleCmd] = Json.reads[RemoveRoleCmd]

}
