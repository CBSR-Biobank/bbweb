package org.biobank.domain.centre

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/** Identifies a unique [[Centre]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class CentreId(id: String) extends IdentifiedValueObject[String]

object CentreId {

  implicit val centreIdReader = (__ \ "id").read[String](minLength[String](2)).map( new CentreId(_) )
  implicit val centreIdWriter = Writes{ (centreId: CentreId) => JsString(centreId.id) }

}
