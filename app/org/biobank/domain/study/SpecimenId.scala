package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._

/** Identifies a unique [[Specimen]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class SpecimenId(id: String) extends IdentifiedValueObject[String]

object SpecimenId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val specimenIdReader = (__).read[String].map( new SpecimenId(_) )
  implicit val specimenIdWriter = Writes{ (id: SpecimenId) => JsString(id.id) }

}
