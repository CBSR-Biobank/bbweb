package org.biobank.service.study

import org.biobank.service.Processor
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{ DomainValidation, DomainError }
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import org.biobank.domain.study.{
  CollectionEventTypeRepositoryComponent,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupRepositoryComponent,
  Study,
  StudyId }
import org.slf4j.LoggerFactory

import akka.persistence.SnapshotOffer
import scalaz._
import scalaz.Scalaz._

/**
  * This is the Specimen Group processor. It is a child actor of [[StudyProcessor]].
  *
  * It handles commands that deal with a Specimen Group.
  *
  */
class SpecimenGroupProcessor(
  specimenGroupRepository: SpecimenGroupRepositoryComponent#SpecimenGroupRepository,
  collectionEventTypeRepository: CollectionEventTypeRepositoryComponent#CollectionEventTypeRepository)
    extends Processor {

  case class SnapshotState(specimenGroups: Set[SpecimenGroup])

  val receiveRecover: Receive = {
    case event: SpecimenGroupAddedEvent => recoverEvent(event)

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.specimenGroups.foreach{ specimenGroup => specimenGroupRepository.put(specimenGroup) }
  }

  val receiveCommand: Receive = {
    case cmd: AddSpecimenGroupCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

    case cmd: UpdateSpecimenGroupCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

    case cmd: RemoveSpecimenGroupCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

    case _ => throw new IllegalStateException("invalid command received")
  }

  private def validateCmd(cmd: AddSpecimenGroupCmd): DomainValidation[SpecimenGroupAddedEvent] = {
    val sgId = specimenGroupRepository.nextIdentity

    if (specimenGroupRepository.getByKey(sgId).isSuccess) {
      throw new IllegalStateException(s"specimen group with id already exsits: $id")
    }

    for {
      nameValid <- nameAvailable(cmd.name)
      newItem <-SpecimenGroup.create(StudyId(cmd.studyId), sgId, 0, cmd.name, cmd.description,
        cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
        cmd.preservationTemperatureType, cmd.specimenType)
      newEvent <- SpecimenGroupAddedEvent(
        newItem.studyId.id, newItem.id.id, newItem.version, newItem.name, newItem.description,
        newItem.units, newItem.anatomicalSourceType, newItem.preservationType,
        newItem.preservationTemperatureType, newItem.specimenType).success
    } yield newEvent
  }

  private def validateCmd(
    cmd: UpdateSpecimenGroupCmd): DomainValidation[SpecimenGroupUpdatedEvent] = {
    val studyId = StudyId(cmd.studyId)

    for {
      nameValid <- nameAvailable(cmd.name)
      oldItem <- specimenGroupRepository.specimenGroupWithId(studyId, SpecimenGroupId(cmd.id))
      notInUse <- checkNotInUse(studyId, oldItem.id)
      newItem <- oldItem.update(cmd.expectedVersion, cmd.name, cmd.description, cmd.units,
	cmd.anatomicalSourceType, cmd.preservationType, cmd.preservationTemperatureType,
	cmd.specimenType)
      newEvent <- SpecimenGroupUpdatedEvent(
        cmd.studyId, newItem.id.id, newItem.version, newItem.name, newItem.description, newItem.units,
        newItem.anatomicalSourceType, newItem.preservationType, newItem.preservationTemperatureType,
        newItem.specimenType).success
    } yield newEvent
  }

  private def validateCmd(
    cmd: RemoveSpecimenGroupCmd): DomainValidation[SpecimenGroupRemovedEvent] = {
    val studyId = StudyId(cmd.studyId)

    for {
      item <- specimenGroupRepository.specimenGroupWithId(studyId, SpecimenGroupId(cmd.id))
      notInUse <- checkNotInUse(studyId, item.id)
      validVersion <- validateVersion(item, cmd.expectedVersion)
      removedItem <- specimenGroupRepository.remove(item).success
      newEvent <- SpecimenGroupRemovedEvent(removedItem.studyId.id, removedItem.id.id).success
    } yield newEvent
  }

  private def recoverEvent(event: SpecimenGroupAddedEvent): Unit = {
    val studyId = StudyId(event.studyId)
    val validation = for {
      newItem <-SpecimenGroup.create(studyId, SpecimenGroupId(event.specimenGroupId), -1, event.name,
	event.description, event.units, event.anatomicalSourceType, event.preservationType,
        event.preservationTemperatureType, event.specimenType)
      savedItem <- specimenGroupRepository.put(newItem).success
    } yield newItem

    if (validation.isFailure) {
      // this should never happen because the only way to get here is when the
      // command passed validation
      throw new IllegalStateException("recovering specimen group from event failed")
    }
  }

  private def recoverEvent(event: SpecimenGroupUpdatedEvent): Unit = {
    val validation = for {
      item <- specimenGroupRepository.getByKey(SpecimenGroupId(event.specimenGroupId))
      updatedItem <- item.update(Some(event.version), event.name,
	event.description, event.units, event.anatomicalSourceType, event.preservationType,
        event.preservationTemperatureType, event.specimenType)
    } yield updatedItem

    if (validation.isFailure) {
      // this should never happen because the only way to get here is when the
      // command passed validation
      throw new IllegalStateException("recovering specimen group update from event failed")
    }
  }

  private def recoverEvent(event: SpecimenGroupRemovedEvent): Unit = {
    val validation = for {
      item <- specimenGroupRepository.getByKey(SpecimenGroupId(event.specimenGroupId))
      removedItem <- specimenGroupRepository.remove(item).success
    } yield removedItem

    if (validation.isFailure) {
      // this should never happen because the only way to get here is when the
      // command passed validation
      throw new IllegalStateException("recovering specimen group remove from event failed")
    }
  }

  private def nameAvailable(specimenGroupName: String): DomainValidation[Boolean] = {
    val exists = specimenGroupRepository.getValues.exists { item =>
      item.name.equals(specimenGroupName)
    }

    if (exists) {
      DomainError(s"specimen group with name already exists: $specimenGroupName").failNel
    } else {
      true.success
    }
  }

  private def checkNotInUse(
    studyId: StudyId,
    specimenGroupId: SpecimenGroupId): DomainValidation[Boolean] = {
    if (collectionEventTypeRepository.specimenGroupInUse(studyId, specimenGroupId)) {
      DomainError(s"specimen group is in use by collection event type: $specimenGroupId").failNel
    } else {
      true.success
    }
  }

  private def validateVersion(
    specimenGroup: SpecimenGroup,
    expectedVersion: Option[Long]): DomainValidation[Boolean] = {
    if (specimenGroup.versionOption == expectedVersion) true.success
    else DomainError(s"version mismatch").failNel
  }

}

