package org.biobank.domain

import play.api.libs.json._
import play.api.libs.functional.syntax._

/** A Location is a street address.
  *
  * @param name the name to refer to this location.
  *
  * @param street the street address for this location.
  *
  * @param city the city the location is in.
  *
  * @param province the province or territory the location is in.
  *
  * @param postal code the postal code for the location.
  *
  * @param poBoxNumber the postal office box number this location receives mail at.
  *
  * @param countryIsoCode the ISO country code for the country the location is in.
  */
case class Location(id:             LocationId,
                    name:           String,
                    street:         String,
                    city:           String,
                    province:       String,
                    postalCode:     String,
                    poBoxNumber:    Option[String],
                    countryIsoCode: String)
    extends IdentifiedValueObject[LocationId]

object Location {

  implicit val locationWriter = Json.writes[Location]

}
