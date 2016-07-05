package org.biobank.domain.participants

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._

/** Identifies a unique [[Participant]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class ParticipantId(id: String) extends IdentifiedValueObject[String]

object ParticipantId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val participantIdReader: Reads[ParticipantId] =
    (__).read[String].map( new ParticipantId(_) )

  implicit val participantIdWriter: Writes[ParticipantId] =
    Writes{ (id: ParticipantId) => JsString(id.id) }

}
