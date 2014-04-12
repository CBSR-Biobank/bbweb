package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.event.Events._

object UserEvents {

  sealed trait UserEvent //extends Event

  case class UserAddedEvent(
    id: String,
    version: Long,
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String])
    extends UserEvent
  with Identity

  case class UserActivatedEvent(id: String) extends UserEvent
  with Identity

}
