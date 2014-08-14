package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.event.Events._

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
    salt: String,
    password: String,
    dateTime: DateTime)
      extends UserEvent
      with HasIdentity

}
