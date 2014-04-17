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

import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
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
      case event: StudyAddedEvent =>
        recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.studies.foreach{ study => studyRepository.put(study) }
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

    private def addStudy(cmd: AddStudyCmd): DomainValidation[StudyAddedEvent] = {
      val studyId = studyRepository.nextIdentity

      val validation = for {
        nameAvailable <- studyRepository.nameAvailable(cmd.name)
        newStudy <- DisabledStudy.create(studyId, -1L, cmd.name, cmd.description)
        event <- StudyAddedEvent(newStudy.id.toString, newStudy.name, newStudy.description).success
       } yield {
        persist(event) { e =>
	  studyRepository.put(newStudy)
	  sender ! e
	}
        event
      }

      if (validation.isFailure) {
	sender ! validation
      }
    }


    private def updateStudy(cmd: UpdateStudyCmd): DomainValidation[StudyUpdatedEvent] = {
      val studyId = StudyId(cmd.id)
      for {
	prevStudy <- isStudyDisabled(studyId)
        updatedStudy <- prevStudy.update(
          cmd.expectedVersion.getOrElse(-1), cmd.name, cmd.description)
        event <- StudyUpdatedEvent(cmd.id, updatedStudy.version, updatedStudy.name, updatedStudy.description).success
      } yield {
        persist(event) { e => studyRepository.put(updatedStudy) }
	event
      }
    }

    private def enableStudy(cmd: EnableStudyCmd): DomainValidation[StudyEnabledEvent] = {
      val studyId = StudyId(cmd.id)
      for {
	disabledStudy <- isStudyDisabled(studyId)
        enabledStudy <- disabledStudy.enable
        event <- StudyEnabledEvent(studyId.id, enabledStudy.version).success
      } yield {
        persist(event) { e => studyRepository.put(enabledStudy) }
	event
      }
    }

    private def disableStudy(cmd: DisableStudyCmd): DomainValidation[StudyDisabledEvent] = {
      val studyId = StudyId(cmd.id)
      for {
	enabledStudy <- isStudyEnabled(studyId)
        disabledStudy <- enabledStudy.disable
        event <- StudyDisabledEvent(cmd.id, disabledStudy.version).success
      } yield {
        persist(event) { e => studyRepository.put(disabledStudy) }
	event
      }
    }

    private def retireStudy(cmd: RetireStudyCmd): DomainValidation[StudyRetiredEvent] = {
      val studyId = StudyId(cmd.id)
      for {
	disabledStudy <- isStudyDisabled(studyId)
        retiredStudy <- disabledStudy.retire
        event <- StudyRetiredEvent(cmd.id, retiredStudy.version).success
      } yield {
        persist(event) { e => studyRepository.put(retiredStudy) }
	event
      }
    }

    private def unretireStudy(cmd: UnetireStudyCmd): DomainValidation[StudyUnretiredEvent] = {
      val studyId = StudyId(cmd.id)
      for {
	retiredStudy <- isStudyRetired(studyId)
        disabledStudy <- retiredStudy.unretire
        event <- StudyUnretiredEvent(studyId.id, disabledStudy.version).success
      } yield {
        persist(event) { e => studyRepository.put(disabledStudy) }
	event
      }
    }

    private def recoverEvent(event: StudyAddedEvent) {
      val studyId = StudyId(event.id)
      DisabledStudy.create(studyId, -1L, event.name, event.description) match {
        case Success(study) =>
	  studyRepository.put(study)

        case Failure(err) =>
          // this should never happen because the only way to get here is that the
          // command passed validation
          throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyUpdatedEvent) {
      val studyId = StudyId(event.id)
      DisabledStudy.create(studyId, -1L, event.name, event.description) match {
        case Success(study) =>
	  studyRepository.put(study)

        case Failure(err) =>
          // this should never happen because the only way to get here is that the
          // command passed validation
          throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyEnabledEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
	disabledStudy <- isStudyDisabled(studyId)
	enabledStudy <- disabledStudy.enable
      } yield {
	studyRepository.put(enabledStudy)
      }

      if (validation.isFailure) {
        // this should never happen because the only way to get here is that the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyDisabledEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
	enabledStudy <- isStudyEnabled(studyId)
	diabledStudy <- enabledStudy.disable
      } yield {
	studyRepository.put(diabledStudy)
      }

      if (validation.isFailure) {
        // this should never happen because the only way to get here is that the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyRetiredEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
	disabledStudy <- isStudyDisabled(studyId)
	retiredStudy <- disabledStudy.retire
      } yield {
	studyRepository.put(retiredStudy)
      }

      if (validation.isFailure) {
        // this should never happen because the only way to get here is that the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyUnretiredEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
	retiredStudy <- isStudyRetired(studyId)
	diabledStudy <- retiredStudy.unretire
      } yield {
	studyRepository.put(diabledStudy)
      }

      if (validation.isFailure) {
        // this should never happen because the only way to get here is that the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    /**
      * Utility method to validiate state of a study
      */
    private def isStudyDisabled(studyId: StudyId): DomainValidation[DisabledStudy] =
      studyRepository.studyWithId(studyId) match {
        case Failure(msglist) => DomainError(s"no study with id: $studyId").failNel
        case Success(study) => study match {
          case dstudy: DisabledStudy => dstudy.success
          case _ => DomainError("study is not disabled: ${study.name}").failNel
        }
      }

    /**
      * Utility method to validiate state of a study
      */
    private def isStudyEnabled(studyId: StudyId): DomainValidation[EnabledStudy] =
      studyRepository.studyWithId(studyId) match {
        case Failure(msglist) => DomainError(s"no study with id: $studyId").failNel
        case Success(study) => study match {
          case enabledStudy: EnabledStudy => enabledStudy.success
          case _ => DomainError("study is not enabled: ${study.name}").failNel
        }
      }

    /**
      * Utility method to validiate state of a study
      */
    private def isStudyRetired(studyId: StudyId): DomainValidation[RetiredStudy] =
      studyRepository.studyWithId(studyId) match {
        case Failure(msglist) => DomainError(s"no study with id: $studyId").failNel
        case Success(study) => study match {
          case retiredStudy: RetiredStudy => retiredStudy.success
          case _ => DomainError("study is not retired: ${study.name}").failNel
        }
      }
  }
}
