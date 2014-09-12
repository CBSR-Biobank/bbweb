package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.event.Events._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime

object UserEvents {

  sealed trait UserEvent extends Event

  case class UserRegisteredEvent(
    id: String,
    dateTime: DateTime,
    name: String,
    email: String,
    password: String,
    salt: String,
    avatarUrl: Option[String])
      extends UserEvent
      with HasIdentity

  case class UserUpdatedEvent(
    id: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    email: String,
    password: String,
    avatarUrl: Option[String])
      extends UserEvent
      with HasIdentity

  case class UserActivatedEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserLockedEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserUnlockedEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserRemovedEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserPasswordResetEvent(
    id: String,
    password: String,
    salt: String,
    dateTime: DateTime)
      extends UserEvent
      with HasIdentity

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
