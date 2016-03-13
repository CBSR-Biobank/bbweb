package org.biobank.service.participants

import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.ParticipantEvents._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.domain.participants._

import javax.inject.{Inject, Named, Singleton}
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

  def processCommand(cmd: ParticipantCommand): Future[DomainValidation[Participant]]

}

@Singleton
class ParticipantsServiceImpl @Inject() (
  @Named("participantsProcessor") val processor: ActorRef,
  val studyRepository:                           StudyRepository,
  val participantRepository:                     ParticipantRepository,
  val collectionEventRepository:                 CollectionEventRepository)
    extends ParticipantsService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  def get(studyId: String, participantId: String): DomainValidation[Participant] = {
    participantRepository.withId(StudyId(studyId), ParticipantId(participantId))
  }

  def getByUniqueId(studyId: String, uniqueId: String): DomainValidation[Participant] = {
    participantRepository.withUniqueId(StudyId(studyId), uniqueId)
  }

  def processCommand(cmd: ParticipantCommand): Future[DomainValidation[Participant]] =
    ask(processor, cmd).mapTo[DomainValidation[ParticipantEvent]].map { validation =>
      for {
        event       <- validation
        participant <- participantRepository.getByKey(ParticipantId(event.id))
      } yield participant
    }

}
