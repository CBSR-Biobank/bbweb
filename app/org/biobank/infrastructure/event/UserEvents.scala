package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.event.Events._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._

object UserEvents {

  sealed trait UserEvent extends Event

  case class UserRegisteredEvent(
    id: String,
    name: String,
    email: String,
    password: String,
    salt: String,
    avatarUrl: Option[String])
      extends UserEvent
      with HasIdentity

  case class UserNameUpdatedEvent(id: String, version: Long, name: String)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserEmailUpdatedEvent(id: String, version: Long, email: String)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserPasswordUpdatedEvent(
    id: String,
    version: Long,
    password: String,
    salt: String)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserAvatarUrlUpdatedEvent(id: String, version: Long, avatarUrl: Option[String])
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserPasswordResetEvent(
    id: String,
    version: Long,
    password: String,
    salt: String)
      extends UserEvent
      with HasIdentity

  case class UserActivatedEvent(id: String, version: Long)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserLockedEvent(id: String, version: Long)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserUnlockedEvent(id: String, version: Long)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserRemovedEvent(id: String, version: Long)
      extends UserEvent
      with HasIdentity
      with HasVersion

  /** Does not convert password or salt to JSON.
    */
  implicit val userRegisteredEventWrites = new Writes[UserRegisteredEvent] {
    def writes(event: UserRegisteredEvent) = Json.obj(
      "id"             -> event.id,
      "name"           -> event.name,
      "email"          -> event.email,
      "avatarUrl"      -> event.avatarUrl
    )
  }

  implicit val userNameUpdatedEventWrites: Writes[UserNameUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "name").write[String]
  )(unlift(UserNameUpdatedEvent.unapply))

  implicit val userEmailUpdatedEventWrites: Writes[UserEmailUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "email").write[String]
  )(unlift(UserEmailUpdatedEvent.unapply))

  implicit val userPasswordUpdatedEventWrites: Writes[UserPasswordUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "password").write[String] and
      (__ \ "salt").write[String]
  )(unlift(UserPasswordUpdatedEvent.unapply))

  implicit val userAvatarUrlUpdatedEventWrites: Writes[UserAvatarUrlUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "avatarUrl").writeNullable[String]
  )(unlift(UserAvatarUrlUpdatedEvent.unapply))

  implicit val userPasswordResetEventWrites: Writes[UserPasswordResetEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "salt").write[String] and
      (__ \ "password").write[String]
  )(unlift(UserPasswordResetEvent.unapply))

  implicit val userActivatedEventWrites: Writes[UserActivatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long]
  )(unlift(UserActivatedEvent.unapply))

  implicit val userLockedEventWrites: Writes[UserLockedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long]
  )(unlift(UserLockedEvent.unapply))

  implicit val userUnlockedEventWrites: Writes[UserUnlockedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long]
  )(unlift(UserUnlockedEvent.unapply))

  implicit val userRemovedEventWrites: Writes[UserRemovedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long]
  )(unlift(UserRemovedEvent.unapply))

}
