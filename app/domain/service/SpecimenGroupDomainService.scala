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
  extends DomainService {

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
    val studySpecimenGroups = specimenGroupRepository.getMap.filter(
      sg => sg._2.studyId.equals(study.id))
    val v = study.addSpecimenGroup(studySpecimenGroups, cmd)
    v match {
      case Success(sg) =>
        specimenGroupRepository.updateMap(sg)
        listeners sendEvent StudySpecimenGroupAddedEvent(sg.studyId, sg.id,
          sg.name, sg.description, sg.units, sg.anatomicalSourceType, sg.preservationType,
          sg.preservationTemperatureType, sg.specimenType)
      case _ => // nothing to do in this case
    }
    v
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
