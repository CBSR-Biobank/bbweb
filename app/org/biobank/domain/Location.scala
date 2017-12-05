package org.biobank.domain

import play.api.libs.json._

/** Identifies a unique [[Loction]] in for a [[Centre]].
  *
  * Used as a value object to maintain associations to with entities in the system.
  */
final case class LocationId(id: String) extends IdentifiedValueObject[String]

object LocationId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val locationIdFormat: Format[LocationId] = new Format[LocationId] {

      override def writes(id: LocationId): JsValue = JsString(id.id)

      override def reads(json: JsValue): JsResult[LocationId] =
        Reads.StringReads.reads(json).map(LocationId.apply _)
    }

}

/**
 * A Location is a street address.
 *
 * This is a value type. Two locations are considered equal if they have the same Id.
 *
 * @param uniqeId an ID to identify this location.
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
final case class Location(id:             LocationId,
                          slug:           String,
                          name:           String,
                          street:         String,
                          city:           String,
                          province:       String,
                          postalCode:     String,
                          poBoxNumber:    Option[String],
                          countryIsoCode: String)
    extends IdentifiedValueObject[LocationId]
    with HasName

object Location {
  import org.biobank.domain.CommonValidations._

  implicit val locationFormat: Format[Location] = Json.format[Location]

  def create(name:           String,
             street:         String,
             city:           String,
             province:       String,
             postalCode:     String,
             poBoxNumber:    Option[String],
             countryIsoCode: String): DomainValidation[Location] = {
    validate(name).map { _ =>
      val id = LocationId(java.util.UUID.randomUUID.toString.replaceAll("-","").toUpperCase)
      Location(id             = id,
               slug           = Slug(name),
               name           = name,
               street         = street,
               city           = city,
               province       = province,
               postalCode     = postalCode,
               poBoxNumber    = poBoxNumber,
               countryIsoCode = countryIsoCode)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(name: String): DomainValidation[Boolean] =
    validateString(name, NameRequired).map { _ => true }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(location: Location): DomainValidation[Boolean] =
    validate(location.name)
}
