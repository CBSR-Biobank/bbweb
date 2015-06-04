package org.biobank.service.study

import org.biobank.domain.study.ParticipantAnnotationType

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
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

  def add(cmd: AddParticipantCmd)(implicit userId: UserId)
      : Future[DomainValidation[Participant]]

  def update(cmd: UpdateParticipantCmd)(implicit userId: UserId)
      : Future[DomainValidation[Participant]]

  /** Returns true if a participant with the 'uniqueId' does not exist in the system, false otherwise.
    */
  def checkUnique(uniqueId: String): DomainValidation[Boolean]

}

class ParticipantsServiceImpl @javaxInject() (@Named("participantsProcessor") val processor: ActorRef,
                                              val participantRepository: ParticipantRepository)
    extends ParticipantsService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  // TODO: add read side API

  def get(studyId: String, participantId: String): DomainValidation[Participant] = {
    participantRepository.withId(StudyId(studyId), ParticipantId(participantId))
  }

  def getByUniqueId(studyId: String, uniqueId: String): DomainValidation[Participant] = {
    participantRepository.withUniqueId(StudyId(studyId), uniqueId)
  }

  def add(cmd: AddParticipantCmd)(implicit userId: UserId)
      : Future[DomainValidation[Participant]] = {
    replyWithParticipant(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])
  }

  def update(cmd: UpdateParticipantCmd)(implicit userId: UserId)
      : Future[DomainValidation[Participant]] = {
    replyWithParticipant(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])
  }

  def checkUnique(uniqueId: String): DomainValidation[Boolean] = {
    val isUnique = ! participantRepository.getValues.exists(p => p.uniqueId == uniqueId)
    isUnique.success
  }

  private def replyWithParticipant(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[Participant]] = {
    future map { validation =>
      for {
        event <- validation
        pt <- {
          val pId = if (event.eventType.isParticipantAdded) {
            event.getParticipantAdded.getParticipantId
          } else {
            event.getParticipantUpdated.getParticipantId
          }
          participantRepository.getByKey(ParticipantId(pId))
        }
      } yield pt
    }
  }

}
