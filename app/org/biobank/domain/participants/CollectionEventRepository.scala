package org.biobank.domain.participants

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CollectionEventRepositoryImpl])
trait CollectionEventRepository
    extends ReadWriteRepository [CollectionEventId, CollectionEvent] {

  def withId(participantId: ParticipantId, collectionEventId: CollectionEventId)
      : DomainValidation[CollectionEvent]

  def withVisitNumber(participantId: ParticipantId, visitNumber: Int): DomainValidation[CollectionEvent]

  def allForParticipant(participantId: ParticipantId): Set[CollectionEvent]

}

@Singleton
class CollectionEventRepositoryImpl
    extends ReadWriteRepositoryRefImpl[CollectionEventId, CollectionEvent](v => v.id)
    with CollectionEventRepository {
  import org.biobank.CommonValidations._

  override val hashidsSalt = "biobank-collection-events"

  def nextIdentity: CollectionEventId = new CollectionEventId(nextIdentityAsString)

  def notFound(id: CollectionEventId) = IdNotFound(s"collection event id: $id")

  override def getByKey(id: CollectionEventId): DomainValidation[CollectionEvent] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def withId(participantId: ParticipantId, collectionEventId: CollectionEventId)
      : DomainValidation[CollectionEvent] = {
    for {
      cevent <- getByKey(collectionEventId)
      valid  <- {
        if (cevent.participantId != participantId) {
          EntityCriteriaError(
            s"collection event invalid for participant : { participantId: $participantId, collectionEventId: $collectionEventId }"
          ).failureNel
        } else {
          cevent.success
        }
      }
    } yield valid
  }

  def withVisitNumber(participantId: ParticipantId, visitNumber: Int): DomainValidation[CollectionEvent] = {
    for {
      cevent <- {
        getValues.find(cevent => cevent.visitNumber == visitNumber).toSuccessNel(
          DomainError(
            s"collection event does not exist: { participantId: $participantId, visitNumber: $visitNumber }"
          ).toString)
      }
      valid <- {
        if (cevent.participantId != participantId) {
          EntityCriteriaError(
            s"participant does not have collection event: { participantId: $participantId, visitNumber: $visitNumber }"
          ).failureNel
        } else {
          cevent.success
        }
      }
    } yield valid
  }

  def allForParticipant(participantId: ParticipantId): Set[CollectionEvent] = {
    getValues.filter { x => x.participantId == participantId }.toSet
  }

}
