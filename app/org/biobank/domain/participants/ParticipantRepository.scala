package org.biobank.domain.participants

import com.google.inject.ImplementedBy
import javax.inject.{Inject , Singleton}
import org.biobank.TestData
import org.biobank.domain._
import org.biobank.domain.studies.StudyId
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[ParticipantRepositoryImpl])
trait ParticipantRepository
    extends ReadWriteRepositoryWithSlug[ParticipantId, Participant] {

  def withId(studyId: StudyId, participantId: ParticipantId): DomainValidation[Participant]

  def withUniqueId(studyId: StudyId, uniqueId: String): DomainValidation[Participant]

  def allForStudy(studyId: StudyId): Set[Participant]

}

@Singleton
class ParticipantRepositoryImpl @Inject() (val testData: TestData)
    extends ReadWriteRepositoryRefImplWithSlug[ParticipantId, Participant](v => v.id)
    with ParticipantRepository {

  import org.biobank.CommonValidations._

  override def init(): Unit = {
    super.init()
    testData.testParticipants.foreach(put)
  }

  def nextIdentity: ParticipantId = new ParticipantId(nextIdentityAsString)

  protected def notFound(id: ParticipantId): IdNotFound = IdNotFound(s"participant id: $id")

  protected def slugNotFound(slug: Slug): EntityCriteriaNotFound =
    EntityCriteriaNotFound(s"participant slug: $slug")

  def withId(studyId: StudyId, participantId: ParticipantId): DomainValidation[Participant] = {
    for {
      participant <- getByKey(participantId)
      validPtcp <- {
        if (participant.studyId != studyId) {
          EntityCriteriaError(
            s"study does not have participant: { studyId: $studyId, participantId: $participantId }"
          ).failureNel[Participant]
        } else {
          participant.successNel[String]
        }
      }
    } yield validPtcp
  }

  def withUniqueId(studyId: StudyId, uniqueId: String): DomainValidation[Participant] = {
    for {
      participant <- {
        getValues.find(p => p.uniqueId == uniqueId).toSuccess(
          EntityCriteriaNotFound(s"participant with unique ID does not exist: $uniqueId").nel)
      }
      valid <- {
        if (participant.studyId != studyId) {
          EntityCriteriaError(
            s"participant not in study: { uniqueId: $uniqueId, studyId: $studyId }"
          ).failureNel[Participant]
        } else {
          participant.successNel[String]
        }
      }
    } yield valid
  }

  def allForStudy(studyId: StudyId): Set[Participant] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }

}
