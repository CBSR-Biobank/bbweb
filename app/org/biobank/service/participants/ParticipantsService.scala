package org.biobank.service.participants

import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.ParticipantEvents._
import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study._

import javax.inject.{Inject => javaxInject, Named}
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

@ImplementedBy(classOf[ParticipantsServiceImpl])
trait ParticipantsService {

  def get(studyId: String, participantId: String): DomainValidation[Participant]

  def getByUniqueId(studyId: String, uniqueId: String): DomainValidation[Participant]

  def add(cmd: AddParticipantCmd): Future[DomainValidation[Participant]]

  def update(cmd: UpdateParticipantCmd): Future[DomainValidation[Participant]]

  /** Returns true if a participant with the 'uniqueId' does not exist in the system, false otherwise.
   */
  def checkUnique(uniqueId: String): DomainValidation[Boolean]

  //-- Collection Event

  def getCollectionEvents(participantId: String): DomainValidation[Set[CollectionEvent]]

  def getCollectionEvent(participantId: String, collectionEventId: String): DomainValidation[CollectionEvent]

  def getCollectionEventByVisitNumber(participantId: String, visitNumber: Int): DomainValidation[CollectionEvent]

  def addCollectionEvent(cmd: AddCollectionEventCmd)
      : Future[DomainValidation[CollectionEvent]]

  def updateCollectionEvent(cmd: UpdateCollectionEventCmd)
      : Future[DomainValidation[CollectionEvent]]

  def removeCollectionEvent(cmd: RemoveCollectionEventCmd)
      : Future[DomainValidation[Boolean]]
}

class ParticipantsServiceImpl @javaxInject() (
  @Named("participantsProcessor") val processor: ActorRef,

  val studyRepository:           StudyRepository,
  val participantRepository:     ParticipantRepository,
  val collectionEventRepository: CollectionEventRepository)
    extends ParticipantsService {

  import org.biobank.service.Utils._

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  // TODO: add read side API

  def get(studyId: String, participantId: String): DomainValidation[Participant] = {
    participantRepository.withId(StudyId(studyId), ParticipantId(participantId))
  }

  def getByUniqueId(studyId: String, uniqueId: String): DomainValidation[Participant] = {
    participantRepository.withUniqueId(StudyId(studyId), uniqueId)
  }

  def add(cmd: AddParticipantCmd)
      : Future[DomainValidation[Participant]] = {
    replyWithParticipant(ask(processor, cmd).mapTo[DomainValidation[ParticipantEvent]])
  }

  def update(cmd: UpdateParticipantCmd)
      : Future[DomainValidation[Participant]] = {
    replyWithParticipant(ask(processor, cmd).mapTo[DomainValidation[ParticipantEvent]])
  }

  //-- Collection Event

  def getCollectionEvents(participantId: String): DomainValidation[Set[CollectionEvent]] = {
    validParticipantId(participantId) { participant =>
      collectionEventRepository.allForParticipant(participant.id).successNel
    }
  }

  def getCollectionEvent(participantId: String, collectionEventId: String)
      : DomainValidation[CollectionEvent] =
    validParticipantId(participantId) { participant =>
      collectionEventRepository.withId(participant.id,
                                       CollectionEventId(collectionEventId))
    }

  def getCollectionEventByVisitNumber(participantId: String, visitNumber: Int)
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

  def addCollectionEvent(cmd: AddCollectionEventCmd)
      : Future[DomainValidation[CollectionEvent]] =
    replyWithCollectionEvent(ask(processor, cmd).mapTo[DomainValidation[ParticipantEvent]])

  def updateCollectionEvent(cmd: UpdateCollectionEventCmd)
      : Future[DomainValidation[CollectionEvent]] =
    replyWithCollectionEvent(ask(processor, cmd).mapTo[DomainValidation[ParticipantEvent]])

  def removeCollectionEvent(cmd: RemoveCollectionEventCmd)
      : Future[DomainValidation[Boolean]] =
    eventValidationToBoolean(ask(processor, cmd).mapTo[DomainValidation[ParticipantEvent]])

  def checkUnique(uniqueId: String): DomainValidation[Boolean] = {
    val isUnique = ! participantRepository.getValues.exists(p => p.uniqueId == uniqueId)
    isUnique.success
  }

  private def replyWithParticipant(future: Future[DomainValidation[ParticipantEvent]])
      : Future[DomainValidation[Participant]] = {
    future map { validation =>
      for {
        event <- validation
        pt    <- participantRepository.getByKey(ParticipantId(event.id))
      } yield pt
    }
  }

  private def replyWithCollectionEvent(future: Future[DomainValidation[ParticipantEvent]])
      : Future[DomainValidation[CollectionEvent]] = {
    future map { validation =>
      for {
        event <- validation
        cevent <- {
          val ceventId = if (event.eventType.isCollectionEventAdded) {
            event.getCollectionEventAdded.getCollectionEventId
          } else {
            event.getCollectionEventUpdated.getCollectionEventId
          }
          collectionEventRepository.getByKey(CollectionEventId(ceventId))
        }
      } yield cevent
    }
  }

}
