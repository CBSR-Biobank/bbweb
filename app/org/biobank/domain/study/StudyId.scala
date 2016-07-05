package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._

/** Identifies a unique [[Study]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class StudyId(id: String) extends IdentifiedValueObject[String]

object StudyId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val studyIdReader: Reads[StudyId] = (__).read[String].map( new StudyId(_) )
  implicit val studyIdWriter: Writes[StudyId] = Writes{ (id: StudyId) => JsString(id.id) }

}
