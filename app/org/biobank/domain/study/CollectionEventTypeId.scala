package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

/** Identifies a unique [[CollectionEventType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class CollectionEventTypeId(val id: String) extends IdentifiedValueObject[String] {}
