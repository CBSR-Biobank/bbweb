package org.biobank.infrastructure.command

import Commands._

import play.api.libs.json._
import play.api.libs.json.Reads._

object AccessCommands {

  trait AccessCommand extends Command with HasSessionUserId

  trait AccessModifyCommand extends AccessCommand with HasExpectedVersion

  trait RoleModifyCommand extends AccessModifyCommand {
    val roleId: String
  }

  final case class AddUserToRoleCmd(sessionUserId:   String,
                                    userId:          String,
                                    roleId:          String,
                                    expectedVersion: Long)
      extends RoleModifyCommand

  implicit val adduserToRoleCmdReads: Reads[AddUserToRoleCmd] = Json.reads[AddUserToRoleCmd]

}
