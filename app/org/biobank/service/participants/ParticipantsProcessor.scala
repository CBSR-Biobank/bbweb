package org.biobank.service.participants

import akka.actor._
import akka.persistence.SnapshotOffer
import javax.inject.Inject
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.Annotation
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.ParticipantEvents._
import org.biobank.service.{Processor, ServiceError, ServiceValidation}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object ParticipantsProcessor {

  def props = Props[ParticipantsProcessor]

}

/**
 *
 */
class ParticipantsProcessor @Inject() (val participantRepository:     ParticipantRepository,
                                       val studyRepository:           StudyRepository)
    extends Processor {

  import ParticipantEvent.EventType
  import org.biobank.infrastructure.event.EventUtils._

  override def persistenceId = "participant-processor-id"

  case class SnapshotState(participants: Set[Participant])

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val receiveRecover: Receive = {
    case event: ParticipantEvent => event.eventType match {
      case et: EventType.Added             => applyAddedEvent(event)
      case et: EventType.UniqueIdUpdated   => applyUniqueIdUpdatedEvent(event)
      case et: EventType.AnnotationAdded   => applyAnnotationAddedEvent(event)
      case et: EventType.AnnotationRemoved => applyAnnotationRemovedEvent(event)

      case event => log.error(s"event not handled: $event")
    }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.participants.foreach{ participant => participantRepository.put(participant) }
  }

  /**
   * These are the commands that are requested. A command can fail, and will send the failure as a response
   * back to the user. Each valid command generates one or more events and is journaled.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val receiveCommand: Receive = {
    case cmd: AddParticipantCmd =>
      process(addCmdToEvent(cmd))(applyAddedEvent)

    case cmd: UpdateParticipantUniqueIdCmd =>
      processUpdateCmd(cmd, updateUniqueIdCmdToEvent, applyUniqueIdUpdatedEvent)

    case cmd: ParticipantAddAnnotationCmd =>
      processUpdateCmd(cmd, addAnnotationCmdToEvent, applyAnnotationAddedEvent)

    case cmd: ParticipantRemoveAnnotationCmd =>
      processUpdateCmd(cmd, removeAnnotationCmdToEvent, applyAnnotationRemovedEvent)

    case "snap" =>
      saveSnapshot(SnapshotState(participantRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"ParticipantsProcessor: message not handled: $cmd")
  }

  val ErrMsgUniqueIdExists = "participant with unique ID already exists"

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
                                              cmd.annotations.toSet)
    } yield ParticipantEvent(newParticipant.id.id).update(
      _.userId            := cmd.userId,
      _.time              := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.added.studyId     := newParticipant.studyId.id,
      _.added.uniqueId    := cmd.uniqueId,
      _.added.annotations := cmd.annotations.map { annotationToEvent(_) })
  }

  private def updateUniqueIdCmdToEvent(cmd:         UpdateParticipantUniqueIdCmd,
                                       study:       Study,
                                       participant: Participant): ServiceValidation[ParticipantEvent] = {
    for {
      uniqueIdAvailable  <- uniqueIdAvailable(cmd.uniqueId, participant.id)
      updatedParticipant <- participant.withUniqueId(cmd.uniqueId)
    } yield ParticipantEvent(updatedParticipant.id.id).update(
      _.userId                   := cmd.userId,
      _.time                     := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.uniqueIdUpdated.version  := cmd.expectedVersion,
      _.uniqueIdUpdated.uniqueId := cmd.uniqueId)
  }

  private def addAnnotationCmdToEvent(cmd:          ParticipantAddAnnotationCmd,
                                      study:       Study,
                                      participant: Participant): ServiceValidation[ParticipantEvent] = {
    for {
      annotation         <- Annotation.create(cmd.annotationTypeId,
                                              cmd.stringValue,
                                              cmd.numberValue,
                                              cmd.selectedValues)
      allAnnotations     <- (participant.annotations + annotation).successNel[String]
      validAnnotation    <- Annotation.validateAnnotations(study.annotationTypes,
                                                           allAnnotations.toList)
      updatedParticipant <- participant.withAnnotation(annotation)
    } yield ParticipantEvent(updatedParticipant.id.id).update(
      _.userId                     := cmd.userId,
      _.time                       := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.annotationAdded.version    := cmd.expectedVersion,
      _.annotationAdded.annotation := annotationToEvent(annotation))
  }

  private def removeAnnotationCmdToEvent(cmd:        ParticipantRemoveAnnotationCmd,
                                          study:       Study,
                                          participant: Participant): ServiceValidation[ParticipantEvent] = {
    for {
      annotType <- {
        study.annotationTypes
          .find { x => x.uniqueId == cmd.annotationTypeId }
          .toSuccessNel(s"annotation type with ID does not exist: ${cmd.annotationTypeId}")
      }
      notRequired <- {
        if (annotType.required) ServiceError(s"annotation is required").failureNel[Boolean]
        else true.successNel[String]
      }
      updatedParticipant <- participant.withoutAnnotation(cmd.annotationTypeId)
    } yield ParticipantEvent(updatedParticipant.id.id).update(
      _.userId                             := cmd.userId,
      _.time                               := ISODateTimeFormat.dateTime.print(DateTime.now),
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
                                 annotations  = addedEvent.annotations.map(annotationFromEvent).toSet)

      if (v.isFailure) {
        log.error(s"could not add collection event from event: $v, $event")
      }

      v.foreach { p => participantRepository.put(p) }
    }
  }

  private def onValidEventAndVersion(event:        ParticipantEvent,
                                     eventType:    Boolean,
                                     eventVersion: Long)
                                    (applyEvent: (Participant, DateTime) => ServiceValidation[Boolean])
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
            val eventTime = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)
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
      val v = participant.withUniqueId(event.getUniqueIdUpdated.getUniqueId)
      v.foreach( p => participantRepository.put(p.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  private def applyAnnotationAddedEvent(event: ParticipantEvent) = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationAdded,
                           event.getAnnotationAdded.getVersion) { (participant, eventTime) =>
      val v = participant.withAnnotation(annotationFromEvent(event.getAnnotationAdded.getAnnotation))
      v.foreach( p => participantRepository.put(p.copy(timeModified = Some(eventTime))))
      v.map(_ => true)
    }
  }

  private def applyAnnotationRemovedEvent(event: ParticipantEvent) = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationRemoved,
                           event.getAnnotationRemoved.getVersion) { (participant, eventTime) =>
      val v = participant.withoutAnnotation(event.getAnnotationRemoved.getAnnotationTypeId)
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

}
