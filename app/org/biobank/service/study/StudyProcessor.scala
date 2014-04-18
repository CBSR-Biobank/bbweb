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
      case event: StudyAddedEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.studies.foreach{ study => studyRepository.put(study) }
    }

    val receiveCommand: Receive = {
      case cmd: AddStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: UpdateStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: EnableStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: DisableStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

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

    private def validateCmd(cmd: AddStudyCmd): DomainValidation[StudyAddedEvent] = {
      val studyId = studyRepository.nextIdentity

      for {
        nameAvailable <- studyRepository.nameAvailable(cmd.name)
        newStudy <- DisabledStudy.create(studyId, -1L, cmd.name, cmd.description)
        event <- StudyAddedEvent(newStudy.id.toString, newStudy.name, newStudy.description).success
       } yield event
    }


    private def validateCmd(cmd: UpdateStudyCmd): DomainValidation[StudyUpdatedEvent] = {
      val studyId = StudyId(cmd.id)
      for {
	prevStudy <- isStudyDisabled(studyId)
        updatedStudy <- prevStudy.update(cmd.expectedVersion, cmd.name, cmd.description)
        event <- StudyUpdatedEvent(cmd.id, updatedStudy.version, updatedStudy.name, updatedStudy.description).success
      } yield event
    }

    private def validateCmd(cmd: EnableStudyCmd): DomainValidation[StudyEnabledEvent] = {
      val studyId = StudyId(cmd.id)
      for {
	disabledStudy <- isStudyDisabled(studyId)
        enabledStudy <- disabledStudy.enable(cmd.expectedVersion)
        event <- StudyEnabledEvent(studyId.id, enabledStudy.version).success
      } yield event
    }

    private def validateCmd(cmd: DisableStudyCmd): DomainValidation[StudyDisabledEvent] = {
      val studyId = StudyId(cmd.id)
      for {
	enabledStudy <- isStudyEnabled(studyId)
        disabledStudy <- enabledStudy.disable(cmd.expectedVersion)
        event <- StudyDisabledEvent(cmd.id, disabledStudy.version).success
      } yield event
    }

    private def validateCmd(cmd: RetireStudyCmd): DomainValidation[StudyRetiredEvent] = {
      val studyId = StudyId(cmd.id)
      for {
	disabledStudy <- isStudyDisabled(studyId)
        retiredStudy <- disabledStudy.retire(cmd.expectedVersion)
        event <- StudyRetiredEvent(cmd.id, retiredStudy.version).success
      } yield event
    }

    private def validateCmd(cmd: UnetireStudyCmd): DomainValidation[StudyUnretiredEvent] = {
      val studyId = StudyId(cmd.id)
      for {
	retiredStudy <- isStudyRetired(studyId)
        disabledStudy <- retiredStudy.unretire(cmd.expectedVersion)
        event <- StudyUnretiredEvent(studyId.id, disabledStudy.version).success
      } yield event
    }

    private def recoverEvent(event: StudyAddedEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
	study <- DisabledStudy.create(studyId, -1L, event.name, event.description)
	savedStudy <- studyRepository.put(study).success
      } yield study

      if (validation.isFailure) {
        // this should never happen because the only way to get here is that the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyUpdatedEvent) {
      val studyId = StudyId(event.id)

      val validation = for {
	study <- DisabledStudy.create(studyId, -1L, event.name, event.description)
	savedStudy <- studyRepository.put(study).success
      } yield study

      if (validation.isFailure) {
	// this should never happen because the only way to get here is that the
	// command passed validation
	throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyEnabledEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
	disabledStudy <- isStudyDisabled(studyId)
	enabledStudy <- disabledStudy.enable(disabledStudy.versionOption)
	savedStudy <- studyRepository.put(enabledStudy).success
      } yield  enabledStudy

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
	disabledStudy <- enabledStudy.disable(enabledStudy.versionOption)
	savedStudy <- studyRepository.put(disabledStudy).success
      } yield disabledStudy

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
	retiredStudy <- disabledStudy.retire(disabledStudy.versionOption)
	savedStudy <- studyRepository.put(retiredStudy).success
      } yield retiredStudy

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
	disabledStudy <- retiredStudy.unretire(retiredStudy.versionOption)
	savedstudy <- studyRepository.put(disabledStudy).success
      } yield disabledStudy

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
