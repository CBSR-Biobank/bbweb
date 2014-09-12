package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.functional.syntax._

/** Identifies a unique [[SpecimenGroup]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class SpecimenGroupId(id: String) extends IdentifiedValueObject[String]

object SpecimenGroupId {

  implicit val specimenGroupIdWriter = Writes{
    (specimenGroupId: SpecimenGroupId) => JsString(specimenGroupId.id)
  }

}
