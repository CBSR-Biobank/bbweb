package org.biobank.domain

import play.api.libs.json._

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
final case class Location(uniqueId:       String,
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
      case that: Location => this.uniqueId.equalsIgnoreCase(that.uniqueId)
      case _ => false
    }
  }

  override def hashCode:Int = {
    uniqueId.toUpperCase.hashCode
  }
}

object Location {
  import org.biobank.domain.CommonValidations._

  implicit val locationWriter: Writes[Location] = Json.writes[Location]

  def create(name:           String,
             street:         String,
             city:           String,
             province:       String,
             postalCode:     String,
             poBoxNumber:    Option[String],
             countryIsoCode: String) = {
    validate(name, street, city, province, postalCode, poBoxNumber,countryIsoCode).map { _ =>
      val uniqueId = java.util.UUID.randomUUID.toString.replaceAll("-","").toUpperCase
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
