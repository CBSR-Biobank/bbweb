package org.biobank.domain

/** A Location is a street address.
  *
  */
case class Location(
  name: String,
  street: String,
  city: String,
  province: String,
  postalCode: String,
  poBoxNumber: String,
  countryIsoCode: String
)
