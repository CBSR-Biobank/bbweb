package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/** Identifies a unique [[CollectionEventType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class CollectionEventTypeId(val id: String) extends IdentifiedValueObject[String] {}

object CollectionEventTypeId {

  implicit val CollectionEventTypeIdReader =
    (__ \ "id").read[String](minLength[String](2)).map( new CollectionEventTypeId(_) )

  implicit val CollectionEventTypeIdWriter =
    Writes{ (id: CollectionEventTypeId) => JsString(id.id) }

}
