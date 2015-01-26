package org.biobank.infrastructure.event

import org.biobank.infrastructure._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Events used by the Centre Aggregate.
  */
trait CentreEventsJson {
  import org.biobank.infrastructure.event.CentreEvents._

  implicit val centreAddedEventWriter            = Json.writes[CentreAddedEvent]
  implicit val centreUpdatedEventWriter          = Json.writes[CentreUpdatedEvent]
  implicit val centreEnabledEventWrites          = Json.writes[CentreEnabledEvent]
  implicit val centreDisabledEventWrites         = Json.writes[CentreDisabledEvent]
  implicit val centreLocationAddedEventWriter    = Json.writes[CentreLocationAddedEvent]
  implicit val centreLocationRemovedEventWriter  = Json.writes[CentreLocationRemovedEvent]
  implicit val centreAdddedToStudyEventWriter    = Json.writes[CentreAddedToStudyEvent]
  implicit val centreRemovedFromStudyEventWriter = Json.writes[CentreRemovedFromStudyEvent]

}
