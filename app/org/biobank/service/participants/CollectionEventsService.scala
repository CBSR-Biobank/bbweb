package org.biobank.service.participants

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.CollectionEventCommands._
import org.biobank.infrastructure.event.CollectionEventEvents._
import org.biobank.infrastructure.{ SortOrder, AscendingOrder }
import org.biobank.service.{ServiceError, ServiceValidation}
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CollectionEventsServiceImpl])
trait CollectionEventsService {

  def get(collectionEventId: String): ServiceValidation[CollectionEvent]

  def list(participantId: String,
           sortFunc: (CollectionEvent, CollectionEvent) => Boolean,
           order:    SortOrder)
      : ServiceValidation[Seq[CollectionEvent]]

  def getByVisitNumber(participantId: String, visitNumber: Int): ServiceValidation[CollectionEvent]

  def processCommand(cmd: CollectionEventCommand): Future[ServiceValidation[CollectionEvent]]

  def processRemoveCommand(cmd: CollectionEventCommand): Future[ServiceValidation[Boolean]]

}

class CollectionEventsServiceImpl @Inject() (
  @Named("collectionEventsProcessor") val processor: ActorRef,
  val studyRepository:                               StudyRepository,
  val participantRepository:                         ParticipantRepository,
  val collectionEventRepository:                     CollectionEventRepository)
    extends CollectionEventsService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  def get(collectionEventId: String): ServiceValidation[CollectionEvent] = {
    collectionEventRepository.getByKey(CollectionEventId(collectionEventId)).leftMap(_ =>
      ServiceError(s"collection event id is invalid: $collectionEventId")).toValidationNel
  }

  def list(participantId: String,
           sortFunc:      (CollectionEvent, CollectionEvent) => Boolean,
           order:         SortOrder)
      : ServiceValidation[Seq[CollectionEvent]] = {
    validParticipantId(participantId) { participant =>
      val result = collectionEventRepository
        .allForParticipant(ParticipantId(participantId))
        .toSeq
        .sortWith(sortFunc)

      if (order == AscendingOrder) {
        result.successNel[String]
      } else {
        result.reverse.successNel[String]
      }
    }
  }

  def getByVisitNumber(participantId: String, visitNumber: Int)
      : ServiceValidation[CollectionEvent] = {
    validParticipantId(participantId) { participant =>
      collectionEventRepository.withVisitNumber(ParticipantId(participantId), visitNumber)
    }
  }

  private def validParticipantId[T](participantId: String)(fn: Participant => ServiceValidation[T])
      : ServiceValidation[T] = {
    for {
      participant <- participantRepository.getByKey(ParticipantId(participantId))
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
