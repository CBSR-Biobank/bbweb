package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._

object UserCommands {

  sealed trait UserCommand extends Command

  case class RegisterUserCmd(
    name: String,
    email: String,
    password: String,
    avatarUrl: Option[String])
    extends UserCommand

  case class UpdateUserCmd(
    expectedVersion: Long,
    name: String,
    email: String,
    password: Option[String],
    avatarUrl: Option[String])
    extends UserCommand

  case class ActivateUserCmd(
    email: String,
    expectedVersion: Long)
      extends UserCommand
      with HasExpectedVersion

  case class LockUserCmd(
    email: String,
    expectedVersion: Long)
      extends UserCommand
      with HasExpectedVersion

  case class UnlockUserCmd(
    email: String,
    expectedVersion: Long)
      extends UserCommand
      with HasExpectedVersion

  case class RemoveUserCmd(
    email: String,
    expectedVersion: Long)
      extends UserCommand
      with HasExpectedVersion

  case class ResetUserPasswordCmd(email: String) extends UserCommand

}
