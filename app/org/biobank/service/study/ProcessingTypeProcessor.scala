package org.biobank.service.study

import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study.{
  Study,
  StudyId,
  ProcessingType,
  ProcessingTypeId,
  ProcessingTypeRepository,
  SpecimenGroupId,
  SpecimenGroupRepository
}
import org.slf4j.LoggerFactory
import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import scalaz._
import scalaz.Scalaz._

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
class ProcessingTypeProcessor(implicit inj: Injector) extends Processor with AkkaInjectable {

  override def persistenceId = "processing-type-processor-id"

  case class SnapshotState(processingTypes: Set[ProcessingType])

  val processingTypeRepository = inject [ProcessingTypeRepository]

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: ProcessingTypeAddedEvent =>   recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: ProcessingTypeUpdatedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: ProcessingTypeRemovedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)

        case event => throw new IllegalStateException(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.processingTypes.foreach{ ceType =>
        processingTypeRepository.put(ceType) }
  }


  /**
    * These are the commands that are requested. A command can fail, and will send the failure as a response
    * back to the user. Each valid command generates one or more events and is journaled.
    */
  val receiveCommand: Receive = {
    case procCmd: WrappedCommand =>
      implicit val userId = procCmd.userId
      procCmd.command match {
        case cmd: AddProcessingTypeCmd =>    process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        case cmd: UpdateProcessingTypeCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        case cmd: RemoveProcessingTypeCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
      }

    case "snap" =>
      saveSnapshot(SnapshotState(processingTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"ProcessingTypeProcessor: message not handled: $cmd")
  }

  def update
    (cmd: ProcessingTypeCommand)
    (fn: ProcessingType => DomainValidation[ProcessingType])
      : DomainValidation[ProcessingType] = {
    for {
      pt <- processingTypeRepository.withId(StudyId(cmd.studyId), ProcessingTypeId(cmd.id))
      notInUse <- checkNotInUse(pt)
      validVersion <- pt.requireVersion(cmd.expectedVersion)
      updatedPt <- fn(pt)
    } yield updatedPt
  }

  private def validateCmd(cmd: AddProcessingTypeCmd): DomainValidation[ProcessingTypeAddedEvent] = {
    val timeNow = DateTime.now
    val studyId = StudyId(cmd.studyId)
    val id = processingTypeRepository.nextIdentity

    for {
      nameValid <- nameAvailable(cmd.name)
      newItem <- ProcessingType.create(studyId, id, -1L, timeNow, cmd.name, cmd.description, cmd.enabled)
      event <- ProcessingTypeAddedEvent(
        cmd.studyId, id.id, timeNow, newItem.name, newItem.description, newItem.enabled).success
    } yield event
  }

  private def validateCmd(
    cmd: UpdateProcessingTypeCmd): DomainValidation[ProcessingTypeUpdatedEvent] = {
    val timeNow = DateTime.now

    val v = update(cmd) { pt =>
      for {
        nameValid <- nameAvailable(cmd.name, ProcessingTypeId(cmd.id))
        updatedPt <- pt.update(cmd.name, cmd.description, cmd.enabled)
      } yield updatedPt
    }

    v.fold(
      err => DomainError(s"error $err occurred on $cmd").failNel,
      pt => ProcessingTypeUpdatedEvent(
        cmd.studyId, pt.id.id, pt.version, DateTime.now, pt.name, pt.description, pt.enabled).success
    )
  }

  private def validateCmd(
    cmd: RemoveProcessingTypeCmd): DomainValidation[ProcessingTypeRemovedEvent] = {
    val v = update(cmd) { pt => pt.success }

    v.fold(
      err => DomainError(s"error $err occurred on $cmd").failNel,
      pt =>  ProcessingTypeRemovedEvent(cmd.studyId, cmd.id).success
    )
  }

  private def recoverEvent(event: ProcessingTypeAddedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    processingTypeRepository.put(ProcessingType(
      StudyId(event.studyId), ProcessingTypeId(event.processingTypeId), 0L, event.dateTime, None,
      event.name, event.description, event.enabled))
    ()
  }

  private def recoverEvent(event: ProcessingTypeUpdatedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    processingTypeRepository.getByKey(ProcessingTypeId(event.processingTypeId)).fold(
      err => throw new IllegalStateException(s"updating processing type from event failed: $err"),
      pt => processingTypeRepository.put(pt.copy(
        version        = event.version,
        timeModified = Some(event.dateTime),
        name           = event.name,
        description    = event.description,
        enabled        = event.enabled))
    )
    ()
  }

  private def recoverEvent(event: ProcessingTypeRemovedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    processingTypeRepository.getByKey(ProcessingTypeId(event.processingTypeId)).fold(
      err => throw new IllegalStateException(s"updating processing type from event failed: $err"),
      sg => processingTypeRepository.remove(sg)
    )
    ()
  }

  val ErrMsgNameExists = "processing type with name already exists"

  private def nameAvailable(name: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists){ item =>
      item.name.equals(name)
    }
  }

  private def nameAvailable(name: String, excludeId: ProcessingTypeId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, processingTypeRepository, ErrMsgNameExists){ item =>
      item.name.equals(name) && (item.id != excludeId)
    }
  }

  def checkNotInUse(processingType: ProcessingType): DomainValidation[ProcessingType] = {
    // FIXME: this is a stub for now
    //
    // it needs to be replaced with the real check
    processingType.success
  }
}
