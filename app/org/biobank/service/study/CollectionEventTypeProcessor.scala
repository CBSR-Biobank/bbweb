package org.biobank.service.study

import org.biobank.domain._
import org.biobank.domain.study.{
  StudyId,
  CollectionEventType,
  CollectionEventTypeId,
  CollectionEventTypeRepository,
  CollectionSpecimenSpec
}
import org.biobank.service.Processor
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.EventUtils
import akka.actor._
import akka.persistence.{ SnapshotOffer, RecoveryCompleted }
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object CollectionEventTypeProcessor {

  def props = Props[CollectionEventTypeProcessor]

}

/**
 * The CollectionEventTypeProcessor is responsible for maintaining state changes for all
 * [[org.biobank.domain.study.CollectionEventType]] aggregates. This particular processor uses
 * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands, and if valid, persists the
 * generated events, afterwhich it will update the state of the
 * [[org.biobank.domain.study.CollectionEventType]] being processed.
 *
 * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
 */
class CollectionEventTypeProcessor @javax.inject.Inject() (
  val collectionEventTypeRepository: CollectionEventTypeRepository)
    extends Processor {

  import org.biobank.infrastructure.event.CollectionEventTypeEvents._
  import org.biobank.infrastructure.event.CollectionEventTypeEvents.CollectionEventTypeEvent.EventType

  override def persistenceId = "collection-event-processor-id"

  case class SnapshotState(collectionEventTypes: Set[CollectionEventType])

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  override def receiveRecover: Receive = {
    case event: CollectionEventTypeEvent =>
      event.eventType match {
        case et: EventType.Added                 => applyAddedEvent(event)
        case et: EventType.Removed               => applyRemovedEvent(event)
        case et: EventType.NameUpdated           => applyNameUpdatedEvent(event)
        case et: EventType.DescriptionUpdated    => applyDescriptionUpdatedEvent(event)
        case et: EventType.RecurringUpdated      => applyRecurringUpdatedEvent(event)
        case et: EventType.AnnotationTypeAdded   => applyAnnotationTypeAddedEvent(event)
        case et: EventType.AnnotationTypeRemoved => applyAnnotationTypeRemovedEvent(event)
        case et: EventType.SpecimenSpecAdded     => applySpecimenSpecAddedEvent(event)
        case et: EventType.SpecimenSpecUpdated   => applySpecimenSpecUpdatedEvent(event)
        case et: EventType.SpecimenSpecRemoved   => applySpecimenSpecRemovedEvent(event)

        case event => log.error(s"event not handled: $event")
      }

    case SnapshotOffer(metadata, snapshot: SnapshotState) =>
      log.info(s"snapshot metadata: $metadata")
      snapshot.collectionEventTypes.foreach { ceType =>
        collectionEventTypeRepository.put(ceType) }

    case RecoveryCompleted => log.debug("recovery completed")

    case msg => log.error(s"message not handled: $msg")

  }

  /**
   * These are the commands that are requested. A command can fail, and will send the failure as a response
   * back to the user. Each valid command generates one or more events and is journaled.
   */
  override def receiveCommand: Receive = {
    case cmd: AddCollectionEventTypeCmd                  => processAddCommand(cmd)
    case cmd: RemoveCollectionEventTypeCmd               => processRemoveCommand(cmd)
    case cmd: UpdateCollectionEventTypeNameCmd           => processUpdateNameCommand(cmd)
    case cmd: UpdateCollectionEventTypeDescriptionCmd    => processUpdateDescriptionCommand(cmd)
    case cmd: UpdateCollectionEventTypeRecurringCmd      => processUpdateRecurringCommand(cmd)
    case cmd: CollectionEventTypeAddAnnotationTypeCmd    => processAddAnnotationTypeCommand(cmd)
    case cmd: CollectionEventTypeUpdateAnnotationTypeCmd => processUpdateAnnotationTypeCommand(cmd)
    case cmd: RemoveCollectionEventTypeAnnotationTypeCmd => processRemoveAnnotationTypeCommand(cmd)
    case cmd: AddCollectionSpecimenSpecCmd               => processAddSepcimenSpecCommand(cmd)
    case cmd: UpdateCollectionSpecimenSpecCmd            => processUpdateSepcimenSpecCommand(cmd)
    case cmd: RemoveCollectionSpecimenSpecCmd            => processRemoveSpecimenSpecCommand(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(collectionEventTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"CollectionEventTypeProcessor: message not handled: $cmd")
  }

  private def processAddCommand(cmd: AddCollectionEventTypeCmd): Unit = {
    val studyId = StudyId(cmd.studyId)
    val id = collectionEventTypeRepository.nextIdentity

    val event = for {
        nameValid <- nameAvailable(cmd.name, studyId)
        newItem <- CollectionEventType.create(studyId,
                                              id,
                                              0L,
                                              cmd.name,
                                              cmd.description,
                                              cmd.recurring,
                                              Set.empty,
                                              Set.empty)
      } yield {
        CollectionEventTypeEvent(id.id).update(
          _.studyId                   := studyId.id,
          _.optionalUserId            := cmd.userId,
          _.time                      := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.added.name                := newItem.name,
          _.added.optionalDescription := newItem.description,
          _.added.recurring           := newItem.recurring)
      }

    process(event) { applyAddedEvent(_) }
  }

  private def processRemoveCommand(cmd: RemoveCollectionEventTypeCmd): Unit = {
    val v = update(cmd) { cet =>
        // FIXME check that this CET is not being used
        CollectionEventTypeEvent(cet.id.id).update(
          _.studyId         := cet.studyId.id,
          _.optionalUserId  := cmd.userId,
          _.time            := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.removed.version := cmd.expectedVersion).success
      }

    process(v){ applyRemovedEvent(_) }
  }

  private def processUpdateNameCommand(cmd: UpdateCollectionEventTypeNameCmd): Unit = {
    val v = update(cmd) { cet =>
        for {
          nameAvailable <- nameAvailable(cmd.name, cet.studyId, CollectionEventTypeId(cmd.id))
          newItem       <- cet.withName(cmd.name)
        } yield CollectionEventTypeEvent(newItem.id.id).update(
          _.studyId             := newItem.studyId.id,
          _.optionalUserId      := cmd.userId,
          _.time                := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.nameUpdated.version := cmd.expectedVersion,
          _.nameUpdated.name    := newItem.name)
      }

    process(v){ applyNameUpdatedEvent(_) }
  }

  def processUpdateDescriptionCommand(cmd: UpdateCollectionEventTypeDescriptionCmd): Unit = {
    val v = update(cmd) { cet =>
        cet.withDescription(cmd.description).map { _ =>
          CollectionEventTypeEvent(cet.id.id).update(
            _.studyId                                := cet.studyId.id,
            _.optionalUserId                         := cmd.userId,
            _.time                                   := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.descriptionUpdated.version             := cmd.expectedVersion,
            _.descriptionUpdated.optionalDescription := cmd.description)
        }
      }

    process(v){ applyDescriptionUpdatedEvent(_) }
  }

  def processUpdateRecurringCommand(cmd: UpdateCollectionEventTypeRecurringCmd): Unit = {
    val v = update(cmd) { cet =>
        cet.withRecurring(cmd.recurring).map { _ =>
          CollectionEventTypeEvent(cet.id.id).update(
            _.studyId                    := cet.studyId.id,
            _.optionalUserId             := cmd.userId,
            _.time                       := ISODateTimeFormat.dateTime.print(DateTime.now),
            _.recurringUpdated.version   := cmd.expectedVersion,
            _.recurringUpdated.recurring := cmd.recurring)
        }
      }

    process(v){ applyRecurringUpdatedEvent(_) }
  }

  def processAddAnnotationTypeCommand(cmd: CollectionEventTypeAddAnnotationTypeCmd): Unit = {
    val v = update(cmd) { cet =>
        for {
          annotationType <- {
            // need to call AnnotationType.create so that a new uniqueId is generated
            AnnotationType.create(cmd.name,
                                  cmd.description,
                                  cmd.valueType,
                                  cmd.maxValueCount,
                                  cmd.options,
                                  cmd.required)
          }
          updatedCet <- cet.withAnnotationType(annotationType)
        } yield CollectionEventTypeEvent(cet.id.id).update(
          _.studyId                            := cet.studyId.id,
          _.optionalUserId                     := cmd.userId,
          _.time                               := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.annotationTypeAdded.version        := cmd.expectedVersion,
          _.annotationTypeAdded.annotationType := EventUtils.annotationTypeToEvent(annotationType))
      }

    process(v){ applyAnnotationTypeAddedEvent(_) }
  }

  def processUpdateAnnotationTypeCommand(cmd: CollectionEventTypeUpdateAnnotationTypeCmd): Unit = {
    val v = update(cmd) { cet =>
        for {
          annotationType <- {
            AnnotationType(cmd.uniqueId,
                           cmd.name,
                           cmd.description,
                           cmd.valueType,
                           cmd.maxValueCount,
                           cmd.options,
                           cmd.required).success
          }
          updatedCet <- cet.withAnnotationType(annotationType)
        } yield CollectionEventTypeEvent(cet.id.id).update(
          _.studyId                              := cet.studyId.id,
          _.optionalUserId                       := cmd.userId,
          _.time                                 := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.annotationTypeUpdated.version        := cmd.expectedVersion,
          _.annotationTypeUpdated.annotationType := EventUtils.annotationTypeToEvent(annotationType))
      }

    process(v){ applyAnnotationTypeUpdatedEvent(_) }
  }

  def processRemoveAnnotationTypeCommand(cmd: RemoveCollectionEventTypeAnnotationTypeCmd): Unit = {
    val v = update(cmd) { cet =>
        cet.removeAnnotationType(cmd.uniqueId) map { c =>
          CollectionEventTypeEvent(cet.id.id).update(
          _.studyId                        := cet.studyId.id,
          _.optionalUserId                 := cmd.userId,
          _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.annotationTypeRemoved.version  := cmd.expectedVersion,
          _.annotationTypeRemoved.uniqueId := cmd.uniqueId)
        }
      }
    process(v){ applyAnnotationTypeRemovedEvent(_) }
  }

  def processAddSepcimenSpecCommand(cmd: AddCollectionSpecimenSpecCmd): Unit = {
    val v = update(cmd) { cet =>
        for {
          specimenSpec <- {
            // need to call AnnotationType.create so that a new uniqueId is generated
            CollectionSpecimenSpec.create(cmd.name,
                                          cmd.description,
                                          cmd.units,
                                          cmd.anatomicalSourceType,
                                          cmd.preservationType,
                                          cmd.preservationTemperatureType,
                                          cmd.specimenType,
                                          cmd.maxCount,
                                          cmd.amount)
          }
          updatedCet <- cet.withSpecimenSpec(specimenSpec)
        } yield CollectionEventTypeEvent(cet.id.id).update(
          _.studyId                        := cet.studyId.id,
          _.optionalUserId                 := cmd.userId,
          _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.specimenSpecAdded.version      := cmd.expectedVersion,
          _.specimenSpecAdded.specimenSpec := EventUtils.specimenSpecToEvent(specimenSpec))
      }

    process(v){ applySpecimenSpecAddedEvent(_) }
  }

  def processUpdateSepcimenSpecCommand(cmd: UpdateCollectionSpecimenSpecCmd): Unit = {
    val v = update(cmd) { cet =>
        for {
          specimenSpec <- {
            CollectionSpecimenSpec(cmd.uniqueId,
                                   cmd.name,
                                   cmd.description,
                                   cmd.units,
                                   cmd.anatomicalSourceType,
                                   cmd.preservationType,
                                   cmd.preservationTemperatureType,
                                   cmd.specimenType,
                                   cmd.maxCount,
                                   cmd.amount).success
          }
          updatedCet <- cet.withSpecimenSpec(specimenSpec)
        } yield CollectionEventTypeEvent(cet.id.id).update(
          _.studyId                          := cet.studyId.id,
          _.optionalUserId                   := cmd.userId,
          _.time                             := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.specimenSpecUpdated.version      := cmd.expectedVersion,
          _.specimenSpecUpdated.specimenSpec := EventUtils.specimenSpecToEvent(specimenSpec))
      }

    process(v){ applySpecimenSpecUpdatedEvent(_) }
  }

  def processRemoveSpecimenSpecCommand(cmd: RemoveCollectionSpecimenSpecCmd): Unit = {
    val v = update(cmd) { cet =>
        cet.removeSpecimenSpec(cmd.uniqueId) map { c =>
          CollectionEventTypeEvent(cet.id.id).update(
          _.studyId                      := cet.studyId.id,
          _.optionalUserId               := cmd.userId,
          _.time                         := ISODateTimeFormat.dateTime.print(DateTime.now),
          _.specimenSpecRemoved.version  := cmd.expectedVersion,
          _.specimenSpecRemoved.uniqueId := cmd.uniqueId)
        }
      }
    process(v){ applySpecimenSpecRemovedEvent(_) }
  }

  private def update(cmd: CollectionEventTypeModifyCommand)
                    (fn: CollectionEventType => DomainValidation[CollectionEventTypeEvent])
      : DomainValidation[CollectionEventTypeEvent] = {
    for {
      cet          <- collectionEventTypeRepository.withId(StudyId(cmd.studyId),
                                                           CollectionEventTypeId(cmd.id))
      validVersion <-  cet.requireVersion(cmd.expectedVersion)
      event        <- fn(cet)
    } yield event
  }

  def onValidEventAndVersion(event:        CollectionEventTypeEvent,
                             eventType:    Boolean,
                             eventVersion: Long)
                            (fn: CollectionEventType => Unit): Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      collectionEventTypeRepository.getByKey(CollectionEventTypeId(event.id)).fold(
        err => log.error(s"collection event type from event does not exist: $err"),
        cet => {
          if (cet.version != eventVersion) {
            log.error(s"event version check failed: cet version: ${cet.version}, event: $event")
          } else {
            fn(cet)
          }
        }
      )
    }
  }

  private def applyAddedEvent(event: CollectionEventTypeEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded

      CollectionEventType.create(
        studyId           = StudyId(event.getStudyId),
        id                = CollectionEventTypeId(event.id),
        version           = 0L,
        name              = addedEvent.getName,
        description       = addedEvent.description,
        recurring         = addedEvent.getRecurring,
        specimenSpecs     = Set.empty,
        annotationTypes   = Set.empty
      ).fold(
        err => log.error(s"could not add collection event type from event: $event"),
        cet => {
          collectionEventTypeRepository.put(
            cet.copy(timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)))
          ()
        }
      )
    }
  }

  private def applyRemovedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isRemoved,
                           event.getRemoved.getVersion) { cet =>
      collectionEventTypeRepository.getByKey(cet.id).fold(
        err => log.error(s"removing collection event type from event failed: $err"),
        cet => {
          collectionEventTypeRepository.remove(cet)
          ()
        }
      )
    }
  }

  private def applyNameUpdatedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isNameUpdated,
                           event.getNameUpdated.getVersion) { cet =>
      val updatedEvent = event.getNameUpdated
      cet.withName(updatedEvent.getName).fold(
        err => log.error(s"updating cevent type from event failed: $err"),
        c => {
          collectionEventTypeRepository.put(
            c.copy(
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyDescriptionUpdatedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isDescriptionUpdated,
                           event.getDescriptionUpdated.getVersion) { cet =>
      val updatedEvent = event.getDescriptionUpdated
      cet.withDescription(updatedEvent.description).fold(
        err => log.error(s"updating cevent type from event failed: $err"),
        c => {
          collectionEventTypeRepository.put(
            c.copy(
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyRecurringUpdatedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isRecurringUpdated,
                           event.getRecurringUpdated.getVersion) { cet =>
      val updatedEvent = event.getRecurringUpdated
      cet.withRecurring(updatedEvent.getRecurring).fold(
        err => log.error(s"updating cevent type from event failed: $err"),
        c => {
          collectionEventTypeRepository.put(
            c.copy(
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyAnnotationTypeAddedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationTypeAdded,
                           event.getAnnotationTypeAdded.getVersion) { cet =>
      val eventAnnotationType = event.getAnnotationTypeAdded.getAnnotationType
      cet.withAnnotationType(EventUtils.annotationTypeFromEvent(eventAnnotationType)).fold(
        err => log.error(s"updating cevent type from event failed: $err"),
        c => {
          collectionEventTypeRepository.put(
            c.copy(
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyAnnotationTypeUpdatedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationTypeUpdated,
                           event.getAnnotationTypeUpdated.getVersion) { cet =>
      val eventAnnotationType = event.getAnnotationTypeUpdated.getAnnotationType
      cet.withAnnotationType(EventUtils.annotationTypeFromEvent(eventAnnotationType)).fold(
        err => log.error(s"updating cevent type from event failed: $err"),
        c => {
          collectionEventTypeRepository.put(
            c.copy(
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  private def applyAnnotationTypeRemovedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationTypeRemoved,
                           event.getAnnotationTypeRemoved.getVersion) { cet =>
      cet.removeAnnotationType(event.getAnnotationTypeRemoved.getUniqueId).fold(
        err => log.error(s"updating cevent type from event failed: $err"),
        c => {
          collectionEventTypeRepository.put(
            c.copy(
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  def applySpecimenSpecAddedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isSpecimenSpecAdded,
                           event.getSpecimenSpecAdded.getVersion) { cet =>
      val eventSpecimenSpec = event.getSpecimenSpecAdded
      cet.withSpecimenSpec(EventUtils.specimenSpecFromEvent(eventSpecimenSpec.getSpecimenSpec)).fold(
        err => log.error(s"updating cevent type from event failed: $err"),
        c => {
          collectionEventTypeRepository.put(
            c.copy(
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  def applySpecimenSpecUpdatedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isSpecimenSpecUpdated,
                           event.getSpecimenSpecUpdated.getVersion) { cet =>
      val eventSpecimenSpec = event.getSpecimenSpecUpdated
      cet.withSpecimenSpec(EventUtils.specimenSpecFromEvent(eventSpecimenSpec.getSpecimenSpec)).fold(
        err => log.error(s"updating cevent type from event failed: $err"),
        c => {
          collectionEventTypeRepository.put(
            c.copy(
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  def applySpecimenSpecRemovedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isSpecimenSpecRemoved,
                           event.getSpecimenSpecRemoved.getVersion) { cet =>
      cet.removeSpecimenSpec(event.getSpecimenSpecRemoved.getUniqueId).fold(
        err => log.error(s"updating cevent type from event failed: $err"),
        c => {
          collectionEventTypeRepository.put(
            c.copy(
              timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    }
  }

  val ErrMsgNameExists = "collection event type with name already exists"

  private def nameAvailable(name: String, studyId: StudyId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, collectionEventTypeRepository, ErrMsgNameExists) { item =>
      (item.name == name) && (item.studyId == studyId)
    }
  }

  private def nameAvailable(name: String,
                            studyId: StudyId,
                            excludeId: CollectionEventTypeId)
      : DomainValidation[Boolean] = {
    nameAvailableMatcher(name, collectionEventTypeRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.studyId == studyId) && (item.id != excludeId)
    }
  }

}
