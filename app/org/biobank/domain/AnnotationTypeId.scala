package org.biobank.domain

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/** Identifies a unique [[AnnotationType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class AnnotationTypeId(val id: String) extends IdentifiedValueObject[String]

object AnnotationTypeId {

  implicit val annotationTypeIdReader =
    (__).read[String](minLength[String](2)).map( new AnnotationTypeId(_) )

  implicit val annotationTypeIdWriter =
    Writes{ (id: AnnotationTypeId) => JsString(id.id) }

}

