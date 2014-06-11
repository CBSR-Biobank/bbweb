package org.biobank.domain.participant

import org.biobank.domain.ConcurrencySafeEntity
import org.biobank.domain.study.StudyId

import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._

/**
  * Records [[Specimen]]s collected from study participants.
  *
  * Participants can only be created for studies that are enabled.
  *
  * This is an aggregate root.
  */
case class Participant(
  studyId: StudyId,
  id: ParticipantId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  uniqueId: String) extends ConcurrencySafeEntity[ParticipantId] {
}
