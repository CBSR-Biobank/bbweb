package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._

/** Identifies a unique [[SpecimenLinkType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class SpecimenLinkTypeId(val id: String) extends IdentifiedValueObject[String]

object SpecimenLinkTypeId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val specimenLinkTypeIdReader: Reads[SpecimenLinkTypeId] =
    (__).read[String].map( new SpecimenLinkTypeId(_) )

  implicit val specimenLinkTypeIdWriter: Writes[SpecimenLinkTypeId] =
    Writes{ (id: SpecimenLinkTypeId) => JsString(id.id) }

}
