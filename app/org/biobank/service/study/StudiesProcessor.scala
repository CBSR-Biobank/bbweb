package org.biobank.service.study

import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{ DomainValidation, DomainError }
import org.biobank.domain.user.UserId
import org.biobank.domain.study._

import akka.actor. { ActorRef, Props }
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  * The StudiesProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.Study]] aggregates. This particular processor uses Akka-Persistence's
  * [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist the generated
  * events, afterwhich it will updated the current state of the [[org.biobank.domain.study.Study]] being
  * processed.
  */
class StudiesProcessor(implicit inj: Injector) extends Processor with AkkaInjectable {

  override def persistenceId = "study-processor-id"

  case class SnapshotState(studies: Set[Study])

  val studyRepository = inject [StudyRepository]

  val processingTypeRepository = inject [ProcessingTypeRepository]

  val specimenGroupRepository = inject [SpecimenGroupRepository]

  val collectionEventTypeRepository = inject [CollectionEventTypeRepository]

  val specimenGroupProcessor =
    injectActorRef [SpecimenGroupProcessor] ("specimenGroup")

  val collectionEventTypeProcessor =
    injectActorRef [CollectionEventTypeProcessor] ("collectionEventType")

  val ceventAnnotationTypeProcessor =
    injectActorRef [CeventAnnotationTypeProcessor] ("ceventAnnotationType")

  val participantAnnotationTypeProcessor =
    injectActorRef [ParticipantAnnotationTypeProcessor] ("participantAnnotationType")

  val processingTypeProcessor =
    injectActorRef [ProcessingTypeProcessor] ("processingType")

  val specimenLinkTypeProcessor =
    injectActorRef [SpecimenLinkTypeProcessor] ("specimenLinkType")

  val specimenLinkAnnotationTypeProcessor =
    injectActorRef [SpecimenLinkAnnotationTypeProcessor] ("specimenLinkAnnotationType")

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: StudyAddedEvent     => recoverStudyAddedEvent(event, wevent.userId, wevent.dateTime)
        case event: StudyUpdatedEvent   => recoverStudyUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: StudyEnabledEvent   => recoverStudyEnabledEvent(event, wevent.userId, wevent.dateTime)
        case event: StudyDisabledEvent  => recoverStudyDisabledEvent(event, wevent.userId, wevent.dateTime)
        case event: StudyRetiredEvent   => recoverStudyRetiredEvent(event, wevent.userId, wevent.dateTime)
        case event: StudyUnretiredEvent => recoverStudyUnretiredEvent(event, wevent.userId, wevent.dateTime)

        case event => log.error(s"event not handled: $event")
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
        case _: StudyCommandWithStudyId
           | _: SpecimenLinkTypeCommand => validateAndForward(procCmd)

        case cmd: AddStudyCmd      => processAddStudyCmd(cmd)
        case cmd: UpdateStudyCmd   => processUpdateStudyCmd(cmd)
        case cmd: EnableStudyCmd   => processEnableStudyCmd(cmd)
        case cmd: DisableStudyCmd  => processDisableStudyCmd(cmd)
        case cmd: RetireStudyCmd   => processRetireStudyCmd(cmd)
        case cmd: UnretireStudyCmd => processUnretireStudyCmd(cmd)

        case cmd => log.error(s"invalid wrapped command: $cmd")
      }

    case "snap" =>
      saveSnapshot(SnapshotState(studyRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"StudiesProcessor: message not handled: $cmd")

  }

  private def validateAndForward(procCmd: WrappedCommand) = {
    procCmd.command match {
      case cmd: StudyCommandWithStudyId =>
        studyRepository.getByKey(StudyId(cmd.studyId)).fold(
          err => context.sender ! DomainError(s"invalid study id: ${cmd.studyId}").failureNel,
          study => study match {
            case study: DisabledStudy => {
              val childActor = cmd match {
                case _: SpecimenGroupCommand                 => specimenGroupProcessor
                case _: CollectionEventTypeCommand           => collectionEventTypeProcessor
                case _: ProcessingTypeCommand                => processingTypeProcessor
                case _: SpecimenLinkTypeCommand              => specimenLinkTypeProcessor

                case _: AddCollectionEventAnnotationTypeCmd
                   | _: UpdateCollectionEventAnnotationTypeCmd
                   | _: RemoveCollectionEventAnnotationTypeCmd => ceventAnnotationTypeProcessor

                case _: AddParticipantAnnotationTypeCmd
                   | _: UpdateParticipantAnnotationTypeCmd
                   | _: RemoveParticipantAnnotationTypeCmd => participantAnnotationTypeProcessor

                case _: AddSpecimenLinkAnnotationTypeCmd
                   | _: UpdateSpecimenLinkAnnotationTypeCmd
                   | _: RemoveSpecimenLinkAnnotationTypeCmd => specimenLinkAnnotationTypeProcessor

              }
              childActor forward procCmd
            }
            case study => context.sender ! DomainError(s"$study for $cmd is not disabled").failureNel
          }
        )

      case cmd: SpecimenLinkTypeCommand =>
        val validation = for {
          processingType <- processingTypeRepository.getByKey(ProcessingTypeId(cmd.processingTypeId))
          study <- studyRepository.getByKey(processingType.studyId)
        } yield study

        validation.fold(
          err => context.sender ! err.failure,
          study => study match {
            case study: DisabledStudy => specimenLinkTypeProcessor forward procCmd
            case study => context.sender ! s"$study for $cmd is not disabled".failureNel
          }
        )

      case cmd => log.error(s"validateAndForward: invalid command: $cmd")
    }
  }

  def updateStudy[T <: Study](cmd: StudyModifyCommand)(fn: Study => DomainValidation[T])
      : DomainValidation[T] = {
    studyRepository.getByKey(StudyId(cmd.id)).fold(
      err => DomainError(s"invalid study id: ${cmd.id}").failureNel,
      study => for {
        validVersion <-  study.requireVersion(cmd.expectedVersion)
        updatedStudy <- fn(study)
      } yield updatedStudy
    )
  }

  def updateDisabled[T <: Study]
    (cmd: StudyModifyCommand)
    (fn: DisabledStudy => DomainValidation[T])
      : DomainValidation[T] = {
    updateStudy(cmd) {
      case study: DisabledStudy => fn(study)
      case study => s"$study for $cmd is not disabled".failureNel
    }
  }

  def updateEnabled[T <: Study]
    (cmd: StudyModifyCommand)
    (fn: EnabledStudy => DomainValidation[T])
      : DomainValidation[T] = {
    updateStudy(cmd) {
      case study: EnabledStudy => fn(study)
      case study => s"$study for $cmd is not enabled".failureNel
    }
  }

  def updateRetired[T <: Study]
    (cmd: StudyModifyCommand)
    (fn: RetiredStudy => DomainValidation[T])
      : DomainValidation[T] = {
    updateStudy(cmd) {
      case study: RetiredStudy => fn(study)
      case study => s"$study for $cmd is not retired".failureNel
    }
  }

  private def processAddStudyCmd(cmd: AddStudyCmd)(implicit userId: Option[UserId]): Unit = {
    val studyId = studyRepository.nextIdentity

    if (studyRepository.getByKey(studyId).isSuccess) {
      log.error(s"study with id already exsits: $studyId")
    }

    val event = for {
      nameAvailable <- nameAvailable(cmd.name)
      newStudy <- DisabledStudy.create(
        studyId, -1L, org.joda.time.DateTime.now, cmd.name, cmd.description)
      event <- StudyAddedEvent(
        id          = newStudy.id.id,
        name        = Some(newStudy.name),
        description = newStudy.description).success
    } yield event

    process(event){ w =>
      recoverStudyAddedEvent(w.event, w.userId, w.dateTime)
    }
  }

  private def processUpdateStudyCmd(cmd: UpdateStudyCmd)(implicit userId: Option[UserId]): Unit = {
    val v = updateDisabled(cmd) { s =>
      for {
        nameAvailable <- nameAvailable(cmd.name, StudyId(cmd.id))
        updatedStudy <- s.update(cmd.name, cmd.description)
      } yield updatedStudy
    }
    val event = v.fold(
      err => err.failure,
      study => StudyUpdatedEvent(
        id          = cmd.id,
        version     = Some(study.version),
        name        = Some(study.name),
        description = study.description).success
    )

    process(event){ w =>
      recoverStudyUpdatedEvent(w.event, w.userId, w.dateTime)
    }
  }

  private def processEnableStudyCmd(cmd: EnableStudyCmd)(implicit userId: Option[UserId]): Unit = {
    val studyId = StudyId(cmd.id)
    val specimenGroupCount = specimenGroupRepository.allForStudy(studyId).size
    val collectionEventtypeCount = collectionEventTypeRepository.allForStudy(studyId).size
    val v = updateDisabled(cmd) { s => s.enable(specimenGroupCount, collectionEventtypeCount) }
    val event = v.fold(
      err => err.failure,
      study => StudyEnabledEvent(id = cmd.id, version = Some(study.version)).success
    )
    process(event){ w =>
      recoverStudyEnabledEvent(w.event, w.userId, w.dateTime)
    }
  }

  private def processDisableStudyCmd(cmd: DisableStudyCmd)(implicit userId: Option[UserId]): Unit = {
    val v = updateEnabled(cmd) { s => s.disable }
    val event = v.fold(
      err => err.failure,
      study => StudyDisabledEvent(id = cmd.id, version = Some(study.version)).success
    )
    process(event){ w =>
      recoverStudyDisabledEvent(w.event, w.userId, w.dateTime)
    }
  }

  private def processRetireStudyCmd(cmd: RetireStudyCmd)(implicit userId: Option[UserId]): Unit = {
    val v = updateDisabled(cmd) { s => s.retire }
    val event = v.fold(
      err => err.failure,
      study => StudyRetiredEvent(id = cmd.id, version = Some(study.version)).success
    )
    process(event){ w =>
      recoverStudyRetiredEvent(w.event, w.userId, w.dateTime)
    }
  }

  private def processUnretireStudyCmd(cmd: UnretireStudyCmd)(implicit userId: Option[UserId]): Unit = {
    val v = updateRetired(cmd) { s => s.unretire }
    val event = v.fold(
      err => err.failure,
      study => StudyUnretiredEvent(id = cmd.id, version = Some(study.version)).success
    )
    process(event){ w =>
      recoverStudyUnretiredEvent(w.event, w.userId, w.dateTime)
    }
  }

  private def recoverStudyAddedEvent(event: StudyAddedEvent, userId: Option[UserId], dateTime: DateTime) {
    studyRepository.put(DisabledStudy(
      id           = StudyId(event.id),
      version      = 0L,
      timeAdded    = dateTime,
      timeModified = None,
      name         = event.getName,
      description  = event.description))
    ()
  }

  private def recoverStudyUpdatedEvent(event: StudyUpdatedEvent, userId: Option[UserId], dateTime: DateTime) {
    studyRepository.getDisabled(StudyId(event.id)).fold(
      err => log.error(s"updating study from event failed: $err"),
      s => {
        studyRepository.put(s.copy(
          version      = event.getVersion,
          name         = event.getName,
          description  = event.description,
          timeModified = Some(dateTime)))
        ()
      }
    )
  }

  private def recoverStudyEnabledEvent(event: StudyEnabledEvent, userId: Option[UserId], dateTime: DateTime) {
    studyRepository.getDisabled(StudyId(event.id)).fold(
      err => log.error(s"enabling study from event failed: $err"),
      s => {
        studyRepository.put(EnabledStudy(
          id           = s.id,
          version      = event.getVersion,
          timeAdded    = s.timeAdded,
          timeModified = Some(dateTime),
          name         = s.name,
          description  = s.description))
        ()
      }
    )
  }

  private def recoverStudyDisabledEvent(event: StudyDisabledEvent, userId: Option[UserId], dateTime: DateTime) {
    studyRepository.getEnabled(StudyId(event.id)).fold(
      err => log.error(s"disabling study from event failed: $err"),
      s => {
        studyRepository.put(DisabledStudy(
          id           = s.id,
          version      = event.getVersion,
          timeAdded    = s.timeAdded,
          timeModified = Some(dateTime),
          name         = s.name,
          description  = s.description))
        ()
      }
    )
  }

  private def recoverStudyRetiredEvent(event: StudyRetiredEvent, userId: Option[UserId], dateTime: DateTime) {
    studyRepository.getDisabled(StudyId(event.id)).fold(
      err => log.error(s"retiring study from event failed: $err"),
      s => {
        studyRepository.put(RetiredStudy(
          id           = s.id,
          version      = event.getVersion,
          timeAdded    = s.timeAdded,
          timeModified = Some(dateTime),
          name         = s.name,
          description  = s.description))
        ()
      }
    )
  }

  private def recoverStudyUnretiredEvent(event: StudyUnretiredEvent, userId: Option[UserId], dateTime: DateTime) {
    studyRepository.getRetired(StudyId(event.id)).fold(
      err => log.error(s"disabling study from event failed: $err"),
      s => {
        studyRepository.put(DisabledStudy(
          id           = s.id,
          version      = event.getVersion,
          timeAdded    = s.timeAdded,
          timeModified = Some(dateTime),
          name         = s.name,
          description  = s.description))
        ()
      }
    )
  }

  val errMsgNameExists = "study with name already exists"

  private def nameAvailable(name: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, studyRepository, errMsgNameExists){ item =>
      item.name == name
    }
  }

  private def nameAvailable(name: String, excludeStudyId: StudyId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, studyRepository, errMsgNameExists){ item =>
      (item.name == name) && (item.id != excludeStudyId)
    }
  }
}
