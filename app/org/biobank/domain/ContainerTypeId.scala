package org.biobank.domain

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/** Identifies a unique [[ContainerType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class ContainerTypeId(val id: String) extends IdentifiedValueObject[String] {}

object ContainerTypeId {

  implicit val containerTypeIdReader = (__ \ "id").read[String](minLength[String](2)).map( new ContainerTypeId(_) )
  implicit val containerTypeIdWrite = Writes{ (id: ContainerTypeId) => JsString(id.id) }

}
