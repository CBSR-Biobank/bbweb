package org.biobank.service.study

import org.biobank.service.Processor
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service.Messages._

import org.biobank.domain.{
  DomainValidation,
  DomainError,
  RepositoryComponent,
  UserId
}

import org.biobank.domain.study._
import org.biobank.domain.study.Study

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps
import org.slf4j.LoggerFactory
import akka.persistence._

import scalaz._
import scalaz.Scalaz._

case class StudyMessage(cmd: Any, userId: UserId, time: Long)

trait StudyProcessorComponent {

  trait StudyProcessor extends Processor

}

case class SnapshotState(studies: Set[Study])

trait StudyProcessorComponentImpl extends StudyProcessorComponent {
  self: RepositoryComponent =>

  /**
   * Handles the commands to configure studies.
   */
  class StudyProcessorImpl extends StudyProcessor {

    val receiveRecover: Receive = {
      case event: StudyEvent =>
        updateState(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.studies.foreach{ study => studyRepository.update(study) }
    }

    val receiveCommand: Receive = {
      case cmd: AddStudyCmd => addStudy(cmd)

      case cmd: UpdateStudyCmd => updateStudy(cmd)

      case cmd: EnableStudyCmd => enableStudy(cmd)

      case cmd: DisableStudyCmd => disableStudy(cmd)

      // case cmd: SpecimenGroupCommand =>
      //   processEntityMsg(cmd, cmd.studyId, specimenGroupService.process)

      // case cmd: CollectionEventTypeCommand =>
      //   processEntityMsg(cmd, cmd.studyId, collectionEventTypeService.process)

      // case cmd: CollectionEventAnnotationTypeCommand =>
      //   processEntityMsg(cmd, cmd.studyId, ceventAnnotationTypeService.process)

      // case cmd: ParticipantAnnotationTypeCommand =>
      //   processEntityMsg(cmd, cmd.studyId, participantAnnotationTypeService.process)

      // case cmd: SpecimenLinkAnnotationTypeCommand =>
      //   processEntityMsg(cmd, cmd.studyId, specimenLinkAnnotationTypeService.process)

      case other => // must be for another command handler
    }

    def updateState(event: StudyEvent) = {
      event match {
        case event: StudyAddedEvent =>
	  DisabledStudy.create(StudyId(event.id), -1L, event.name, event.description) match {
            case Success(study) =>
              studyRepository.add(study)
              log.info(s"updateState: study added to repository: ${study.name}")

            case Failure(x) =>
              // this should never happen because the event gets generated after all
	      // validation passes
              throw new IllegalStateException("adding study from event failed")
	  }

        case event: StudyUpdatedEvent =>
	  DisabledStudy.create(StudyId(event.id), event.version, event.name, event.description) match {
            case Success(study) =>
              studyRepository.update(study)
              log.info(s"updateState: study updated in repository: ${study.name}")

            case Failure(x) =>
              // this should never happen because the event gets generated after all
	      // validation passes
              throw new IllegalStateException("updating study from event failed")
	  }

	case _ =>
	  log.error(s"updateState: event not handled yet: $event")
      }
    }

    private def validateStudy(studyId: StudyId): DomainValidation[DisabledStudy] =
      studyRepository.studyWithId(studyId) match {
        case Failure(msglist) => DomainError(s"no study with id: $studyId").failNel
        case Success(study) => study match {
          case dstudy: DisabledStudy => dstudy.success
          case _ => DomainError("study is not disabled: ${study.name}").failNel
        }
      }

    private def processEntityMsg[T](
      cmd: StudyCommand,
      studyId: String,
      processFunc: StudyProcessorMsg => DomainValidation[T]): DomainValidation[T] = {
      for {
        study <- validateStudy(new StudyId(studyId))
        event <- processFunc(StudyProcessorMsg(cmd, study))
      } yield event
    }

    private def addStudy(cmd: AddStudyCmd): DomainValidation[StudyAddedEvent] = {

      val studyId = studyRepository.nextIdentity

      val validation = for {
        nameAvailable <- studyRepository.nameAvailable(cmd.name)
        newStudy <- DisabledStudy.create(studyId, -1L, cmd.name, cmd.description)
        event <- StudyAddedEvent(newStudy.id.toString, newStudy.name, newStudy.description).successNel
      } yield {
        persist(event) { e => context.system.eventStream.publish(e) }
        event
      }
      sender ! validation
      validation
    }

    private def updateStudy(cmd: UpdateStudyCmd): DomainValidation[StudyUpdatedEvent] = {
      for {
        study <- DisabledStudy.create(
          new StudyId(cmd.id), cmd.expectedVersion.getOrElse(-1), cmd.name, cmd.description)
        newItem <- studyRepository.update(study)
        event <- StudyUpdatedEvent(newItem.id.id, newItem.version, newItem.name, newItem.description).success
      } yield event
    }

    private def enableStudy(cmd: EnableStudyCmd): DomainValidation[StudyEnabledEvent] = {
      val studyId = StudyId(cmd.id)
      for {
        study <- studyRepository.enable(studyId,
          specimenGroupRepository.allSpecimenGroupsForStudy(studyId).size,
          collectionEventTypeRepository.allCollectionEventTypesForStudy(studyId).size)
        event <- StudyEnabledEvent(studyId.id, study.version).success
      } yield event
    }

    private def disableStudy(cmd: DisableStudyCmd): DomainValidation[StudyDisabledEvent] = {
      val studyId = StudyId(cmd.id)
      for {
        study <- studyRepository.disable(studyId)
        event <- StudyDisabledEvent(studyId.id, study.version).success
      } yield event
    }
  }
}
