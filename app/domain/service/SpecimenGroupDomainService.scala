package domain.service

import org.eligosource.eventsourced.core._

import domain._
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import domain.study.{
  DisabledStudy,
  EnabledStudy,
  SpecimenGroup,
  SpecimenGroupId,
  Study,
  StudyId
}
import domain.service.StudyValidationUtil._

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
class SpecimenGroupDomainService(
  studyRepository: ReadRepository[StudyId, Study],
  specimenGroupRepository: ReadWriteRepository[SpecimenGroupId, SpecimenGroup])
  extends CommandHandler {

  /**
   * This partial function handles each command. The input is a Tuple3 consisting of:
   *
   *  1. The command to handle.
   *  2. The study entity the command is associated with,
   *  3. The event message listener to be notified if the command is successful.
   *
   *  If the command is invalid, then this method throws an Error exception.
   */
  def process = {
    case _@ (cmd: AddSpecimenGroupCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addSpecimenGroup(cmd, study, listeners)
    case _@ (cmd: UpdateSpecimenGroupCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      updateSpecimenGroup(cmd, study, listeners)
    case _@ (cmd: RemoveSpecimenGroupCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeSpecimenGroup(cmd, study, listeners)
    case _ =>
      throw new Error("invalid command received")

  }

  private def addSpecimenGroup(
    cmd: AddSpecimenGroupCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroup] = {

    def addItem(item: SpecimenGroup) {
      specimenGroupRepository.updateMap(item);
      listeners sendEvent StudySpecimenGroupAddedEvent(item.studyId, item.id,
        item.name, item.description, item.units, item.anatomicalSourceType,
        item.preservationType, item.preservationTemperatureType, item.specimenType)
    }

    for {
      specimenGroups <- specimenGroupRepository.getMap.filter(
        cet => cet._2.studyId.equals(study.id)).success
      newItem <- study.addSpecimenGroup(specimenGroups, cmd)
      addItem <- addItem(newItem).success
    } yield newItem
  }

  private def updateSpecimenGroup(
    cmd: UpdateSpecimenGroupCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroup] = {
    def update(prevItem: SpecimenGroup): SpecimenGroup = {
      val item = SpecimenGroup(prevItem.id, study.id, prevItem.version + 1,
        cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
        cmd.preservationTemperatureType, cmd.specimenType)
      specimenGroupRepository.updateMap(item)
      listeners sendEvent StudySpecimenGroupUpdatedEvent(item.studyId,
        item.id, item.name, item.description, item.units, item.anatomicalSourceType,
        item.preservationType, item.preservationTemperatureType, item.specimenType)
      item
    }

    for {
      prevItem <- validateSpecimenGroupId(study, specimenGroupRepository, cmd.specimenGroupId)
      versionCheck <- prevItem.requireVersion(cmd.expectedVersion)
      item <- update(prevItem).success
    } yield item
  }

  private def removeSpecimenGroup(
    cmd: RemoveSpecimenGroupCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroup] = {

    def removeItem(item: SpecimenGroup): SpecimenGroup = {
      specimenGroupRepository.remove(item)
      listeners sendEvent StudySpecimenGroupRemovedEvent(item.studyId, item.id)
      item
    }

    for {
      prevItem <- validateSpecimenGroupId(study, specimenGroupRepository, cmd.specimenGroupId)
      versionCheck <- prevItem.requireVersion(cmd.expectedVersion)
      item <- removeItem(prevItem).success
    } yield item
  }

}
