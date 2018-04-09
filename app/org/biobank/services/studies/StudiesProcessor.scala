package org.biobank.services.studies

import akka.actor._
import akka.persistence.{RecoveryCompleted, SaveSnapshotSuccess, SaveSnapshotFailure, SnapshotOffer}
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject._
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.studies._
import org.biobank.infrastructure.commands.StudyCommands._
import org.biobank.infrastructure.events.EventUtils
import org.biobank.infrastructure.events.StudyEvents._
import org.biobank.services.{Processor, ServiceError, ServiceValidation, SnapshotWriter}
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object StudiesProcessor {

  def props: Props = Props[StudiesProcessor]

  final case class SnapshotState(studies: Set[Study])

  implicit val snapshotStateFormat: Format[SnapshotState] = Json.format[SnapshotState]

}

/**
 * The StudiesProcessor is responsible for maintaining state changes for the [[domain.studies.Study]]
 * aggregate. This particular processor uses [[https://doc.akka.io/docs/akka/2.5/persistence.html Akka's
 * PersistentActor]]. It receives Commands and if valid will persist the generated events, whereafter it will
 * updated the current state of the [[domain.studies.Study]] being processed.
 */
class StudiesProcessor @Inject() (val studyRepository:               StudyRepository,
                                  val collectionEventTypeRepository: CollectionEventTypeRepository,
                                  val snapshotWriter:                SnapshotWriter)
    extends Processor {
  import StudiesProcessor._
  import org.biobank.CommonValidations._

  override def persistenceId: String = "studies-processor-id"

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var replyTo: Option[ActorRef] = None

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
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

    case SnapshotOffer(_, snapshotFilename: String) =>
      applySnapshot(snapshotFilename)

    case RecoveryCompleted =>
      log.debug(s"StudiesProcessor: recovery completed")
  }

  /**
   * These are the commands that are received. A command can fail, and will send the failure as a response
   * back to the higher layer. Each valid command generates one or more events and is journaled.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Throw"))
  val receiveCommand: Receive = {
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
      mySaveSnapshot
      replyTo = Some(sender())

    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"snapshot saved successfully: ${metadata}")
      replyTo.foreach(_ ! akka.actor.Status.Success(s"snapshot saved: $metadata"))
      replyTo = None

    case SaveSnapshotFailure(metadata, reason) =>
      log.debug(s"snapshot save error: ${metadata}")
      replyTo.foreach(_ ! akka.actor.Status.Failure(reason))
      replyTo = None

    case "persistence_restart" =>
      throw new Exception("Intentionally throwing exception to test persistence by restarting the actor")

    case cmd => log.error(s"StudiesProcessor: message not handled: $cmd")
  }

  private def mySaveSnapshot(): Unit = {
    val snapshotState = SnapshotState(studyRepository.getValues.toSet)
    val filename = snapshotWriter.save(persistenceId, Json.toJson(snapshotState).toString)
    log.debug(s"saved snapshot to: $filename")
    saveSnapshot(filename)
  }

  private def applySnapshot(filename: String): Unit = {
    log.debug(s"snapshot recovery file: $filename")
    val fileContents = snapshotWriter.load(filename);
    Json.parse(fileContents).validate[SnapshotState].fold(
      errors => log.error(s"could not apply snapshot: $filename: $errors"),
      snapshot =>  {
        log.debug(s"snapshot contains ${snapshot.studies.size} studies")
        snapshot.studies.foreach(studyRepository.put)
      }
    )
  }

  private def addCmdToEvent(cmd: AddStudyCmd): ServiceValidation[StudyEvent] = {
    for {
      name  <- nameAvailable(cmd.name)
      id    <- validNewIdentity(studyRepository.nextIdentity, studyRepository)
      study <- DisabledStudy.create(id,
                                    0L,
                                    cmd.name,
                                    cmd.description,
                                    Set.empty)
    } yield {
      StudyEvent(study.id.id).update(
        _.optionalSessionUserId     := cmd.sessionUserId,
        _.time                      := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.added.name                := cmd.name,
        _.added.optionalDescription := cmd.description)
    }
  }

  private def updateNameCmdToEvent(cmd: UpdateStudyNameCmd, study: DisabledStudy)
      : ServiceValidation[StudyEvent] = {
    (nameAvailable(cmd.name, StudyId(cmd.id)) |@|
       study.withName(cmd.name)) { case (_, _) =>
        StudyEvent(study.id.id).update(
          _.optionalSessionUserId := cmd.sessionUserId,
          _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
          _.nameUpdated.version   := cmd.expectedVersion,
          _.nameUpdated.name      := cmd.name)
    }
  }

  private def updateDescriptionCmdToEvent(cmd: UpdateStudyDescriptionCmd, study: DisabledStudy)
      : ServiceValidation[StudyEvent] = {
    study.withDescription(cmd.description).map { _ =>
      StudyEvent(study.id.id).update(
        _.optionalSessionUserId                  := cmd.sessionUserId,
        _.time                                   := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.descriptionUpdated.version             := cmd.expectedVersion,
        _.descriptionUpdated.optionalDescription := cmd.description)
    }
  }

  private def addAnnotationTypeCmdToEvent(cmd:   StudyAddParticipantAnnotationTypeCmd,
                                          study: DisabledStudy)
      : ServiceValidation[StudyEvent] = {
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
      _.optionalSessionUserId              := cmd.sessionUserId,
      _.time                               := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.annotationTypeAdded.version        := cmd.expectedVersion,
      _.annotationTypeAdded.annotationType := EventUtils.annotationTypeToEvent(annotationType))
  }

  private def updateAnnotationTypeCmdToEvent(cmd:   StudyUpdateParticipantAnnotationTypeCmd,
                                             study: DisabledStudy)
      : ServiceValidation[StudyEvent] = {
    for {
      valid          <- AnnotationType.create(name          = cmd.name,
                                              description   = cmd.description,
                                              valueType     = cmd.valueType,
                                              maxValueCount = cmd.maxValueCount,
                                              options       = cmd.options,
                                              required      = cmd.required)
      annotationType <- valid.copy(id = AnnotationTypeId(cmd.annotationTypeId)).successNel[String]
      updatedStudy   <- study.withParticipantAnnotationType(annotationType)
    } yield {
      val timeStr = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      StudyEvent(study.id.id).update(
        _.optionalSessionUserId                := cmd.sessionUserId,
        _.time                                 := timeStr,
        _.annotationTypeUpdated.version        := cmd.expectedVersion,
        _.annotationTypeUpdated.annotationType := EventUtils.annotationTypeToEvent(annotationType))
    }
  }

  private def removeAnnotationTypeCmdToEvent(cmd: UpdateStudyRemoveAnnotationTypeCmd,
                                             study: DisabledStudy)
      : ServiceValidation[StudyEvent] = {
    study.removeParticipantAnnotationType(AnnotationTypeId(cmd.annotationTypeId)) map { s =>
      StudyEvent(study.id.id).update(
        _.optionalSessionUserId          := cmd.sessionUserId,
        _.time                           := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.annotationTypeRemoved.version  := cmd.expectedVersion,
        _.annotationTypeRemoved.id       := cmd.annotationTypeId)
    }
  }

  private def enableCmdToEvent(cmd: EnableStudyCmd,
                               study: DisabledStudy)
      : ServiceValidation[StudyEvent] = {
    val collectionEventTypes = collectionEventTypeRepository.allForStudy(study.id)

    if (collectionEventTypes.isEmpty) {
      EntityCriteriaError("no collection event types").failureNel[StudyEvent]
    } else if (collectionEventTypes.filter { cet => cet.hasSpecimenDefinitions }.isEmpty) {
      EntityCriteriaError("no collection specimen specs").failureNel[StudyEvent]
    } else {
      study.enable.map { _ =>
        StudyEvent(study.id.id).update(
          _.optionalSessionUserId := cmd.sessionUserId,
          _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
          _.enabled.version       := cmd.expectedVersion)
      }
    }
  }

  private def disableCmdToEvent(cmd: DisableStudyCmd,
                                study: EnabledStudy)
      : ServiceValidation[StudyEvent] = {
    study.disable map { _ =>
      StudyEvent(study.id.id).update(
        _.optionalSessionUserId := cmd.sessionUserId,
        _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.disabled.version      := cmd.expectedVersion)
    }
  }

  private def retireCmdToEvent(cmd: RetireStudyCmd,
                               study: DisabledStudy)
      : ServiceValidation[StudyEvent] = {
    study.retire map { _ =>
      StudyEvent(study.id.id).update(
        _.optionalSessionUserId := cmd.sessionUserId,
        _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.retired.version       := cmd.expectedVersion)
    }
  }

  private def unretireCmdToEvent(cmd: UnretireStudyCmd,
                                 study: RetiredStudy)
      : ServiceValidation[StudyEvent] = {
    study.unretire map { _ =>
      StudyEvent(study.id.id).update(
        _.optionalSessionUserId := cmd.sessionUserId,
        _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.unretired.version     := cmd.expectedVersion)
    }
  }

  private def processUpdateCmd[T <: StudyModifyCommand]
    (cmd: T,
     cmdToEvent: (T, Study) => ServiceValidation[StudyEvent],
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
     cmdToEvent: (T, DisabledStudy) => ServiceValidation[StudyEvent],
     applyEvent: StudyEvent => Unit): Unit = {

    def internal(cmd: T, study: Study): ServiceValidation[StudyEvent] =
      study match {
        case s: DisabledStudy => cmdToEvent(cmd, s)
        case s => InvalidStatus(s"study not disabled: ${study.id}").failureNel[StudyEvent]
      }

    processUpdateCmd(cmd, internal, applyEvent)
  }

  private def processUpdateCmdOnEnabledStudy[T <: StudyModifyCommand]
    (cmd: T,
     cmdToEvent: (T, EnabledStudy) => ServiceValidation[StudyEvent],
     applyEvent: StudyEvent => Unit): Unit = {

    def internal(cmd: T, study: Study): ServiceValidation[StudyEvent] =
      study match {
        case s: EnabledStudy => cmdToEvent(cmd, s)
        case s => InvalidStatus(s"study not enabled: ${study.id}").failureNel[StudyEvent]
      }

    processUpdateCmd(cmd, internal, applyEvent)
  }

  private def processUpdateCmdOnRetiredStudy[T <: StudyModifyCommand]
    (cmd: T,
     cmdToEvent: (T, RetiredStudy) => ServiceValidation[StudyEvent],
     applyEvent: StudyEvent => Unit): Unit = {

    def internal(cmd: T, study: Study): ServiceValidation[StudyEvent] =
      study match {
        case s: RetiredStudy => cmdToEvent(cmd, s)
        case s => InvalidStatus(s"study not retired: ${study.id}").failureNel[StudyEvent]
      }

    processUpdateCmd(cmd, internal, applyEvent)
  }

  private def onValidEventStudyAndVersion(event: StudyEvent,
                                          eventType: Boolean,
                                          eventVersion: Long)
                                         (applyEvent: (Study, OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      studyRepository.getByKey(StudyId(event.id)).fold(
        err => log.error(s"study from event does not exist: $err"),
        study => {
          if (study.version != eventVersion) {
            log.error(s"event version check failed: study version: ${study.version}, event: $event")
          } else {
            val eventTime = OffsetDateTime.parse(event.getTime)
            val update = applyEvent(study, eventTime)

            if (update.isFailure) {
              log.error(s"study update from event failed: $update")
            }
          }
        }
      )
    }
  }

  private def onValidEventDisabledStudyAndVersion(event: StudyEvent,
                                                  eventType: Boolean,
                                                  eventVersion: Long)
                                                 (applyEvent: (DisabledStudy, OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    onValidEventStudyAndVersion(event, eventType, eventVersion) { (study, eventTime) =>
      study match {
        case study: DisabledStudy => applyEvent(study, eventTime)
        case study => ServiceError(s"study not disabled: $event").failureNel[Boolean]
      }
    }
  }

  private def onValidEventEnabledStudyAndVersion(event: StudyEvent,
                                                 eventType: Boolean,
                                                 eventVersion: Long)
                                                 (applyEvent: (EnabledStudy, OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    onValidEventStudyAndVersion(event, eventType, eventVersion) { (study, eventTime) =>
      study match {
        case study: EnabledStudy => applyEvent(study, eventTime)
        case study => ServiceError(s"study not enabled: $event").failureNel[Boolean]
      }
    }
  }

  private def onValidEventRetiredStudyAndVersion(event: StudyEvent,
                                                 eventType: Boolean,
                                                 eventVersion: Long)
                                                 (applyEvent: (RetiredStudy, OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    onValidEventStudyAndVersion(event, eventType, eventVersion) { (study, eventTime) =>
      study match {
        case study: RetiredStudy => applyEvent(study, eventTime)
        case study => ServiceError(s"study not retired: $event").failureNel[Boolean]
      }
    }
  }

  private def applyAddedEvent(event: StudyEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded

      val v = DisabledStudy.create(id                         = StudyId(event.id),
                                   version                    = 0L,
                                   name                       = addedEvent.getName,
                                   description                = addedEvent.description,
                                   annotationTypes = Set.empty).map { s =>
          // the slug needs to be recalculated in case there are mutliples
          s.copy(slug     = studyRepository.uniqueSlugFromStr(s.name),
                 timeAdded = OffsetDateTime.parse(event.getTime))
        }

      if (v.isFailure) {
        log.error(s"could not add study from event: $event")
      }
      v.foreach(studyRepository.put)
    }
  }

  private def putDisabledOnSuccess(validation: ServiceValidation[DisabledStudy],
                                   eventTime: OffsetDateTime): ServiceValidation[Boolean] = {
    validation.foreach { s => studyRepository.put(s.copy(timeModified = Some(eventTime))) }
    validation.map { _ => true }
  }

  private def putEnabledOnSuccess(validation: ServiceValidation[EnabledStudy],
                                   eventTime: OffsetDateTime): ServiceValidation[Boolean] = {
    validation.foreach { s => studyRepository.put(s.copy(timeModified = Some(eventTime))) }
    validation.map { _ => true }
  }

  private def putRetiredOnSuccess(validation: ServiceValidation[RetiredStudy],
                                   eventTime: OffsetDateTime): ServiceValidation[Boolean] = {
    validation.foreach { s => studyRepository.put(s.copy(timeModified = Some(eventTime))) }
    validation.map { _ => true }
  }

  private def applyNameUpdatedEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isNameUpdated,
                                        event.getNameUpdated.getVersion) { (study, eventTime) =>

      val v = study.withName(event.getNameUpdated.getName).map { s =>
          s.copy(slug = studyRepository.uniqueSlugFromStr(s.name))
        }
      putDisabledOnSuccess(v, eventTime)
    }
  }

  private def applyDescriptionUpdatedEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isDescriptionUpdated,
                                        event.getDescriptionUpdated.getVersion) { (study, eventTime) =>
      putDisabledOnSuccess(study.withDescription(event.getDescriptionUpdated.description), eventTime)
    }
  }

  private def applyParticipantAnnotationTypeAddedEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isAnnotationTypeAdded,
                                        event.getAnnotationTypeAdded.getVersion) { (study, eventTime) =>
      val eventAnnotationType = event.getAnnotationTypeAdded.getAnnotationType
      val annotationType = AnnotationType(AnnotationTypeId(eventAnnotationType.getId),
                                          Slug(eventAnnotationType.getName),
                                          eventAnnotationType.getName,
                                          eventAnnotationType.description,
                                          AnnotationValueType.withName(eventAnnotationType.getValueType),
                                          eventAnnotationType.maxValueCount,
                                          eventAnnotationType.options,
                                          eventAnnotationType.getRequired)
      putDisabledOnSuccess(study.withParticipantAnnotationType(annotationType), eventTime)
    }
  }

  private def applyParticipantAnnotationTypeUpdatedEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isAnnotationTypeUpdated,
                                        event.getAnnotationTypeUpdated.getVersion) { (study, eventTime) =>
      val eventAnnotationType = event.getAnnotationTypeUpdated.getAnnotationType
      val annotationType = AnnotationType(AnnotationTypeId(eventAnnotationType.getId),
                                          Slug(eventAnnotationType.getName),
                                          eventAnnotationType.getName,
                                          eventAnnotationType.description,
                                          AnnotationValueType.withName(eventAnnotationType.getValueType),
                                          eventAnnotationType.maxValueCount,
                                          eventAnnotationType.options,
                                          eventAnnotationType.getRequired)
      putDisabledOnSuccess(study.withParticipantAnnotationType(annotationType), eventTime)
    }
  }

  private def applyParticipantAnnotationTypeRemovedEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isAnnotationTypeRemoved,
                                        event.getAnnotationTypeRemoved.getVersion) { (study, eventTime) =>
      putDisabledOnSuccess(
        study.removeParticipantAnnotationType(AnnotationTypeId(event.getAnnotationTypeRemoved.getId)),
        eventTime)
    }
  }

  private def applyEnabledEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isEnabled,
                                        event.getEnabled.getVersion) { (study, eventTime) =>
      putEnabledOnSuccess(study.enable, eventTime)
    }
  }

  private def applyDisabledEvent(event: StudyEvent) : Unit = {
    onValidEventEnabledStudyAndVersion(event,
                                       event.eventType.isDisabled,
                                       event.getDisabled.getVersion) { (study, eventTime) =>
      putDisabledOnSuccess(study.disable, eventTime)
    }
  }

  private def applyRetiredEvent(event: StudyEvent) : Unit = {
    onValidEventDisabledStudyAndVersion(event,
                                        event.eventType.isRetired,
                                        event.getRetired.getVersion) { (study, eventTime) =>
      putRetiredOnSuccess(study.retire, eventTime)
    }
  }

  private def applyUnretiredEvent(event: StudyEvent) : Unit = {
    onValidEventRetiredStudyAndVersion(event,
                                       event.eventType.isUnretired,
                                       event.getUnretired.getVersion) { (study, eventTime) =>
      putDisabledOnSuccess(study.unretire, eventTime)
    }
  }

  val ErrMsgNameExists: String = "name already used"

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, studyRepository, ErrMsgNameExists) { item =>
      item.name == name
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String, excludeStudyId: StudyId): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, studyRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.id != excludeStudyId)
    }
  }

  private def init(): Unit = {
    studyRepository.init
  }

  init
}
