package org.biobank.domain.participants

import org.biobank.domain._
import org.slf4j.LoggerFactory
import org.biobank.domain.study.StudyId

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz._
import Scalaz._

@ImplementedBy(classOf[ParticipantRepositoryImpl])
trait ParticipantRepository
    extends ReadWriteRepository [ParticipantId, Participant] {

  def withId(studyId: StudyId, participantId: ParticipantId): DomainValidation[Participant]

  def withUniqueId(studyId: StudyId, uniqueId: String): DomainValidation[Participant]

  def allForStudy(studyId: StudyId): Set[Participant]

}

@Singleton
class ParticipantRepositoryImpl
    extends ReadWriteRepositoryRefImpl[ParticipantId, Participant](v => v.id)
    with ParticipantRepository {

  val log = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: ParticipantId = new ParticipantId(nextIdentityAsString)

  def withId(studyId: StudyId, participantId: ParticipantId): DomainValidation[Participant] = {
    getByKey(participantId).fold(
      err => DomainError(
        s"participant does not exist: { studyId: $studyId, participantId: $participantId }"
      ).failureNel,
      ptcp =>
      if (ptcp.studyId != studyId) {
        DomainError(
          s"study does not have participant: { studyId: $studyId, participantId: $participantId }"
        ).failureNel
      } else {
        ptcp.success
      }
    )
  }

  def withUniqueId(studyId: StudyId, uniqueId: String): DomainValidation[Participant] = {
    getValues.find(p => p.uniqueId == uniqueId) match {
      case None =>
        DomainError(
          s"participant does not exist: { studyId: $studyId, uniqueId: $uniqueId }"
        ).failureNel
      case Some(ptcp) => {
        if (ptcp.studyId != studyId) {
          DomainError(
            s"study does not have participant: { studyId: $studyId, uniqueId: $uniqueId }"
          ).failureNel
        } else {
          ptcp.success
        }
      }
    }
  }

  def allForStudy(studyId: StudyId): Set[Participant] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }

}
