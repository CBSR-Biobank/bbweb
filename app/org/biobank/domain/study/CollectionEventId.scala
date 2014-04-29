package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

/** Identifies a unique [[CollectionEvent]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class CollectionEventId(val id: String) extends IdentifiedValueObject[String] {}
