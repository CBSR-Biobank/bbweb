package org.biobank.infrastructure.command

import org.biobank.infrastructure._
import org.biobank.infrastructure.command.Commands._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object UserCommands {

  trait UserCommand extends Command

  trait UserModifyCommand extends UserCommand with HasIdentity with HasExpectedVersion

  case class RegisterUserCmd(
    name: String,
    email: String,
    password: String,
    avatarUrl: Option[String])
      extends UserCommand

  case class UpdateUserNameCmd(
    id: String,
    expectedVersion: Long,
    name: String)
      extends UserModifyCommand

  case class UpdateUserEmailCmd(
    id: String,
    expectedVersion: Long,
    email: String)
      extends UserModifyCommand

  case class UpdateUserPasswordCmd(
    id: String,
    expectedVersion: Long,
    currentPassword: String,
    newPassword: String)
      extends UserModifyCommand

  case class UpdateUserAvatarUrlCmd(
    id: String,
    expectedVersion: Long,
    avatarUrl: Option[String])
      extends UserModifyCommand

  case class ActivateUserCmd(
    id: String,
    expectedVersion: Long)
      extends UserModifyCommand

  case class LockUserCmd(
    id: String,
    expectedVersion: Long)
      extends UserModifyCommand

  case class UnlockUserCmd(
    id: String,
    expectedVersion: Long)
      extends UserModifyCommand

  case class ResetUserPasswordCmd(
    id: String,
    expectedVersion: Long,
    email: String)
      extends UserModifyCommand

  // The id and expectedVersion fields are don't care in ResetUserPasswordCmd
  // use this object to create this command
  object ResetUserPasswordCmd {
    def apply(email: String): ResetUserPasswordCmd = ResetUserPasswordCmd("", -1, email)
  }

  implicit val registerUserCmdReads        = Json.reads[RegisterUserCmd]
  implicit val updateUserNameCmdReads      = Json.reads[UpdateUserNameCmd]
  implicit val updateUserEmailCmdReads     = Json.reads[UpdateUserEmailCmd]
  implicit val updateUserPasswordCmdReads  = Json.reads[UpdateUserPasswordCmd]
  implicit val updateUserAvatarUrlCmdReads = Json.reads[UpdateUserAvatarUrlCmd]
  implicit val activateUserCmdReads        = Json.reads[ActivateUserCmd]
  implicit val lockUserCmdReads            = Json.reads[LockUserCmd]
  implicit val unlockUserCmdReads          = Json.reads[UnlockUserCmd]

  implicit val resetUserPasswordCmdReads: Reads[ResetUserPasswordCmd] = (
    (__ \ "email").read[String](minLength[String](5))
  ).map { ResetUserPasswordCmd(_) }

}
