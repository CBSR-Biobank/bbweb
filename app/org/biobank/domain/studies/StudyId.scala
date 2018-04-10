package org.biobank.domain.studies

import org.biobank.domain.IdentifiedValueObject
import play.api.libs.json._

/** Identifies a unique [[Study]] in the system.
  *
  * Used as a value object to maintain associations to with other entities in the system.
  */
final case class StudyId(id: String) extends IdentifiedValueObject[String]

object StudyId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val studyIdFormat: Format[StudyId] = new Format[StudyId] {

      override def writes(id: StudyId): JsValue = JsString(id.id)

      override def reads(json: JsValue): JsResult[StudyId] =
        Reads.StringReads.reads(json).map(StudyId.apply _)
    }

}
