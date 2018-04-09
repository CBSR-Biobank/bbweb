package org.biobank.services.participants

import akka.actor._
import akka.persistence.{RecoveryCompleted, SaveSnapshotSuccess, SaveSnapshotFailure, SnapshotOffer}
import com.github.ghik.silencer.silent
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import org.biobank.domain.annotations.AnnotationTypeId
import org.biobank.domain.participants._
import org.biobank.domain.studies._
import org.biobank.domain.annotations.Annotation
import org.biobank.infrastructure.commands.ParticipantCommands._
import org.biobank.infrastructure.events.ParticipantEvents._
import org.biobank.services.{Processor, ServiceError, ServiceValidation, SnapshotWriter}
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object ParticipantsProcessor {

  def props: Props = Props[ParticipantsProcessor]

  final case class SnapshotState(participants: Set[Participant])

  implicit val snapshotStateFormat: Format[SnapshotState] = Json.format[SnapshotState]

}

/**
 *
 */
class ParticipantsProcessor @Inject() (val participantRepository: ParticipantRepository,
                                       val studyRepository:       StudyRepository,
                                       val snapshotWriter:        SnapshotWriter)
    extends Processor {

  import ParticipantsProcessor._
  import ParticipantEvent.EventType
  import org.biobank.infrastructure.events.EventUtils._

  override def persistenceId: String = "participant-processor-id"

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var replyTo: Option[ActorRef] = None


  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val receiveRecover: Receive = {
    case event: ParticipantEvent =>
      log.debug(s"ParticipantsProcessor: receiveRecover: $event")
      event.eventType match {
        case et: EventType.Added             => applyAddedEvent(event)
        case et: EventType.UniqueIdUpdated   => applyUniqueIdUpdatedEvent(event)
        case et: EventType.AnnotationUpdated => applyAnnotationUpdatedEvent(event)
        case et: EventType.AnnotationRemoved => applyAnnotationRemovedEvent(event)

        case event => log.error(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshotFilename: String) =>
      applySnapshot(snapshotFilename)

    case RecoveryCompleted =>
      log.debug("ParticipantsProcessor: recovery completed")

    case msg =>
      log.error(s"message not handled: $msg")
  }

  /**
   * These are the commands that are requested. A command can fail, and will send the failure as a response
   * back to the user. Each valid command generates one or more events and is journaled.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Throw"))
  val receiveCommand: Receive = {
    case cmd: AddParticipantCmd =>
      process(addCmdToEvent(cmd))(applyAddedEvent)

    case cmd: UpdateParticipantUniqueIdCmd =>
      processUpdateCmd(cmd, updateUniqueIdCmdToEvent, applyUniqueIdUpdatedEvent)

    case cmd: ParticipantUpdateAnnotationCmd =>
      processUpdateCmd(cmd, updateAnnotationCmdToEvent, applyAnnotationUpdatedEvent)

    case cmd: ParticipantRemoveAnnotationCmd =>
      processUpdateCmd(cmd, removeAnnotationCmdToEvent, applyAnnotationRemovedEvent)

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

    case cmd => log.error(s"ParticipantsProcessor: message not handled: $cmd")
  }

  private def mySaveSnapshot(): Unit = {
    val snapshotState = SnapshotState(participantRepository.getValues.toSet)
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
        log.debug(s"snapshot contains ${snapshot.participants.size} participants")
        snapshot.participants.foreach(participantRepository.put)
      }
    )
  }

  val ErrMsgUniqueIdExists: String = "participant with unique ID already exists"

  private def addCmdToEvent(cmd: AddParticipantCmd): ServiceValidation[ParticipantEvent] = {
    for {
      study             <- studyRepository.getEnabled(StudyId(cmd.studyId))
      participantId     <- validNewIdentity(participantRepository.nextIdentity, participantRepository)
      uniqueIdAvailable <- uniqueIdAvailable(cmd.uniqueId)
      annotTypes        <- Annotation.validateAnnotations(study.annotationTypes, cmd.annotations)
      newParticipant    <- Participant.create(study.id,
                                              participantId,
                                              0L,
                                              cmd.uniqueId,
                                              cmd.annotations.toSet,
                                              OffsetDateTime.now)
    } yield ParticipantEvent(newParticipant.id.id).update(
      _.sessionUserId     := cmd.sessionUserId,
      _.time              := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.added.studyId     := newParticipant.studyId.id,
      _.added.uniqueId    := cmd.uniqueId,
      _.added.annotations := cmd.annotations.map { annotationToEvent(_) })
  }

  @silent private def updateUniqueIdCmdToEvent(cmd:         UpdateParticipantUniqueIdCmd,
                                               study:       Study,
                                               participant: Participant): ServiceValidation[ParticipantEvent] = {
    for {
      uniqueIdAvailable  <- uniqueIdAvailable(cmd.uniqueId, participant.id)
      updatedParticipant <- participant.withUniqueId(cmd.uniqueId)
    } yield ParticipantEvent(updatedParticipant.id.id).update(
      _.sessionUserId            := cmd.sessionUserId,
      _.time                     := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.uniqueIdUpdated.version  := cmd.expectedVersion,
      _.uniqueIdUpdated.uniqueId := cmd.uniqueId)
  }

  private def updateAnnotationCmdToEvent(cmd:          ParticipantUpdateAnnotationCmd,
                                         study:       Study,
                                         participant: Participant): ServiceValidation[ParticipantEvent] = {
    val id = AnnotationTypeId(cmd.annotationTypeId)
    for {
      hasAnnotationType  <- {
        study.annotationTypes
          .find(at => at.id == id)
          .toSuccessNel(s"IdNotFound: study does not have annotation type: $id")
      }
      annotation         <- Annotation.create(id,
                                              cmd.stringValue,
                                              cmd.numberValue,
                                              cmd.selectedValues)
      updatedParticipant <- participant.withAnnotation(annotation)
    } yield ParticipantEvent(updatedParticipant.id.id).update(
      _.sessionUserId                := cmd.sessionUserId,
      _.time                         := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.annotationUpdated.version    := cmd.expectedVersion,
      _.annotationUpdated.annotation := annotationToEvent(annotation))
  }

  private def removeAnnotationCmdToEvent(cmd:        ParticipantRemoveAnnotationCmd,
                                          study:       Study,
                                          participant: Participant): ServiceValidation[ParticipantEvent] = {
    for {
      annotType <- {
        study.annotationTypes
          .find { x => x.id.id == cmd.annotationTypeId }
          .toSuccessNel(s"annotation type with ID does not exist: ${cmd.annotationTypeId}")
      }
      notRequired <- {
        if (annotType.required) ServiceError(s"annotation is required").failureNel[Boolean]
        else true.successNel[String]
      }
      updatedParticipant <- participant.withoutAnnotation(AnnotationTypeId(cmd.annotationTypeId))
    } yield ParticipantEvent(updatedParticipant.id.id).update(
      _.sessionUserId                      := cmd.sessionUserId,
      _.time                               := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.annotationRemoved.version          := cmd.expectedVersion,
      _.annotationRemoved.annotationTypeId := cmd.annotationTypeId)
  }

  private def processUpdateCmd[T <: ParticipantModifyCommand](
    cmd: T,
    validation: (T, Study, Participant) => ServiceValidation[ParticipantEvent],
    applyEvent: ParticipantEvent => Unit): Unit = {

    val event = for {
        participant  <- participantRepository.getByKey(ParticipantId(cmd.id))
        enabledStudy <- studyRepository.getEnabled(participant.studyId)
        validVersion <- participant.requireVersion(cmd.expectedVersion)
        event        <- validation(cmd, enabledStudy, participant)
      } yield event
    process(event)(applyEvent)
  }

  private def applyAddedEvent(event: ParticipantEvent) = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded

      val v = Participant.create(studyId      = StudyId(addedEvent.getStudyId),
                                 id           = ParticipantId(event.id),
                                 version      = 0L,
                                 uniqueId     = addedEvent.getUniqueId,
                                 annotations  = addedEvent.annotations.map(annotationFromEvent).toSet,
                                 timeAdded    = OffsetDateTime.parse(event.getTime)).map { p =>
          p.copy(slug = participantRepository.uniqueSlugFromStr(p.uniqueId))
        }

      if (v.isFailure) {
        log.error(s"could not add collection event from event: $v, $event")
      }

      v.foreach(participantRepository.put)
    }
  }

  private def onValidEventAndVersion(event:        ParticipantEvent,
                                     eventType:    Boolean,
                                     eventVersion: Long)
                                    (applyEvent: (Participant, OffsetDateTime) => ServiceValidation[Boolean])
      : Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      participantRepository.getByKey(ParticipantId(event.id)).fold(
        err => log.error(s"participant from event does not exist: $err"),
        participant => {
          if (participant.version != eventVersion) {
            log.error(s"event version check failed: participant version: ${participant.version}, event: $event")
          } else {
            val eventTime = OffsetDateTime.parse(event.getTime)
            val update = applyEvent(participant, eventTime)

            if (update.isFailure) {
              log.error(s"participant update from event failed: $update")
            }
          }
        }
      )
    }
  }

  private def applyUniqueIdUpdatedEvent(event: ParticipantEvent) = {
    onValidEventAndVersion(event,
                           event.eventType.isUniqueIdUpdated,
                           event.getUniqueIdUpdated.getVersion) { (participant, eventTime) =>
      val v = participant.withUniqueId(event.getUniqueIdUpdated.getUniqueId).map { p =>
          p.copy(slug = participantRepository.uniqueSlugFromStr(p.uniqueId))
        }
      v.foreach(p => participantRepository.put(p.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  private def applyAnnotationUpdatedEvent(event: ParticipantEvent) = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationUpdated,
                           event.getAnnotationUpdated.getVersion) { (participant, eventTime) =>
      val v = participant.withAnnotation(annotationFromEvent(event.getAnnotationUpdated.getAnnotation))
      v.foreach( p => participantRepository.put(p.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  private def applyAnnotationRemovedEvent(event: ParticipantEvent) = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationRemoved,
                           event.getAnnotationRemoved.getVersion) { (participant, eventTime) =>
      val v = participant.withoutAnnotation(AnnotationTypeId(event.getAnnotationRemoved.getAnnotationTypeId))
      v.foreach( p => participantRepository.put(p.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  /** Searches the repository for a matching item.
   */
  protected def uniqueIdAvailableMatcher(uniqueId: String)(matcher: Participant => Boolean)
      : ServiceValidation[Boolean] = {
    val exists = participantRepository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) ServiceError(s"$ErrMsgUniqueIdExists: $uniqueId").failureNel[Boolean]
    else true.successNel[String]
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def uniqueIdAvailable(uniqueId: String): ServiceValidation[Boolean] = {
    uniqueIdAvailableMatcher(uniqueId){ item =>
      item.uniqueId == uniqueId
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def uniqueIdAvailable(uniqueId: String, excludeParticipantId: ParticipantId)
      : ServiceValidation[Boolean] = {
    uniqueIdAvailableMatcher(uniqueId){ item =>
      (item.uniqueId == uniqueId) && (item.id != excludeParticipantId)
    }
  }

  private def init(): Unit = {
    participantRepository.init
  }

  init
}
