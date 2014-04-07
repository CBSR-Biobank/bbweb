package service.events

import domain.UserId

object UserEvents {

  sealed trait UserEvent

  case class UserAddedEvent(
    id: UserId,
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String])
    extends UserEvent

  case class UserAuthenticatedEvent(
    id: UserId,
    name: String,
    email: String)
    extends UserEvent

}
