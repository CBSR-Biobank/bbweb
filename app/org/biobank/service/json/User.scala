package org.biobank.service.json

import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.collection.immutable.Map

object User {
  import JsonUtils._

  implicit val userIdWriter = Writes{ (userId: UserId) => JsString(userId.id) }

  implicit val userWrites = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id"             -> user.id,
      "version"        -> user.version,
      "addedDate"      -> user.addedDate,
      "lastUpdateDate" -> user.lastUpdateDate,
      "name"           -> user.name,
      "email"          -> user.email,
      "avatarUrl"      -> user.avatarUrl,
      "status"         -> user.status
    )
  }

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

  /** Does not convert password or salt to JSON.
    */
  implicit val userRegisteredEventWrites = new Writes[UserRegisteredEvent] {
    def writes(event: UserRegisteredEvent) = Json.obj(
      "id"             -> event.id,
      "dateTime"       -> event.dateTime,
      "name"           -> event.name,
      "email"          -> event.email,
      "avatarUrl"      -> event.avatarUrl
    )
  }

  /** Does not convert password or salt to JSON.
    */
  implicit val userUpdatedEventWrites = new Writes[UserUpdatedEvent] {
    def writes(event: UserUpdatedEvent) = Json.obj(
      "id"             -> event.id,
      "version"        -> event.version,
      "dateTime"       -> event.dateTime,
      "name"           -> event.name,
      "email"          -> event.email,
      "avatarUrl"      -> event.avatarUrl
    )
  }

  implicit val userActivatedEventWrites: Writes[UserActivatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime]
  )(unlift(UserActivatedEvent.unapply))

  implicit val userLockedEventWrites: Writes[UserLockedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime]
  )(unlift(UserLockedEvent.unapply))

  implicit val userUnlockedEventWrites: Writes[UserUnlockedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime]
  )(unlift(UserUnlockedEvent.unapply))

  implicit val userRemovedEventWrites: Writes[UserRemovedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime]
  )(unlift(UserRemovedEvent.unapply))

  implicit val userPasswordResetEventWrites: Writes[UserPasswordResetEvent] = (
    (__ \ "id").write[String] and
      (__ \ "salt").write[String] and
      (__ \ "password").write[String] and
      (__ \ "dateTime").write[DateTime]
  )(unlift(UserPasswordResetEvent.unapply))

}
