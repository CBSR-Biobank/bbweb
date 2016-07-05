package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._

/** Identifies a unique [[SpecimenGroup]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class SpecimenGroupId(id: String) extends IdentifiedValueObject[String]

object SpecimenGroupId {

  implicit val specimenGroupIdWriter: Writes[SpecimenGroupId] = Writes{
    (specimenGroupId: SpecimenGroupId) => JsString(specimenGroupId.id)
  }

}
