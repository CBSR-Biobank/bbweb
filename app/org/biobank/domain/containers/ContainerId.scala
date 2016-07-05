package org.biobank.domain.containers

import org.biobank.domain._

import play.api.libs.json._
import play.api.libs.json.Reads._

/** Identifies a unique [[Container]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class ContainerId(val id: String) extends IdentifiedValueObject[String] {}

object ContainerId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val containerIdReader: Reads[ContainerId] = (__ \ "id").read[String].map( new ContainerId(_) )
  implicit val containerIdWrite: Writes[ContainerId] = Writes{ (id: ContainerId) => JsString(id.id) }

}
