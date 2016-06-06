package org.biobank.domain.processing

import org.biobank.domain.IdentifiedValueObject
import play.api.libs.json._

case class ProcessingEventId(id: String) extends IdentifiedValueObject[String]

object ProcessingEventId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val specimenIdReader = (__).read[String].map( new ProcessingEventId(_) )
  implicit val specimenIdWriter = Writes{ (id: ProcessingEventId) => JsString(id.id) }
}
