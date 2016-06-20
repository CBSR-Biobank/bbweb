package org.biobank.service.study

import akka.actor._
import akka.persistence.SnapshotOffer
import javax.inject._
import org.biobank.TestData
import org.biobank.domain.study._
import org.biobank.domain.{ AnnotationType, AnnotationValueType, DomainValidation }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.EventUtils
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.infrastructure.command.CollectionEventTypeCommands._
import org.biobank.service.Processor
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
  @Named("collectionEventType") val collectionEventTypeProcessor: ActorRef,
  @Named("processingType")      val processingTypeProcessor:      ActorRef,
  @Named("specimenLinkType")    val specimenLinkTypeProcessor:    ActorRef,
  val studyRepository:                                            StudyRepository,
  val processingTypeRepository:                                   ProcessingTypeRepository,
  val specimenGroupRepository:                                    SpecimenGroupRepository,
  val collectionEventTypeRepository:                              CollectionEventTypeRepository,
  val testData:                                                   TestData)
    extends Processor {
  import org.biobank.CommonValidations._

  override def persistenceId = "study-processor-id"

  case class SnapshotState(studies: Set[Study])

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  val receiveRecover: Receive = {
    case event: StudyEvent => event.eventType match {
      case et: StudyEvent.EventType.Added                 => applyAddedEvent(event)
      case et: StudyEvent.EventType.NameUpdated           => applyNameUpdatedEvent(event)
      case et: StudyEvent.EventType.DescriptionUpdated    => applyDescriptionUpdatedEvent(event)
      case et: StudyEvent.EventType.AnnotationTypeAdded   => applyParticipantAnnotationTypeAddedEvent(event)
      case et: StudyEvent.EventType.AnnotationTypeUpdated => applyParticipantAnnotationTypeUpdatedEvent(event)
      case et: StudyEvent.EventType.AnnotationTypeRemoved => applyParticipantAnnotationTypeRemovedEvent(event)
      case et: StudyEvent.EventType.Enabled               => applyEnabledEvent(event)
      case et: StudyEvent.EventType.Disabled              => applyDisabledEvent(event)
      case et: StudyEvent.EventType.Retired               => applyRetiredEvent(event)
      case et: StudyEvent.EventType.Unretired             => applyUnretiredEvent(event)

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

    case cmd: AddStudyCmd =>
      process(addCmdToEvent(cmd))(applyAddedEvent)

    case cmd: UpdateStudyNameCmd =>
      processUpdateCmdOnDisabledStudy(cmd, updateNameCmdToEvent, applyNameUpdatedEvent)

    case cmd: UpdateStudyDescriptionCmd =>
      processUpdateCmdOnDisabledStudy(cmd, updateDescriptionCmdToEvent, applyDescriptionUpdatedEvent)

    case cmd: StudyAddParticipantAnnotationTypeCmd =>
      processUpdateCmdOnDisabledStudy(cmd,
                                      addAnnotationTypeCmdToEvent,
                                      applyParticipantAnnotationTypeAddedEvent)

    case cmd: StudyUpdateParticipantAnnotationTypeCmd =>
      processUpdateCmdOnDisabledStudy(cmd,
                                      updateAnnotationTypeCmdToEvent,
                                      applyParticipantAnnotationTypeUpdatedEvent)

    case cmd: UpdateStudyRemoveAnnotationTypeCmd =>
      processUpdateCmdOnDisabledStudy(cmd,
                                      removeAnnotationTypeCmdToEvent,
                                      applyParticipantAnnotationTypeRemovedEvent)

    case cmd: EnableStudyCmd =>
      processUpdateCmdOnDisabledStudy(cmd, enableCmdToEvent, applyEnabledEvent)

    case cmd: DisableStudyCmd =>
      processUpdateCmdOnEnabledStudy(cmd, disableCmdToEvent, applyDisabledEvent)

    case cmd: RetireStudyCmd =>
      processUpdateCmdOnDisabledStudy(cmd, retireCmdToEvent, applyRetiredEvent)

    case cmd: UnretireStudyCmd =>
      processUpdateCmdOnRetiredStudy(cmd, unretireCmdToEvent, applyUnretiredEvent)

    case "snap" =>
      saveSnapshot(SnapshotState(studyRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"StudiesProcessor: message not handled: $cmd")
  }

  private def validateAndForward(cmd: StudyCommand) = {
    cmd match {
      case cmd: StudyCommandWithStudyId =>
        studyRepository.getByKey(StudyId(cmd.studyId)).fold(
          err => context.sender ! IdNotFound(s"study: ${cmd.studyId}").failureNel,
          study => study match {
            case study: DisabledStudy => {
              val childActor = cmd match {
                  case _: CollectionEventTypeCommand => collectionEventTypeProcessor
                  case _: ProcessingTypeCommand      => processingTypeProcessor
                  case _: SpecimenLinkTypeCommand    => specimenLinkTypeProcessor

                }
              childActor forward cmd
            }
            case study =>
              context.sender ! InvalidStatus(s"study not disabled: ${study.id}").failureNel
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
            case study =>
              context.sender ! InvalidStatus(s"study not disabled: ${study.id}").failureNel
          }
        )

      case cmd => log.error(s"validateAndForward: invalid command: $cmd")
    }
  }

  private def addCmdToEvent(cmd: AddStudyCmd): DomainValidation[StudyEvent] = {
    for {
      name  <- nameAvailable(cmd.name)
      id    <- validNewIdentity(studyRepository.nextIdentity, studyRepository)
      study <- DisabledStudy.create(id, 0L, cmd.name, cmd.description, Set.empty)
    } yield {
      StudyEvent(study.id.id).update(
        _.optionalUserId            := cmd.userId,
        _.time                      := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.added.name                := cmd.name,
        _.added.optionalDescription := cmd.description)
    }
  }

  private def updateNameCmdToEvent(cmd: UpdateStudyNameCmd, study: DisabledStudy)
      : DomainValidation[StudyEvent] = {
    (nameAvailable(cmd.name, StudyId(cmd.id)) |@|
       study.withName(cmd.name)) { case (_, _) =>
        StudyEvent(study.id.id).update(
          _.optionalUserId      := cmd.userId,
          _.time                := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.nameUpdated.version := cmd.expectedVersion,
          _.nameUpdated.name    := cmd.name)
    }
  }

  private def updateDescriptionCmdToEvent(cmd: UpdateStudyDescriptionCmd, study: DisabledStudy)
      : DomainValidation[StudyEvent] = {
    study.withDescription(cmd.description).map { _ =>
      StudyEvent(study.id.id).update(
        _.optionalUserId                         := cmd.userId,
        _.time                                   := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.descriptionUpdated.version             := cmd.expectedVersion,
        _.descriptionUpdated.optionalDescription := cmd.description)
    }
  }

  private def addAnnotationTypeCmdToEvent(cmd:   StudyAddParticipantAnnotationTypeCmd,
                                          study: DisabledStudy)
      : DomainValidation[StudyEvent] = {
    for {
      annotationType <- {
        AnnotationType.create(cmd.name,
                              cmd.description,
                              cmd.valueType,
                              cmd.maxValueCount,
                              cmd.options,
                                  cmd.required)
      }
      updatedStudy <- study.withParticipantAnnotationType(annotationType)
    } yield StudyEvent(study.id.id).update(
      _.optionalUserId                     := cmd.userId,
      _.time                               := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.annotationTypeAdded.version        := cmd.expectedVersion,
      _.annotationTypeAdded.annotationType := EventUtils.annotationTypeToEvent(annotationType))
  }

  private def updateAnnotationTypeCmdToEvent(cmd:   StudyUpdateParticipantAnnotationTypeCmd,
                                             study: DisabledStudy)
      : DomainValidation[StudyEvent] = {
    for {
      annotationType <- AnnotationType(cmd.uniqueId,
                                       cmd.name,
                                       cmd.description,
                                       cmd.valueType,
                                       cmd.maxValueCount,
                                       cmd.options,
                                       cmd.required).success
      updatedStudy   <- study.withParticipantAnnotationType(annotationType)
    } yield StudyEvent(study.id.id).update(
      _.optionalUserId                       := cmd.userId,
      _.time                                 := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.annotationTypeUpdated.version        := cmd.expectedVersion,
      _.annotationTypeUpdated.annotationType := EventUtils.annotationTypeToEvent(annotationType))
  }

  private def removeAnnotationTypeCmdToEvent(cmd: UpdateStudyRemoveAnnotationTypeCmd,
                                             study: DisabledStudy)
      : DomainValidation[StudyEvent] = {
    study.removeParticipantAnnotationType(cmd.uniqueId) map { s =>
      StudyEvent(study.id.id).update(
        _.optionalUserId                 := cmd.userId,
        _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.annotationTypeRemoved.version  := cmd.expectedVersion,
        _.annotationTypeRemoved.uniqueId := cmd.uniqueId)
    }
  }

  private def enableCmdToEvent(cmd: EnableStudyCmd,
                               study: DisabledStudy)
      : DomainValidation[StudyEvent] = {
    val collectionEventTypes = collectionEventTypeRepository.allForStudy(study.id)

    if (collectionEventTypes.isEmpty) {
      EntityCriteriaError("no collection event types").failureNel[StudyEvent]
    } else if (collectionEventTypes.filter { cet => cet.hasSpecimenSpecs }.isEmpty) {
      EntityCriteriaError("no collection specimen specs").failureNel[StudyEvent]
    } else {
      study.enable.map { _ =>
        StudyEvent(study.id.id).update(
          _.optionalUserId  := cmd.userId,
          _.time            := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.enabled.version := cmd.expectedVersion)
      }
    }
  }

  private def disableCmdToEvent(cmd: DisableStudyCmd,
                                study: EnabledStudy)
      : DomainValidation[StudyEvent] = {
    study.disable map { _ =>
      StudyEvent(study.id.id).update(
        _.optionalUserId   := cmd.userId,
        _.time             := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.disabled.version := cmd.expectedVersion)
    }
  }

  private def retireCmdToEvent(cmd: RetireStudyCmd,
                               study: DisabledStudy)
      : DomainValidation[StudyEvent] = {
    study.retire map { _ =>
      StudyEvent(study.id.id).update(
        _.optionalUserId  := cmd.userId,
        _.time            := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.retired.version := cmd.expectedVersion)
    }
  }

  private def unretireCmdToEvent(cmd: UnretireStudyCmd,
                                 study: RetiredStudy)
      : DomainValidation[StudyEvent] = {
    study.unretire map { _ =>
      StudyEvent(study.id.id).update(
        _.optionalUserId    := cmd.userId,
        _.time              := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.unretired.version := cmd.expectedVersion)
    }
  }

  private def processUpdateCmd[T <: StudyModifyCommand]
    (cmd: T,
     cmdToEvent: (T, Study) => DomainValidation[StudyEvent],
     applyEvent: StudyEvent => Unit): Unit = {
    val event = for {
        study        <- studyRepository.getByKey(StudyId(cmd.id))
        validVersion <- study.requireVersion(cmd.expectedVersion)
        event        <- cmdToEvent(cmd, study)
      } yield event

    process(event)(applyEvent)
  }

  private def processUpdateCmdOnDisabledStudy[T <: StudyModifyCommand]
    (cmd: T,
     cmdToEvent: (T, DisabledStudy) => DomainValidation[StudyEvent],
     applyEvent: StudyEvent => Unit): Unit = {

    def internal(cmd: T, study: Study): DomainValidation[StudyEvent] =
      study match {
        case s: DisabledStudy => cmdToEvent(cmd, s)
        case s => InvalidStatus(s"study not disabled: ${study.id}").failureNel
      }

    processUpdateCmd(cmd, internal, applyEvent)
  }

  private def processUpdateCmdOnEnabledStudy[T <: StudyModifyCommand]
    (cmd: T,
     cmdToEvent: (T, EnabledStudy) => DomainValidation[StudyEvent],
     applyEvent: StudyEvent => Unit): Unit = {

    def internal(cmd: T, study: Study): DomainValidation[StudyEvent] =
      study match {
        case s: EnabledStudy => cmdToEvent(cmd, s)
        case s => InvalidStatus(s"study not enabled: ${study.id}").failureNel
      }

    processUpdateCmd(cmd, internal, applyEvent)
  }

  private def processUpdateCmdOnRetiredStudy[T <: StudyModifyCommand]
    (cmd: T,
     cmdToEvent: (T, RetiredStudy) => DomainValidation[StudyEvent],
     applyEvent: StudyEvent => Unit): Unit = {

    def internal(cmd: T, study: Study): DomainValidation[StudyEvent] =
      study match {
        case s: RetiredStudy => cmdToEvent(cmd, s)
        case s => InvalidStatus(s"study not retired: ${study.id}").failureNel
      }

    processUpdateCmd(cmd, internal, applyEvent)
  }

  private def onValidEventStudyAndVersion(event: StudyEvent,
                                  eventType: Boolean,
                                  eventVersion: Long)
                                 (fn: Study => Unit): Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      studyRepository.getByKey(StudyId(event.id)).fold(
        err => log.error(s"study from event does not exist: $err"),
        study => {
          if (study.version != eventVersion) {
            log.error(s"event version check failed: study version: ${study.version}, event: $event")
          } else {
            fn(study)
          }
        }
      )
    }
  }

  private def onValidEventDisabledStudyAndVersion(event: StudyEvent,
                                          eventType: Boolean,
                                          eventVersion: Long)
                                         (fn: DisabledStudy => Unit): Unit = {
    onValidEventStudyAndVersion(event, eventType, eventVersion) {
      case study: DisabledStudy => fn(study)
      case study => log.error(s"$study for $event is not disabled")
    }
  }

  private def onValidEventEnabledStudyAndVersion(event: StudyEvent,
                                         eventType: Boolean,
                                         eventVersion: Long)
                                        (fn: EnabledStudy => Unit): Unit = {
    onValidEventStudyAndVersion(event, eventType, eventVersion) {
      case study: EnabledStudy => fn(study)
      case study => log.error(s"$study for $event is not disabled")
    }
  }

  private def onValidEventRetiredStudyAndVersion(event: StudyEvent,
                                         eventType: Boolean,
                                         eventVersion: Long)
                                        (fn: RetiredStudy => Unit): Unit = {
    onValidEventStudyAndVersion(event, eventType, eventVersion) {
      case study: RetiredStudy => fn(study)
      case study => log.error(s"$study for $event is not disabled")
    }
  }

  private def applyAddedEvent(event: StudyEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded

      DisabledStudy.create(id                         = StudyId(event.id),
                           version                    = 0L,
                           name                       = addedEvent.getName,
                           description                = addedEvent.description,
                           annotationTypes = Set.empty
      ).fold (
        err => log.error(s"could not add study from event: $event"),
        s => {
          studyRepository.put(
            s.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  private def applyNameUpdatedEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isNameUpdated,
                                        event.getNameUpdated.getVersion) { study =>
      val nameUpdatedEvent = event.getNameUpdated

      study.withName(nameUpdatedEvent.getName).fold(
        err => log.error(s"updating study from event failed: $err"),
        s => {
          studyRepository.put(
            s.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyDescriptionUpdatedEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isDescriptionUpdated,
                                        event.getDescriptionUpdated.getVersion) { study =>
      val descriptionUpdatedEvent = event.getDescriptionUpdated

      study.withDescription(descriptionUpdatedEvent.description).fold(
        err => log.error(s"updating study from event failed: $err"),
        s => {
          studyRepository.put(
            s.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyParticipantAnnotationTypeAddedEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isAnnotationTypeAdded,
                                        event.getAnnotationTypeAdded.getVersion) { study =>
      val eventAnnotationType = event.getAnnotationTypeAdded.getAnnotationType

      study.withParticipantAnnotationType(
        AnnotationType(eventAnnotationType.getUniqueId,
                       eventAnnotationType.getName,
                       eventAnnotationType.description,
                       AnnotationValueType.withName(eventAnnotationType.getValueType),
                       eventAnnotationType.maxValueCount,
                       eventAnnotationType.options,
                       eventAnnotationType.getRequired)).fold(
        err => log.error(s"updating study from event failed: $err"),
        s => {
          studyRepository.put(
            s.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyParticipantAnnotationTypeUpdatedEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isAnnotationTypeUpdated,
                                        event.getAnnotationTypeUpdated.getVersion) { study =>
      val eventAnnotationType = event.getAnnotationTypeUpdated.getAnnotationType

      study.withParticipantAnnotationType(
        AnnotationType(eventAnnotationType.getUniqueId,
                       eventAnnotationType.getName,
                       eventAnnotationType.description,
                       AnnotationValueType.withName(eventAnnotationType.getValueType),
                       eventAnnotationType.maxValueCount,
                       eventAnnotationType.options,
                       eventAnnotationType.getRequired)).fold(
        err => log.error(s"updating study from event failed: $err"),
        s => {
          studyRepository.put(
            s.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyParticipantAnnotationTypeRemovedEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isAnnotationTypeRemoved,
                                        event.getAnnotationTypeRemoved.getVersion) { study =>
      val removedEvent = event.getAnnotationTypeRemoved

      study.removeParticipantAnnotationType(removedEvent.getUniqueId).fold(
        err => log.error(s"updating study from event failed: $err"),
        s => {
          studyRepository.put(
            s.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyEnabledEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isEnabled,
                                        event.getEnabled.getVersion) { study =>
      study.enable.fold(
        err => log.error(s"updating study from event failed: $err"),
        s => {
          studyRepository.put(
            s.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyDisabledEvent(event: StudyEvent) : Unit = {
    onValidEventEnabledStudyAndVersion(event,
                                       event.eventType.isDisabled,
                                       event.getDisabled.getVersion) { study =>
      study.disable.fold(
        err => log.error(s"updating study from event failed: $err"),
        s => {
          studyRepository.put(
            s.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyRetiredEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isRetired,
                                        event.getRetired.getVersion) { study =>
      study.retire.fold(
        err => log.error(s"updating study from event failed: $err"),
        s => {
          studyRepository.put(
            s.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyUnretiredEvent(event: StudyEvent) : Unit = {
    onValidEventRetiredStudyAndVersion(event,
                                       event.eventType.isUnretired,
                                       event.getUnretired.getVersion) { study =>
      study.unretire.fold(
        err => log.error(s"updating study from event failed: $err"),
        s => {
          studyRepository.put(
            s.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  val ErrMsgNameExists = "name already used"

  private def nameAvailable(name: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, studyRepository, ErrMsgNameExists) { item =>
      item.name == name
    }
  }

  private def nameAvailable(name: String, excludeStudyId: StudyId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, studyRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.id != excludeStudyId)
    }
  }

  testData.addMultipleStudies

  // ------------------- REMOVE -------------------

  private def updateStudy(cmd: StudyModifyCommand)(fn: Study => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    for {
      study        <- studyRepository.getByKey(StudyId(cmd.id))
      validVersion <- study.requireVersion(cmd.expectedVersion)
      updatedStudy <- fn(study)
    } yield updatedStudy
  }

  private def updateDisabled(cmd: StudyModifyCommand)(fn: DisabledStudy => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    updateStudy(cmd) {
      case study: DisabledStudy => fn(study)
      case study => InvalidStatus(s"study not disabled: ${study.id}").failureNel
    }
  }

  private def updateEnabled(cmd: StudyModifyCommand)(fn: EnabledStudy => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    updateStudy(cmd) {
      case study: EnabledStudy => fn(study)
      case study => InvalidStatus(s"study not enabled: ${study.id}").failureNel
    }
  }

  private def updateRetired(cmd: StudyModifyCommand)(fn: RetiredStudy => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    updateStudy(cmd) {
      case study: RetiredStudy => fn(study)
      case study => InvalidStatus(s"study not retired: ${study.id}").failureNel
    }
  }


}
