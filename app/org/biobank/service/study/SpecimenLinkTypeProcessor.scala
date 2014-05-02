package org.biobank.service.study

import org.biobank.service.Messages._
import org.biobank.domain._
import org.biobank.domain.study.{
  ProcessingTypeId,
  ProcessingTypeRepositoryComponent,
  SpecimenLinkType,
  SpecimenLinkTypeId,
  SpecimenLinkTypeRepositoryComponent,
  SpecimenLinkAnnotationTypeRepositoryComponent,
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

trait SpecimenLinkTypeProcessorComponent {
  self: SpecimenLinkTypeRepositoryComponent
      with SpecimenLinkAnnotationTypeRepositoryComponent
      with ProcessingTypeRepositoryComponent
      with SpecimenGroupRepositoryComponent =>

  /**
    * This is the Collection Event Type processor. It is a child actor of
    * [[org.biobank.service.study.StudyProcessorComponent.StudyProcessor]].
    *
    * It handles commands that deal with a Collection Event Type.
    */
  class SpecimenLinkTypeProcessor extends Processor {

    case class SnapshotState(specimenLinkTypes: Set[SpecimenLinkType])

    val receiveRecover: Receive = {
      case event: SpecimenLinkTypeAddedEvent => recoverEvent(event)

      case event: SpecimenLinkTypeUpdatedEvent => recoverEvent(event)

      case event: SpecimenLinkTypeRemovedEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
	snapshot.specimenLinkTypes.foreach{ ceType =>
	  specimenLinkTypeRepository.put(ceType) }
    }


    val receiveCommand: Receive = {

      case cmd: AddSpecimenLinkTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: UpdateSpecimenLinkTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: RemoveSpecimenLinkTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case _ =>
	throw new Error("invalid message received")
    }

    private def validateCmd(
      cmd: AddSpecimenLinkTypeCmd): DomainValidation[SpecimenLinkTypeAddedEvent] = {

      val processingTypeId = ProcessingTypeId(cmd.processingTypeId)
      val id = specimenLinkTypeRepository.nextIdentity

      for {
	newItem <- SpecimenLinkType.create(
          processingTypeId,
	  id,
	  -1L,
	  cmd.expectedInputChange,
	  cmd.expectedOutputChange,
	  cmd.inputCount,
	  cmd.outputCount,
	  cmd.inputGroupId,
	  cmd.outputGroupId,
	  cmd.inputContainerTypeId,
	  cmd.outputContainerTypeId,
	  cmd.annotationTypeData)
	inputSgExists <- specimenGroupRepository.getByKey(newItem.inputGroupId)
	outputSgExists <- specimenGroupRepository.getByKey(newItem.outputGroupId)
	// FIXME: check that container types are valid
	validSpecimenGroups <- validateSpecimenGroups(newItem.inputGroupId, newItem.outputGroupId)
	validAnnotData <- validateAnnotationTypeData(processingTypeId, cmd.annotationTypeData)
	event <- SpecimenLinkTypeAddedEvent(
          cmd.processingTypeId,
	  id.id,
	  newItem.version,
	  newItem.expectedInputChange,
	  newItem.expectedOutputChange,
	  newItem.inputCount,
	  newItem.outputCount,
	  newItem.inputGroupId,
	  newItem.outputGroupId,
	  newItem.inputContainerTypeId,
	  newItem.outputContainerTypeId,
	  newItem.annotationTypeData).success
      } yield event
    }

    private def validateCmd(
      cmd: UpdateSpecimenLinkTypeCmd): DomainValidation[SpecimenLinkTypeUpdatedEvent] = {
      val processingTypeId = ProcessingTypeId(cmd.processingTypeId)
      val id = SpecimenLinkTypeId(cmd.id)

      for {
	oldItem <- specimenLinkTypeRepository.withId(processingTypeId,id)
	newItem <- oldItem.update(
	  cmd.expectedVersion,
	  cmd.expectedInputChange,
	  cmd.expectedOutputChange,
	  cmd.inputCount,
	  cmd.outputCount,
	  cmd.inputGroupId,
	  cmd.outputGroupId,
	  cmd.inputContainerTypeId,
	  cmd.outputContainerTypeId,
	  cmd.annotationTypeData)
	inputSgExists <- specimenGroupRepository.getByKey(newItem.inputGroupId)
	outputSgExists <- specimenGroupRepository.getByKey(newItem.outputGroupId)
	// FIXME: check that container types are valid
	validSpecimenGroups <- validateSpecimenGroups(newItem.inputGroupId, newItem.outputGroupId, newItem.id)
	validAtData <- validateAnnotationTypeData(processingTypeId, cmd.annotationTypeData)
	event <- SpecimenLinkTypeUpdatedEvent(
          cmd.processingTypeId,
	  newItem.id.id,
	  newItem.version,
	  newItem.expectedInputChange,
	  newItem.expectedOutputChange,
	  newItem.inputCount,
	  newItem.outputCount,
	  newItem.inputGroupId,
	  newItem.outputGroupId,
	  newItem.inputContainerTypeId,
	  newItem.outputContainerTypeId,
	  newItem.annotationTypeData).success
      } yield event
    }

    private def validateCmd(
      cmd: RemoveSpecimenLinkTypeCmd): DomainValidation[SpecimenLinkTypeRemovedEvent] = {
      val processingTypeId = ProcessingTypeId(cmd.processingTypeId)
      val id = SpecimenLinkTypeId(cmd.id)

      for {
	item <- specimenLinkTypeRepository.withId(processingTypeId, id)
	validVersion <- validateVersion(item, cmd.expectedVersion)
	event <- SpecimenLinkTypeRemovedEvent(cmd.processingTypeId, cmd.id).success
      } yield event
    }

    private def recoverEvent(event: SpecimenLinkTypeAddedEvent): Unit = {
      val processingTypeId = ProcessingTypeId(event.processingTypeId)
      val validation = for {
	newItem <- SpecimenLinkType.create(
	  processingTypeId,
	  SpecimenLinkTypeId(event.specimenLinkTypeId),
	  -1L,
	  event.expectedInputChange,
	  event.expectedOutputChange,
	  event.inputCount,
	  event.outputCount,
	  event.inputGroupId,
	  event.outputGroupId,
	  event.inputContainerTypeId,
	  event.outputContainerTypeId,
	  event.annotationTypeData)
	savedItem <- specimenLinkTypeRepository.put(newItem).success
      } yield newItem

      if (validation.isFailure) {
	// this should never happen because the only way to get here is when the
	// command passed validation
	throw new IllegalStateException("recovering collection event type from event failed")
      }
    }

    private def recoverEvent(event: SpecimenLinkTypeUpdatedEvent): Unit = {
      val validation = for {
	item <- specimenLinkTypeRepository.getByKey(SpecimenLinkTypeId(event.specimenLinkTypeId))
	updatedItem <- item.update(
	  item.versionOption,
	  event.expectedInputChange,
	  event.expectedOutputChange,
	  event.inputCount,
	  event.outputCount,
	  event.inputGroupId,
	  event.outputGroupId,
	  event.inputContainerTypeId,
	  event.outputContainerTypeId,
	  event.annotationTypeData)
	savedItem <- specimenLinkTypeRepository.put(updatedItem).success
      } yield updatedItem

      if (validation.isFailure) {
	// this should never happen because the only way to get here is when the
	// command passed validation
	val err = validation.swap.getOrElse(List.empty)
	throw new IllegalStateException(
	  s"recovering collection event type update from event failed: $err")
      }
    }

    private def recoverEvent(event: SpecimenLinkTypeRemovedEvent): Unit = {
      val validation = for {
	item <- specimenLinkTypeRepository.getByKey(SpecimenLinkTypeId(event.specimenLinkTypeId))
	removedItem <- specimenLinkTypeRepository.remove(item).success
      } yield removedItem

      if (validation.isFailure) {
	// this should never happen because the only way to get here is when the
	// command passed validation
	val err = validation.swap.getOrElse(List.empty)
	throw new IllegalStateException(
	  s"recovering collection event type remove from event failed: $err")
      }
    }

    // should only have one specimen link type with these two specimen groups
    private def validateSpecimenGroupMatcher(
      inputGroupId: SpecimenGroupId,
      outputGroupId: SpecimenGroupId)(
      matcher: SpecimenLinkType => Boolean): DomainValidation[Boolean] = {
      val exists = specimenLinkTypeRepository.getValues.exists { slt =>
	(slt.inputGroupId == inputGroupId) && (slt.outputGroupId == outputGroupId)
      }

      if (exists) {
	DomainError("specimen link type with specimen groups already exists").failNel
      } else {
	true.success
      }
    }

    private def validateSpecimenGroups(
      inputGroupId: SpecimenGroupId,
      outputGroupId: SpecimenGroupId): DomainValidation[Boolean] = {
      validateSpecimenGroupMatcher(inputGroupId, outputGroupId) { slt =>
	(slt.inputGroupId == inputGroupId) && (slt.outputGroupId == outputGroupId)
      }
    }

    private def validateSpecimenGroups(
      inputGroupId: SpecimenGroupId,
      outputGroupId: SpecimenGroupId,
      specimenLinkTypeId: SpecimenLinkTypeId): DomainValidation[Boolean] = {
      validateSpecimenGroupMatcher(inputGroupId, outputGroupId) { slt =>
	!slt.id.equals(specimenLinkTypeId) &&
	(slt.inputGroupId == inputGroupId) &&
	(slt.outputGroupId == outputGroupId)
      }
    }

    /**
      * Checks that each annotation type belongs to the same study as the collection event type. If
      * one or more annotation types are found that belong to a different study, they are returned in
      * the DomainError.
      */
    private def validateAnnotationTypeData(
      processingTypeId: ProcessingTypeId,
      annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData]): DomainValidation[Boolean] = {

      val validation = processingTypeRepository.getByKey(processingTypeId)

      if (validation.isFailure) {
	throw new IllegalStateException(s"processing type does not exist: $processingTypeId")
      }

      val processingType = validation | null

      val invalidSet = annotationTypeData.map(v => AnnotationTypeId(v.annotationTypeId)).map { id =>
	(id -> specimenLinkAnnotationTypeRepository.withId(processingType.studyId, id).isSuccess)
      }.filter(x => !x._2).map(_._1)

      if (invalidSet.isEmpty) true.success
      else DomainError("annotation type(s) do not belong to study: " + invalidSet.mkString(", ")).failNel
    }

  }

}
