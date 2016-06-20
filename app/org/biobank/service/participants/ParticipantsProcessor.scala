package org.biobank.service.participants

import akka.actor._
import akka.persistence.SnapshotOffer
import javax.inject.Inject
import org.biobank.domain.{
  Annotation,
  DomainValidation,
  DomainError
}
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.ParticipantEvents._
import org.biobank.service.Processor
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

  private def addCmdToEvent(cmd: AddParticipantCmd): DomainValidation[ParticipantEvent] = {
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
                                       participant: Participant): DomainValidation[ParticipantEvent] = {
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
                                      participant: Participant): DomainValidation[ParticipantEvent] = {
    for {
      annotation         <- Annotation.create(cmd.annotationTypeId,
                                              cmd.stringValue,
                                              cmd.numberValue,
                                              cmd.selectedValues)
      allAnnotations     <- (participant.annotations + annotation).success
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
                                          participant: Participant): DomainValidation[ParticipantEvent] = {
    for {
      annotType <- {
        study.annotationTypes
          .find { x => x.uniqueId == cmd.annotationTypeId }
          .toSuccessNel(s"annotation type with ID does not exist: ${cmd.annotationTypeId}")
      }
      notRequired <- {
        if (annotType.required) DomainError(s"annotation is required").failureNel
        else true.success
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
    validation: (T, Study, Participant) => DomainValidation[ParticipantEvent],
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

      Participant.create(studyId      = StudyId(addedEvent.getStudyId),
                         id           = ParticipantId(event.id),
                         version      = 0L,
                         uniqueId     = addedEvent.getUniqueId,
                         annotations  = addedEvent.annotations.map(annotationFromEvent).toSet
      ).fold(
        err => log.error(s"could not add collection event from event: $err, $event"),
        p => {
          participantRepository.put(p)
          ()
        }
      )
    }
  }

  private def onValidEventAndVersion(event:        ParticipantEvent,
                             eventType:    Boolean,
                             eventVersion: Long)
                            (fn: Participant => Unit): Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      participantRepository.getByKey(ParticipantId(event.id)).fold(
        err => log.error(s"participant from event does not exist: $err"),
        participant => {
          if (participant.version != eventVersion) {
            log.error(s"event version check failed: participant version: ${participant.version}, event: $event")
          } else {
            fn(participant)
          }
        }
      )
    }
  }

  private def storeUpdated(participant: Participant, time: String): Unit = {
    participantRepository.put(
      participant.copy(timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(time))))
    ()
  }

  private def applyUniqueIdUpdatedEvent(event: ParticipantEvent) = {
    onValidEventAndVersion(event,
                           event.eventType.isUniqueIdUpdated,
                           event.getUniqueIdUpdated.getVersion) { participant =>
      participant.withUniqueId(event.getUniqueIdUpdated.getUniqueId).fold(
        err => log.error(s"updating participant from event failed: $err"),
        p => storeUpdated(p, event.getTime)
      )
    }
  }

  private def applyAnnotationAddedEvent(event: ParticipantEvent) = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationAdded,
                           event.getAnnotationAdded.getVersion) { participant =>
      val addedEvent = event.getAnnotationAdded
      participant.withAnnotation(annotationFromEvent(addedEvent.getAnnotation)).fold(
        err => log.error(s"updating participant from event failed: $err"),
        p => storeUpdated(p, event.getTime)
      )
    }
  }

  private def applyAnnotationRemovedEvent(event: ParticipantEvent) = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationRemoved,
                           event.getAnnotationRemoved.getVersion) { participant =>
      participant.withoutAnnotation(event.getAnnotationRemoved.getAnnotationTypeId).fold(
        err => log.error(s"removing annotation from collection event failed: $err"),
        p => storeUpdated(p, event.getTime)
      )
    }
  }

  /** Searches the repository for a matching item.
   */
  protected def uniqueIdAvailableMatcher(uniqueId: String)(matcher: Participant => Boolean)
      : DomainValidation[Boolean] = {
    val exists = participantRepository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) {
      DomainError(s"$ErrMsgUniqueIdExists: $uniqueId").failureNel
    } else {
      true.success
    }
  }

  private def uniqueIdAvailable(uniqueId: String): DomainValidation[Boolean] = {
    uniqueIdAvailableMatcher(uniqueId){ item =>
      item.uniqueId == uniqueId
    }
  }

  private def uniqueIdAvailable(uniqueId: String, excludeParticipantId: ParticipantId)
      : DomainValidation[Boolean] = {
    uniqueIdAvailableMatcher(uniqueId){ item =>
      (item.uniqueId == uniqueId) && (item.id != excludeParticipantId)
    }
  }

}
