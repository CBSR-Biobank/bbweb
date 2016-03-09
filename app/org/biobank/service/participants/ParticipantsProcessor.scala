package org.biobank.service.participants

import akka.actor._
import akka.persistence.SnapshotOffer
import javax.inject.{Inject, Named}
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
class ParticipantsProcessor @Inject() (
  @Named("specimens") val specimensProcessor: ActorRef,
  val participantRepository:                  ParticipantRepository,
  val collectionEventRepository:              CollectionEventRepository,
  val studyRepository:                        StudyRepository)
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
      case et: EventType.AnnotationUpdated => applyAnnotationUpdatedEvent(event)
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
    case cmd: AddParticipantCmd              => processAddCmd(cmd)
    case cmd: UpdateParticipantUniqueIdCmd   => processUpdateUniqueIdCmd(cmd)
    case cmd: UpdateParticipantAnnotationCmd => processUpdateAnnotationCmd(cmd)
    case cmd: RemoveParticipantAnnotationCmd => processRemoveAnnotationCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(participantRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"ParticipantsProcessor: message not handled: $cmd")
  }

  val ErrMsgUniqueIdExists = "participant with unique ID already exists"

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

  private def processAddCmd(cmd: AddParticipantCmd): Unit = {
    val studyId = StudyId(cmd.studyId)
    val participantId = participantRepository.nextIdentity

    if (participantRepository.getByKey(participantId).isSuccess) {
      log.error(s"participant with id already exsits: $participantId")
    }

    val event = for {
      study             <- studyRepository.getEnabled(studyId)
      uniqueIdAvailable <- uniqueIdAvailable(cmd.uniqueId)
      annotTypes        <- Annotation.validateAnnotations(study.annotationTypes,
                                                          cmd.annotations)
      newParticipant    <- Participant.create(studyId,
                                              participantId,
                                              0L,
                                              cmd.uniqueId,
                                              cmd.annotations.toSet)
    } yield ParticipantEvent(newParticipant.id.id).update(
        _.optionalUserId    := cmd.userId,
        _.time              := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.added.studyId     := newParticipant.studyId.id,
        _.added.uniqueId    := cmd.uniqueId,
        _.added.annotations := cmd.annotations.map { annotationToEvent(_) })

    process(event) { applyAddedEvent(_) }
  }

  private def update(cmd: ParticipantModifyCommand)
                    (fn: (Study, Participant) => DomainValidation[ParticipantEvent])
      : DomainValidation[ParticipantEvent] = {
    val participantId = ParticipantId(cmd.id)

    for {
      participant <- {
        participantRepository.getByKey(participantId).leftMap(_ =>
          DomainError(s"participant id not found: ${cmd.id}")).toValidationNel
      }
      enabledStudy <- studyRepository.getEnabled(participant.studyId)
      validVersion <- participant.requireVersion(cmd.expectedVersion)
      event        <- fn(enabledStudy, participant)
    } yield event
  }

  private def processUpdateUniqueIdCmd(cmd: UpdateParticipantUniqueIdCmd): Unit = {
    val v = update(cmd) { (study, participant) =>
        for {
          uniqueIdAvailable  <- uniqueIdAvailable(cmd.uniqueId, participant.id)
          updatedParticipant <- participant.withUniqueId(cmd.uniqueId)
        } yield ParticipantEvent(updatedParticipant.id.id).update(
          _.optionalUserId           := cmd.userId,
          _.time                     := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.uniqueIdUpdated.version  := cmd.expectedVersion,
          _.uniqueIdUpdated.uniqueId := cmd.uniqueId)
      }

    process(v){ applyUniqueIdUpdatedEvent(_) }
  }

  private def processUpdateAnnotationCmd(cmd: UpdateParticipantAnnotationCmd): Unit = {
    val v = update(cmd) { (study, participant) =>
        for {
          validAnnotation    <- Annotation.validateAnnotations(study.annotationTypes,
                                                               List(cmd.annotation))
          updatedParticipant <- participant.withAnnotation(cmd.annotation)
        } yield ParticipantEvent(updatedParticipant.id.id).update(
          _.optionalUserId               := cmd.userId,
          _.time                         := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.annotationUpdated.version    := cmd.expectedVersion,
          _.annotationUpdated.annotation := annotationToEvent(cmd.annotation))
      }

    process(v) { applyAnnotationUpdatedEvent(_) }
  }

  private def processRemoveAnnotationCmd(cmd: RemoveParticipantAnnotationCmd): Unit = {
    val v = update(cmd) { (study, participant) =>
        for {
          annotType <- {
            study.annotationTypes
              .find { x => x.uniqueId == cmd.annotationTypeId }
              .toSuccess(s"annotation type with ID does not exist: ${cmd.annotationTypeId}")
              .toValidationNel
          }
          notRequired <- {
            if (annotType.required) DomainError(s"annotation is required").failureNel
            else true.success
          }
          updatedParticipant <- participant.withoutAnnotation(cmd.annotationTypeId)
        } yield ParticipantEvent(updatedParticipant.id.id).update(
          _.optionalUserId                     := cmd.userId,
          _.time                               := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.annotationRemoved.version          := cmd.expectedVersion,
          _.annotationRemoved.annotationTypeId := cmd.annotationTypeId)
      }

    process(v) { applyAnnotationRemovedEvent(_) }
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
                         annotations  = addedEvent.annotations.map { annotationFromEvent(_) }.toSet
      ).fold(
        err => log.error(s"could not add collection event from event: $err, $event"),
        p => {
          participantRepository.put(p)
          ()
        }
      )
    }
  }

  def onValidEventAndVersion(event:        ParticipantEvent,
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

  private def applyAnnotationUpdatedEvent(event: ParticipantEvent) = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationUpdated,
                           event.getAnnotationUpdated.getVersion) { participant =>
      val updatedEvent = event.getAnnotationUpdated
      participant.withAnnotation(annotationFromEvent(updatedEvent.getAnnotation)).fold(
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

}
