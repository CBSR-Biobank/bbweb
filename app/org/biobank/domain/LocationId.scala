package org.biobank.domain

import play.api.libs.json._

/** Identifies a unique [[Location]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class LocationId(id: String) extends IdentifiedValueObject[String]

object LocationId {

  implicit val locationIdWriter = Writes{ (locationId: LocationId) => JsString(locationId.id) }

}
