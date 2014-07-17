package org.biobank.domain.centre

import org.biobank.domain.IdentifiedValueObject

/** Identifies a relationship between a [[Study]] and a  [[Centre]] in the system.
  *
  */
case class StudyCentreId(id: String) extends IdentifiedValueObject[String]
