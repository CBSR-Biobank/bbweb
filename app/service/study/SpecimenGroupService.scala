package service.study

import service._
import service.Messages._
import infrastructure.command.StudyCommands._
import infrastructure.event.StudyEvents._
import domain._
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.study._
import domain.study.Study._
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

/**
 * This is the Specimen Group Domain Service.
 *
 * It handles commands that deal with a Specimen Group.
 *
 * @param studyRepository The repository for study entities. This repository is only used for
 *        reading.
 * @param specimenGroupRepository The repository for specimen group entities.
 */

trait SpecimenGroupServiceComponent {
  self: RepositoryComponent =>

  val specimenGroupService: SpecimenGroupService = new SpecimenGroupService

  class SpecimenGroupService extends CommandHandler {

    val log = LoggerFactory.getLogger(this.getClass)

    /**
     * This partial function handles each command. The command is contained within the
     * StudyProcessorMsg.
     *
     *  If the command is invalid, then this method throws an Error exception.
     */
    def process = {

      case msg: StudyProcessorMsg =>
        msg.cmd match {
          case cmd: AddSpecimenGroupCmd =>
            addSpecimenGroup(cmd, msg.study, msg.id)
          case cmd: UpdateSpecimenGroupCmd =>
            updateSpecimenGroup(cmd, msg.study)
          case cmd: RemoveSpecimenGroupCmd =>
            removeSpecimenGroup(cmd, msg.study)
          case _ =>
            throw new Error("invalid command received")
        }

      case _ =>
        throw new Error("invalid message received")

    }

    private def addSpecimenGroup(
      cmd: AddSpecimenGroupCmd,
      study: DisabledStudy,
      id: Option[String]): DomainValidation[SpecimenGroupAddedEvent] = {

      val sgId = specimenGroupRepository.nextIdentity

      for {
        newItem <- specimenGroupRepository.add(
          SpecimenGroup(sgId, 0, study.id, cmd.name, cmd.description,
            cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
            cmd.preservationTemperatureType, cmd.specimenType))
        newEvent <- SpecimenGroupAddedEvent(
          newItem.studyId.id, newItem.id.id, newItem.version, newItem.name, newItem.description,
          newItem.units, newItem.anatomicalSourceType, newItem.preservationType,
          newItem.preservationTemperatureType, newItem.specimenType).success
      } yield newEvent
    }

    private def checkNotInUse(specimenGroup: SpecimenGroup): DomainValidation[Boolean] = {
      if (collectionEventTypeRepository.specimenGroupInUse(specimenGroup)) {
        DomainError("specimen group is in use by collection event type: " + specimenGroup.id).failNel
      } else {
        true.success
      }
    }

    private def updateSpecimenGroup(
      cmd: UpdateSpecimenGroupCmd,
      study: DisabledStudy): DomainValidation[SpecimenGroupUpdatedEvent] = {

      for {
        oldItem <- specimenGroupRepository.specimenGroupWithId(
          StudyId(cmd.studyId), SpecimenGroupId(cmd.id))
        notInUse <- checkNotInUse(oldItem)
        newItem <- specimenGroupRepository.update(
          SpecimenGroup(oldItem.id, cmd.expectedVersion.getOrElse(-1), study.id, cmd.name, cmd.description,
            cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
            cmd.preservationTemperatureType, cmd.specimenType))
        newEvent <- SpecimenGroupUpdatedEvent(
          study.id.id, newItem.id.id, newItem.version, newItem.name, newItem.description, newItem.units,
          newItem.anatomicalSourceType, newItem.preservationType, newItem.preservationTemperatureType,
          newItem.specimenType).success
      } yield newEvent
    }

    private def removeSpecimenGroup(
      cmd: RemoveSpecimenGroupCmd,
      study: DisabledStudy): DomainValidation[SpecimenGroupRemovedEvent] = {

      for {
        oldItem <- specimenGroupRepository.specimenGroupWithId(
          StudyId(cmd.studyId), SpecimenGroupId(cmd.id))
        notInUse <- checkNotInUse(oldItem)
        itemToRemove <- SpecimenGroup(oldItem.id, cmd.expectedVersion.getOrElse(-1),
          study.id, oldItem.name, oldItem.description, oldItem.units, oldItem.anatomicalSourceType,
          oldItem.preservationType, oldItem.preservationTemperatureType, oldItem.specimenType).success
        removedItem <- specimenGroupRepository.remove(itemToRemove)
        newEvent <- SpecimenGroupRemovedEvent(
          removedItem.studyId.id, removedItem.id.id).success
      } yield newEvent
    }
  }
}
