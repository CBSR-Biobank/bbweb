package org.biobank.service.participants

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.infrastructure.AscendingOrder
import org.biobank.infrastructure.command.CollectionEventCommands._
import org.biobank.infrastructure.event.CollectionEventEvents._
import org.biobank.service._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CollectionEventsServiceImpl])
trait CollectionEventsService extends BbwebService {

  def get(collectionEventId: String): ServiceValidation[CollectionEvent]

  def list(participantId: ParticipantId,
           filter:        FilterString,
           sort:          SortString): ServiceValidation[Seq[CollectionEvent]]

  def getByVisitNumber(participantId: ParticipantId, visitNumber: Int): ServiceValidation[CollectionEvent]

  def processCommand(cmd: CollectionEventCommand): Future[ServiceValidation[CollectionEvent]]

  def processRemoveCommand(cmd: CollectionEventCommand): Future[ServiceValidation[Boolean]]

}

class CollectionEventsServiceImpl @Inject() (
  @Named("collectionEventsProcessor") val processor: ActorRef,
  val studyRepository:                               StudyRepository,
  val participantRepository:                         ParticipantRepository,
  val collectionEventRepository:                     CollectionEventRepository)
    extends CollectionEventsService
    with BbwebServiceImpl {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def get(collectionEventId: String): ServiceValidation[CollectionEvent] = {
    collectionEventRepository.getByKey(CollectionEventId(collectionEventId)).leftMap(_ =>
      ServiceError(s"collection event id is invalid: $collectionEventId")).toValidationNel
  }

  def list(participantId: ParticipantId,
           filter:        FilterString,
           sort:          SortString): ServiceValidation[Seq[CollectionEvent]] = {
    val sortStr = if (sort.expression.isEmpty) new SortString("visitNumber")
                  else sort

    validParticipantId(participantId) { participant =>
      val allCevents = collectionEventRepository.allForParticipant(participantId).toSet

      for {
        cevents         <- CollectionEventFilter.filterCollectionEvents(allCevents, filter)
        sortExpressions <- { QuerySortParser(sortStr).
                              toSuccessNel(ServiceError(s"could not parse sort expression: $sort")) }
        firstSort       <- { sortExpressions.headOption.
                              toSuccessNel(ServiceError("at least one sort expression is required")) }
        sortFunc        <- { CollectionEvent.sort2Compare.get(firstSort.name).
                              toSuccessNel(ServiceError(s"invalid sort field: ${firstSort.name}")) }
      } yield {
        val result = cevents.toSeq.sortWith(sortFunc)
        if (firstSort.order == AscendingOrder) result
        else result.reverse
      }
    }
  }

  def getByVisitNumber(participantId: ParticipantId, visitNumber: Int)
      : ServiceValidation[CollectionEvent] = {
    validParticipantId(participantId) { participant =>
      collectionEventRepository.withVisitNumber(participantId, visitNumber)
    }
  }

  private def validParticipantId[T](participantId: ParticipantId)(fn: Participant => ServiceValidation[T])
      : ServiceValidation[T] = {
    for {
      participant <- participantRepository.getByKey(participantId)
      study       <- studyRepository.getByKey(participant.studyId)
      result      <- fn(participant)
    } yield result
  }

  def processCommand(cmd: CollectionEventCommand): Future[ServiceValidation[CollectionEvent]] =
    ask(processor, cmd).mapTo[ServiceValidation[CollectionEventEvent]].map { validation =>
      for {
        event  <- validation
        cevent <- collectionEventRepository.getByKey(CollectionEventId(event.id))
      } yield cevent
    }

  def processRemoveCommand(cmd: CollectionEventCommand): Future[ServiceValidation[Boolean]] =
    ask(processor, cmd).mapTo[ServiceValidation[CollectionEventEvent]].map { validation =>
      for {
        event  <- validation
        result <- true.successNel[String]
      } yield result
    }

}
