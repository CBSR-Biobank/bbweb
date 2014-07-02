package org.biobank.service.study

import org.biobank.service.Processor
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  DomainValidation,
  DomainError
}
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import org.biobank.domain.study.{
  CollectionEventTypeRepositoryComponent,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupRepositoryComponent,
  SpecimenLinkTypeRepositoryComponent,
  Study,
  StudyId }

import akka.persistence.SnapshotOffer
import scalaz._
import scalaz.Scalaz._

trait SpecimenGroupProcessorComponent {
  self: SpecimenGroupRepositoryComponent
      with CollectionEventTypeRepositoryComponent
      with SpecimenLinkTypeRepositoryComponent =>

  /**
    * This is the Specimen Group processor. It is a child actor of
    *  [[org.biobank.service.study.StudyProcessorComponent.StudyProcessor]].
    *
    * It handles commands that deal with a Specimen Group.
    *
    */
  class SpecimenGroupProcessor extends Processor {

    case class SnapshotState(specimenGroups: Set[SpecimenGroup])

    val receiveRecover: Receive = {
      case event: SpecimenGroupAddedEvent => recoverEvent(event)

      case event: SpecimenGroupUpdatedEvent => recoverEvent(event)

      case event: SpecimenGroupRemovedEvent => recoverEvent(event)

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
        newItem <-SpecimenGroup.create(
          StudyId(cmd.studyId), sgId, -1, org.joda.time.DateTime.now, cmd.name, cmd.description,
          cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
          cmd.preservationTemperatureType, cmd.specimenType)
        newEvent <- SpecimenGroupAddedEvent(
          newItem.studyId.id, newItem.id.id, newItem.addedDate, newItem.name, newItem.description,
          newItem.units, newItem.anatomicalSourceType, newItem.preservationType,
          newItem.preservationTemperatureType, newItem.specimenType).success
      } yield newEvent
    }

    private def validateCmd(
      cmd: UpdateSpecimenGroupCmd): DomainValidation[SpecimenGroupUpdatedEvent] = {
      val studyId = StudyId(cmd.studyId)
      val specimenGroupId = SpecimenGroupId(cmd.id)

      for {
        nameValid <- nameAvailable(cmd.name, specimenGroupId)
        oldItem <- specimenGroupRepository.withId(studyId, specimenGroupId)
        notInUse <- checkNotInUse(studyId, oldItem.id)
        newItem <- oldItem.update(
         Some(cmd.expectedVersion), org.joda.time.DateTime.now, cmd.name, cmd.description, cmd.units,
          cmd.anatomicalSourceType, cmd.preservationType, cmd.preservationTemperatureType,
          cmd.specimenType)
        newEvent <- SpecimenGroupUpdatedEvent(
          cmd.studyId, newItem.id.id, newItem.version, newItem.lastUpdateDate.get, cmd.name, cmd.description,
          cmd.units, cmd.anatomicalSourceType, cmd.preservationType, cmd.preservationTemperatureType,
          cmd.specimenType).success
      } yield newEvent
    }

    private def validateCmd(
      cmd: RemoveSpecimenGroupCmd): DomainValidation[SpecimenGroupRemovedEvent] = {
      val studyId = StudyId(cmd.studyId)

      for {
        item <- specimenGroupRepository.withId(studyId, SpecimenGroupId(cmd.id))
        notInUse <- checkNotInUse(studyId, item.id)
        validVersion <- validateVersion(item,Some(cmd.expectedVersion))
        newEvent <- SpecimenGroupRemovedEvent(item.studyId.id, item.id.id).success
      } yield newEvent
    }

    private def recoverEvent(event: SpecimenGroupAddedEvent): Unit = {
      val studyId = StudyId(event.studyId)
      val validation = for {
        newItem <- SpecimenGroup.create(
          studyId, SpecimenGroupId(event.specimenGroupId), -1, event.dateTime, event.name,
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
        updatedItem <- item.update(
          item.versionOption, event.dateTime, event.name, event.description, event.units,
          event.anatomicalSourceType, event.preservationType, event.preservationTemperatureType,
          event.specimenType)
        savedItem <- specimenGroupRepository.put(updatedItem).success
      } yield updatedItem

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        val err = validation.swap.getOrElse(List.empty)
        throw new IllegalStateException(
          s"recovering specimen group update from event failed: $err")
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
        val err = validation.swap.getOrElse(List.empty)
        throw new IllegalStateException(
          s"recovering specimen group remove from event failed: $err")
      }
    }

    private def nameAvailable(specimenGroupName: String): DomainValidation[Boolean] = {
      nameAvailableMatcher(specimenGroupName, specimenGroupRepository) { item =>
        item.name.equals(specimenGroupName)
      }
    }

    private def nameAvailable(
      specimenGroupName: String,
      id: SpecimenGroupId): DomainValidation[Boolean] = {
      nameAvailableMatcher(specimenGroupName, specimenGroupRepository) { item =>
        item.name.equals(specimenGroupName) && (item.id != id)
      }
    }

    private def checkNotInUse(
      studyId: StudyId,
      specimenGroupId: SpecimenGroupId): DomainValidation[Boolean] = {

      def checkNotInUseByCollectionEventType: DomainValidation[Boolean] = {
        if (collectionEventTypeRepository.specimenGroupInUse(studyId, specimenGroupId)) {
          DomainError(s"specimen group is in use by collection event type: $specimenGroupId").failNel
        } else {
          true.success
        }
      }

      def checkNotInUseBySpecimenLinkType: DomainValidation[Boolean] = {
        if (specimenLinkTypeRepository.specimenGroupInUse(specimenGroupId)) {
          DomainError(s"specimen group is in use by specimen link type: $specimenGroupId").failNel
        } else {
          true.success
        }
      }

      (checkNotInUseByCollectionEventType |@| checkNotInUseBySpecimenLinkType) { case (_, _) => true }
    }

  }

}
