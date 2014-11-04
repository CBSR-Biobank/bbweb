package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/** Identifies a unique [[Participant]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class ParticipantId(id: String) extends IdentifiedValueObject[String]

object ParticipantId {

  implicit val participantIdReader = (__ \ "id").read[String](minLength[String](2)).map( new ParticipantId(_) )
  implicit val participantIdWriter = Writes{ (id: ParticipantId) => JsString(id.id) }

}
