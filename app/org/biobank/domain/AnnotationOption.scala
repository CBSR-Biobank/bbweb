package org.biobank.domain

/** Stores a value selected by the user for an annotation that is of type 'Select'. Note that
  * a 'Select' annotation may have multiple values selected.
  */
case class AnnotationOption(annotationTypeId: AnnotationTypeId, value: String)
