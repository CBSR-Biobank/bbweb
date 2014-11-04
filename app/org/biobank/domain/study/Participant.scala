package org.biobank.domain.study

import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation,
  ValidationKey
}
import org.biobank.infrastructure.JsonUtils._

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scalaz._
import scalaz.Scalaz._

trait ParticipantValidations {

  case object UniqueIdInvalid extends ValidationKey

}

/** The subject for which a set of specimens were collected from. The subject can be human or non human.
  * A participant belongs to a single study.
  *
  * @param uniqueId A participant has a unique identifier that is used to identify the participant in
  *        the system. This identifier is not the same as the ParticipantId value object
  *        used by the domain model.
  */
case class Participant(
  studyId: StudyId,
  id: ParticipantId,
  version: Long,
  timeAdded: DateTime,
  timeModified: Option[DateTime],
  uniqueId: String)
    extends ConcurrencySafeEntity[ParticipantId]
    with HasStudyId {

  override def toString: String =
    s"""|CollectionEventType:{
        |  studyId: $studyId,
        |  id: $id,
        |  version: $version,
        |  timeAdded: $timeAdded,
        |  timeModified: $timeModified,
        |  uniqueId: $uniqueId,
        |}""".stripMargin
}

object Participant extends ParticipantValidations {
  import org.biobank.domain.CommonValidations._

  def create(
    studyId: StudyId,
    id: ParticipantId,
    version: Long,
    dateTime: DateTime,
    uniqueId: String): DomainValidation[Participant] = {
    (validateId(studyId) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateString(uniqueId, NameRequired)) {
      Participant(_, _, _, dateTime, None, _)
    }
  }

  implicit val participantWrites: Writes[Participant] = (
    (__ \ "studyId").write[StudyId] and
      (__ \ "id").write[ParticipantId] and
      (__ \ "version").write[Long] and
      (__ \ "timeAdded").write[DateTime] and
      (__ \ "timeModified").write[Option[DateTime]] and
      (__ \ "uniqueId").write[String]
  )(unlift(Participant.unapply))


}
