package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/** Identifies a unique [[SpecimenLinkType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class SpecimenLinkTypeId(val id: String) extends IdentifiedValueObject[String]

object SpecimenLinkTypeId {

  implicit val specimenLinkTypeIdReader = (__ \ "id").read[String](minLength[String](2)).map( new SpecimenLinkTypeId(_) )
  implicit val specimenLinkTypeIdWriter = Writes{ (id: SpecimenLinkTypeId) => JsString(id.id) }

}

