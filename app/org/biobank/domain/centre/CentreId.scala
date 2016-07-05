package org.biobank.domain.centre

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._

/** Identifies a unique [[Centre]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class CentreId(id: String) extends IdentifiedValueObject[String]

object CentreId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val centreIdReader: Reads[CentreId] = (__ \ "id").read[String].map( new CentreId(_) )
  implicit val centreIdWriter: Writes[CentreId] = Writes{ (centreId: CentreId) => JsString(centreId.id) }

}
