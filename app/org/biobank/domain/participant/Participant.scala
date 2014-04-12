package org.biobank.domain.participant

import org.biobank.domain._
import org.biobank.domain.study.StudyId

import scalaz._
import scalaz.Scalaz._

case class Participant(
  id: ParticipantId,
  version: Long = -1,
  studyId: StudyId,
  uniqueId: String) extends ConcurrencySafeEntity[ParticipantId] {
}