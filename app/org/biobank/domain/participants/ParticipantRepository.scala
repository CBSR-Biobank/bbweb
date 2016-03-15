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
  import org.biobank.CommonValidations._

  override val hashidsSalt = "biobank-participants"

  def nextIdentity: ParticipantId = new ParticipantId(nextIdentityAsString)

  def notFound(id: ParticipantId) = IdNotFound(s"participant id: $id")

  override def getByKey(id: ParticipantId): DomainValidation[Participant] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def withId(studyId: StudyId, participantId: ParticipantId): DomainValidation[Participant] = {
    for {
      participant <- getByKey(participantId)
      validPtcp <- {
        if (participant.studyId != studyId) {
          EntityCriteriaError(
            s"study does not have participant: { studyId: $studyId, participantId: $participantId }"
          ).failureNel
        } else {
          participant.success
        }
      }
    } yield validPtcp
  }

  def withUniqueId(studyId: StudyId, uniqueId: String): DomainValidation[Participant] = {
    for {
      participant <- {
        getValues.find(p => p.uniqueId == uniqueId).toSuccessNel(
          EntityCriteriaNotFound(s"participant with unique ID does not exist: $uniqueId").toString)
      }
      valid <- {
        if (participant.studyId != studyId) {
          EntityCriteriaError(
            s"participant not in study: { uniqueId: $uniqueId, studyId: $studyId }"
          ).failureNel
        } else {
          participant.success
        }
      }
    } yield valid
  }

  def allForStudy(studyId: StudyId): Set[Participant] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }

}
