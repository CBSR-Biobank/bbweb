package org.biobank.service.participants

import akka.actor._
import akka.persistence.SnapshotOffer
import javax.inject.Inject
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.{ Annotation, DomainValidation, DomainError }
import org.biobank.infrastructure.command.CollectionEventCommands._
import org.biobank.infrastructure.event.CollectionEventEvents._
import org.biobank.infrastructure.event.CommonEvents._
import org.biobank.service.Processor
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object CollectionEventsProcessor {

  def props = Props[CollectionEventsProcessor]

}

/**
 * Responsible for handing collection event commands to add, update and remove.
 */
class CollectionEventsProcessor @Inject() (
  val collectionEventRepository:     CollectionEventRepository,
  val collectionEventTypeRepository: CollectionEventTypeRepository,
  val participantRepository:         ParticipantRepository,
  val studyRepository:               StudyRepository)
    extends Processor {

  import org.biobank.CommonValidations._
  import CollectionEventEvent.EventType
  import org.biobank.infrastructure.event.EventUtils._

  override def persistenceId = "collection-events-processor-id"

  case class SnapshotState(collectionEvents: Set[CollectionEvent])

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  val receiveRecover: Receive = {
    case event: CollectionEventEvent => event.eventType match {
      case et: EventType.Added                => applyAddedEvent(event)
      case et: EventType.VisitNumberUpdated   => applyVisitNumberUpdatedEvent(event)
      case et: EventType.TimeCompletedUpdated => applyTimeCompletedUpdatedEvent(event)
      case et: EventType.AnnotationUpdated    => applyAnnotationUpdatedEvent(event)
      case et: EventType.AnnotationRemoved    => applyAnnotationRemovedEvent(event)
      case et: EventType.Removed              => applyRemovedEvent(event)

      case event => log.error(s"event not handled: $event")
    }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.collectionEvents.foreach{ collectionEventRepository.put(_) }
  }

  /**
   * These are the commands that are requested. A command can fail, and will send the failure as a response
   * back to the user. Each valid command generates one or more events and is journaled.
   */
  val receiveCommand: Receive = {
    case cmd: AddCollectionEventCmd                 => processAddCmd(cmd)
    case cmd: UpdateCollectionEventVisitNumberCmd   => processUpdateVisitNumberCmd(cmd)
    case cmd: UpdateCollectionEventTimeCompletedCmd => processUpdateTimeCompletedCmd(cmd)
    case cmd: UpdateCollectionEventAnnotationCmd    => processUpdateAnnotationCmd(cmd)
    case cmd: RemoveCollectionEventAnnotationCmd    => processRemoveAnnotationCmd(cmd)
    case cmd: RemoveCollectionEventCmd              => processRemoveCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(collectionEventRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"collectionEventsProcessor: message not handled: $cmd")

  }

  private def processAddCmd(cmd: AddCollectionEventCmd): Unit = {
    val collectionEventId = collectionEventRepository.nextIdentity

    if (collectionEventRepository.getByKey(collectionEventId).isSuccess) {
      log.error(s"processAddCollectionEventCmd: collection event with id already exsits: $collectionEventId")
    }

    val participantId = ParticipantId(cmd.participantId)
    val collectionEventTypeId = CollectionEventTypeId(cmd.collectionEventTypeId)
    var annotationsSet = cmd.annotations.toSet

    val event = for {
      participant          <- participantRepository.getByKey(participantId)
      collectionEventType  <- collectionEventTypeRepository.getByKey(collectionEventTypeId)
      studyIdMatching      <- studyIdsMatch(participant, collectionEventType)
      studyEnabled         <- studyRepository.getEnabled(participant.studyId)
      validAnnotations     <- Annotation.validateAnnotations(collectionEventType.annotationTypes,
                                                             cmd.annotations)
      visitNumberAvailable <- visitNumberAvailable(participant.id, cmd.visitNumber)
      newCollectionEvent   <- CollectionEvent.create(collectionEventId,
                                                     participantId,
                                                     collectionEventTypeId,
                                                     0L,
                                                     cmd.timeCompleted,
                                                     cmd.visitNumber,
                                                     annotationsSet)
      } yield CollectionEventEvent(newCollectionEvent.id.id).update(
        _.participantId         := cmd.participantId,
        _.collectionEventTypeId := newCollectionEvent.collectionEventTypeId.id,
        _.optionalUserId        := cmd.userId,
        _.time                  := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.added.timeCompleted   := ISODateTimeFormatter.print(cmd.timeCompleted),
        _.added.visitNumber     := cmd.visitNumber,
        _.added.annotations     := cmd.annotations.map { annotationToEvent(_) })

    process(event) { applyAddedEvent(_) }
  }

  private def processUpdateVisitNumberCmd(cmd: UpdateCollectionEventVisitNumberCmd): Unit = {
    val v = update(cmd) { (participant, collectionEventType, cevent) =>
      for {
        visitNumberAvailable <- visitNumberAvailable(participant.id, cmd.visitNumber, cevent.id)
        updatedCevent        <- cevent.withVisitNumber(cmd.visitNumber)
      } yield CollectionEventEvent(updatedCevent.id.id).update(
        _.participantId                  := participant.id.id,
        _.collectionEventTypeId          := updatedCevent.collectionEventTypeId.id,
        _.optionalUserId                 := cmd.userId,
        _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.visitNumberUpdated.version     := cmd.expectedVersion,
        _.visitNumberUpdated.visitNumber := updatedCevent.visitNumber)
    }

    process(v) { applyVisitNumberUpdatedEvent(_) }
  }

  private def processUpdateTimeCompletedCmd(cmd: UpdateCollectionEventTimeCompletedCmd): Unit = {
    val v = update(cmd) { (participant, collectionEventType, cevent) =>
        cevent.withTimeCompleted(cmd.timeCompleted).map { updatedCevent =>
          CollectionEventEvent(updatedCevent.id.id).update(
            _.participantId                      := participant.id.id,
            _.collectionEventTypeId              := updatedCevent.collectionEventTypeId.id,
            _.optionalUserId                     := cmd.userId,
            _.time                               := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.timeCompletedUpdated.version       := cmd.expectedVersion,
            _.timeCompletedUpdated.timeCompleted := ISODateTimeFormat.dateTime.print(updatedCevent.timeCompleted))
        }
    }

    process(v) { applyTimeCompletedUpdatedEvent(_) }
  }

  private def processUpdateAnnotationCmd(cmd: UpdateCollectionEventAnnotationCmd): Unit = {
    val v = update(cmd) { (participant, collectionEventType, cevent) =>
        for {
          annotation      <- Annotation.create(cmd.annotationTypeId,
                                               cmd.stringValue,
                                               cmd.numberValue,
                                               cmd.selectedValues)
          updatedCevent   <- cevent.withAnnotation(annotation)
          validAnnotation <- Annotation.validateAnnotations(collectionEventType.annotationTypes,
                                                            List(annotation))
        } yield CollectionEventEvent(updatedCevent.id.id).update(
          _.participantId                := participant.id.id,
          _.collectionEventTypeId        := updatedCevent.collectionEventTypeId.id,
          _.optionalUserId               := cmd.userId,
          _.time                         := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.annotationUpdated.version    := cmd.expectedVersion,
          _.annotationUpdated.annotation := annotationToEvent(annotation))
      }

    process(v) { applyAnnotationUpdatedEvent(_) }
  }

  private def processRemoveAnnotationCmd(cmd: RemoveCollectionEventAnnotationCmd): Unit = {
    val v = update(cmd) { (participant, collectionEventType, cevent) =>
        for {
          annotType <- {
            collectionEventType.annotationTypes
              .find { x => x.uniqueId == cmd.annotationTypeId }
              .toSuccess(s"annotation type with ID does not exist: ${cmd.annotationTypeId}")
              .toValidationNel
          }
          notRequired <- {
            if (annotType.required) EntityRequried(s"annotation is required").failureNel
            else true.success
          }
          updatedCevent <- cevent.withoutAnnotation(cmd.annotationTypeId)
        } yield CollectionEventEvent(updatedCevent.id.id).update(
          _.participantId                      := participant.id.id,
          _.collectionEventTypeId              := updatedCevent.collectionEventTypeId.id,
          _.optionalUserId                     := cmd.userId,
          _.time                               := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.annotationRemoved.version          := cmd.expectedVersion,
          _.annotationRemoved.annotationTypeId := cmd.annotationTypeId)
      }

    process(v) { applyAnnotationRemovedEvent(_) }
  }

  private def processRemoveCmd(cmd: RemoveCollectionEventCmd): Unit = {
    val v = update(cmd) { (participant, collectionEventType, cevent) =>
        CollectionEventEvent(cevent.id.id).update(
          _.participantId         := participant.id.id,
          _.collectionEventTypeId := cevent.collectionEventTypeId.id,
          _.optionalUserId        := cmd.userId,
          _.time                  := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.removed.version       := cevent.version).success
      }
    process(v) { applyRemovedEvent(_) }
  }

  def update(cmd: CollectionEventModifyCommand)
            (fn: (Participant, CollectionEventType, CollectionEvent) => DomainValidation[CollectionEventEvent])
      : DomainValidation[CollectionEventEvent] = {
    val collectionEventId = CollectionEventId(cmd.id)

    for {
      cevent              <- collectionEventRepository.getByKey(collectionEventId)
      validVersion       <-  cevent.requireVersion(cmd.expectedVersion)
      collectionEventType <- collectionEventTypeRepository.getByKey(cevent.collectionEventTypeId)
      participant         <- {
        participantRepository.getByKey(cevent.participantId).leftMap(_ =>
          DomainError(s"participant id not found: ${cevent.participantId}")).toValidationNel
      }
      studyIdMatching     <- studyIdsMatch(participant, collectionEventType)
      studyEnabled        <- studyRepository.getEnabled(participant.studyId)
      event <- fn(participant, collectionEventType, cevent)
    } yield event
  }

  def studyIdsMatch(participant: Participant, collectionEventType: CollectionEventType)
      : DomainValidation[Boolean] =  {
    if (participant.studyId == collectionEventType.studyId) {
      true.success
    } else {
      EntityCriteriaError(s"participant and collection event type not in the same study").failureNel
    }
  }

  private def applyAddedEvent(event: CollectionEventEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded

      CollectionEvent.create(
        id                    = CollectionEventId(event.id),
        collectionEventTypeId = CollectionEventTypeId(event.getCollectionEventTypeId),
        participantId         = ParticipantId(event.getParticipantId),
        version               = 0L,
        timeCompleted         = ISODateTimeParser.parseDateTime(addedEvent.getTimeCompleted),
        visitNumber           = addedEvent.getVisitNumber,
        annotations           = addedEvent.annotations.map { annotationFromEvent(_) }.toSet
      ).fold(
        err => log.error(s"could not add collection event from event: $err, $event"),
        ce => {
          collectionEventRepository.put(ce)
          ()
        }
      )
    }
  }

  def onValidEventAndVersion(event:        CollectionEventEvent,
                             eventType:    Boolean,
                             eventVersion: Long)
                            (fn: CollectionEvent => Unit): Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      collectionEventRepository.getByKey(CollectionEventId(event.id)).fold(
        err => log.error(s"collection event from event does not exist: $err"),
        cevent => {
          if (cevent.version != eventVersion) {
            log.error(s"event version check failed: cevent version: ${cevent.version}, event: $event")
          } else {
            fn(cevent)
          }
        }
      )
    }
  }

  private def storeUpdated(cevent: CollectionEvent, time: String): Unit = {
    collectionEventRepository.put(
      cevent.copy(
        timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(time))))
    ()
  }

  private def applyVisitNumberUpdatedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isVisitNumberUpdated,
                           event.getVisitNumberUpdated.getVersion) { cevent =>
      val updatedEvent = event.getVisitNumberUpdated

      cevent.withVisitNumber(updatedEvent.getVisitNumber).fold(
        err => log.error(s"updating cevent from event failed: $err"),
        c => storeUpdated(c, event.getTime)
      )
    }
  }

  private def applyTimeCompletedUpdatedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isTimeCompletedUpdated,
                           event.getTimeCompletedUpdated.getVersion) { cevent =>
      val updatedEvent = event.getTimeCompletedUpdated
      val timeCompleted = ISODateTimeFormat.dateTime.parseDateTime(updatedEvent.getTimeCompleted)

      cevent.withTimeCompleted(timeCompleted).fold(
        err => log.error(s"updating cevent from event failed: $err"),
        c => storeUpdated(c, event.getTime)
      )
    }
  }

  private def applyAnnotationUpdatedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationUpdated,
                           event.getAnnotationUpdated.getVersion) { cevent =>
      val updatedEvent = event.getAnnotationUpdated

      cevent.withAnnotation(annotationFromEvent(updatedEvent.getAnnotation)).fold(
        err => log.error(s"updating cevent from event failed: $err"),
        c => storeUpdated(c, event.getTime)
      )
    }
  }

  private def applyAnnotationRemovedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationRemoved,
                           event.getAnnotationRemoved.getVersion) { cevent =>
      cevent.withoutAnnotation(event.getAnnotationRemoved.getAnnotationTypeId).fold(
        err => log.error(s"removing annotation from collection event failed: $err"),
        c => storeUpdated(c, event.getTime)
      )
    }
  }

  private def applyRemovedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isRemoved,
                           event.getRemoved.getVersion) { cevent =>
      collectionEventRepository.remove(cevent)
      ()
    }
  }

  val errMsgVisitNumberExists = "a collection event with this visit number already exists"

  /**
   *  Searches the repository for a matching item.
   */
  protected def visitNumberAvailableMatcher(visitNumber: Int)(matcher: CollectionEvent => Boolean)
      : DomainValidation[Boolean] = {
    val exists = collectionEventRepository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) {
      EntityCriteriaError(s"$errMsgVisitNumberExists: $visitNumber").failureNel
    } else {
      true.success
    }
  }

  private def visitNumberAvailable(participantId: ParticipantId, visitNumber: Int): DomainValidation[Boolean] = {
    visitNumberAvailableMatcher(visitNumber){ item =>
      (item.participantId == participantId) && (item.visitNumber == visitNumber)
    }
  }

  private def visitNumberAvailable(participantId: ParticipantId,
                                   visitNumber: Int,
                                   excludeCollectionEventId: CollectionEventId)
      : DomainValidation[Boolean] = {
    visitNumberAvailableMatcher(visitNumber){ item =>
      (item.participantId == participantId) && (item.visitNumber == visitNumber) && (item.id != excludeCollectionEventId)
    }
  }
}
