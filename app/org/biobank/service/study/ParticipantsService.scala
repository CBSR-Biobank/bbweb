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

trait ParticipantsService {

  def get(studyId: String, participantId: String): DomainValidation[Participant]

  def add(cmd: AddParticipantCmd)(implicit userId: UserId)
      : Future[DomainValidation[ParticipantAddedEvent]]

  def update(cmd: UpdateParticipantCmd)(implicit userId: UserId)
      : Future[DomainValidation[ParticipantUpdatedEvent]]

  /** Returns true if a participant with the ID does not exist in the system, false otherwise.
    */
  def checkUnique(id: String): DomainValidation[Boolean]

}

class ParticipantsServiceImpl(implicit inj: Injector)
    extends ParticipantsService
    with ApplicationService
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

  def add(cmd: AddParticipantCmd)(implicit userId: UserId)
      : Future[DomainValidation[ParticipantAddedEvent]] = {
    ask(processor, cmd, userId).map(
      _.asInstanceOf[DomainValidation[ParticipantAddedEvent]])
  }

  def update(cmd: UpdateParticipantCmd)(implicit userId: UserId)
      : Future[DomainValidation[ParticipantUpdatedEvent]] = {
    ask(processor, cmd, userId).map(
      _.asInstanceOf[DomainValidation[ParticipantUpdatedEvent]])
  }

  def checkUnique(id: String): DomainValidation[Boolean] = {
    participantRepository.getByKey(ParticipantId(id)).fold(
      err => true.success,
      participant => false.success
    )
  }
}
