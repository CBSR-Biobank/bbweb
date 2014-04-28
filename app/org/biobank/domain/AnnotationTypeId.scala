package org.biobank.domain

/** Identifies a unique [[AnnotationType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class AnnotationTypeId(val id: String) extends IdentifiedValueObject[String]
