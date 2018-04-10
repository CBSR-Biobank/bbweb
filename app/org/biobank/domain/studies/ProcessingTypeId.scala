package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._

/** Identifies a unique [[ProcessingType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class ProcessingTypeId(val id: String) extends IdentifiedValueObject[String] {}

object ProcessingTypeId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val processingTypeIdRead: Reads[ProcessingTypeId] =
    (__).read[String].map( new ProcessingTypeId(_) )

  implicit val processingTypeIdWrite: Writes[ProcessingTypeId] =
    Writes{ (id: ProcessingTypeId) => JsString(id.id) }

}
