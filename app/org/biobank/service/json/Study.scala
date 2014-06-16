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
    (__ \ "type").read[String](Reads.verifying[String](_ == "AddStudyCmd")) andKeep
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )((name, description) => AddStudyCmd(name, description))

  implicit val updateStudyCmdReads: Reads[UpdateStudyCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "UpdateStudyCmd")) andKeep
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )((id, version, name, description) => UpdateStudyCmd(id, version, name, description))

  implicit val enableStudyCmdReads: Reads[EnableStudyCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "EnableStudyCmd")) andKeep
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )((id, expectedVersion) => EnableStudyCmd(id, expectedVersion))

  implicit val disableStudyCmdReads: Reads[DisableStudyCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "DisableStudyCmd")) andKeep
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )((id, expectedVersion) => DisableStudyCmd(id, expectedVersion))

  implicit val retireStudyCmdReads: Reads[RetireStudyCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "RetireStudyCmd")) andKeep
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )((id, expectedVersion) => RetireStudyCmd(id, expectedVersion))

  implicit val unretireStudyCmdReads: Reads[UnretireStudyCmd] = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "UnretireStudyCmd")) andKeep
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").readNullable[Long](min[Long](0))
  )((id, expectedVersion) => UnretireStudyCmd(id, expectedVersion))

  implicit val studyAddedEventWriter = new Writes[StudyAddedEvent] {
    def writes(event: StudyAddedEvent) = Json.obj(
      "type"        -> "StudyAddedEvent",
      "id"          -> event.id,
      "dateTime "   -> event.dateTime,
      "name"        -> event.name,
      "description" -> event.description
    )
  }

  implicit val studyUpdatedEventWriter = new Writes[StudyUpdatedEvent] {
    def writes(event: StudyUpdatedEvent) = Json.obj(
      "type"        -> "StudyUpdatedEvent",
      "id"          -> event.id,
      "version"     -> event.version,
      "dateTime "   -> event.dateTime,
      "name"        -> event.name,
      "description" -> event.description
    )
  }

  implicit val studyStatusChangeWrites = new Writes[StudyStatusChangedEvent] {
    def writes(event: StudyStatusChangedEvent) = Json.obj(
      "type"     -> event.getClass.getSimpleName,
      "id"       -> event.id,
      "version"  -> event.version,
      "dateTime" -> event.dateTime
    )
  }

  def eventToJsonReply[T <: StudyEvent](event: T, message: String)(implicit writes: Writes[T]): JsObject = {
    Json.obj(
      "status" ->"success",
      "data" -> Json.obj("event" -> Json.toJson(event))
    )
  }
}
