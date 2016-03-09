package org.biobank.service.participants

import org.biobank.infrastructure.{ SortOrder, AscendingOrder }
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.CollectionEventEvents._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.domain.participants._

import javax.inject.{Inject, Named}
import com.google.inject.ImplementedBy
import akka.actor._
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CollectionEventsServiceImpl])
trait CollectionEventsService {

  def get(collectionEventId: String): DomainValidation[CollectionEvent]

  def list(participantId: String,
           sortFunc: (CollectionEvent, CollectionEvent) => Boolean,
           order:    SortOrder)
      : DomainValidation[Seq[CollectionEvent]]

  def getByVisitNumber(participantId: String, visitNumber: Int): DomainValidation[CollectionEvent]

  def processCommand(cmd: ParticipantCommand): Future[DomainValidation[CollectionEvent]]

  def processRemoveCommand(cmd: ParticipantCommand): Future[DomainValidation[Boolean]]

}

class CollectionEventsServiceImpl @Inject() (
  @Named("collectionEventsProcessor") val processor: ActorRef,
  val studyRepository:                               StudyRepository,
  val participantRepository:                         ParticipantRepository,
  val collectionEventRepository:                     CollectionEventRepository)
    extends CollectionEventsService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  def get(collectionEventId: String): DomainValidation[CollectionEvent] = {
    collectionEventRepository.getByKey(CollectionEventId(collectionEventId)).leftMap(_ =>
      DomainError(s"collection event id is invalid: $collectionEventId")).toValidationNel
  }

  def list(participantId: String,
           sortFunc: (CollectionEvent, CollectionEvent) => Boolean,
           order:    SortOrder)
      : DomainValidation[Seq[CollectionEvent]] = {
    validParticipantId(participantId) { participant =>
      val result = collectionEventRepository
        .allForParticipant(ParticipantId(participantId))
        .toSeq
        .sortWith(sortFunc)

      if (order == AscendingOrder) {
        result.success
      } else {
        result.reverse.success
      }
    }
  }

  def getByVisitNumber(participantId: String, visitNumber: Int)
      : DomainValidation[CollectionEvent] = {
    validParticipantId(participantId) { participant =>
      collectionEventRepository.withVisitNumber(ParticipantId(participantId), visitNumber)
    }
  }

  private def validParticipantId[T](participantId: String)(fn: Participant => DomainValidation[T])
      : DomainValidation[T] = {
    participantRepository.getByKey(ParticipantId(participantId)).fold(
      err => DomainError(s"invalid participant id: $participantId").failureNel,
      participant => {
        studyRepository.getByKey(participant.studyId).fold(
          err => DomainError(s"invalid study id: ${participant.studyId}").failureNel,
          study => fn(participant)
        )
      }
    )
  }

  def processCommand(cmd: ParticipantCommand): Future[DomainValidation[CollectionEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[CollectionEventEvent]].map { validation =>
      for {
        event  <- validation
        cevent <- collectionEventRepository.getByKey(CollectionEventId(event.id))
      } yield cevent
    }

  def processRemoveCommand(cmd: ParticipantCommand): Future[DomainValidation[Boolean]] =
    ask(processor, cmd).mapTo[DomainValidation[CollectionEventEvent]].map { validation =>
      for {
        event  <- validation
        result <- true.success
      } yield result
    }

}
