package org.biobank.domain.centre

import org.biobank.domain.IdentifiedValueObject

/** Identifies a unique [[Centre]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class CentreId(id: String) extends IdentifiedValueObject[String]
