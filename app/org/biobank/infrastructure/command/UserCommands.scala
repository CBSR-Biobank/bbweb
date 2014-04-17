package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._

object UserCommands {

  sealed trait UserCommand extends Command

  case class AddUserCommand(
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String])
    extends UserCommand

  case class ActivateUserCommand(
    email: String,
    expectedVersion: Option[Long])
      extends UserCommand
      with HasExpectedVersion

  case class LockUserCommand(
    email: String,
    expectedVersion: Option[Long])
      extends UserCommand
      with HasExpectedVersion

  case class UnlockUserCommand(
    email: String,
    expectedVersion: Option[Long])
      extends UserCommand
      with HasExpectedVersion

}
