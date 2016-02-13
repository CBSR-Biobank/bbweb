package org.biobank.service.participants

import org.biobank.domain.{ Annotation, DomainValidation, DomainError }
import org.biobank.service.{ Processor, Utils }
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.ParticipantEvents._
import org.biobank.infrastructure.event.CommonEvents._
import org.biobank.domain.user.UserId
import org.biobank.domain.study._
import org.biobank.domain.participants._

import javax.inject.{Inject => javaxInject}
import akka.actor._
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
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
class CollectionEventsProcessor @javaxInject() (
  val collectionEventRepository:     CollectionEventRepository,
  val collectionEventTypeRepository: CollectionEventTypeRepository,
  val participantRepository:         ParticipantRepository)
    extends Processor {

  import ParticipantEvent.EventType
  import org.biobank.infrastructure.event.EventUtils._
  import org.biobank.infrastructure.event.ParticipantEventsUtil._

  override def persistenceId = "collections-event-processor-id"

  case class SnapshotState(collectionEvents: Set[CollectionEvent])

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  val receiveRecover: Receive = {
    case event: ParticipantEvent => event.eventType match {
      case et: EventType.CollectionEventAdded   => applyCollectionEventAddedEvent(event)
      case et: EventType.CollectionEventUpdated => applyCollectionEventUpdatedEvent(event)
      case et: EventType.CollectionEventRemoved => applyCollectionEventRemovedEvent(event)

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
    case cmd: AddCollectionEventCmd    => processAddCollectionEventCmd(cmd)
    case cmd: UpdateCollectionEventCmd => processUpdateCollectionEventCmd(cmd)
    case cmd: RemoveCollectionEventCmd => processRemoveCollectionEventCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(collectionEventRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"collectionEventsProcessor: message not handled: $cmd")

  }

  private def processAddCollectionEventCmd(cmd: AddCollectionEventCmd): Unit = {
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
      annotTypes           <- Annotation.validateAnnotations(collectionEventType.annotationTypes,
                                                             annotationsSet)
      visitNumberAvailable <- visitNumberAvailable(cmd.visitNumber)
      newCollectionEvent   <- CollectionEvent.create(collectionEventId,
                                                     participantId,
                                                     collectionEventTypeId,
                                                     -1L,
                                                     cmd.timeCompleted,
                                                     cmd.visitNumber,
                                                     annotationsSet)
      event                <- createEvent(participant, cmd).withCollectionEventAdded(
        CollectionEventAddedEvent(
          collectionEventId     = Some(collectionEventId.id),
          collectionEventTypeId = Some(cmd.collectionEventTypeId),
          timeCompleted         = Some(ISODateTimeFormatter.print(cmd.timeCompleted)),
          visitNumber           = Some(cmd.visitNumber),
          annotations           = Utils.convertAnnotationsToEvent(cmd.annotations))).success
    } yield event

    process(event) { applyCollectionEventAddedEvent(_) }
  }

  private def processUpdateCollectionEventCmd(cmd: UpdateCollectionEventCmd): Unit = {
    val collectionEventTypeId = CollectionEventTypeId(cmd.collectionEventTypeId)
    var annotationsSet = cmd.annotations.toSet

    val v = update(cmd) { (participant, cevent) =>
      for {
        collectionEventType  <- collectionEventTypeRepository.getByKey(collectionEventTypeId)
        studyIdMatching      <- studyIdsMatch(participant, collectionEventType)
        visitNumberAvailable <- visitNumberAvailable(cmd.visitNumber, cevent.id)
        annotTypes           <- Annotation.validateAnnotations(collectionEventType.annotationTypes,
                                                               annotationsSet)
        updatedCevent1       <- cevent.withTimeCompleted(cmd.timeCompleted)
        updatedCevent2       <- cevent.withVisitNumber(cmd.visitNumber)
        updatedCevent3       <- cevent.withAnnotations(annotationsSet)
        event                <- createEvent(participant, cmd).withCollectionEventUpdated(
          CollectionEventUpdatedEvent(
            collectionEventId     = Some(updatedCevent3.id.id),
            collectionEventTypeId = Some(cmd.collectionEventTypeId),
            version               = Some(updatedCevent3.version),
            timeCompleted         = Some(ISODateTimeFormatter.print(cmd.timeCompleted)),
            visitNumber           = Some(cmd.visitNumber),
            annotations           = Utils.convertAnnotationsToEvent(cmd.annotations))).success
      } yield event
    }

    process(v) { applyCollectionEventUpdatedEvent(_) }
  }

  private def processRemoveCollectionEventCmd(cmd: RemoveCollectionEventCmd): Unit = {
    val v = update(cmd) { (participant, cevent) =>
      createEvent(participant, cmd).withCollectionEventRemoved(
        CollectionEventRemovedEvent(Some(cevent.id.id))).success
    }
    process(v) { applyCollectionEventRemovedEvent(_) }
  }

  def update
    (cmd: CollectionEventModifyCommand)
    (fn: (Participant, CollectionEvent) => DomainValidation[ParticipantEvent])
      : DomainValidation[ParticipantEvent] = {
    val collectionEventId = CollectionEventId(cmd.id)
    val participantId = ParticipantId(cmd.participantId)

    for {
      participant  <- participantRepository.getByKey(participantId)
      cevent       <- collectionEventRepository.withId(participantId, collectionEventId)
      validVersion <- cevent.requireVersion(cmd.expectedVersion)
      event        <- fn(participant, cevent)
    } yield event
  }

  def studyIdsMatch(participant: Participant, collectionEventType: CollectionEventType)
      : DomainValidation[Boolean] =  {
    if (participant.studyId == collectionEventType.studyId) {
      true.success
    } else {
      DomainError(s"participant and collection event type not in the same study").failureNel
    }
  }

  private def applyCollectionEventAddedEvent(event: ParticipantEvent): Unit = {
    log.debug(s"applyCollectionEventAddedEvent: event:$event")

    if (event.eventType.isCollectionEventAdded) {
      val addedEvent = event.getCollectionEventAdded

      collectionEventRepository.put(
        CollectionEvent(
          id                    = CollectionEventId(addedEvent.getCollectionEventId),
          participantId         = ParticipantId(event.id),
          collectionEventTypeId = CollectionEventTypeId(addedEvent.getCollectionEventTypeId),
          version               = 0L,
          timeAdded             = ISODateTimeParser.parseDateTime(event.getTime),
          timeModified          = None,
          timeCompleted         = ISODateTimeParser.parseDateTime(addedEvent.getTimeCompleted),
          visitNumber           = addedEvent.getVisitNumber,
          annotations           = Utils.convertAnnotationsFromEvent(addedEvent.annotations)))
      ()
    } else {
      log.error(s"applyCollectionEventAddedEvent: invalid event type: $event")
    }
  }

  private def applyCollectionEventUpdatedEvent(event: ParticipantEvent): Unit = {
    log.debug(s"applyCollectionEventUpdatedEvent: event:$event")

    if (event.eventType.isCollectionEventUpdated) {
      val updatedEvent = event.getCollectionEventUpdated
      val collectionEventId = CollectionEventId(updatedEvent.getCollectionEventId)
      val participantId = ParticipantId(event.id)
      val collectionEventTypeId = CollectionEventTypeId(updatedEvent.getCollectionEventTypeId)

      collectionEventRepository.withId(participantId, collectionEventId).fold(
        err    => log.error(s"updating collection event from event failed: $err"),
        cevent => {
          collectionEventRepository.put(
            cevent.copy(
              version       = updatedEvent.getVersion,
              timeCompleted = ISODateTimeFormat.dateTime.parseDateTime(updatedEvent.getTimeCompleted),
              visitNumber   = updatedEvent.getVisitNumber,
              annotations   = Utils.convertAnnotationsFromEvent(updatedEvent.annotations),
              timeModified  = Some(ISODateTimeParser.parseDateTime(event.getTime))))
          ()
        }
      )
    } else {
      log.error(s"applyCollectionEventAddedEvent: invalid event type: $event")
    }
  }

  private def applyCollectionEventRemovedEvent(event: ParticipantEvent): Unit = {
    if (event.eventType.isCollectionEventRemoved) {
      collectionEventRepository.getByKey(
        CollectionEventId(event.getCollectionEventRemoved.getCollectionEventId))
      .fold(
        err => log.error(s"removing collection event from event failed: $err"),
        cevent => {
          collectionEventRepository.remove(cevent)
          ()
        }
      )
    } else {
      log.error(s"applyCollectionEventRemovedEvent: invalid event type: $event")
    }
  }

  val ErrMsgVisitNumberExists = "a collection event with this visit number already exists"

  /** Searches the repository for a matching item.
   */
  protected def visitNumberAvailableMatcher(visitNumber: Int)(matcher: CollectionEvent => Boolean)
      : DomainValidation[Boolean] = {
    val exists = collectionEventRepository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) {
      DomainError(s"$ErrMsgVisitNumberExists: $visitNumber").failureNel
    } else {
      true.success
    }
  }

  private def visitNumberAvailable(visitNumber: Int): DomainValidation[Boolean] = {
    visitNumberAvailableMatcher(visitNumber){ item =>
      item.visitNumber == visitNumber
    }
  }

  private def visitNumberAvailable(visitNumber: Int, excludeCollectionEventId: CollectionEventId)
      : DomainValidation[Boolean] = {
    visitNumberAvailableMatcher(visitNumber){ item =>
      (item.visitNumber == visitNumber) && (item.id != excludeCollectionEventId)
    }
  }
}
