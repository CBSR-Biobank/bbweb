package org.biobank.service.study

import org.biobank.domain._
import org.biobank.domain.study.{
  StudyId,
  ProcessingType,
  ProcessingTypeId,
  ProcessingTypeRepository
}
import org.biobank.service.Processor
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

import akka.actor._
import akka.persistence.SnapshotOffer
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object ProcessingTypeProcessor {

  def props = Props[ProcessingTypeProcessor]

}

/**
  * The ProcessingTypeProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.ProcessingType]] aggregates. This particular processor uses
  * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
  * the generated events, afterwhich it will updated the current state of the
  * [[org.biobank.domain.study.ProcessingType]] being processed.
  *
  * It is a child actor of
  * [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
  */
class ProcessingTypeProcessor @javax.inject.Inject() (val processingTypeRepository: ProcessingTypeRepository)
    extends Processor {
  import org.biobank.infrastructure.event.StudyEventsUtil._
  import StudyEvent.EventType

  override def persistenceId = "processing-type-processor-id"

  case class SnapshotState(processingTypes: Set[ProcessingType])

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case event: StudyEvent => event.eventType match {
      case et: EventType.ProcessingTypeAdded   => applyProcessingTypeAddedEvent(event)
      case et: EventType.ProcessingTypeUpdated => applyProcessingTypeUpdatedEvent(event)
      case et: EventType.ProcessingTypeRemoved => applyProcessingTypeRemovedEvent(event)

      case event => log.error(s"event not handled: $event")
    }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.processingTypes.foreach { ceType =>
        processingTypeRepository.put(ceType)
      }
  }


  /**
    * These are the commands that are requested. A command can fail, and will send the failure as a response
    * back to the user. Each valid command generates one or more events and is journaled.
    */
  val receiveCommand: Receive = {
    case cmd: AddProcessingTypeCmd    => processAddProcessingTypeCmd(cmd)
    case cmd: UpdateProcessingTypeCmd => processUpdateProcessingTypeCmd(cmd)
    case cmd: RemoveProcessingTypeCmd => processRemoveProcessingTypeCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(processingTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"ProcessingTypeProcessor: message not handled: $cmd")
  }

  private def processAddProcessingTypeCmd(cmd: AddProcessingTypeCmd): Unit = {
    val studyId = StudyId(cmd.studyId)
    val id = processingTypeRepository.nextIdentity

    val event = for {
      nameValid <- nameAvailable(cmd.name, studyId)
      newItem   <- ProcessingType.create(studyId,
                                         id,
                                         -1L,
                                         cmd.name,
                                         cmd.description,
                                         cmd.enabled)
      event     <- createStudyEvent(newItem.studyId, cmd).withProcessingTypeAdded(
        ProcessingTypeAddedEvent(
          processingTypeId = Some(newItem.id.id),
          name             = Some(newItem.name),
          description      = newItem.description,
          enabled          = Some(newItem.enabled))).success
    } yield event

    process(event){ applyProcessingTypeAddedEvent(_) }
  }

  private def processUpdateProcessingTypeCmd(cmd: UpdateProcessingTypeCmd): Unit = {
    val v = update(cmd) { pt =>
      for {
        nameValid <- nameAvailable(cmd.name, StudyId(cmd.studyId), ProcessingTypeId(cmd.id))
        updatedPt <- pt.update(cmd.name, cmd.description, cmd.enabled)
        event <- createStudyEvent(updatedPt.studyId, cmd).withProcessingTypeUpdated(
          ProcessingTypeUpdatedEvent(
            processingTypeId = Some(updatedPt.id.id),
            version          = Some(updatedPt.version),
            name             = Some(updatedPt.name),
            description      = updatedPt.description,
            enabled          = Some(updatedPt.enabled))).success
      } yield event
    }
    process(v) { applyProcessingTypeUpdatedEvent(_) }
  }

  private def processRemoveProcessingTypeCmd(cmd: RemoveProcessingTypeCmd): Unit = {
    val v = update(cmd) { pt =>
      createStudyEvent(pt.studyId, cmd).withProcessingTypeRemoved(
        ProcessingTypeRemovedEvent(Some(cmd.id))).success
    }
    process(v){ applyProcessingTypeRemovedEvent(_) }
  }

  def update
    (cmd: ProcessingTypeModifyCommand)
    (fn: ProcessingType => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    for {
      pt           <- processingTypeRepository.withId(StudyId(cmd.studyId), ProcessingTypeId(cmd.id))
      notInUse     <- checkNotInUse(pt)
      validVersion <- pt.requireVersion(cmd.expectedVersion)
      event        <- fn(pt)
    } yield event
  }

  private def applyProcessingTypeAddedEvent(event: StudyEvent): Unit = {
    if (event.eventType.isProcessingTypeAdded) {
      val addedEvent = event.getProcessingTypeAdded

      processingTypeRepository.put(
        ProcessingType(studyId      = StudyId(event.id),
                       id           = ProcessingTypeId(addedEvent.getProcessingTypeId),
                       version      = 0L,
                       timeAdded    = ISODateTimeFormat.dateTime.parseDateTime(event.getTime),
                       timeModified = None,
                       name         = addedEvent.getName,
                       description  = addedEvent.description,
                       enabled      = addedEvent.getEnabled))
      ()
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyProcessingTypeUpdatedEvent(event: StudyEvent): Unit = {
    if (event.eventType.isProcessingTypeUpdated) {
      val updatedEvent = event.getProcessingTypeUpdated

      processingTypeRepository.getByKey(ProcessingTypeId(updatedEvent.getProcessingTypeId)).fold(
        err => log.error(s"updating processing type from event failed: $err"),
        pt => {
          processingTypeRepository.put(
            pt.copy(version      = updatedEvent.getVersion,
                    timeModified  = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
                    name         = updatedEvent.getName,
                    description  = updatedEvent.description,
                    enabled      = updatedEvent.getEnabled))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyProcessingTypeRemovedEvent(event: StudyEvent): Unit = {
    if (event.eventType.isProcessingTypeRemoved) {

      processingTypeRepository.getByKey(
        ProcessingTypeId(event.getProcessingTypeRemoved.getProcessingTypeId))
      .fold(
        err => log.error(s"updating processing type from event failed: $err"),
        pt => {
          processingTypeRepository.remove(pt)
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  val ErrMsgNameExists = "processing type with name already exists"

  private def nameAvailable(name: String, studyId: StudyId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.studyId == studyId)
    }
  }

  private def nameAvailable(name: String,
                            studyId: StudyId,
                            excludeId: ProcessingTypeId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.studyId == studyId) && (item.id != excludeId)
    }
  }

  def checkNotInUse(processingType: ProcessingType): DomainValidation[ProcessingType] = {
    // FIXME: this is a stub for now
    //
    // it needs to be replaced with the real check
    processingType.success
  }
}
