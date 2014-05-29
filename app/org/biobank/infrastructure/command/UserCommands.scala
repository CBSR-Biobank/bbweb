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
    expectedVersion: Option[Long],
    name: String,
    email: String,
    password: String,
    avatarUrl: Option[String])
    extends UserCommand

  case class ActivateUserCmd(
    expectedVersion: Option[Long],
    email: String)
      extends UserCommand
      with HasExpectedVersion

  case class LockUserCmd(
    expectedVersion: Option[Long],
    email: String)
      extends UserCommand
      with HasExpectedVersion

  case class UnlockUserCmd(
    expectedVersion: Option[Long],
    email: String)
      extends UserCommand
      with HasExpectedVersion

  case class RemoveUserCmd(
    expectedVersion: Option[Long],
    email: String)
      extends UserCommand
      with HasExpectedVersion

}
