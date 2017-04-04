package org.biobank.domain

import play.api.libs.json._

final case class LocationId(val id: String) extends AnyVal {
  override def toString: String = id
}

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
 * This is a value type. Two locations are considered equal if they have the same uniqueId.
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
final case class Location(uniqueId:       LocationId,
                          name:           String,
                          street:         String,
                          city:           String,
                          province:       String,
                          postalCode:     String,
                          poBoxNumber:    Option[String],
                          countryIsoCode: String)
    extends HasName {

  override def equals(that: Any): Boolean = {
    that match {
      case that: Location => this.uniqueId.id.equalsIgnoreCase(that.uniqueId.id)
      case _ => false
    }
  }

  override def hashCode:Int = {
    uniqueId.id.toUpperCase.hashCode
  }
}

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
    validate(name, street, city, province, postalCode, poBoxNumber,countryIsoCode).map { _ =>
      val uniqueId = LocationId(java.util.UUID.randomUUID.toString.replaceAll("-","").toUpperCase)
      Location(uniqueId, name, street, city, province, postalCode, poBoxNumber, countryIsoCode)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(name:           String,
               street:         String,
               city:           String,
               province:       String,
               postalCode:     String,
               poBoxNumber:    Option[String],
               countryIsoCode: String): DomainValidation[Boolean] =
    validateString(name, NameRequired).map { _ => true }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(location: Location): DomainValidation[Boolean] =
    validate(location.name,
             location.street,
             location.city,
             location.province,
             location.postalCode,
             location.poBoxNumber,
             location.countryIsoCode)
}
