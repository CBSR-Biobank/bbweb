package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._

import play.api.libs.json._
import play.api.libs.json.Reads._

object UserCommands {

  trait UserCommand extends Command

  trait UserModifyCommand extends UserCommand with HasIdentity with HasExpectedVersion

  final case class RegisterUserCmd(userId:    Option[String],
                                   name:      String,
                                   email:     String,
                                   password:  String,
                                   avatarUrl: Option[String])
      extends UserCommand

  final case class UpdateUserNameCmd(userId:    Option[String],
                                     id: String,
                                     expectedVersion: Long,
                                     name: String)
      extends UserModifyCommand

  final case class UpdateUserEmailCmd(userId:    Option[String],
                                      id: String,
                                      expectedVersion: Long,
                                      email: String)
      extends UserModifyCommand

  final case class UpdateUserPasswordCmd(userId:    Option[String],
                                         id: String,
                                         expectedVersion: Long,
                                         currentPassword: String,
                                         newPassword: String)
      extends UserModifyCommand

  final case class UpdateUserAvatarUrlCmd(userId:    Option[String],
                                          id: String,
                                          expectedVersion: Long,
                                          avatarUrl: Option[String])
      extends UserModifyCommand

  final case class ActivateUserCmd(userId:    Option[String],
                                   id: String,
                                   expectedVersion: Long)
      extends UserModifyCommand

  final case class LockUserCmd(userId:    Option[String],
                               id: String,
                               expectedVersion: Long)
      extends UserModifyCommand

  final case class UnlockUserCmd(userId:    Option[String],
                                 id: String,
                                 expectedVersion: Long)
      extends UserModifyCommand

  final case class ResetUserPasswordCmd(userId:          Option[String],
                                        id:              String,
                                        expectedVersion: Long,
                                        email:           String)
      extends UserModifyCommand

  // The id and expectedVersion fields are don't care in ResetUserPasswordCmd
  // use this object to create this command
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  object ResetUserPasswordCmd {
    def apply(email: String): ResetUserPasswordCmd = ResetUserPasswordCmd(None, "", 0L, email)
  }

  implicit val registerUserCmdReads: Reads[RegisterUserCmd] =
    Json.reads[RegisterUserCmd]

  implicit val updateUserNameCmdReads: Reads[UpdateUserNameCmd] =
    Json.reads[UpdateUserNameCmd]

  implicit val updateUserEmailCmdReads: Reads[UpdateUserEmailCmd] =
    Json.reads[UpdateUserEmailCmd]

  implicit val updateUserPasswordCmdReads: Reads[UpdateUserPasswordCmd] =
    Json.reads[UpdateUserPasswordCmd]

  implicit val updateUserAvatarUrlCmdReads: Reads[UpdateUserAvatarUrlCmd] =
    Json.reads[UpdateUserAvatarUrlCmd]

  implicit val activateUserCmdReads: Reads[ActivateUserCmd] =
    Json.reads[ActivateUserCmd]

  implicit val lockUserCmdReads: Reads[LockUserCmd] =
    Json.reads[LockUserCmd]

  implicit val unlockUserCmdReads: Reads[UnlockUserCmd] =
    Json.reads[UnlockUserCmd]


  implicit val resetUserPasswordCmdReads: Reads[ResetUserPasswordCmd] = (
    (__ \ "email").read[String](minLength[String](5))
  ).map { ResetUserPasswordCmd(_) }

}
