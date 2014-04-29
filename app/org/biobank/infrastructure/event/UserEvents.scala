package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.event.Events._

object UserEvents {

  sealed trait UserEvent //extends Event

  case class UserRegisterdEvent(
    id: String,
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String])
      extends UserEvent
      with HasIdentity

  case class UserActivatedEvent(
    id: String,
    version: Long)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserLockedEvent(
    id: String,
    version: Long)
      extends UserEvent
      with HasIdentity
      with HasVersion

  case class UserUnlockedEvent(
    id: String,
    version: Long)
      extends UserEvent
      with HasIdentity
      with HasVersion

}
