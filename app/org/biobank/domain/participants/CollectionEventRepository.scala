package org.biobank.domain.participants

import org.biobank.domain._
import org.slf4j.LoggerFactory

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz._
import Scalaz._

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


  val log = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: CollectionEventId = new CollectionEventId(nextIdentityAsString)

  def withId(participantId: ParticipantId, collectionEventId: CollectionEventId)
      : DomainValidation[CollectionEvent] = {
    getByKey(collectionEventId).fold(
      err => DomainError(
        s"collection event does not exist: { participantId: $participantId, collectionEventId: $collectionEventId }"
      ).failureNel,
      collectionEvent =>
      if (collectionEvent.participantId != participantId) {
        DomainError(
          s"participant does not have collection event: { participantId: $participantId, collectionEventId: $collectionEventId }"
        ).failureNel
      } else {
        collectionEvent.success
      }
    )
  }

  def withVisitNumber(participantId: ParticipantId, visitNumber: Int): DomainValidation[CollectionEvent] = {
    getValues.find(cevent => cevent.visitNumber == visitNumber) match {
      case None =>
        DomainError(
          s"collection event does not exist: { participantId: $participantId, visitNumber: $visitNumber }"
        ).failureNel
      case Some(cevent) => {
        if (cevent.participantId != participantId) {
          DomainError(
            s"participant does not have collection event: { participantId: $participantId, visitNumber: $visitNumber }"
          ).failureNel
        } else {
          cevent.success
        }
      }
    }
  }

  def allForParticipant(participantId: ParticipantId): Set[CollectionEvent] = {
    getValues.filter { x => x.participantId == participantId }.toSet
  }

}
