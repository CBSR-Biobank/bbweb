package org.biobank.domain

import play.api.libs.json._
import play.api.libs.json.Reads._
import scalaz.Scalaz._

/** Stores a value selected by the user for an annotation that is of type 'Select'. Note that
  * a 'Select' annotation may have multiple values selected.
  */
case class AnnotationOption(annotationTypeId: AnnotationTypeId, value: String)

object AnnotationOption {
  import org.biobank.domain.CommonValidations._

  def create(annotationTypeId: AnnotationTypeId, value: String) = {
    (validateId(annotationTypeId) |@| validateString(value, NonEmptyString)) {
      AnnotationOption(_, _)
    }
  }

  implicit val annotationOptionFormat = Json.format[AnnotationOption]

}

