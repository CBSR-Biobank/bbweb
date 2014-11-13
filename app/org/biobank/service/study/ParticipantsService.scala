package org.biobank.service.study

import org.biobank.domain.study.ParticipantAnnotationType

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.study._

import scala.concurrent._

trait ParticipantsService {

  def get(studyId: String, participantId: String): DomainValidation[Participant]

  def add(cmd: AddParticipantCmd): Future[DomainValidation[ParticipantAddedEvent]]

  def update(cmd: UpdateParticipantCmd): Future[DomainValidation[ParticipantUpdatedEvent]]

}
