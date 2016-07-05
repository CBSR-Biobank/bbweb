package org.biobank.domain.containers

import org.biobank.domain._

import play.api.libs.json._
import play.api.libs.json.Reads._

/** Identifies a unique [[ContainerType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class ContainerTypeId(val id: String) extends IdentifiedValueObject[String] {}

object ContainerTypeId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val containerTypeIdReader: Reads[ContainerTypeId] =
    (__ \ "id").read[String].map( new ContainerTypeId(_) )

  implicit val containerTypeIdWrite: Writes[ContainerTypeId] =
    Writes{ (id: ContainerTypeId) => JsString(id.id) }

}
