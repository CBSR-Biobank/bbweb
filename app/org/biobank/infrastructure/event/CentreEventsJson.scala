package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Events used by the Centre Aggregate.
  */
object CentreEventsJson {
  import org.biobank.infrastructure.event.CentreEvents._

  // sealed trait CentreEvent extends Event

  // case class CentreAddedEvent(
  //   id: String,
  //   name: String,
  //   description: Option[String])
  //     extends CentreEvent

  // case class CentreUpdatedEvent(
  //   id: String,
  //   version: Long,
  //   name: String,
  //   description: Option[String])
  //     extends CentreEvent
  //     with HasVersion

  // sealed trait CentreStatusChangedEvent extends CentreEvent {
  //   val id: String
  //   val version: Long
  // }

  // case class CentreEnabledEvent(id: String, version: Long)
  //     extends CentreStatusChangedEvent
  //     with HasVersion

  // case class CentreDisabledEvent(id: String, version: Long)
  //     extends CentreStatusChangedEvent
  //     with HasVersion

  // case class CentreLocationAddedEvent(
  //   centreId: String,
  //   locationId: String,
  //   name: String,
  //   street: String,
  //   city: String,
  //   province: String,
  //   postalCode: String,
  //   poBoxNumber: Option[String],
  //   countryIsoCode: String)
  //     extends CentreEvent

  // case class CentreLocationRemovedEvent(centreId: String, locationId: String)
  //     extends CentreEvent

  // case class CentreAddedToStudyEvent(centreId: String, studyId: String)
  //     extends CentreEvent

  // case class CentreRemovedFromStudyEvent(centreId: String, studyId: String)
  //     extends CentreEvent

  implicit val centreAddedEventWriter: Writes[CentreAddedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String]
  )(unlift(CentreAddedEvent.unapply))

  implicit val centreUpdatedEventWriter: Writes[CentreUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long] and
      (__ \ "name").writeNullable[String] and
      (__ \ "description").writeNullable[String]
  )(unlift(CentreUpdatedEvent.unapply))

  implicit val centreEnabledEventWrites: Writes[CentreEnabledEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long]
  )(unlift(CentreEnabledEvent.unapply))

  implicit val centreDisabledEventWrites: Writes[CentreDisabledEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").writeNullable[Long]
  )(unlift(CentreDisabledEvent.unapply))

  implicit val centreLocationAddedEventWriter: Writes[CentreLocationAddedEvent] = (
    (__ \ "centreId").write[String] and
      (__ \ "locationId").write[String] and
      (__ \ "name").writeNullable[String] and
      (__ \ "street").writeNullable[String] and
      (__ \ "city").writeNullable[String] and
      (__ \ "province").writeNullable[String] and
      (__ \ "postalCode").writeNullable[String] and
      (__ \ "poBoxNumber").writeNullable[String] and
      (__ \ "countryIsoCode").writeNullable[String]
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
