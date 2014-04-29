package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

/** Identifies a unique [[ProcessingType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class ProcessingTypeId(val id: String) extends IdentifiedValueObject[String] {}
