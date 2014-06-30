package org.biobank.service.study

import org.biobank.service.Messages._
import org.biobank.domain._
import org.biobank.domain.study.{
  Study,
  StudyId,
  ProcessingType,
  ProcessingTypeId,
  ProcessingTypeRepositoryComponent,
  SpecimenGroupId,
  SpecimenGroupRepositoryComponent
}
import org.slf4j.LoggerFactory
import org.biobank.service._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

import akka.persistence.SnapshotOffer
import scalaz._
import scalaz.Scalaz._

trait ProcessingTypeProcessorComponent {
  self: ProcessingTypeRepositoryComponent =>

  /**
    * This is the Collection Event Type processor. It is a child actor of
    * [[org.biobank.service.study.StudyProcessorComponent.StudyProcessor]].
    *
    * It handles commands that deal with a Collection Event Type.
    */
  class ProcessingTypeProcessor extends Processor {

    override def persistenceId = "processing-type-processor-id"

    case class SnapshotState(processingTypes: Set[ProcessingType])

    val receiveRecover: Receive = {
      case event: ProcessingTypeAddedEvent => recoverEvent(event)

      case event: ProcessingTypeUpdatedEvent => recoverEvent(event)

      case event: ProcessingTypeRemovedEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.processingTypes.foreach{ ceType =>
          processingTypeRepository.put(ceType) }
    }


    val receiveCommand: Receive = {

      case cmd: AddProcessingTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: UpdateProcessingTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: RemoveProcessingTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case _ =>
        throw new Error("invalid message received")
    }

    private def validateCmd(
      cmd: AddProcessingTypeCmd): DomainValidation[ProcessingTypeAddedEvent] = {

      val studyId = StudyId(cmd.studyId)
      val id = processingTypeRepository.nextIdentity

      for {
        nameValid <- nameAvailable(cmd.name)
        newItem <- ProcessingType.create(studyId, id, -1L, org.joda.time.DateTime.now, cmd.name, cmd.description, cmd.enabled)
        event <- ProcessingTypeAddedEvent(
          cmd.studyId, id.id, newItem.addedDate, newItem.name, newItem.description,
          newItem.enabled).success
      } yield event
    }

    private def validateCmd(
      cmd: UpdateProcessingTypeCmd): DomainValidation[ProcessingTypeUpdatedEvent] = {
      val studyId = StudyId(cmd.studyId)
      val id = ProcessingTypeId(cmd.id)

      for {
        oldItem <- processingTypeRepository.withId(studyId,id)
        nameValid <- nameAvailable(cmd.name, id)
        newItem <- oldItem.update(
          cmd.expectedVersion, org.joda.time.DateTime.now, cmd.name, cmd.description, cmd.enabled)
        event <- ProcessingTypeUpdatedEvent(
          cmd.studyId, newItem.id.id, newItem.version, newItem.lastUpdateDate.get, newItem.name,
          newItem.description, newItem.enabled).success
      } yield event
    }

    private def validateCmd(
      cmd: RemoveProcessingTypeCmd): DomainValidation[ProcessingTypeRemovedEvent] = {
      val studyId = StudyId(cmd.studyId)
      val id = ProcessingTypeId(cmd.id)

      for {
        item <- processingTypeRepository.withId(studyId, id)
        validVersion <- validateVersion(item, cmd.expectedVersion)
        event <- ProcessingTypeRemovedEvent(cmd.studyId, cmd.id).success
      } yield event
    }

    private def recoverEvent(event: ProcessingTypeAddedEvent): Unit = {
      val studyId = StudyId(event.studyId)
      val validation = for {
        newItem <- ProcessingType.create(
          studyId, ProcessingTypeId(event.processingTypeId), -1L, event.dateTime,
          event.name, event.description, event.enabled)
        savedItem <- processingTypeRepository.put(newItem).success
      } yield newItem

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering collection event type from event failed")
      }
    }

    private def recoverEvent(event: ProcessingTypeUpdatedEvent): Unit = {
      val validation = for {
        item <- processingTypeRepository.getByKey(ProcessingTypeId(event.processingTypeId))
        updatedItem <- item.update(
          item.versionOption, event.dateTime, event.name, event.description, event.enabled)
        savedItem <- processingTypeRepository.put(updatedItem).success
      } yield updatedItem

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        val err = validation.swap.getOrElse(List.empty)
        throw new IllegalStateException(
          s"recovering collection event type update from event failed: $err")
      }
    }

    private def recoverEvent(event: ProcessingTypeRemovedEvent): Unit = {
      val validation = for {
        item <- processingTypeRepository.getByKey(ProcessingTypeId(event.processingTypeId))
        removedItem <- processingTypeRepository.remove(item).success
      } yield removedItem

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        val err = validation.swap.getOrElse(List.empty)
        throw new IllegalStateException(
          s"recovering collection event type remove from event failed: $err")
      }
    }

    private def nameAvailable(name: String): DomainValidation[Boolean] = {
      nameAvailableMatcher(name, processingTypeRepository)(item => item.name.equals(name))
    }

    private def nameAvailable(name: String, excludeId: ProcessingTypeId): DomainValidation[Boolean] = {
      nameAvailableMatcher(name, processingTypeRepository){ item =>
        item.name.equals(name) && (item.id != excludeId)
      }
    }

  }

}
