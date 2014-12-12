package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/** Identifies a unique [[Study]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
case class StudyId(id: String) extends IdentifiedValueObject[String]

object StudyId {

  implicit val studyIdReader = (__).read[String](minLength[String](2)).map( new StudyId(_) )
  implicit val studyIdWriter = Writes{ (id: StudyId) => JsString(id.id) }

}
