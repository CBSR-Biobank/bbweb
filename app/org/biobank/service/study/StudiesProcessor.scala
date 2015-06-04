package org.biobank.service.study

import org.biobank.service.{ Processor, WrappedEvent }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{ DomainValidation, DomainError }
import org.biobank.domain.user.UserId
import org.biobank.domain.study._
import org.biobank.TestData

import javax.inject._
import akka.actor._
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object StudiesProcessor {

  def props = Props[StudiesProcessor]

}

/**
 * The StudiesProcessor is responsible for maintaining state changes for all
 * [[org.biobank.domain.study.Study]] aggregates. This particular processor uses Akka-Persistence's
 * [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist the generated
 * events, afterwhich it will updated the current state of the [[org.biobank.domain.study.Study]] being
 * processed.
 */
class StudiesProcessor @javax.inject.Inject() (
  @Named("collectionEventType")        val collectionEventTypeProcessor:        ActorRef,
  @Named("ceventAnnotationType")       val ceventAnnotationTypeProcessor:       ActorRef,
  @Named("participantAnnotationType")  val participantAnnotationTypeProcessor:  ActorRef,
  @Named("processingType")             val processingTypeProcessor:             ActorRef,
  @Named("specimenGroup")              val specimenGroupProcessor:              ActorRef,
  @Named("specimenLinkAnnotationType") val specimenLinkAnnotationTypeProcessor: ActorRef,
  @Named("specimenLinkType")           val specimenLinkTypeProcessor:           ActorRef,

  val studyRepository:               StudyRepository,
  val processingTypeRepository:      ProcessingTypeRepository,
  val specimenGroupRepository:       SpecimenGroupRepository,
  val collectionEventTypeRepository: CollectionEventTypeRepository,
  val testData:                      TestData)
    extends Processor {
  import org.biobank.infrastructure.event.StudyEventsUtil._
  import StudyEvent.EventType

  override def persistenceId = "study-processor-id"

  case class SnapshotState(studies: Set[Study])

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  val receiveRecover: Receive = {
    case event: StudyEvent => event.eventType match {
      case et: EventType.Added     => applyStudyAddedEvent(event)
      case et: EventType.Updated   => applyStudyUpdatedEvent(event)
      case et: EventType.Enabled   => applyStudyEnabledEvent(event)
      case et: EventType.Disabled  => applyStudyDisabledEvent(event)
      case et: EventType.Retired   => applyStudyRetiredEvent(event)
      case et: EventType.Unretired => applyStudyUnretiredEvent(event)

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
    case cmd: StudyCommandWithStudyId => validateAndForward(cmd)
    case cmd: SpecimenLinkTypeCommand => validateAndForward(cmd)
    case cmd: AddStudyCmd             => processAddStudyCmd(cmd)
    case cmd: UpdateStudyCmd          => processUpdateStudyCmd(cmd)
    case cmd: EnableStudyCmd          => processEnableStudyCmd(cmd)
    case cmd: DisableStudyCmd         => processDisableStudyCmd(cmd)
    case cmd: RetireStudyCmd          => processRetireStudyCmd(cmd)
    case cmd: UnretireStudyCmd        => processUnretireStudyCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(studyRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"StudiesProcessor: message not handled: $cmd")
  }

  private def validateAndForward(cmd: StudyCommand) = {
    cmd match {
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
              childActor forward cmd
            }
            case study => context.sender ! DomainError(s"study is not disabled: studyId: ${study.id}").failureNel
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
            case study: DisabledStudy => specimenLinkTypeProcessor forward cmd
            case study => context.sender ! DomainError(s"study is not disabled: studyId: ${study.id}").failureNel
          }
        )

      case cmd => log.error(s"validateAndForward: invalid command: $cmd")
    }
  }

  private def processAddStudyCmd(cmd: AddStudyCmd): Unit = {
    val studyId = studyRepository.nextIdentity

    if (studyRepository.getByKey(studyId).isSuccess) {
      log.error(s"study with id already exsits: $studyId")
    }

    val v = for {
      nameAvailable <- nameAvailable(cmd.name)
      newStudy <- DisabledStudy.create(studyId,
                                       -1L,
                                       org.joda.time.DateTime.now,
                                       cmd.name,
                                       cmd.description)
      event <- createStudyEvent(newStudy.id, cmd).withAdded(
        StudyAddedEvent(
          name        = Some(newStudy.name),
          description = newStudy.description)).success
      } yield event

    process(v) { applyStudyAddedEvent(_) }
  }

  private def processUpdateStudyCmd(cmd: UpdateStudyCmd): Unit = {
    val v = updateDisabled(cmd) { s =>
      for {
        nameAvailable <- nameAvailable(cmd.name, StudyId(cmd.id))
        updatedStudy <- s.update(cmd.name, cmd.description)
        event <- createStudyEvent(updatedStudy.id, cmd).withUpdated(
          StudyUpdatedEvent(
            version     = Some(updatedStudy.version),
            name        = Some(updatedStudy.name),
            description = updatedStudy.description)).success
      } yield event
    }

    process(v){ applyStudyUpdatedEvent(_) }
  }

  private def processEnableStudyCmd(cmd: EnableStudyCmd): Unit = {
    val studyId = StudyId(cmd.id)
    val specimenGroupCount = specimenGroupRepository.allForStudy(studyId).size
    val collectionEventtypeCount = collectionEventTypeRepository.allForStudy(studyId).size
    val v = updateDisabled(cmd) { s =>
      for {
        study <- s.enable(specimenGroupCount, collectionEventtypeCount)
        event <- createStudyEvent(study.id, cmd).withEnabled(
          StudyEnabledEvent(version = Some(study.version))).success
      } yield event
    }
    process(v) { applyStudyEnabledEvent(_) }
  }

  private def processDisableStudyCmd(cmd: DisableStudyCmd): Unit = {
    val v = updateEnabled(cmd) { s =>
      for {
        study <- s.disable
        event <- createStudyEvent(study.id, cmd).withDisabled(
          StudyDisabledEvent(version = Some(study.version))).success
      } yield event
    }
    process(v){ applyStudyDisabledEvent(_) }
  }

  private def processRetireStudyCmd(cmd: RetireStudyCmd): Unit = {
    val v = updateDisabled(cmd) { s =>
      for {
        study <- s.retire
        event <- createStudyEvent(study.id, cmd).withRetired(
          StudyRetiredEvent(version = Some(study.version))).success
      } yield event
    }
    process(v){ applyStudyRetiredEvent(_) }
  }

  private def processUnretireStudyCmd(cmd: UnretireStudyCmd): Unit = {
    val v = updateRetired(cmd) { s =>
      for {
        study <- s.unretire
        event <- createStudyEvent(study.id, cmd).withUnretired(
          StudyUnretiredEvent(version = Some(study.version))).success
      } yield event
    }
    process(v) { applyStudyUnretiredEvent(_) }
  }

  def updateStudy
    (cmd: StudyModifyCommand)(fn: Study => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    studyRepository.getByKey(StudyId(cmd.id)).fold(
      err => DomainError(s"invalid study id: ${cmd.id}").failureNel,
      study => for {
        validVersion <-  study.requireVersion(cmd.expectedVersion)
        updatedStudy <- fn(study)
      } yield updatedStudy
    )
  }

  def updateDisabled
    (cmd: StudyModifyCommand)(fn: DisabledStudy => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    updateStudy(cmd) {
      case study: DisabledStudy => fn(study)
      case study => s"$study for $cmd is not disabled".failureNel
    }
  }

  def updateEnabled
    (cmd: StudyModifyCommand)(fn: EnabledStudy => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    updateStudy(cmd) {
      case study: EnabledStudy => fn(study)
      case study => s"$study for $cmd is not enabled".failureNel
    }
  }

  def updateRetired
    (cmd: StudyModifyCommand)(fn: RetiredStudy => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    updateStudy(cmd) {
      case study: RetiredStudy => fn(study)
      case study => s"$study for $cmd is not retired".failureNel
    }
  }

  private def applyStudyAddedEvent(event: StudyEvent) : Unit = {
    if (event.eventType.isAdded) {
      val addedEvent = event.getAdded

      studyRepository.put(
        DisabledStudy(id           = StudyId(event.id),
                      version      = 0L,
                      timeAdded    = ISODateTimeFormat.dateTime.parseDateTime(event.getTime),
                      timeModified = None,
                      name         = addedEvent.getName,
                      description  = addedEvent.description))
      ()
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyStudyUpdatedEvent(event: StudyEvent) : Unit = {
    if (event.eventType.isUpdated) {
      val updatedEvent = event.getUpdated

      studyRepository.getDisabled(StudyId(event.id)).fold(
        err => log.error(s"updating study from event failed: $err"),
        s => {
          studyRepository.put(
            s.copy(version      = updatedEvent.getVersion,
                   timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
                   name         = updatedEvent.getName,
                   description  = updatedEvent.description))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyStudyEnabledEvent(event: StudyEvent) : Unit = {
    if (event.eventType.isEnabled) {
      val enabledEvent = event.getEnabled

      studyRepository.getDisabled(StudyId(event.id)).fold(
        err => log.error(s"enabling study from event failed: $err"),
        s => {
          studyRepository.put(
            EnabledStudy(
              id           = s.id,
              version      = enabledEvent.getVersion,
              timeAdded    = s.timeAdded,
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
              name         = s.name,
              description  = s.description))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyStudyDisabledEvent(event: StudyEvent) : Unit = {
    if (event.eventType.isDisabled) {
      val disabledEvent = event.getDisabled

      studyRepository.getEnabled(StudyId(event.id)).fold(
        err => log.error(s"disabling study from event failed: $err"),
        s => {
          studyRepository.put(
            DisabledStudy(
              id           = s.id,
              version      = disabledEvent.getVersion,
              timeAdded    = s.timeAdded,
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
              name         = s.name,
              description  = s.description))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyStudyRetiredEvent(event: StudyEvent) : Unit = {
    if (event.eventType.isRetired) {
      val retiredEvent = event.getRetired

      studyRepository.getDisabled(StudyId(event.id)).fold(
        err => log.error(s"retiring study from event failed: $err"),
        s => {
          studyRepository.put(
            RetiredStudy(
              id           = s.id,
              version      = retiredEvent.getVersion,
              timeAdded    = s.timeAdded,
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
              name         = s.name,
              description  = s.description))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyStudyUnretiredEvent(event: StudyEvent) : Unit = {
    if (event.eventType.isUnretired) {
      val unretiredEvent = event.getUnretired

      studyRepository.getRetired(StudyId(event.id)).fold(
        err => log.error(s"disabling study from event failed: $err"),
        s => {
          studyRepository.put(
            DisabledStudy(
              id           = s.id,
              version      = unretiredEvent.getVersion,
              timeAdded    = s.timeAdded,
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
              name         = s.name,
              description  = s.description))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
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

  testData.addMultipleStudies
}
