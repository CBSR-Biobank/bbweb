package org.biobank.infrastructure.event

import org.biobank.infrastructure.event.UserEvents._
import org.biobank.infrastructure._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._

/** User events are defined in app/protobuf/UserEvents.proto
  */
trait UserEventsJson {
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

  implicit val userNameUpdatedEventWrites      = Json.writes[UserNameUpdatedEvent]
  implicit val userEmailUpdatedEventWrites     = Json.writes[UserEmailUpdatedEvent]
  implicit val userPasswordUpdatedEventWrites  = Json.writes[UserPasswordUpdatedEvent]
  implicit val userAvatarUrlUpdatedEventWrites = Json.writes[UserAvatarUrlUpdatedEvent]
  implicit val userPasswordResetEventWrites    = Json.writes[UserPasswordResetEvent]
  implicit val userActivatedEventWrites        = Json.writes[UserActivatedEvent]
  implicit val userLockedEventWrites           = Json.writes[UserLockedEvent]
  implicit val userUnlockedEventWrites         = Json.writes[UserUnlockedEvent]

}
