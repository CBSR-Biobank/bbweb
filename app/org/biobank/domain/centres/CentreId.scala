package org.biobank.domain.centres

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._

/** Identifies a unique [[Centre]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class CentreId(id: String) extends IdentifiedValueObject[String]

object CentreId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val centreIdReader: Format[CentreId] = new Format[CentreId] {

      override def writes(id: CentreId): JsValue = JsString(id.id)

      override def reads(json: JsValue): JsResult[CentreId] =
        Reads.StringReads.reads(json).map(CentreId.apply _)
    }
}
