package org.biobank.domain

/** Identifies a unique [[Location]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class LocationId(id: String) extends IdentifiedValueObject[String]
