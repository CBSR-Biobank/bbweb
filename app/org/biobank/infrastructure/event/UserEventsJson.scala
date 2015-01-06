package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._

/** User events are defined in app/protobuf/UserEvents.proto
  */
object UserEventsJson {
  import org.biobank.infrastructure.event.UserEvents._

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
      (__ \ "version").writeNullable[Long] and
      (__ \ "name").writeNullable[String]
  )(unlift(UserNameUpdatedEvent.unapply))

  implicit val userEmailUpdatedEventWrites: Writes[UserEmailUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "email").writeNullable[String]
  )(unlift(UserEmailUpdatedEvent.unapply))

  implicit val userPasswordUpdatedEventWrites: Writes[UserPasswordUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "password").writeNullable[String] and
      (__ \ "salt").writeNullable[String]
  )(unlift(UserPasswordUpdatedEvent.unapply))

  implicit val userAvatarUrlUpdatedEventWrites: Writes[UserAvatarUrlUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "avatarUrl").writeNullable[String]
  )(unlift(UserAvatarUrlUpdatedEvent.unapply))

  implicit val userPasswordResetEventWrites: Writes[UserPasswordResetEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "salt").writeNullable[String] and
      (__ \ "password").writeNullable[String]
  )(unlift(UserPasswordResetEvent.unapply))

  implicit val userActivatedEventWrites: Writes[UserActivatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long]
  )(unlift(UserActivatedEvent.unapply))

  implicit val userLockedEventWrites: Writes[UserLockedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long]
  )(unlift(UserLockedEvent.unapply))

  implicit val userUnlockedEventWrites: Writes[UserUnlockedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long]
  )(unlift(UserUnlockedEvent.unapply))

}
