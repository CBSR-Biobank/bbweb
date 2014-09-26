package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object UserCommands {

  sealed trait UserCommand extends Command {
    val id: String
    val expectedVersion: Long
  }

  case class RegisterUserCmd(
    id: String,               // don't care
    expectedVersion: Long,    // don't care
    name: String,
    email: String,
    password: String,
    avatarUrl: Option[String])
      extends UserCommand

  // The id and expectedVersion fields are don't care in ResetUserPasswordCmd
  // use this object to create this command
  object RegisterUserCmd {
    def apply(
      name: String,
      email: String,
      password: String,
      avatarUrl: Option[String]): RegisterUserCmd = RegisterUserCmd("", -1L, name, email, password, avatarUrl)
  }

  case class UpdateUserNameCmd(
    id: String,
    expectedVersion: Long,
    name: String)
      extends UserCommand
      with HasExpectedVersion

  case class UpdateUserEmailCmd(
    id: String,
    expectedVersion: Long,
    email: String)
      extends UserCommand
      with HasExpectedVersion

  case class UpdateUserPasswordCmd(
    id: String,
    expectedVersion: Long,
    oldPassword: String,
    newPassword: String)
      extends UserCommand
      with HasExpectedVersion

  case class ActivateUserCmd(
    id: String,
    expectedVersion: Long)
      extends UserCommand
      with HasExpectedVersion

  case class LockUserCmd(
    id: String,
    expectedVersion: Long)
      extends UserCommand
      with HasExpectedVersion

  case class UnlockUserCmd(
    id: String,
    expectedVersion: Long)
      extends UserCommand
      with HasExpectedVersion

  case class RemoveUserCmd(
    id: String,
    expectedVersion: Long)
      extends UserCommand
      with HasExpectedVersion

  case class ResetUserPasswordCmd(
    id: String,
    expectedVersion: Long,
    email: String) extends UserCommand

  // The id and expectedVersion fields are don't care in ResetUserPasswordCmd
  // use this object to create this command
  object ResetUserPasswordCmd {
    def apply(email: String): ResetUserPasswordCmd = ResetUserPasswordCmd("", -1, email)
  }

  implicit val registerUserCmdReads = (
    (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "email").read[String](minLength[String](5)) and
      (__ \ "password").read[String](minLength[String](2)) and
      (__ \ "avatarUrl").readNullable[String](minLength[String](2))
  ){ RegisterUserCmd("", -1L, _, _, _, _) }

  implicit val updateUserNameCmdReads = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2))
  )(UpdateUserNameCmd.apply _)

  implicit val updateUserEmailCmdReads = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "email").read[String](minLength[String](5))
  )(UpdateUserEmailCmd.apply _)

  implicit val updateUserPasswordCmdReads = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "oldPassword").read[String](minLength[String](2)) and
      (__ \ "newPassword").read[String](minLength[String](2))
  )(UpdateUserPasswordCmd.apply _)

  implicit val activateUserCmdReads = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(ActivateUserCmd.apply _)

  implicit val lockUserCmdReads = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(LockUserCmd.apply _)

  implicit val unlockUserCmdReads = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(UnlockUserCmd.apply _)

  implicit val resetUserPasswordCmdReads: Reads[ResetUserPasswordCmd] = (
    (__ \ "email").read[String](minLength[String](5))
  ).map { ResetUserPasswordCmd(_) }

}
