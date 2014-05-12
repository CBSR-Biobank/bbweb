package org.biobank.service.json

import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.format.ISODateTimeFormat

object Study {

  implicit val studyWrites = new Writes[Study] {

    val fmt = ISODateTimeFormat.dateTime();

    def writes(study: Study) = Json.obj(
      "id"             -> study.id.id,
      "version"        -> study.version,
      "addedDate"      -> fmt.print(study.addedDate),
      "lastUpdateDate" -> study.lastUpdateDate.map(fmt.print(_)),
      "name"           -> study.name,
      "description"    -> study.description,
      "status"         -> study.status
    )

  }

  implicit val studyIdReads: Reads[StudyId] = (
    (__ \ "id").read[String](minLength[String](2))
  ) map StudyId

  implicit val addStudyCmdReads = (
    (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "description").readNullable[String](minLength[String](2))
  )(AddStudyCmd)

  implicit val updateStudyCmdReads: Reads[UpdateStudyCmd] = (
    (JsPath \ "id").read[String](minLength[String](2)) and
      (JsPath \ "version").readNullable[Long](min[Long](0)) and
      (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "description").readNullable[String](minLength[String](2))
  )(UpdateStudyCmd)

  implicit val enableStudyCmdReads: Reads[EnableStudyCmd] = (
    (JsPath \ "id").read[String](minLength[String](2)) and
      (JsPath \ "expectedVersion").readNullable[Long](min[Long](0))
  )(EnableStudyCmd)

  implicit val disableStudyCmdReads: Reads[DisableStudyCmd] = (
    (JsPath \ "id").read[String](minLength[String](2)) and
      (JsPath \ "expectedVersion").readNullable[Long](min[Long](0))
  )(DisableStudyCmd)

  implicit val retireStudyCmdReads: Reads[RetireStudyCmd] = (
    (JsPath \ "id").read[String](minLength[String](2)) and
      (JsPath \ "expectedVersion").readNullable[Long](min[Long](0))
  )(RetireStudyCmd)

  implicit val unretireStudyCmdReads: Reads[UnretireStudyCmd] = (
    (JsPath \ "id").read[String](minLength[String](2)) and
      (JsPath \ "expectedVersion").readNullable[Long](min[Long](0))
  )(UnretireStudyCmd)

}
