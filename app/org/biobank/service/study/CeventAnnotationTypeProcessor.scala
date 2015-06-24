package org.biobank.service.study

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study._
import org.biobank.domain.AnnotationValueType._

import akka.actor._
import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object CeventAnnotationTypeProcessor {

  def props = Props[CeventAnnotationTypeProcessor]

}

/**
  * The CeventAnnotationTypeProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.CollectionEventAnnotationType]] aggregates. This particular processor uses
  * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
  * the generated events, afterwhich it will updated the current state of the
  * [[org.biobank.domain.study.CollectionEventAnnotationType]] being processed.
  *
  * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
  */
class CeventAnnotationTypeProcessor @javax.inject.Inject() (
  val annotationTypeRepository: CollectionEventAnnotationTypeRepository,
  val collectionEventTypeRepository: CollectionEventTypeRepository)
    extends StudyAnnotationTypeProcessor[CollectionEventAnnotationType] {
  import org.biobank.infrastructure.event.StudyEventsUtil._
  import StudyEvent.EventType

  override def persistenceId = "cevent-annotation-type-processor-id"

  case class SnapshotState(ceventAnnotationTypes: Set[CollectionEventAnnotationType])

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case event: StudyEvent => event.eventType match {
        case et: EventType.CollectionEventAnnotationTypeAdded => applyCollectionEventAnnotationTypeAddedEvent(event)
        case et: EventType.CollectionEventAnnotationTypeUpdated => applyCollectionEventAnnotationTypeUpdatedEvent(event)
        case et: EventType.CollectionEventAnnotationTypeRemoved => applyCollectionEventAnnotationTypeRemovedEvent(event)

        case event => log.error(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.ceventAnnotationTypes.foreach{ annotType =>
        annotationTypeRepository.put(annotType) }
  }

  /**
    * These are the commands that are requested. A command can fail, and will send the failure as a response
    * back to the user. Each valid command generates one or more events and is journaled.
    */
  val receiveCommand: Receive = {
    case cmd: AddCollectionEventAnnotationTypeCmd =>    processAddCollectionEventAnnotationTypeCmd(cmd)
    case cmd: UpdateCollectionEventAnnotationTypeCmd => processUpdateCollectionEventAnnotationTypeCmd(cmd)
    case cmd: RemoveCollectionEventAnnotationTypeCmd => processRemoveCollectionEventAnnotationTypeCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(annotationTypeRepository.getValues.toSet))
      stash()

    case msg => log.error(s"CeventAnnotationTypeProcessor: message not handled: $msg")
  }

  private def processAddCollectionEventAnnotationTypeCmd
    (cmd: AddCollectionEventAnnotationTypeCmd): Unit = {
    val timeNow = DateTime.now
    val studyId = StudyId(cmd.studyId)
    val id = annotationTypeRepository.nextIdentity
    val event = for {
      nameValid <- nameAvailable(cmd.name, studyId)
      newItem <- CollectionEventAnnotationType.create(
        studyId, id, -1L, timeNow, cmd.name, cmd.description,
        cmd.valueType, cmd.maxValueCount, cmd.options)
      event <- createStudyEvent(newItem.studyId, cmd).withCollectionEventAnnotationTypeAdded(
        CollectionEventAnnotationTypeAddedEvent(
          annotationTypeId = Some(newItem.id.id),
          name             = Some(newItem.name),
          description      = newItem.description,
          valueType        = Some(newItem.valueType.toString),
          maxValueCount    = newItem.maxValueCount,
          options          = newItem.options)).success
      } yield event

    process(event) { applyCollectionEventAnnotationTypeAddedEvent(_) }
  }

  private def processUpdateCollectionEventAnnotationTypeCmd
    (cmd: UpdateCollectionEventAnnotationTypeCmd): Unit = {
    val v = update(cmd) { at =>
      for {
        nameAvailable <- nameAvailable(cmd.name, StudyId(cmd.studyId), AnnotationTypeId(cmd.id))
        newItem       <- at.update(cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
        event         <- createStudyEvent(at.studyId, cmd).withCollectionEventAnnotationTypeUpdated(
        CollectionEventAnnotationTypeUpdatedEvent(
          annotationTypeId = Some(newItem.id.id),
          version          = Some(newItem.version),
          name             = Some(newItem.name),
          description      = newItem.description,
          valueType        = Some(newItem.valueType.toString),
          maxValueCount    = newItem.maxValueCount,
          options          = newItem.options)).success
      } yield event
    }

    process(v){ applyCollectionEventAnnotationTypeUpdatedEvent(_) }
  }

  private def processRemoveCollectionEventAnnotationTypeCmd
    (cmd: RemoveCollectionEventAnnotationTypeCmd): Unit = {
    val v = update(cmd) { at =>
      createStudyEvent(at.studyId, cmd).withCollectionEventAnnotationTypeRemoved(
        CollectionEventAnnotationTypeRemovedEvent(Some(at.id.id))).success
    }

    process(v){ applyCollectionEventAnnotationTypeRemovedEvent(_) }
  }

  /** Updates to annotation types only allowed if they are not being used by any collection event types.
    */
  def update
    (cmd: StudyAnnotationTypeModifyCommand)
    (fn: CollectionEventAnnotationType => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    for {
      annotType    <- annotationTypeRepository.withId(StudyId(cmd.studyId), cmd.id)
      notInUse     <- checkNotInUse(annotType)
      validVersion <- annotType.requireVersion( cmd.expectedVersion)
      event        <- fn(annotType)
    } yield event
  }

  private def applyCollectionEventAnnotationTypeAddedEvent(event: StudyEvent) : Unit = {
    log.debug(s"recoverEvent: $event")

    if (event.eventType.isCollectionEventAnnotationTypeAdded) {
      val addedEvent = event.getCollectionEventAnnotationTypeAdded

      annotationTypeRepository.put(
        CollectionEventAnnotationType(
          studyId       = StudyId(event.id),
          id            = AnnotationTypeId(addedEvent.getAnnotationTypeId),
          version       = 0L,
          timeAdded     = ISODateTimeFormat.dateTime.parseDateTime(event.getTime),
          timeModified  = None,
          name          = addedEvent.getName,
          description   = addedEvent.description,
          valueType     = AnnotationValueType.withName(addedEvent.getValueType),
          maxValueCount = addedEvent.maxValueCount,
          options       = addedEvent.options))
      ()
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyCollectionEventAnnotationTypeUpdatedEvent
    (event: StudyEvent) : Unit = {
    if (event.eventType.isCollectionEventAnnotationTypeUpdated) {
      val updatedEvent = event.getCollectionEventAnnotationTypeUpdated

      annotationTypeRepository.getByKey(AnnotationTypeId(updatedEvent.getAnnotationTypeId)).fold(
        err => log.error(s"updating annotation type from event failed: $err"),
        at => {
          annotationTypeRepository.put(
            at.copy(version       = updatedEvent.getVersion,
                    name          = updatedEvent.getName,
                    description   = updatedEvent.description,
                    valueType     = AnnotationValueType.withName(updatedEvent.getValueType),
                    maxValueCount = updatedEvent.maxValueCount,
                    options       = updatedEvent.options,
                    timeModified  = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  protected def applyCollectionEventAnnotationTypeRemovedEvent(event: StudyEvent) : Unit = {
    if (event.eventType.isCollectionEventAnnotationTypeRemoved) {
      applyStudyAnnotationTypeRemovedEvent(
        AnnotationTypeId(event.getCollectionEventAnnotationTypeRemoved.getAnnotationTypeId))
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  def checkNotInUse
    (annotationType: CollectionEventAnnotationType)
      : DomainValidation[CollectionEventAnnotationType] = {
    if (collectionEventTypeRepository.annotationTypeInUse(annotationType)) {
      DomainError(s"annotation type is in use by collection event type: ${annotationType.id}").failureNel
    } else {
      annotationType.success
    }
  }
}
