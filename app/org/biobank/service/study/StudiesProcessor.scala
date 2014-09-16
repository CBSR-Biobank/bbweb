package org.biobank.service.study

import org.biobank.service.Processor
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  DomainValidation,
  DomainError,
  RepositoriesComponent,
  RepositoriesComponentImpl
}
import org.biobank.domain.user.UserId
import org.biobank.domain.study._

import akka.actor. { ActorRef, Props }
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

trait StudiesProcessorComponent
    extends CollectionEventTypeProcessorComponent
    with CeventAnnotationTypeProcessorComponent
    with SpecimenGroupProcessorComponent
    with ParticipantAnnotationTypeProcessorComponent
    with ProcessingTypeProcessorComponent
    with SpecimenLinkTypeProcessorComponent
    with SpecimenLinkAnnotationTypeProcessorComponent {
  self: RepositoriesComponent =>

  /**
    * An actor that processes commands related to the [[org.biobank.domain.study.Study]] aggregate root.
    *
    * This implementation uses Akka persistence.
    */
  sealed class StudiesProcessor extends Processor {

    override def persistenceId = "study-processor-id"

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
      case event: StudyAddedEvent =>     recoverEvent(event)
      case event: StudyUpdatedEvent =>   recoverEvent(event)
      case event: StudyEnabledEvent =>   recoverEvent(event)
      case event: StudyDisabledEvent =>  recoverEvent(event)
      case event: StudyRetiredEvent =>   recoverEvent(event)
      case event: StudyUnretiredEvent => recoverEvent(event)


      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.studies.foreach{ study => studyRepository.put(study) }
    }

    val receiveCommand: Receive = {
      case cmd: AddStudyCmd =>      process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: UpdateStudyCmd =>   process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: EnableStudyCmd =>   process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: DisableStudyCmd =>  process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: RetireStudyCmd =>   process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: UnretireStudyCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: SpecimenGroupCommand =>                 validateAndForward(specimenGroupProcessor, cmd)
      case cmd: CollectionEventTypeCommand =>           validateAndForward(collectionEventTypeProcessor, cmd)
      case cmd: CollectionEventAnnotationTypeCommand => validateAndForward(ceventAnnotationTypeProcessor, cmd)
      case cmd: ParticipantAnnotationTypeCommand =>     validateAndForward(participantAnnotationTypeProcessor, cmd)
      case cmd: ProcessingTypeCommand =>                validateAndForward(processingTypeProcessor, cmd)
      case cmd: SpecimenLinkTypeCommand =>              validateAndForward(specimenLinkTypeProcessor, cmd)
      case cmd: SpecimenLinkAnnotationTypeCommand =>    validateAndForward(specimenLinkAnnotationTypeProcessor,  cmd)

      case other =>
        DomainError("invalid command received")
        ()
    }

    private def validateAndForward(childActor: ActorRef, cmd: StudyCommandWithId) = {
      studyRepository.getDisabled(StudyId(cmd.id)).fold(
        err => context.sender ! err,
        study => childActor forward cmd
      )
    }

    private def validateAndForward(childActor: ActorRef, cmd: SpecimenLinkTypeCommand) = {
      val processingTypeId = ProcessingTypeId(cmd.processingTypeId)
      val validation = for {
        processingType <- processingTypeRepository.getByKey(processingTypeId)
        study <- studyRepository.getDisabled(processingType.studyId)
      } yield study

      validation.fold(
        err => context.sender ! err,
        study => childActor forward cmd
      )
    }

    def updateStudy[T <: Study](cmd: StudyCommand)(fn: Study => DomainValidation[T]): DomainValidation[T] = {
      studyRepository.getByKey(StudyId(cmd.id)).fold(
        err => s"study for $cmd does not exist".failNel,
        study => study.requireVersion(study, cmd.expectedVersion).fold(
          err => err.failure,
          validStudy => fn(validStudy)
        )
      )
    }

    def updateDisabled[T <: Study](cmd: StudyCommand)(fn: DisabledStudy => DomainValidation[T]): DomainValidation[T] = {
      updateStudy(cmd) {
        case study: DisabledStudy => fn(study)
        case study => s"$study for $cmd is not disabled".failNel
      }
    }

    def updateEnabled[T <: Study](cmd: StudyCommand)(fn: EnabledStudy => DomainValidation[T]): DomainValidation[T] = {
      updateStudy(cmd) {
        case study: EnabledStudy => fn(study)
        case study => s"$study for $cmd is not enabled".failNel
      }
    }

    def updateRetired[T <: Study](cmd: StudyCommand)(fn: RetiredStudy => DomainValidation[T]): DomainValidation[T] = {
      updateStudy(cmd) {
        case study: RetiredStudy => fn(study)
        case study => s"$study for $cmd is not retired".failNel
      }
    }

    private def validateCmd(cmd: AddStudyCmd): DomainValidation[StudyAddedEvent] = {
      val studyId = studyRepository.nextIdentity

      if (studyRepository.getByKey(studyId).isSuccess) {
        throw new IllegalStateException(s"study with id already exsits: $studyId")
      }

      for {
        nameAvailable <- nameAvailable(cmd.name)
        newStudy <- DisabledStudy.create(
          studyId, -1L, org.joda.time.DateTime.now, cmd.name, cmd.description)
        event <- StudyAddedEvent(
          newStudy.id.id, newStudy.addedDate, newStudy.name, newStudy.description).success
       } yield event
    }

    private def validateCmd(cmd: UpdateStudyCmd): DomainValidation[StudyUpdatedEvent] = {
      val timeNow = DateTime.now
      val v = updateDisabled(cmd) { s =>
        for {
          nameAvailable <- nameAvailable(cmd.name, StudyId(cmd.id))
          updatedStudy <- s.update(cmd.name, cmd.description)
        } yield updatedStudy
      }
      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        study => StudyUpdatedEvent(cmd.id, study.version, timeNow, study.name, study.description).success
      )
    }

    private def validateCmd(cmd: EnableStudyCmd): DomainValidation[StudyEnabledEvent] = {
      val timeNow = DateTime.now
      val studyId = StudyId(cmd.id)
      val specimenGroupCount = specimenGroupRepository.allForStudy(studyId).size
      val collectionEventtypeCount = collectionEventTypeRepository.allForStudy(studyId).size
      val v = updateDisabled(cmd) { s => s.enable(specimenGroupCount, collectionEventtypeCount) }
      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        study => StudyEnabledEvent(cmd.id, study.version, timeNow).success
      )
    }

    private def validateCmd(cmd: DisableStudyCmd): DomainValidation[StudyDisabledEvent] = {
      val timeNow = DateTime.now
      val v = updateEnabled(cmd) { s => s.disable }
      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        study => StudyDisabledEvent(cmd.id, study.version, timeNow).success
      )
    }

    private def validateCmd(cmd: RetireStudyCmd): DomainValidation[StudyRetiredEvent] = {
      val timeNow = DateTime.now
      val v = updateDisabled(cmd) { s => s.retire }
      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        study => StudyRetiredEvent(cmd.id, study.version, timeNow).success
      )
    }

    private def validateCmd(cmd: UnretireStudyCmd): DomainValidation[StudyUnretiredEvent] = {
      val timeNow = DateTime.now
      val v = updateRetired(cmd) { s => s.unretire }
      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        study => StudyUnretiredEvent(cmd.id, study.version, timeNow).success
      )
    }

    private def recoverEvent(event: StudyAddedEvent) {
      studyRepository.put(DisabledStudy(
        StudyId(event.id), 0L, event.dateTime, None, event.name, event.description))
      ()
    }

    private def recoverEvent(event: StudyUpdatedEvent) {
      studyRepository.getDisabled(StudyId(event.id)).fold(
        err => throw new IllegalStateException(s"updating study from event failed: $err"),
        s => studyRepository.put(s.copy(
          version = event.version, name = event.name, description = event.description,
          lastUpdateDate = Some(event.dateTime)))
      )
      ()
    }

    private def recoverEvent(event: StudyEnabledEvent) {
      studyRepository.getDisabled(StudyId(event.id)).fold(
        err => throw new IllegalStateException(s"enabling study from event failed: $err"),
        s => studyRepository.put(EnabledStudy(s.id, event.version, s.addedDate, Some(event.dateTime),
          s.name, s.description))
      )
      ()
    }

    private def recoverEvent(event: StudyDisabledEvent) {
      studyRepository.getEnabled(StudyId(event.id)).fold(
        err => throw new IllegalStateException(s"disabling study from event failed: $err"),
        s => studyRepository.put(DisabledStudy(s.id, event.version, s.addedDate, Some(event.dateTime),
          s.name, s.description))
      )
      ()
    }

    private def recoverEvent(event: StudyRetiredEvent) {
      studyRepository.getDisabled(StudyId(event.id)).fold(
        err => throw new IllegalStateException(s"retiring study from event failed: $err"),
        s => studyRepository.put(RetiredStudy(s.id, event.version, s.addedDate, Some(event.dateTime),
          s.name, s.description))
      )
      ()
    }

    private def recoverEvent(event: StudyUnretiredEvent) {
      studyRepository.getRetired(StudyId(event.id)).fold(
        err => throw new IllegalStateException(s"disabling study from event failed: $err"),
        s => studyRepository.put(DisabledStudy(s.id, event.version, s.addedDate, Some(event.dateTime),
          s.name, s.description))
      )
      ()
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

    val errMsgNameExists = "study with name already exists"

    private def nameAvailable(name: String): DomainValidation[Boolean] = {
      nameAvailableMatcher(name, studyRepository, errMsgNameExists){ item =>
        item.name.equals(name)
      }
    }

    private def nameAvailable(name: String, excludeStudyId: StudyId): DomainValidation[Boolean] = {
      nameAvailableMatcher(name, studyRepository, errMsgNameExists){ item =>
        item.name.equals(name) && (item.id != excludeStudyId)
      }
    }
  }
}
