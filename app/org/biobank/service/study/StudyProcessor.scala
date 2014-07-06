package org.biobank.service.study

import org.biobank.service.Processor
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  DomainValidation,
  DomainError,
  RepositoryComponent,
  RepositoryComponentImpl,
  UserId
}
import org.biobank.domain.study._
import org.biobank.domain.study.Study

import akka.actor. { ActorRef, Props }
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import scalaz._
import scalaz.Scalaz._

case class StudyMessage(cmd: Any, userId: UserId, time: Long)

trait StudyProcessorComponent
    extends CollectionEventTypeProcessorComponent
    with CeventAnnotationTypeProcessorComponent
    with SpecimenGroupProcessorComponent
    with ParticipantAnnotationTypeProcessorComponent
    with ProcessingTypeProcessorComponent
    with SpecimenLinkTypeProcessorComponent
    with SpecimenLinkAnnotationTypeProcessorComponent {
  self: RepositoryComponent =>

  /**
    * An actor that processes commands related to the [[org.biobank.domain.study.Study]] aggregate root.
    *
    * This implementation uses Akka persistence.
    */
  sealed class StudyProcessor extends Processor {

    case class SnapshotState(studies: Set[Study])

    val specimenGroupProcessor = context.system.actorOf(
      Props(new SpecimenGroupProcessor), "sgproc")

    val collectionEventTypeProcessor = context.actorOf(
      Props(new CollectionEventTypeProcessor), "cetproc")

    val ceventAnnotationTypeProcessor = context.actorOf(
      Props(new CeventAnnotationTypeProcessor), "ceatproc")

    val participantAnnotationTypeProcessor = context.actorOf(
      Props(new ParticipantAnnotationTypeProcessor), "partAnnotTypeProc")

    val processingTypeProcessor = context.actorOf(
      Props(new ProcessingTypeProcessor), "processingProc")

    val specimenLinkTypeProcessor = context.actorOf(
      Props(new SpecimenLinkTypeProcessor), "spcLinkTypeProc")

    val specimenLinkAnnotationTypeProcessor = context.actorOf(
      Props(new SpecimenLinkAnnotationTypeProcessor), "spcLinkAnnotTypeProc")

    val receiveRecover: Receive = {
      case event: StudyAddedEvent => recoverEvent(event)
      case event: StudyUpdatedEvent => recoverEvent(event)
      case event: StudyEnabledEvent => recoverEvent(event)
      case event: StudyDisabledEvent => recoverEvent(event)
      case event: StudyRetiredEvent => recoverEvent(event)
      case event: StudyUnretiredEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.studies.foreach{ study => studyRepository.put(study) }
    }

    val receiveCommand: Receive = {
      case cmd: AddStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: UpdateStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: EnableStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: DisableStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: RetireStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: UnretireStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: SpecimenGroupCommand => validateAndForward(specimenGroupProcessor, cmd)

      case cmd: CollectionEventTypeCommand => validateAndForward(collectionEventTypeProcessor, cmd)

      case cmd: CollectionEventAnnotationTypeCommand => validateAndForward(ceventAnnotationTypeProcessor, cmd)

      case cmd: ParticipantAnnotationTypeCommand => validateAndForward(participantAnnotationTypeProcessor, cmd)

      case cmd: ProcessingTypeCommand => validateAndForward(processingTypeProcessor, cmd)

      case cmd: SpecimenLinkTypeCommand => validateAndForward(specimenLinkTypeProcessor, cmd)

      case cmd: SpecimenLinkAnnotationTypeCommand => validateAndForward(specimenLinkAnnotationTypeProcessor,  cmd)

      case other =>
        DomainError("invalid command received")
        ()
    }

    private def validateAndForward(childActor: ActorRef, cmd: StudyCommandWithId) = {
      val studyId = StudyId(cmd.studyId)
      val validation = for {
        study <- studyRepository.getByKey(studyId)
        disabledStudy <- isStudyDisabled(studyId)
      } yield {
        childActor forward cmd
      }

      if (validation.isFailure) {
        context.sender ! validation
      }
    }

    private def validateAndForward(childActor: ActorRef, cmd: SpecimenLinkTypeCommand) = {
      val processingTypeId = ProcessingTypeId(cmd.processingTypeId)
      val validation = for {
        processingType <- processingTypeRepository.getByKey(processingTypeId)
        study <- studyRepository.getByKey(processingType.studyId)
        disabledStudy <- isStudyDisabled(study.id)
      } yield {
        childActor forward cmd
      }

      if (validation.isFailure) {
        context.sender ! validation
      }
    }

    private def validateCmd(cmd: AddStudyCmd): DomainValidation[StudyAddedEvent] = {
      val studyId = studyRepository.nextIdentity

      if (studyRepository.getByKey(studyId).isSuccess) {
        throw new IllegalStateException(s"study with id already exsits: $id")
      }

      for {
        nameAvailable <- nameAvailable(cmd.name)
        newStudy <- DisabledStudy.create(
          studyId, -1L, org.joda.time.DateTime.now, cmd.name, cmd.description)
        event <- StudyAddedEvent(
          newStudy.id.toString, newStudy.addedDate, newStudy.name, newStudy.description).success
       } yield event
    }


    private def validateCmd(cmd: UpdateStudyCmd): DomainValidation[StudyUpdatedEvent] = {
      val studyId = StudyId(cmd.id)
      for {
        nameAvailable <- nameAvailable(cmd.name, studyId)
        prevStudy <- isStudyDisabled(studyId)
        updatedStudy <- prevStudy.update(
         Some(cmd.expectedVersion), org.joda.time.DateTime.now, cmd.name, cmd.description)
        event <- StudyUpdatedEvent(
          cmd.id, updatedStudy.version, updatedStudy.lastUpdateDate.get, updatedStudy.name,
          updatedStudy.description).success
      } yield event
    }

    private def validateCmd(cmd: EnableStudyCmd): DomainValidation[StudyEnabledEvent] = {
      val studyId = StudyId(cmd.id)
      val specimenGroupCount = specimenGroupRepository.allForStudy(studyId).size
      val collectionEventtypeCount = collectionEventTypeRepository.allForStudy(studyId).size

      for {
        disabledStudy <- isStudyDisabled(studyId)
        enabledStudy <- disabledStudy.enable(
         Some(cmd.expectedVersion), org.joda.time.DateTime.now, specimenGroupCount, collectionEventtypeCount)
        event <- StudyEnabledEvent(
          studyId.id, enabledStudy.version, enabledStudy.lastUpdateDate.get).success
      } yield event
    }

    private def validateCmd(cmd: DisableStudyCmd): DomainValidation[StudyDisabledEvent] = {
      val studyId = StudyId(cmd.id)
      for {
        enabledStudy <- isStudyEnabled(studyId)
        disabledStudy <- enabledStudy.disable(Some(cmd.expectedVersion), org.joda.time.DateTime.now)
        event <- StudyDisabledEvent(
          cmd.id, disabledStudy.version, disabledStudy.lastUpdateDate.get).success
      } yield event
    }

    private def validateCmd(cmd: RetireStudyCmd): DomainValidation[StudyRetiredEvent] = {
      val studyId = StudyId(cmd.id)
      for {
        disabledStudy <- isStudyDisabled(studyId)
        retiredStudy <- disabledStudy.retire(Some(cmd.expectedVersion), org.joda.time.DateTime.now)
        event <- StudyRetiredEvent(
          cmd.id, retiredStudy.version, retiredStudy.lastUpdateDate.get).success
      } yield event
    }

    private def validateCmd(cmd: UnretireStudyCmd): DomainValidation[StudyUnretiredEvent] = {
      val studyId = StudyId(cmd.id)
      for {
        retiredStudy <- isStudyRetired(studyId)
        unretiredStudy <- retiredStudy.unretire(Some(cmd.expectedVersion), org.joda.time.DateTime.now)
        event <- StudyUnretiredEvent(
          studyId.id, unretiredStudy.version, unretiredStudy.lastUpdateDate.get).success
      } yield event
    }

    private def recoverEvent(event: StudyAddedEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
        study <- DisabledStudy.create(
          studyId, -1L, event.dateTime, event.name, event.description)
        savedStudy <- studyRepository.put(study).success
      } yield study

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyUpdatedEvent) {
      val validation = for {
        disabledStudy <- isStudyDisabled(StudyId(event.id))
        updatedStudy <- disabledStudy.update(
          disabledStudy.versionOption, event.dateTime, event.name, event.description)
        savedStudy <- studyRepository.put(updatedStudy).success
      } yield savedStudy

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyEnabledEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
        disabledStudy <- isStudyDisabled(studyId)
        enabledStudy <- disabledStudy.enable(
          disabledStudy.versionOption, event.dateTime, 1, 1)
        savedStudy <- studyRepository.put(enabledStudy).success
      } yield  enabledStudy

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyDisabledEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
        enabledStudy <- isStudyEnabled(studyId)
        disabledStudy <- enabledStudy.disable(enabledStudy.versionOption, event.dateTime)
        savedStudy <- studyRepository.put(disabledStudy).success
      } yield disabledStudy

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyRetiredEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
        disabledStudy <- isStudyDisabled(studyId)
        retiredStudy <- disabledStudy.retire(disabledStudy.versionOption, event.dateTime)
        savedStudy <- studyRepository.put(retiredStudy).success
      } yield retiredStudy

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    private def recoverEvent(event: StudyUnretiredEvent) {
      val studyId = StudyId(event.id)
      val validation = for {
        retiredStudy <- isStudyRetired(studyId)
        disabledStudy <- retiredStudy.unretire(retiredStudy.versionOption, event.dateTime)
        savedstudy <- studyRepository.put(disabledStudy).success
      } yield disabledStudy

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering study from event failed")
      }
    }

    /**
      * Utility method to validiate state of a study
      */
    private def isStudyDisabled(studyId: StudyId): DomainValidation[DisabledStudy] = {
      studyRepository.getByKey(studyId).fold(
        err => DomainError(s"no study with id: $studyId").failNel,
        study => study match {
          case dstudy: DisabledStudy => dstudy.success
          case _ => DomainError(s"study is not disabled: ${study.name}").failNel
        }
      )
    }

    /**
      * Utility method to validiate state of a study
      */
    private def isStudyEnabled(studyId: StudyId): DomainValidation[EnabledStudy] = {
      studyRepository.getByKey(studyId).fold(
        err => DomainError(s"no study with id: $studyId").failNel,
        study => study match {
          case enabledStudy: EnabledStudy => enabledStudy.success
          case _ => DomainError(s"study is not enabled: ${study.name}").failNel
        }
      )
    }

    /**
      * Utility method to validiate state of a study
      */
    private def isStudyRetired(studyId: StudyId): DomainValidation[RetiredStudy] = {
      studyRepository.getByKey(studyId).fold(
        err => DomainError(s"no study with id: $studyId").failNel,
        study => study match {
          case retiredStudy: RetiredStudy => retiredStudy.success
          case _ => DomainError(s"study is not retired: ${study.name}").failNel
        }
      )
    }

    private def nameAvailable(name: String): DomainValidation[Boolean] = {
      val exists = studyRepository.getValues.exists { item =>
        item.name.equals(name)
      }

      if (exists) {
        DomainError(s"study with name already exists: $name").failNel
      } else {
        true.successNel
      }
    }

    private def nameAvailable(name: String, excludeStudyId: StudyId): DomainValidation[Boolean] = {
      val exists = studyRepository.getValues.exists { item =>
        item.name.equals(name) && (item.id != excludeStudyId)
      }

      if (exists) {
        DomainError(s"study with name already exists: $name").failNel
      } else {
        true.successNel
      }
    }
  }
}
