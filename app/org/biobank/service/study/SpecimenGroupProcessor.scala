package org.biobank.service.study

import org.biobank.service._
import org.biobank.service.Messages._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import org.biobank.domain.study._
import org.biobank.domain.study.Study
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
  specimenGroupRepository: SpecimenGroupRepositoryComponent#SpecimenGroupRepository)
    extends Processor {

  case class SnapshotState(specimenGroups: Set[SpecimenGroup])

  val receiveRecover: Receive = {
    case event: SpecimenGroupAddedEvent => recoverEvent(event)

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.specimenGroups.foreach{ specimenGroup => specimenGroupRepository.put(specimenGroup) }
  }

  val receiveCommand: Receive = {
    case cmd: AddSpecimenGroupCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      //case cmd: UpdateSpecimenGroupCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      //case cmd: RemoveSpecimenGroupCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

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

  //   private def checkNotInUse(specimenGroup: SpecimenGroup): DomainValidation[Boolean] = {
  //     if (collectionEventTypeRepository.specimenGroupInUse(specimenGroup)) {
  //       DomainError("specimen group is in use by collection event type: " + specimenGroup.id).failNel
  //     } else {
  //       true.success
  //     }
  //   }

  //   private def updateSpecimenGroup(
  //     cmd: UpdateSpecimenGroupCmd,
  //     study: DisabledStudy): DomainValidation[SpecimenGroupUpdatedEvent] = {

  //     for {
  //       oldItem <- specimenGroupRepository.specimenGroupWithId(
  //         StudyId(cmd.studyId), SpecimenGroupId(cmd.id))
  //       notInUse <- checkNotInUse(oldItem)
  //       newItem <- specimenGroupRepository.update(
  //         SpecimenGroup(oldItem.id, cmd.expectedVersion.getOrElse(-1), study.id, cmd.name, cmd.description,
  //           cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
  //           cmd.preservationTemperatureType, cmd.specimenType))
  //       newEvent <- SpecimenGroupUpdatedEvent(
  //         study.id.id, newItem.id.id, newItem.version, newItem.name, newItem.description, newItem.units,
  //         newItem.anatomicalSourceType, newItem.preservationType, newItem.preservationTemperatureType,
  //         newItem.specimenType).success
  //     } yield newEvent
  //   }

  //   private def removeSpecimenGroup(
  //     cmd: RemoveSpecimenGroupCmd,
  //     study: DisabledStudy): DomainValidation[SpecimenGroupRemovedEvent] = {

  //     for {
  //       oldItem <- specimenGroupRepository.specimenGroupWithId(
  //         StudyId(cmd.studyId), SpecimenGroupId(cmd.id))
  //       notInUse <- checkNotInUse(oldItem)
  //       itemToRemove <- SpecimenGroup(oldItem.id, cmd.expectedVersion.getOrElse(-1),
  //         study.id, oldItem.name, oldItem.description, oldItem.units, oldItem.anatomicalSourceType,
  //         oldItem.preservationType, oldItem.preservationTemperatureType, oldItem.specimenType).success
  //       removedItem <- specimenGroupRepository.remove(itemToRemove)
  //       newEvent <- SpecimenGroupRemovedEvent(
  //         removedItem.studyId.id, removedItem.id.id).success
  //     } yield newEvent
  //   }
  // }

  def recoverEvent(event: SpecimenGroupAddedEvent): Unit = {
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
}

