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
      with HasDateTime

  case class UserNameUpdatedEvent(
    id: String,
    version: Long,
    dateTime: DateTime,
    name: String)
      extends UserEvent
      with HasIdentity
      with HasVersion
      with HasDateTime

  case class UserEmailUpdatedEvent(
    id: String,
    version: Long,
    dateTime: DateTime,
    email: String)
      extends UserEvent
      with HasIdentity
      with HasVersion
      with HasDateTime

  case class UserPasswordUpdatedEvent(
    id: String,
    version: Long,
    dateTime: DateTime,
    password: String,
    salt: String)
      extends UserEvent
      with HasIdentity
      with HasVersion
      with HasDateTime

  case class UserPasswordResetEvent(
    id: String,
    version: Long,
    password: String,
    salt: String,
    dateTime: DateTime)
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
      with HasDateTime

  case class UserUnlockedEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends UserEvent
      with HasIdentity
      with HasVersion
      with HasDateTime

  case class UserRemovedEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends UserEvent
      with HasIdentity
      with HasVersion
      with HasDateTime

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

  implicit val userNameUpdatedEventWrites: Writes[UserNameUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String]
  )(unlift(UserNameUpdatedEvent.unapply))

  implicit val userEmailUpdatedEventWrites: Writes[UserEmailUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "email").write[String]
  )(unlift(UserEmailUpdatedEvent.unapply))

  implicit val userPasswordUpdatedEventWrites: Writes[UserPasswordUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "password").write[String] and
      (__ \ "salt").write[String]
  )(unlift(UserPasswordUpdatedEvent.unapply))

  implicit val userPasswordResetEventWrites: Writes[UserPasswordResetEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "salt").write[String] and
      (__ \ "password").write[String] and
      (__ \ "dateTime").write[DateTime]
  )(unlift(UserPasswordResetEvent.unapply))

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

}
