package org.biobank.domain.participants

import org.biobank.domain._
import org.biobank.domain.study.StudyId

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

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

  override val NotFoundError = "participant with id not found:"

  def nextIdentity: ParticipantId = new ParticipantId(nextIdentityAsString)

  def withId(studyId: StudyId, participantId: ParticipantId): DomainValidation[Participant] = {
    for {
      ptcp <- getByKey(participantId)
      validPtcp <- {
        if (ptcp.studyId != studyId) {
          DomainError(
            s"study does not have participant: { studyId: $studyId, participantId: $participantId }"
          ).failureNel
        } else {
          ptcp.success
        }
      }
    } yield validPtcp
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
