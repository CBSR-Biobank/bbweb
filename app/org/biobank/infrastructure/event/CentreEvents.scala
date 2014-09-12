package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.event.Events._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._
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

  implicit val centreAddedEventWriter: Writes[CentreAddedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String]
  )(unlift(CentreAddedEvent.unapply))

  implicit val centreUpdatedEventWriter: Writes[CentreUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String]
  )(unlift(CentreUpdatedEvent.unapply))

  implicit val centreStatusChangeWrites = new Writes[CentreStatusChangedEvent] {
    def writes(event: CentreStatusChangedEvent) = Json.obj(
      "id"       -> event.id,
      "version"  -> event.version,
      "dateTime" -> event.dateTime
    )
  }

  implicit val centreLocationAddedEventWriter: Writes[CentreLocationAddedEvent] = (
    (__ \ "centreId").write[String] and
      (__ \ "locationId").write[String] and
      (__ \ "name").write[String] and
      (__ \ "street").write[String] and
      (__ \ "city").write[String] and
      (__ \ "province").write[String] and
      (__ \ "postalCode").write[String] and
      (__ \ "poBoxNumber").writeNullable[String] and
      (__ \ "countryIsoCode").write[String]
  )(unlift(CentreLocationAddedEvent.unapply))

  implicit val centreLocationRemovedEventWriter: Writes[CentreLocationRemovedEvent] = (
    (__ \ "centreId").write[String] and
      (__ \ "locationId").write[String]
  )(unlift(CentreLocationRemovedEvent.unapply))

  implicit val centreAdddedToStudyEventWriter: Writes[CentreAddedToStudyEvent] = (
    (__ \ "centreId").write[String] and
      (__ \ "studyId").write[String]
  )(unlift(CentreAddedToStudyEvent.unapply))

  implicit val centreRemovedFromStudyEventWriter: Writes[CentreRemovedFromStudyEvent] = (
    (__ \ "centreId").write[String] and
      (__ \ "studyId").write[String]
  )(unlift(CentreRemovedFromStudyEvent.unapply))

}
