package domain.participant

import domain._
import domain.study.StudyId

import scalaz._
import scalaz.Scalaz._

case class Participant(
  id: ParticipantId,
  version: Long = -1,
  studyId: StudyId,
  uniqueId: String) extends ConcurrencySafeEntity[ParticipantId] {
}