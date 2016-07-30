package org.biobank.service.study

import akka.actor._
import akka.persistence.{ SnapshotOffer, RecoveryCompleted }
import org.biobank.domain._
import org.biobank.domain.participants.CollectionEventRepository
import org.biobank.domain.study.{StudyId, CollectionEventType, CollectionEventTypeId, CollectionEventTypeRepository, CollectionSpecimenSpec }
import org.biobank.infrastructure.command.CollectionEventTypeCommands._
import org.biobank.infrastructure.event.EventUtils
import org.biobank.service.{Processor, ServiceValidation}
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
  val collectionEventTypeRepository: CollectionEventTypeRepository,
  val collectionEventRepository:     CollectionEventRepository)
    extends Processor {

  import org.biobank.CommonValidations._
  import org.biobank.infrastructure.event.CollectionEventTypeEvents._
  import org.biobank.infrastructure.event.CollectionEventTypeEvents.CollectionEventTypeEvent.EventType

  override def persistenceId = "collection-event-processor-id"

  case class SnapshotState(collectionEventTypes: Set[CollectionEventType])

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
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
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  override def receiveCommand: Receive = {
    case cmd: AddCollectionEventTypeCmd =>
      process(addCmdToEvent(cmd))(applyAddedEvent)

    case cmd: RemoveCollectionEventTypeCmd =>
      processUpdateCmd(cmd, removeCmdToEvent, applyRemovedEvent)

    case cmd: UpdateCollectionEventTypeNameCmd =>
      processUpdateCmd(cmd, updateNameCmdToEvent, applyNameUpdatedEvent)

    case cmd: UpdateCollectionEventTypeDescriptionCmd =>
      processUpdateCmd(cmd, updateDescriptionCmdToEvent, applyDescriptionUpdatedEvent)

    case cmd: UpdateCollectionEventTypeRecurringCmd =>
      processUpdateCmd(cmd, updateRecurringCmdToEvent, applyRecurringUpdatedEvent)

    case cmd: CollectionEventTypeAddAnnotationTypeCmd =>
      processUpdateCmd(cmd, addAnnotationTypeCmdToEvent, applyAnnotationTypeAddedEvent)

    case cmd: RemoveCollectionEventTypeAnnotationTypeCmd =>
      processUpdateCmd(cmd, removeAnnotationTypeCmdToEvent, applyAnnotationTypeRemovedEvent)

    case cmd: AddCollectionSpecimenSpecCmd =>
      processUpdateCmd(cmd, addSepcimenSpecCmdToEvent, applySpecimenSpecAddedEvent)

    case cmd: UpdateCollectionSpecimenSpecCmd =>
      processUpdateCmd(cmd, updateSepcimenSpecCmdToEvent, applySpecimenSpecUpdatedEvent)

    case cmd: RemoveCollectionSpecimenSpecCmd =>
      processUpdateCmd(cmd, removeSpecimenSpecCmdToEvent, applySpecimenSpecRemovedEvent)

    case "snap" =>
      saveSnapshot(SnapshotState(collectionEventTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"CollectionEventTypeProcessor: message not handled: $cmd")
  }

  private def addCmdToEvent(cmd: AddCollectionEventTypeCmd): ServiceValidation[CollectionEventTypeEvent] = {
    val studyId = StudyId(cmd.studyId)
    for {
      cetId     <- validNewIdentity(collectionEventTypeRepository.nextIdentity, collectionEventTypeRepository)
      nameValid <- nameAvailable(cmd.name, studyId)
      newItem   <- CollectionEventType.create(studyId,
                                              cetId,
                                              0L,
                                              cmd.name,
                                              cmd.description,
                                              cmd.recurring,
                                              Set.empty,
                                              Set.empty)
    } yield CollectionEventTypeEvent(cetId.id).update(
      _.studyId                   := studyId.id,
      _.optionalUserId            := cmd.userId,
      _.time                      := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.added.name                := newItem.name,
      _.added.optionalDescription := newItem.description,
      _.added.recurring           := newItem.recurring)
  }

  private def removeCmdToEvent(cmd: RemoveCollectionEventTypeCmd, ceventType: CollectionEventType)
      : ServiceValidation[CollectionEventTypeEvent] = {
    if (collectionEventRepository.collectionEventTypeInUse(ceventType.id)) {
      EntityInUse(s"collection event type in use: ${ceventType.id}").failureNel[CollectionEventTypeEvent]
    } else {
      CollectionEventTypeEvent(ceventType.id.id).update(
        _.studyId         := ceventType.studyId.id,
        _.optionalUserId  := cmd.userId,
        _.time            := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.removed.version := cmd.expectedVersion).successNel[String]
    }
  }

  private def updateNameCmdToEvent(cmd: UpdateCollectionEventTypeNameCmd, cet: CollectionEventType)
      : ServiceValidation[CollectionEventTypeEvent] = {
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

  private def updateDescriptionCmdToEvent(cmd: UpdateCollectionEventTypeDescriptionCmd,
                                          cet: CollectionEventType)
      : ServiceValidation[CollectionEventTypeEvent] = {
    cet.withDescription(cmd.description).map { _ =>
      CollectionEventTypeEvent(cet.id.id).update(
        _.studyId                                := cet.studyId.id,
        _.optionalUserId                         := cmd.userId,
        _.time                                   := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.descriptionUpdated.version             := cmd.expectedVersion,
        _.descriptionUpdated.optionalDescription := cmd.description)
    }
  }

  private def updateRecurringCmdToEvent(cmd: UpdateCollectionEventTypeRecurringCmd,
                                        cet: CollectionEventType)
      : ServiceValidation[CollectionEventTypeEvent] = {
    cet.withRecurring(cmd.recurring).map { _ =>
      CollectionEventTypeEvent(cet.id.id).update(
        _.studyId                    := cet.studyId.id,
        _.optionalUserId             := cmd.userId,
        _.time                       := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.recurringUpdated.version   := cmd.expectedVersion,
        _.recurringUpdated.recurring := cmd.recurring)
    }
  }

  private def addAnnotationTypeCmdToEvent(cmd: CollectionEventTypeAddAnnotationTypeCmd,
                                          cet: CollectionEventType)
      : ServiceValidation[CollectionEventTypeEvent] = {
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

private def removeAnnotationTypeCmdToEvent(cmd: RemoveCollectionEventTypeAnnotationTypeCmd,
                                             cet: CollectionEventType)
      : ServiceValidation[CollectionEventTypeEvent] = {
    cet.removeAnnotationType(cmd.uniqueId) map { c =>
      CollectionEventTypeEvent(cet.id.id).update(
        _.studyId                        := cet.studyId.id,
        _.optionalUserId                 := cmd.userId,
        _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.annotationTypeRemoved.version  := cmd.expectedVersion,
        _.annotationTypeRemoved.uniqueId := cmd.uniqueId)
    }
  }

  private def addSepcimenSpecCmdToEvent(cmd: AddCollectionSpecimenSpecCmd,
                                        cet: CollectionEventType)
      : ServiceValidation[CollectionEventTypeEvent] = {
    for {
      specimenSpec <- CollectionSpecimenSpec.create(cmd.name,
                                                    cmd.description,
                                                    cmd.units,
                                                    cmd.anatomicalSourceType,
                                                    cmd.preservationType,
                                                    cmd.preservationTemperatureType,
                                                    cmd.specimenType,
                                                    cmd.maxCount,
                                                    cmd.amount)
      updatedCet <- cet.withSpecimenSpec(specimenSpec)
    } yield CollectionEventTypeEvent(cet.id.id).update(
      _.studyId                        := cet.studyId.id,
      _.optionalUserId                 := cmd.userId,
      _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.specimenSpecAdded.version      := cmd.expectedVersion,
      _.specimenSpecAdded.specimenSpec := EventUtils.specimenSpecToEvent(specimenSpec))
  }

  private def updateSepcimenSpecCmdToEvent(cmd: UpdateCollectionSpecimenSpecCmd,
                                           cet: CollectionEventType)
      : ServiceValidation[CollectionEventTypeEvent] = {
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
                               cmd.amount).successNel[String]
      }
      updatedCet <- cet.withSpecimenSpec(specimenSpec)
    } yield CollectionEventTypeEvent(cet.id.id).update(
      _.studyId                          := cet.studyId.id,
      _.optionalUserId                   := cmd.userId,
      _.time                             := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.specimenSpecUpdated.version      := cmd.expectedVersion,
      _.specimenSpecUpdated.specimenSpec := EventUtils.specimenSpecToEvent(specimenSpec))
  }

  private def removeSpecimenSpecCmdToEvent(cmd: RemoveCollectionSpecimenSpecCmd,
                                           cet: CollectionEventType)
      : ServiceValidation[CollectionEventTypeEvent] = {
    cet.removeSpecimenSpec(cmd.uniqueId) map { c =>
      CollectionEventTypeEvent(cet.id.id).update(
        _.studyId                      := cet.studyId.id,
        _.optionalUserId               := cmd.userId,
        _.time                         := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.specimenSpecRemoved.version  := cmd.expectedVersion,
        _.specimenSpecRemoved.uniqueId := cmd.uniqueId)
    }
  }

  private def processUpdateCmd[T <: CollectionEventTypeModifyCommand]
    (cmd: T,
     cmdToEvent: (T, CollectionEventType) => ServiceValidation[CollectionEventTypeEvent],
     applyEvent: CollectionEventTypeEvent => Unit): Unit = {
    val event = for {
        cet          <- collectionEventTypeRepository.withId(StudyId(cmd.studyId),
                                                             CollectionEventTypeId(cmd.id))
        validVersion <- cet.requireVersion(cmd.expectedVersion)
        event        <- cmdToEvent(cmd, cet)
      } yield event

    process(event)(applyEvent)
  }

  private def onValidEventAndVersion(event:        CollectionEventTypeEvent,
                                     eventType:    Boolean,
                                     eventVersion: Long)
                                    (applyEvent: (CollectionEventType, DateTime) => ServiceValidation[Boolean])
      : Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      collectionEventTypeRepository.getByKey(CollectionEventTypeId(event.id)).fold(
        err => log.error(s"collection event type from event does not exist: $err"),
        cet => {
          if (cet.version != eventVersion) {
            log.error(s"event version check failed: cet version: ${cet.version}, event: $event")
          } else {
            val eventTime = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)
            val update = applyEvent(cet, eventTime)

            if (update.isFailure) {
              log.error(s"centre update from event failed: $update")
            }
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

      val v = CollectionEventType.create(
        studyId           = StudyId(event.getStudyId),
        id                = CollectionEventTypeId(event.id),
        version           = 0L,
        name              = addedEvent.getName,
        description       = addedEvent.description,
        recurring         = addedEvent.getRecurring,
        specimenSpecs     = Set.empty,
        annotationTypes   = Set.empty)

      if (v.isFailure) {
        log.error(s"could not add collection event type from event: $v")
      }

      v.foreach { ct =>
        val timeAdded = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)
        collectionEventTypeRepository.put(ct.copy(timeAdded = timeAdded))
      }
    }
  }

  private def applyRemovedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isRemoved,
                           event.getRemoved.getVersion) { (cet, _) =>
      val v = collectionEventTypeRepository.getByKey(cet.id)
      v.foreach(collectionEventTypeRepository.remove)
      v.map(_ => true)
    }
  }

  private def storeIfValid(validation: ServiceValidation[CollectionEventType],
                           eventTime: DateTime): ServiceValidation[Boolean] = {
    validation.foreach { c =>
      collectionEventTypeRepository.put(c.copy(timeModified = Some(eventTime)))
    }
    validation.map(_ => true)
  }

  private def applyNameUpdatedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isNameUpdated,
                           event.getNameUpdated.getVersion) { (cet, eventTime) =>
      storeIfValid(cet.withName(event.getNameUpdated.getName), eventTime)
    }
  }

  private def applyDescriptionUpdatedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isDescriptionUpdated,
                           event.getDescriptionUpdated.getVersion) { (cet, eventTime) =>
      storeIfValid(cet.withDescription(event.getDescriptionUpdated.description), eventTime)
    }
  }

  private def applyRecurringUpdatedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isRecurringUpdated,
                           event.getRecurringUpdated.getVersion) { (cet, eventTime) =>
      storeIfValid(cet.withRecurring(event.getRecurringUpdated.getRecurring), eventTime)
    }
  }

  private def applyAnnotationTypeAddedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationTypeAdded,
                           event.getAnnotationTypeAdded.getVersion) { (cet, eventTime) =>
      val eventAnnotationType = event.getAnnotationTypeAdded.getAnnotationType
      storeIfValid(cet.withAnnotationType(EventUtils.annotationTypeFromEvent(eventAnnotationType)), eventTime)
    }
  }

  private def applyAnnotationTypeRemovedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isAnnotationTypeRemoved,
                           event.getAnnotationTypeRemoved.getVersion) { (cet, eventTime) =>
      storeIfValid(cet.removeAnnotationType(event.getAnnotationTypeRemoved.getUniqueId), eventTime)
    }
  }

  private def applySpecimenSpecAddedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isSpecimenSpecAdded,
                           event.getSpecimenSpecAdded.getVersion) { (cet, eventTime) =>
      storeIfValid(
        cet.withSpecimenSpec(EventUtils.specimenSpecFromEvent(event.getSpecimenSpecAdded.getSpecimenSpec)),
        eventTime)
    }
  }

  private def applySpecimenSpecUpdatedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isSpecimenSpecUpdated,
                           event.getSpecimenSpecUpdated.getVersion) { (cet, eventTime) =>
      storeIfValid(
        cet.withSpecimenSpec(EventUtils.specimenSpecFromEvent(event.getSpecimenSpecUpdated.getSpecimenSpec)),
        eventTime)
    }
  }

  private def applySpecimenSpecRemovedEvent(event: CollectionEventTypeEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isSpecimenSpecRemoved,
                           event.getSpecimenSpecRemoved.getVersion) { (cet, eventTime) =>
      storeIfValid(cet.removeSpecimenSpec(event.getSpecimenSpecRemoved.getUniqueId), eventTime)
    }
  }

  val ErrMsgNameExists = "collection event type with name already exists"

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String, studyId: StudyId): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, collectionEventTypeRepository, ErrMsgNameExists) { item =>
      (item.name == name) && (item.studyId == studyId)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String,
                            studyId: StudyId,
                            excludeId: CollectionEventTypeId)
      : ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, collectionEventTypeRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.studyId == studyId) && (item.id != excludeId)
    }
  }

}
