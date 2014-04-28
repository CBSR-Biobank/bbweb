package org.biobank.domain

/** Identifies a unique [[ContainerType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class ContainerTypeId(val id: String) extends IdentifiedValueObject[String] {}
