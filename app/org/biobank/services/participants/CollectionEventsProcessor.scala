package org.biobank.service.participants

import akka.actor._
import akka.persistence.{RecoveryCompleted, SaveSnapshotSuccess, SaveSnapshotFailure, SnapshotOffer}
import com.github.ghik.silencer.silent
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import org.biobank.domain.AnnotationTypeId
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.Annotation
import org.biobank.infrastructure.command.CollectionEventCommands._
import org.biobank.infrastructure.event.CollectionEventEvents._
import org.biobank.infrastructure.event.CommonEvents._
import org.biobank.service.{Processor, ServiceValidation, SnapshotWriter}
import play.api.libs.json._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object CollectionEventsProcessor {

  def props: Props = Props[CollectionEventsProcessor]

  final case class SnapshotState(collectionEvents: Set[CollectionEvent])

  implicit val snapshotStateFormat: Format[SnapshotState] = Json.format[SnapshotState]

}

/**
 * Responsible for handing collection event commands to add, update and remove.
 */
class CollectionEventsProcessor @Inject() (
  val collectionEventRepository:     CollectionEventRepository,
  val collectionEventTypeRepository: CollectionEventTypeRepository,
  val participantRepository:         ParticipantRepository,
  val studyRepository:               StudyRepository,
  val snapshotWriter:                SnapshotWriter)
    extends Processor {

  import CollectionEventsProcessor._
  import org.biobank.CommonValidations._
  import CollectionEventEvent.EventType
  import org.biobank.infrastructure.event.EventUtils._

  override def persistenceId: String = "collection-events-processor-id"

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

    case SnapshotOffer(_, snapshotFilename: String) =>
      applySnapshot(snapshotFilename)

    case RecoveryCompleted =>
      log.debug("CollectionEventsProcessor: recovery completed")

    case msg =>
      log.error(s"message not handled: $msg")
  }

  /**
   * These are the commands that are requested. A command can fail, and will send the failure as a response
   * back to the user. Each valid command generates one or more events and is journaled.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Throw"))
  val receiveCommand: Receive = {
    case cmd: AddCollectionEventCmd =>
      process(addCmdToEvent(cmd))(applyAddedEvent)

    case cmd: UpdateCollectionEventVisitNumberCmd =>
      processUpdateCmd(cmd, updateVisitNumberCmdToEvent, applyVisitNumberUpdatedEvent)

    case cmd: UpdateCollectionEventTimeCompletedCmd =>
      processUpdateCmd(cmd, updateTimeCompletedCmdToEvent, applyTimeCompletedUpdatedEvent)

    case cmd: CollectionEventUpdateAnnotationCmd =>
      processUpdateCmd(cmd, updateAnnotationCmdToEvent, applyAnnotationUpdatedEvent)

    case cmd: RemoveCollectionEventAnnotationCmd =>
      processUpdateCmd(cmd, removeAnnotationCmdToEvent, applyAnnotationRemovedEvent)

    case cmd: RemoveCollectionEventCmd =>
      processUpdateCmd(cmd, removeCmdToEvent, applyRemovedEvent)

    case "snap" =>
     mySaveSnapshot

    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"snapshot saved successfully: ${metadata}")

    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"snapshot save error: ${metadata}")
      reason.printStackTrace

    case "persistence_restart" =>
      throw new Exception("Intentionally throwing exception to test persistence by restarting the actor")

    case cmd =>
      log.error(s"collectionEventsProcessor: message not handled: $cmd")

  }

  private def mySaveSnapshot(): Unit = {
    val snapshotState = SnapshotState(collectionEventRepository.getValues.toSet)
    val filename = snapshotWriter.save(persistenceId, Json.toJson(snapshotState).toString)
    log.debug(s"saved snapshot to: $filename")
    saveSnapshot(filename)
  }

  private def applySnapshot(filename: String): Unit = {
    log.info(s"snapshot recovery file: $filename")
    val fileContents = snapshotWriter.load(filename);
    Json.parse(fileContents).validate[SnapshotState].fold(
      errors => log.error(s"could not apply snapshot: $filename: $errors"),
      snapshot =>  {
        log.debug(s"snapshot contains ${snapshot.collectionEvents.size} collection events")
        snapshot.collectionEvents.foreach(collectionEventRepository.put)
      }
    )
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
                                                     OffsetDateTime.now,
                                                     cmd.timeCompleted,
                                                     cmd.visitNumber,
                                                     annotationsSet)
    } yield CollectionEventEvent(newCollectionEvent.id.id).update(
      _.participantId         := cmd.participantId,
      _.collectionEventTypeId := newCollectionEvent.collectionEventTypeId.id,
      _.sessionUserId         := cmd.sessionUserId,
      _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.added.timeCompleted   := cmd.timeCompleted.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.added.visitNumber     := cmd.visitNumber,
      _.added.annotations     := cmd.annotations.map { annotationToEvent(_) })
  }

  @silent private def updateVisitNumberCmdToEvent(cmd:                 UpdateCollectionEventVisitNumberCmd,
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
      _.sessionUserId                  := cmd.sessionUserId,
      _.time                           := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.visitNumberUpdated.version     := cmd.expectedVersion,
      _.visitNumberUpdated.visitNumber := updatedCevent.visitNumber)
  }

  @silent private def updateTimeCompletedCmdToEvent(cmd:                 UpdateCollectionEventTimeCompletedCmd,
                                                    participant:         Participant,
                                                    collectionEventType: CollectionEventType,
                                                    cevent:              CollectionEvent)
      : ServiceValidation[CollectionEventEvent] = {
    cevent.withTimeCompleted(cmd.timeCompleted).map { updatedCevent =>
      CollectionEventEvent(updatedCevent.id.id).update(
        _.participantId                      := participant.id.id,
        _.collectionEventTypeId              := updatedCevent.collectionEventTypeId.id,
        _.sessionUserId                      := cmd.sessionUserId,
        _.time                               := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        _.timeCompletedUpdated.version       := cmd.expectedVersion,
        _.timeCompletedUpdated.timeCompleted := updatedCevent.timeCompleted.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
    }
  }

  private def updateAnnotationCmdToEvent(cmd:                 CollectionEventUpdateAnnotationCmd,
                                         participant:         Participant,
                                         collectionEventType: CollectionEventType,
                                         cevent:              CollectionEvent)
      : ServiceValidation[CollectionEventEvent] = {
    val id = AnnotationTypeId(cmd.annotationTypeId)
    for {
      hasAnnotationType  <- {
        collectionEventType.annotationTypes
          .find(at => at.id == id)
          .toSuccessNel(s"IdNotFound: Collection Event Type does not have annotation type: $id")
      }
      annotation     <- Annotation.create(id,
                                          cmd.stringValue,
                                          cmd.numberValue,
                                          cmd.selectedValues)
      updatedCevent  <- cevent.withAnnotation(annotation)
    } yield CollectionEventEvent(updatedCevent.id.id).update(
      _.participantId                := participant.id.id,
      _.collectionEventTypeId        := updatedCevent.collectionEventTypeId.id,
      _.sessionUserId                := cmd.sessionUserId,
      _.time                         := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
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
          .find { x => x.id.id == cmd.annotationTypeId }
          .toSuccessNel(s"annotation type with ID does not exist: ${cmd.annotationTypeId}")
      }
      notRequired <- {
        if (annotType.required) EntityRequired(s"annotation is required").failureNel[Boolean]
        else true.successNel[String]
      }
      updatedCevent <- cevent.withoutAnnotation(AnnotationTypeId(cmd.annotationTypeId))
    } yield CollectionEventEvent(updatedCevent.id.id).update(
      _.participantId                      := participant.id.id,
      _.collectionEventTypeId              := updatedCevent.collectionEventTypeId.id,
      _.sessionUserId                      := cmd.sessionUserId,
      _.time                               := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
      _.annotationRemoved.version          := cmd.expectedVersion,
      _.annotationRemoved.annotationTypeId := cmd.annotationTypeId)
  }

  @silent private def removeCmdToEvent(cmd:                 RemoveCollectionEventCmd,
                                       participant:         Participant,
                                       collectionEventType: CollectionEventType,
                                       cevent:              CollectionEvent)
      : ServiceValidation[CollectionEventEvent] = {
    CollectionEventEvent(cevent.id.id).update(
      _.participantId         := participant.id.id,
      _.collectionEventTypeId := cevent.collectionEventTypeId.id,
      _.sessionUserId         := cmd.sessionUserId,
      _.time                  := OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
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
          timeAdded             = OffsetDateTime.parse(event.getTime),
          timeCompleted         = OffsetDateTime.parse(addedEvent.getTimeCompleted),
          visitNumber           = addedEvent.getVisitNumber,
          annotations           = addedEvent.annotations.map { annotationFromEvent(_) }.toSet)

      if (v.isFailure) {
        log.error(s"could not add collection event from event: $v, $event")
      }

      v.foreach { ce => collectionEventRepository.put(ce) }
    }
  }

  private def onValidEventAndVersion(event:        CollectionEventEvent,
                                     eventType:    Boolean,
                                     eventVersion: Long)
                                    (applyEvent: (CollectionEvent, OffsetDateTime) => ServiceValidation[Boolean])
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
            val eventTime = OffsetDateTime.parse(event.getTime)
            val update = applyEvent(cevent, eventTime)
            if (update.isFailure) {
              log.error(s"collection event update from event failed: $update")
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

      val timeCompleted = OffsetDateTime.parse(
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
      val v = cevent.withoutAnnotation(AnnotationTypeId(event.getAnnotationRemoved.getAnnotationTypeId))

      v.foreach { c =>
        collectionEventRepository.put(c.copy(timeModified = Some(eventTime)))
      }

      v.map(_ => true)
    }
  }

  private def applyRemovedEvent(event: CollectionEventEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isRemoved,
                           event.getRemoved.getVersion) { (cevent, eventTime) =>
      collectionEventRepository.remove(cevent)
      true.successNel[String]
    }
  }

  val errMsgVisitNumberExists: String = "a collection event with this visit number already exists"

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

  private def init(): Unit = {
    collectionEventRepository.init
  }

  init
}
