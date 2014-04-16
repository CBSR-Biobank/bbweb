package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

/** Identifies a unique [[SpecimenGroup]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class SpecimenGroupId(id: String) extends IdentifiedValueObject[String]
