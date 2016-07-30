package org.biobank.service.participants

import akka.actor._
import akka.persistence.SnapshotOffer
import javax.inject.Inject
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.Annotation
import org.biobank.infrastructure.command.CollectionEventCommands._
import org.biobank.infrastructure.event.CollectionEventEvents._
import org.biobank.infrastructure.event.CommonEvents._
import org.biobank.service.{Processor, ServiceValidation}
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
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
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
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val receiveCommand: Receive = {
    case cmd: AddCollectionEventCmd =>
      process(addCmdToEvent(cmd))(applyAddedEvent)

    case cmd: UpdateCollectionEventVisitNumberCmd =>
      processUpdateCmd(cmd, updateVisitNumberCmdToEvent, applyVisitNumberUpdatedEvent)

    case cmd: UpdateCollectionEventTimeCompletedCmd =>
      processUpdateCmd(cmd, updateTimeCompletedCmdToEvent, applyTimeCompletedUpdatedEvent)

    case cmd: UpdateCollectionEventAnnotationCmd =>
      processUpdateCmd(cmd, updateAnnotationCmdToEvent, applyAnnotationUpdatedEvent)

    case cmd: RemoveCollectionEventAnnotationCmd =>
      processUpdateCmd(cmd, removeAnnotationCmdToEvent, applyAnnotationRemovedEvent)

    case cmd: RemoveCollectionEventCmd =>
      processUpdateCmd(cmd, removeCmdToEvent, applyRemovedEvent)

    case "snap" =>
      saveSnapshot(SnapshotState(collectionEventRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"collectionEventsProcessor: message not handled: $cmd")

  }

  private def addCmdToEvent(cmd:AddCollectionEventCmd): ServiceValidation[CollectionEventEvent] = {
    val participantId = ParticipantId(cmd.participantId)
    val collectionEventTypeId = CollectionEventTypeId(cmd.collectionEventTypeId)
    val annotationsSet = cmd.annotations.toSet

    for {
      collectionEventId    <- validNewIdentity(collectionEventRepository.nextIdentity, collectionEventRepository)
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
      _.userId                := cmd.userId,
      _.time                  := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.added.timeCompleted   := ISODateTimeFormatter.print(cmd.timeCompleted),
      _.added.visitNumber     := cmd.visitNumber,
      _.added.annotations     := cmd.annotations.map { annotationToEvent(_) })
  }

  private def updateVisitNumberCmdToEvent(cmd:                 UpdateCollectionEventVisitNumberCmd,
                                          participant:         Participant,
                                          collectionEventType: CollectionEventType,
                                          cevent:              CollectionEvent)
      : ServiceValidation[CollectionEventEvent] = {
    for {
      visitNumberAvailable <- visitNumberAvailable(participant.id, cmd.visitNumber, cevent.id)
      updatedCevent        <- cevent.withVisitNumber(cmd.visitNumber)
    } yield CollectionEventEvent(updatedCevent.id.id).update(
      _.participantId                  := participant.id.id,
      _.collectionEventTypeId          := updatedCevent.collectionEventTypeId.id,
      _.userId                         := cmd.userId,
      _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.visitNumberUpdated.version     := cmd.expectedVersion,
      _.visitNumberUpdated.visitNumber := updatedCevent.visitNumber)
  }

  private def updateTimeCompletedCmdToEvent(cmd:                 UpdateCollectionEventTimeCompletedCmd,
                                            participant:         Participant,
                                            collectionEventType: CollectionEventType,
                                            cevent:              CollectionEvent)
      : ServiceValidation[CollectionEventEvent] = {
    cevent.withTimeCompleted(cmd.timeCompleted).map { updatedCevent =>
      CollectionEventEvent(updatedCevent.id.id).update(
        _.participantId                      := participant.id.id,
        _.collectionEventTypeId              := updatedCevent.collectionEventTypeId.id,
        _.userId                             := cmd.userId,
        _.time                               := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.timeCompletedUpdated.version       := cmd.expectedVersion,
        _.timeCompletedUpdated.timeCompleted := ISODateTimeFormat.dateTime.print(updatedCevent.timeCompleted))
    }
  }

  private def updateAnnotationCmdToEvent(cmd:                 UpdateCollectionEventAnnotationCmd,
                                         participant:         Participant,
                                         collectionEventType: CollectionEventType,
                                         cevent:              CollectionEvent)
      : ServiceValidation[CollectionEventEvent] = {
    for {
      annotation      <- Annotation.create(cmd.annotationTypeId,
                                           cmd.stringValue,
                                           cmd.numberValue,
                                           cmd.selectedValues)
      allAnnotations  <- (cevent.annotations + annotation).successNel[String]
      validAnnotation <- Annotation.validateAnnotations(collectionEventType.annotationTypes,
                                                        allAnnotations.toList)
      updatedCevent   <- cevent.withAnnotation(annotation)
    } yield CollectionEventEvent(updatedCevent.id.id).update(
      _.participantId                := participant.id.id,
      _.collectionEventTypeId        := updatedCevent.collectionEventTypeId.id,
      _.userId                       := cmd.userId,
      _.time                         := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.annotationUpdated.version    := cmd.expectedVersion,
      _.annotationUpdated.annotation := annotationToEvent(annotation))
  }

  private def removeAnnotationCmdToEvent(cmd:                 RemoveCollectionEventAnnotationCmd,
                                         participant:         Participant,
                                         collectionEventType: CollectionEventType,
                                         cevent:              CollectionEvent)
      : ServiceValidation[CollectionEventEvent] = {
    for {
      annotType <- {
        collectionEventType.annotationTypes
          .find { x => x.uniqueId == cmd.annotationTypeId }
          .toSuccessNel(s"annotation type with ID does not exist: ${cmd.annotationTypeId}")
      }
      notRequired <- {
        if (annotType.required) EntityRequired(s"annotation is required").failureNel[Boolean]
        else true.successNel[String]
      }
      updatedCevent <- cevent.withoutAnnotation(cmd.annotationTypeId)
    } yield CollectionEventEvent(updatedCevent.id.id).update(
      _.participantId                      := participant.id.id,
      _.collectionEventTypeId              := updatedCevent.collectionEventTypeId.id,
      _.userId                             := cmd.userId,
      _.time                               := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.annotationRemoved.version          := cmd.expectedVersion,
      _.annotationRemoved.annotationTypeId := cmd.annotationTypeId)
  }

  private def removeCmdToEvent(cmd:                 RemoveCollectionEventCmd,
                               participant:         Participant,
                               collectionEventType: CollectionEventType,
                               cevent:              CollectionEvent)
      : ServiceValidation[CollectionEventEvent] = {
    CollectionEventEvent(cevent.id.id).update(
      _.participantId         := participant.id.id,
      _.collectionEventTypeId := cevent.collectionEventTypeId.id,
      _.userId                := cmd.userId,
      _.time                  := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.removed.version       := cevent.version).successNel[String]
  }

  private def processUpdateCmd[T <: CollectionEventModifyCommand](
    cmd: T,
    validation: (T,
                 Participant,
                 CollectionEventType,
                 CollectionEvent) => ServiceValidation[CollectionEventEvent],
    applyEvent: CollectionEventEvent => Unit): Unit = {
    val event = for {
        cevent              <- collectionEventRepository.getByKey(CollectionEventId(cmd.id))
        validVersion        <- cevent.requireVersion(cmd.expectedVersion)
        collectionEventType <- collectionEventTypeRepository.getByKey(cevent.collectionEventTypeId)
        participant         <- participantRepository.getByKey(cevent.participantId)
        studyIdMatching     <- studyIdsMatch(participant, collectionEventType)
        studyEnabled        <- studyRepository.getEnabled(participant.studyId)
        event               <- validation(cmd, participant, collectionEventType, cevent)
      } yield event
    process(event)(applyEvent)
  }

  private def studyIdsMatch(participant: Participant, collectionEventType: CollectionEventType)
      : ServiceValidation[Boolean] =  {
    if (participant.studyId == collectionEventType.studyId) true.successNel[String]
    else EntityCriteriaError(s"participant and collection event type not in the same study").failureNel[Boolean]
  }

  private def applyAddedEvent(event: CollectionEventEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded

      val v = CollectionEvent.create(
          id                    = CollectionEventId(event.id),
          collectionEventTypeId = CollectionEventTypeId(event.getCollectionEventTypeId),
          participantId         = ParticipantId(event.getParticipantId),
          version               = 0L,
          timeCompleted         = ISODateTimeParser.parseDateTime(addedEvent.getTimeCompleted),
          visitNumber           = addedEvent.getVisitNumber,
          annotations           = addedEvent.annotations.map { annotationFromEvent(_) }.toSet)

      if (v.isFailure) {
        log.error(s"could not add collection event from event: $v, $event")
      }

      v.foreach { ce =>
        collectionEventRepository.put(ce)
      }
    }
  }

  private def onValidEventAndVersion(event:        CollectionEventEvent,
                                     eventType:    Boolean,
                                     eventVersion: Long)
                                    (applyEvent: (CollectionEvent, DateTime) => ServiceValidation[Boolean])
      : Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      collectionEventRepository.getByKey(CollectionEventId(event.id)).fold(
        err => log.error(s"collection event from event does not exist: $err"),
        cevent => {
          if (cevent.version != eventVersion) {
            log.error(s"event version check failed: cevent version: ${cevent.version}, event: $event")
          } else {
            val eventTime = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)
            val update = applyEvent(cevent, eventTime)
            if (update.isFailure) {
              log.error(s"shipment specimen update from event failed: $update")
            }
          }
        }
      )
    }
  }

  private def applyVisitNumberUpdatedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isVisitNumberUpdated,
                           event.getVisitNumberUpdated.getVersion) { (cevent, eventTime) =>
      val v = cevent.withVisitNumber(event.getVisitNumberUpdated.getVisitNumber)

      v.foreach { c =>
        collectionEventRepository.put(c.copy(timeModified = Some(eventTime)))
      }

      v.map(_ => true)
    }
  }

  private def applyTimeCompletedUpdatedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isTimeCompletedUpdated,
                           event.getTimeCompletedUpdated.getVersion) { (cevent, eventTime) =>

      val timeCompleted = ISODateTimeFormat.dateTime.parseDateTime(
          event.getTimeCompletedUpdated.getTimeCompleted)
      val v = cevent.withTimeCompleted(timeCompleted)

      v.foreach { c =>
        collectionEventRepository.put(c.copy(timeModified = Some(eventTime)))
      }

      v.map(_ => true)
    }
  }

  private def applyAnnotationUpdatedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationUpdated,
                           event.getAnnotationUpdated.getVersion) { (cevent, eventTime) =>
      val v = cevent.withAnnotation(annotationFromEvent(event.getAnnotationUpdated.getAnnotation))

      v.foreach { c =>
        collectionEventRepository.put(c.copy(timeModified = Some(eventTime)))
      }

      v.map(_ => true)
    }
  }

  private def applyAnnotationRemovedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationRemoved,
                           event.getAnnotationRemoved.getVersion) { (cevent, eventTime) =>
      val v = cevent.withoutAnnotation(event.getAnnotationRemoved.getAnnotationTypeId)

      v.foreach { c =>
        collectionEventRepository.put(c.copy(timeModified = Some(eventTime)))
      }

      v.map(_ => true)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  private def applyRemovedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isRemoved,
                           event.getRemoved.getVersion) { (cevent, eventTime) =>
      collectionEventRepository.put(cevent.copy(timeModified = Some(eventTime)))
      true.successNel[String]
    }
  }

  val errMsgVisitNumberExists = "a collection event with this visit number already exists"

  /**
   *  Searches the repository for a matching item.
   */
  protected def visitNumberAvailableMatcher(visitNumber: Int)(matcher: CollectionEvent => Boolean)
      : ServiceValidation[Boolean] = {
    val exists = collectionEventRepository.getValues.exists { item =>
        matcher(item)
      }
    if (exists) {
      EntityCriteriaError(s"$errMsgVisitNumberExists: $visitNumber").failureNel[Boolean]
    } else {
      true.successNel[String]
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def visitNumberAvailable(participantId: ParticipantId, visitNumber: Int)
      : ServiceValidation[Boolean] = {
    visitNumberAvailableMatcher(visitNumber){ item =>
      (item.participantId == participantId) && (item.visitNumber == visitNumber)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def visitNumberAvailable(participantId: ParticipantId,
                                   visitNumber: Int,
                                   excludeCollectionEventId: CollectionEventId)
      : ServiceValidation[Boolean] = {
    visitNumberAvailableMatcher(visitNumber){ item =>
      (item.participantId == participantId) && (item.visitNumber == visitNumber) && (item.id != excludeCollectionEventId)
    }
  }
}
