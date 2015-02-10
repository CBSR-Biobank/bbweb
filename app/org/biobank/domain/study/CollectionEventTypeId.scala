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

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val collectionEventTypeIdReader = (__).read[String].map( new CollectionEventTypeId(_) )
  implicit val collectionEventTypeIdWriter = Writes{ (id: CollectionEventTypeId) => JsString(id.id) }

}
