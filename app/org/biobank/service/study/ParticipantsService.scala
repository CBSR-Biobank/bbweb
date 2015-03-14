package org.biobank.service.study

import org.biobank.domain.study.ParticipantAnnotationType

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study._

import akka.actor._
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent._
import org.slf4j.LoggerFactory
import ExecutionContext.Implicits.global
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

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

class ParticipantsServiceImpl(implicit inj: Injector)
    extends ParticipantsService
    with AkkaInjectable {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val system = inject [ActorSystem]

  implicit val timeout = inject [Timeout] ('akkaTimeout)

  val processor = injectActorRef [ParticipantsProcessor] ("participants")

  val participantRepository = inject [ParticipantRepository]

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
