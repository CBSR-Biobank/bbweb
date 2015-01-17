package org.biobank.domain

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/** Stores a value selected by the user for an annotation that is of type 'Select'. Note that
  * a 'Select' annotation may have multiple values selected.
  */
case class AnnotationOption(annotationTypeId: AnnotationTypeId, value: String)

object AnnotationOption {

  implicit val annotationOptionFormat = Json.format[AnnotationOption]

}

