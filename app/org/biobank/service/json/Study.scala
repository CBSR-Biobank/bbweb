package org.biobank.service.json

import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime

object Study {
  import JsonUtils._

  implicit val studyIdReader = (__ \ "id").read[String](minLength[String](2)).map( new StudyId(_) )
  implicit val studyIdWriter = Writes{ (studyId: StudyId) => JsString(studyId.id) }

  implicit val studyWrites = new Writes[Study] {
    def writes(study: Study) = Json.obj(
      "id"             -> study.id,
      "version"        -> study.version,
      "addedDate"      -> study.addedDate,
      "lastUpdateDate" -> study.lastUpdateDate,
      "name"           -> study.name,
      "description"    -> study.description,
      "status"         -> study.status
    )
  }

  implicit val addStudyCmdReads = (
    (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )(AddStudyCmd.apply _ )

  implicit val updateStudyCmdReads: Reads[UpdateStudyCmd] = (
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )(UpdateStudyCmd.apply _)

  implicit val enableStudyCmdReads: Reads[EnableStudyCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(EnableStudyCmd.apply _ )

  implicit val disableStudyCmdReads: Reads[DisableStudyCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(DisableStudyCmd.apply _)

  implicit val retireStudyCmdReads: Reads[RetireStudyCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(RetireStudyCmd.apply _)

  implicit val unretireStudyCmdReads: Reads[UnretireStudyCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(UnretireStudyCmd.apply _)

  implicit val studyAddedEventWriter: Writes[StudyAddedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String]
  )(unlift(StudyAddedEvent.unapply))

  implicit val studyUpdatedEventWriter: Writes[StudyUpdatedEvent] = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "dateTime").write[DateTime] and
      (__ \ "name").write[String] and
      (__ \ "description").writeNullable[String]
  )(unlift(StudyUpdatedEvent.unapply))

  implicit val studyStatusChangeWrites = new Writes[StudyStatusChangedEvent] {
    def writes(event: StudyStatusChangedEvent) = Json.obj(
      "id"       -> event.id,
      "version"  -> event.version,
      "dateTime" -> event.dateTime
    )
  }
}
