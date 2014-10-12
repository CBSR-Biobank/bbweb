package org.biobank.service.study

import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
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
    * The StudiesProcessor is responsible for maintaining state changes for all
    * [[org.biobank.domain.study.Study]] aggregates. This particular processor uses Akka-Persistence's
    * [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist the generated
    * events, afterwhich it will updated the current state of the [[org.biobank.domain.study.Study]] being
    * processed.
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

    /**
      * These are the events that are recovered during journal recovery. They cannot fail and must be
      * processed to recreate the current state of the aggregate.
      */
    val receiveRecover: Receive = {
      case wevent: WrappedEvent[_] =>
        wevent.event match {
          case event: StudyEvent => recoverEvent(event, wevent.userId, wevent.dateTime)

          case event => throw new IllegalStateException(s"event not handled: $event")
        }


      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.studies.foreach{ study => studyRepository.put(study) }
    }

    /**
      * These are the commands that are requested. A command can fail, and will send the failure as a response
      * back to the user. Each valid command generates one or more events and is journaled.
      *
      * Some commands are forwared to child actors for processing. The child actors are:
      *
      *  - [[CeventAnnotationTypeProcessor]]
      *  - [[CollectionEventTypeProcessor]]
      *  - [[ParticipantAnnotationTypeProcessor]]
      *  - [[ProcessingTypeProcessor]]
      *  - [[SpecimenGroupProcessor]]
      *  - [[SpecimenLinkAnnotationTypeProcessor]]
      *  - [[SpecimenLinkTypeProcessor]]
      *  - [[StudiesProcessor]]
      *  - [[StudyAnnotationTypeProcessor]]
      */
    val receiveCommand: Receive = {
      case procCmd: WrappedCommand =>
        implicit val userId = procCmd.userId

        // order is important in the pattern match used below
        procCmd.command match {
          case cmd : StudyCommandWithId =>
            validateAndForward(cmd)

          case cmd: StudyCommand =>
            process(validateCmd(cmd)){ w => recoverEvent(w.event, w.userId, w.dateTime) }

          case cmd => log.error(s"invalid command: $cmd")
        }

      case "snap" =>
        saveSnapshot(SnapshotState(studyRepository.getValues.toSet))
        stash()

      case cmd => log.error(s"message not handled: $cmd")

    }

    private def validateAndForward(cmd: StudyCommandWithId) = {
      studyRepository.getByKey(StudyId(cmd.studyId)).fold(
        err => context.sender ! DomainError(s"invalid study id: ${cmd.studyId}").failNel,
        study => study match {
          case study: DisabledStudy => {
            val childActor = cmd match {
              case cmd: SpecimenGroupCommand                 => specimenGroupProcessor
              case cmd: CollectionEventTypeCommand           => collectionEventTypeProcessor
              case cmd: CollectionEventAnnotationTypeCommand => ceventAnnotationTypeProcessor
              case cmd: ParticipantAnnotationTypeCommand     => participantAnnotationTypeProcessor
              case cmd: ProcessingTypeCommand                => processingTypeProcessor
              case cmd: SpecimenLinkTypeCommand              => specimenLinkTypeProcessor
              case cmd: SpecimenLinkAnnotationTypeCommand    => specimenLinkAnnotationTypeProcessor
            }
            childActor forward cmd
          }
          case study => context.sender ! DomainError(s"$study for $cmd is not disabled").failNel
        }
      )
    }

    private def validateAndForward(childActor: ActorRef, cmd: SpecimenLinkTypeCommand) = {
      val validation = for {
        processingType <- processingTypeRepository.getByKey(ProcessingTypeId(cmd.processingTypeId))
        study <- studyRepository.getByKey(processingType.studyId)
      } yield study

      validation.fold(
        err => context.sender ! err.failure,
        study => study match {
          case study: DisabledStudy => childActor forward cmd
          case study => context.sender ! s"$study for $cmd is not disabled".failNel
        }
      )
    }

    def updateStudy[T <: Study](cmd: StudyCommand)(fn: Study => DomainValidation[T])
        : DomainValidation[T] = {
      studyRepository.getByKey(StudyId(cmd.id)).fold(
        err => DomainError(s"invalid study id: ${cmd.id}").failNel,
        study => for {
          validVersion <-  study.requireVersion(cmd.expectedVersion)
          updatedStudy <- fn(study)
        } yield updatedStudy
      )
    }

    def updateDisabled[T <: Study]
      (cmd: StudyCommand)
      (fn: DisabledStudy => DomainValidation[T])
        : DomainValidation[T] = {
      updateStudy(cmd) {
        case study: DisabledStudy => fn(study)
        case study => s"$study for $cmd is not disabled".failNel
      }
    }

    def updateEnabled[T <: Study]
      (cmd: StudyCommand)
      (fn: EnabledStudy => DomainValidation[T])
        : DomainValidation[T] = {
      updateStudy(cmd) {
        case study: EnabledStudy => fn(study)
        case study => s"$study for $cmd is not enabled".failNel
      }
    }

    def updateRetired[T <: Study]
      (cmd: StudyCommand)
      (fn: RetiredStudy => DomainValidation[T])
        : DomainValidation[T] = {
      updateStudy(cmd) {
        case study: RetiredStudy => fn(study)
        case study => s"$study for $cmd is not retired".failNel
      }
    }

    private def validateCmd(cmd: StudyCommand): DomainValidation[StudyAddedEvent] = {
      cmd match {
        case cmd: AddStudyCmd =>      validateCmd(cmd)
        case cmd: UpdateStudyCmd =>   validateCmd(cmd)
        case cmd: EnableStudyCmd =>   validateCmd(cmd)
        case cmd: DisableStudyCmd =>  validateCmd(cmd)
        case cmd: RetireStudyCmd =>   validateCmd(cmd)
        case cmd: UnretireStudyCmd => validateCmd(cmd)

        case cmd => DomainError(s"invalid command: $cmd").failNel
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
          newStudy.id.id, newStudy.timeAdded, newStudy.name, newStudy.description).success
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
        err => err.fail,
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
        err => err.fail,
        study => StudyEnabledEvent(cmd.id, study.version, timeNow).success
      )
    }

    private def validateCmd(cmd: DisableStudyCmd): DomainValidation[StudyDisabledEvent] = {
      val timeNow = DateTime.now
      val v = updateEnabled(cmd) { s => s.disable }
      v.fold(
        err => err.fail,
        study => StudyDisabledEvent(cmd.id, study.version, timeNow).success
      )
    }

    private def validateCmd(cmd: RetireStudyCmd): DomainValidation[StudyRetiredEvent] = {
      val timeNow = DateTime.now
      val v = updateDisabled(cmd) { s => s.retire }
      v.fold(
        err => err.fail,
        study => StudyRetiredEvent(cmd.id, study.version, timeNow).success
      )
    }

    private def validateCmd(cmd: UnretireStudyCmd): DomainValidation[StudyUnretiredEvent] = {
      val timeNow = DateTime.now
      val v = updateRetired(cmd) { s => s.unretire }
      v.fold(
        err => err.fail,
        study => StudyUnretiredEvent(cmd.id, study.version, timeNow).success
      )
    }

    private def recoverEvent(event: StudyEvent, userId: UserId, dateTime: DateTime) {
      event match {
        case event: StudyAddedEvent =>     recoverEvent(event, userId, dateTime)
        case event: StudyUpdatedEvent =>   recoverEvent(event, userId, dateTime)
        case event: StudyEnabledEvent =>   recoverEvent(event, userId, dateTime)
        case event: StudyDisabledEvent =>  recoverEvent(event, userId, dateTime)
        case event: StudyRetiredEvent =>   recoverEvent(event, userId, dateTime)
        case event: StudyUnretiredEvent => recoverEvent(event, userId, dateTime)

        case event => log.error(s"invalid event: $event")
      }
    }

    private def recoverEvent(event: StudyAddedEvent, userId: UserId, dateTime: DateTime) {
      studyRepository.put(DisabledStudy(
        StudyId(event.id), 0L, event.dateTime, None, event.name, event.description))
      ()
    }

    private def recoverEvent(event: StudyUpdatedEvent, userId: UserId, dateTime: DateTime) {
      studyRepository.getDisabled(StudyId(event.id)).fold(
        err => throw new IllegalStateException(s"updating study from event failed: $err"),
        s => studyRepository.put(s.copy(
          version = event.version, name = event.name, description = event.description,
          timeModified = Some(event.dateTime)))
      )
      ()
    }

    private def recoverEvent(event: StudyEnabledEvent, userId: UserId, dateTime: DateTime) {
      studyRepository.getDisabled(StudyId(event.id)).fold(
        err => throw new IllegalStateException(s"enabling study from event failed: $err"),
        s => studyRepository.put(EnabledStudy(s.id, event.version, s.timeAdded, Some(event.dateTime),
          s.name, s.description))
      )
      ()
    }

    private def recoverEvent(event: StudyDisabledEvent, userId: UserId, dateTime: DateTime) {
      studyRepository.getEnabled(StudyId(event.id)).fold(
        err => throw new IllegalStateException(s"disabling study from event failed: $err"),
        s => studyRepository.put(DisabledStudy(s.id, event.version, s.timeAdded, Some(event.dateTime),
          s.name, s.description))
      )
      ()
    }

    private def recoverEvent(event: StudyRetiredEvent, userId: UserId, dateTime: DateTime) {
      studyRepository.getDisabled(StudyId(event.id)).fold(
        err => throw new IllegalStateException(s"retiring study from event failed: $err"),
        s => studyRepository.put(RetiredStudy(s.id, event.version, s.timeAdded, Some(event.dateTime),
          s.name, s.description))
      )
      ()
    }

    private def recoverEvent(event: StudyUnretiredEvent, userId: UserId, dateTime: DateTime) {
      studyRepository.getRetired(StudyId(event.id)).fold(
        err => throw new IllegalStateException(s"disabling study from event failed: $err"),
        s => studyRepository.put(DisabledStudy(s.id, event.version, s.timeAdded, Some(event.dateTime),
          s.name, s.description))
      )
      ()
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
