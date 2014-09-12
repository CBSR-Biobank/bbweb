package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object UserCommands {

  sealed trait UserCommand extends Command

  case class RegisterUserCmd(
    name: String,
    email: String,
    password: String,
    avatarUrl: Option[String])
    extends UserCommand

  case class UpdateUserCmd(
    id: String,
    expectedVersion: Long,
    name: String,
    email: String,
    password: Option[String],
    avatarUrl: Option[String])
    extends UserCommand

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

  case class ResetUserPasswordCmd(email: String) extends UserCommand

  implicit val registerUserCmdReads = (
    (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "email").read[String](minLength[String](5)) and
      (__ \ "password").read[String](minLength[String](2)) and
      (__ \ "avatarUrl").readNullable[String](minLength[String](2))
  )(RegisterUserCmd.apply _)

  implicit val updateUserCmdReads = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "email").read[String](minLength[String](5)) and
      (__ \ "password").readNullable[String](minLength[String](2)) and
      (__ \ "avatarUrl").readNullable[String](minLength[String](2))
  )(UpdateUserCmd.apply _)

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

  implicit val resetUserPasswordCmdReads: Reads[ResetUserPasswordCmd] =
    (__ \ "email").read[String](minLength[String](5)).map{ x => ResetUserPasswordCmd(x) }

}
