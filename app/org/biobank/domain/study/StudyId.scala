package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

/** Identifies a unique [[Study]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class StudyId(id: String) extends IdentifiedValueObject[String]
