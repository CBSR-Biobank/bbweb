package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/** Identifies a unique [[ProcessingType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class ProcessingTypeId(val id: String) extends IdentifiedValueObject[String] {}

object ProcessingTypeId {

  implicit val processingTypeIdRead = (__ \ "id").read[String](minLength[String](2)).map( new ProcessingTypeId(_) )
  implicit val processingTypeIdWrite = Writes{ (id: ProcessingTypeId) => JsString(id.id) }

}
