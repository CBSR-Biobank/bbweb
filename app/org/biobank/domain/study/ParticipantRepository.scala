package org.biobank.domain.study

import org.biobank.domain._

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait ParticipantRepository
    extends ReadWriteRepository [ParticipantId, Participant] {

  def withId(studyId: StudyId, participantId: ParticipantId): DomainValidation[Participant]

  def allForStudy(studyId: StudyId): Set[Participant]

}

class ParticipantRepositoryImpl
    extends ReadWriteRepositoryRefImpl[ParticipantId, Participant](v => v.id)
    with ParticipantRepository {

  val log = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: ParticipantId = new ParticipantId(nextIdentityAsString)

  def withId(studyId: StudyId, participantId: ParticipantId): DomainValidation[Participant] = {
    getByKey(participantId).fold(
      err => DomainError(
        s"participant does not exist: { studyId: $studyId, participantId: $participantId }")
        .failNel,
      ptcp =>
      if (ptcp.studyId.equals(studyId)) {
        ptcp.success
      } else {
        DomainError(
          s"study does not have participant: { studyId: $studyId, participantId: $participantId }")
          .failNel
      }
    )
  }

  def allForStudy(studyId: StudyId): Set[Participant] = {
    getValues.filter(x => x.studyId.equals(studyId)).toSet
  }

}
