package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.event.Events._
import org.joda.time.DateTime

/**
  * Events used by the Centre Aggregate.
  */
object CentreEvents {

  sealed trait CentreEvent extends Event

  case class CentreAddedEvent(
    id: String,
    dateTime: DateTime,
    name: String,
    description: Option[String])
      extends CentreEvent

  case class CentreUpdatedEvent(
    id: String,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String])
      extends CentreEvent

  sealed trait CentreStatusChangedEvent extends CentreEvent {
    val id: String
    val version: Long
    val dateTime: DateTime
  }

  case class CentreEnabledEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends CentreStatusChangedEvent

  case class CentreDisabledEvent(
    id: String,
    version: Long,
    dateTime: DateTime)
      extends CentreStatusChangedEvent

  case class CentreLocationAddedEvent(
    centreId: String,
    locationId: String,
    name: String,
    street: String,
    city: String,
    province: String,
    postalCode: String,
    poBoxNumber: Option[String],
    countryIsoCode: String)
      extends CentreEvent

  case class CentreLocationRemovedEvent(
    centreId: String,
    locationId: String)
      extends CentreEvent

  case class CentreAddedToStudyEvent(
    centreId: String,
    studyId: String)
      extends CentreEvent

  case class CentreRemovedFromStudyEvent(
    centreId: String,
    studyId: String)
      extends CentreEvent

}
