package org.biobank.domain.participants

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import org.biobank.TestData
import org.biobank.domain._
import org.biobank.domain.study.CollectionEventTypeId
import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CollectionEventRepositoryImpl])
trait CollectionEventRepository
    extends ReadWriteRepositoryWithSlug[CollectionEventId, CollectionEvent] {

  def withId(participantId: ParticipantId, collectionEventId: CollectionEventId)
      : DomainValidation[CollectionEvent]

  def collectionEventTypeInUse(collectionEventTypeId: CollectionEventTypeId): Boolean

  def withVisitNumber(participantId: ParticipantId, visitNumber: Int): DomainValidation[CollectionEvent]

  def allForParticipant(participantId: ParticipantId): Set[CollectionEvent]

}

@Singleton
class CollectionEventRepositoryImpl @Inject() (val testData: TestData)
    extends ReadWriteRepositoryRefImplWithSlug[CollectionEventId, CollectionEvent](v => v.id)
    with CollectionEventRepository {
  import org.biobank.CommonValidations._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  override def init(): Unit = {
    super.init()
    testData.testEvents.foreach(put)
  }

  def nextIdentity: CollectionEventId = new CollectionEventId(nextIdentityAsString)

  protected def notFound(id: CollectionEventId): IdNotFound = IdNotFound(s"collection event id: $id")

  protected def slugNotFound(slug: String): EntityCriteriaNotFound =
    EntityCriteriaNotFound(s"collection event slug: $slug")

  def withId(participantId: ParticipantId, collectionEventId: CollectionEventId)
      : DomainValidation[CollectionEvent] = {
    for {
      cevent <- getByKey(collectionEventId)
      valid  <- {
        if (cevent.participantId != participantId) {
          EntityCriteriaError(
            s"collection event invalid for participant : { participantId: $participantId, collectionEventId: $collectionEventId }"
          ).failureNel[CollectionEvent]
        } else {
          cevent.successNel[String]
        }
      }
    } yield valid
  }

  def collectionEventTypeInUse(collectionEventTypeId: CollectionEventTypeId): Boolean = {
    getValues.find { event => event.collectionEventTypeId == collectionEventTypeId } match {
      case Some(cevent) => true
      case _ => false
    }
  }

  def withVisitNumber(participantId: ParticipantId,
                      visitNumber:   Int): DomainValidation[CollectionEvent] = {
    internalMap.single.get
      .find { case (id, cevent) =>
        (cevent.visitNumber == visitNumber) && (cevent.participantId == participantId)
      }
      .map { case (id, cevent) => cevent }
      .toSuccessNel(
        DomainError(
          s"collection event does not exist: participantId/$participantId, visitNumber/$visitNumber }"
        ).toString)
  }

  def allForParticipant(participantId: ParticipantId): Set[CollectionEvent] = {
    getValues.filter { _.participantId == participantId }.toSet
  }

}
