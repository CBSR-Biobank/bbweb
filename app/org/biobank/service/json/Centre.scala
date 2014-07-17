package org.biobank.service.json

import org.biobank.domain.{ Location, LocationId }
import org.biobank.domain.centre._
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime

object Centre {
  import JsonUtils._

  implicit val centreIdReader = (__ \ "id").read[String](minLength[String](2)).map( new CentreId(_) )
  implicit val centreIdWriter = Writes{ (centreId: CentreId) => JsString(centreId.id) }

  implicit val centreWrites = new Writes[Centre] {
    def writes(centre: Centre) = Json.obj(
      "id"             -> centre.id,
      "version"        -> centre.version,
      "addedDate"      -> centre.addedDate,
      "lastUpdateDate" -> centre.lastUpdateDate,
      "name"           -> centre.name,
      "description"    -> centre.description,
      "status"         -> centre.status
    )
  }

  implicit val addCentreCmdReads = (
    (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )(AddCentreCmd.apply _ )

  implicit val updateCentreCmdReads: Reads[UpdateCentreCmd] = (
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )(UpdateCentreCmd.apply _)

  implicit val enableCentreCmdReads: Reads[EnableCentreCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(EnableCentreCmd.apply _ )

  implicit val disableCentreCmdReads: Reads[DisableCentreCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(DisableCentreCmd.apply _)
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

  implicit val locationIdWriter = Writes{ (locationId: LocationId) => JsString(locationId.id) }

  implicit val locationWriter: Writes[Location] = (
    (__ \ "id").write[LocationId] and
      (__ \ "name").write[String] and
      (__ \ "street").write[String] and
      (__ \ "city").write[String] and
      (__ \ "province").write[String] and
      (__ \ "postalCode").write[String] and
      (__ \ "poBoxNumber").writeNullable[String] and
      (__ \ "countryIsoCode").write[String]
  )(unlift(Location.unapply))

  implicit val addCentreLocationCmdReads = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "street").read[String](minLength[String](2)) and
      (__ \ "city").read[String](minLength[String](2)) and
      (__ \ "province").read[String](minLength[String](2)) and
      (__ \ "postalCode").read[String](minLength[String](2)) and
      (__ \ "poBoxNumber").readNullable[String](minLength[String](2)) and
      (__ \ "countryIsoCode").read[String](minLength[String](2))
  )(AddCentreLocationCmd.apply _ )

  implicit val removeCentreLocationCmdReads = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "locationId").read[String](minLength[String](2))
  )(RemoveCentreLocationCmd.apply _ )

  implicit val addCentreToStudyCmdReads = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "studyId").read[String](minLength[String](2))
  )(AddCentreToStudyCmd.apply _ )

  implicit val removeCentreFromStudyCmdReads = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "studyId").read[String](minLength[String](2))
  )(RemoveCentreFromStudyCmd.apply _ )

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
