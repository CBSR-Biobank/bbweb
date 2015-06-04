package org.biobank.domain

import play.api.libs.json._
import play.api.libs.json.Reads._

/** Identifies a unique [[AnnotationType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class AnnotationTypeId(val id: String) extends IdentifiedValueObject[String]

object AnnotationTypeId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val annotationTypeIdReader = (__).read[String].map( new AnnotationTypeId(_) )
  implicit val annotationTypeIdWriter = Writes{ (id: AnnotationTypeId) => JsString(id.id) }

}

