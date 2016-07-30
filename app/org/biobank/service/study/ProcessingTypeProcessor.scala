package org.biobank.service.study

import akka.actor._
import akka.persistence.SnapshotOffer
import org.biobank.domain.study.{StudyId, ProcessingType, ProcessingTypeId, ProcessingTypeRepository }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service.{Processor, ServiceValidation}
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
  import StudyEventOld.EventType

  override def persistenceId = "processing-type-processor-id"

  case class SnapshotState(processingTypes: Set[ProcessingType])

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  val receiveRecover: Receive = {
    case event: StudyEventOld => event.eventType match {
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
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
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
                                         0L,
                                         cmd.name,
                                         cmd.description,
                                         cmd.enabled)
      event     <- createStudyEvent(newItem.studyId, cmd).withProcessingTypeAdded(
        ProcessingTypeAddedEvent(
          processingTypeId = Some(newItem.id.id),
          name             = Some(newItem.name),
          description      = newItem.description,
          enabled          = Some(newItem.enabled))).successNel[String]
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
            enabled          = Some(updatedPt.enabled))).successNel[String]
      } yield event
    }
    process(v) { applyProcessingTypeUpdatedEvent(_) }
  }

  private def processRemoveProcessingTypeCmd(cmd: RemoveProcessingTypeCmd): Unit = {
    val v = update(cmd) { pt =>
      createStudyEvent(pt.studyId, cmd).withProcessingTypeRemoved(
        ProcessingTypeRemovedEvent(Some(cmd.id))).successNel[String]
    }
    process(v){ applyProcessingTypeRemovedEvent(_) }
  }

  def update
    (cmd: ProcessingTypeModifyCommand)
    (fn: ProcessingType => ServiceValidation[StudyEventOld])
      : ServiceValidation[StudyEventOld] = {
    for {
      pt           <- processingTypeRepository.withId(StudyId(cmd.studyId), ProcessingTypeId(cmd.id))
      notInUse     <- checkNotInUse(pt)
      validVersion <- pt.requireVersion(cmd.expectedVersion)
      event        <- fn(pt)
    } yield event
  }

  private def applyProcessingTypeAddedEvent(event: StudyEventOld): Unit = {
    if (event.eventType.isProcessingTypeAdded) {
      val addedEvent = event.getProcessingTypeAdded

      val pt = ProcessingType.create(studyId      = StudyId(event.id),
                                     id           = ProcessingTypeId(addedEvent.getProcessingTypeId),
                                     version      = 0L,
                                     name         = addedEvent.getName,
                                     description  = addedEvent.description,
                                     enabled      = addedEvent.getEnabled)

      pt.foreach(processingTypeRepository.put)
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyProcessingTypeUpdatedEvent(event: StudyEventOld): Unit = {
    if (event.eventType.isProcessingTypeUpdated) {
      val updatedEvent = event.getProcessingTypeUpdated

      val v = processingTypeRepository.getByKey(ProcessingTypeId(updatedEvent.getProcessingTypeId))

      if (v.isFailure) {
        log.error(s"updating processing type from event failed: $v")
      }

      v.foreach(pt => processingTypeRepository.put(
                  pt.copy(version      = updatedEvent.getVersion,
                          timeModified  = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
                          name         = updatedEvent.getName,
                          description  = updatedEvent.description,
                          enabled      = updatedEvent.getEnabled)))

    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyProcessingTypeRemovedEvent(event: StudyEventOld): Unit = {
    if (event.eventType.isProcessingTypeRemoved) {

      val v = processingTypeRepository.getByKey(
          ProcessingTypeId(event.getProcessingTypeRemoved.getProcessingTypeId))

      if (v.isFailure) {
        log.error(s"removing processing type from event failed: $v")
      }

      v.foreach(processingTypeRepository.remove)
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  val ErrMsgNameExists = "processing type with name already exists"

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String, studyId: StudyId): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.studyId == studyId)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def nameAvailable(name: String,
                            studyId: StudyId,
                            excludeId: ProcessingTypeId): ServiceValidation[Boolean] = {
    nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.studyId == studyId) && (item.id != excludeId)
    }
  }

  def checkNotInUse(processingType: ProcessingType): ServiceValidation[ProcessingType] = {
    // FIXME: this is a stub for now
    //
    // it needs to be replaced with the real check
    processingType.successNel[String]
  }
}
