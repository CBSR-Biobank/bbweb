package service.study

import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import domain._
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.study.{ DisabledStudy, SpecimenGroup, SpecimenGroupId, Study, StudyId }
import domain.study.Study._
import service.CommandHandler

import org.eligosource.eventsourced.core._
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
class SpecimenGroupDomainService(
  studyRepository: ReadRepository[StudyId, Study],
  specimenGroupRepository: ReadWriteRepository[SpecimenGroupId, SpecimenGroup])
  extends CommandHandler {
  import SpecimenGroupDomainService._

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

    case msg: StudyProcessorMsg =>
      msg.cmd match {
        case cmd: AddSpecimenGroupCmdWithId =>
          addSpecimenGroup(cmd, msg.study, msg.listeners)
        case cmd: UpdateSpecimenGroupCmd =>
          updateSpecimenGroup(cmd, msg.study, msg.listeners)
        case cmd: RemoveSpecimenGroupCmd =>
          removeSpecimenGroup(cmd, msg.study, msg.listeners)
        case _ =>
          throw new Error("invalid command received")
      }

    case _ =>
      throw new Error("invalid message received")

  }

  private def addSpecimenGroup(
    cmd: AddSpecimenGroupCmdWithId,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroup] = {

    def addItem(item: SpecimenGroup) {
      specimenGroupRepository.updateMap(item);
      listeners sendEvent StudySpecimenGroupAddedEvent(item.studyId, item.id,
        item.name, item.description, item.units, item.anatomicalSourceType,
        item.preservationType, item.preservationTemperatureType, item.specimenType)
    }

    val item = for {
      newItem <- study.addSpecimenGroup(specimenGroupRepository, cmd)
      addItem <- addItem(newItem).success
    } yield newItem
    CommandHandler.logMethod(log, "addSpecimenGroup", cmd, item)
    item
  }

  private def updateSpecimenGroup(
    cmd: UpdateSpecimenGroupCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroup] = {
    def update(item: SpecimenGroup) = {
      specimenGroupRepository.updateMap(item)
      listeners sendEvent StudySpecimenGroupUpdatedEvent(study.id,
        item.id, item.name, item.description, item.units, item.anatomicalSourceType,
        item.preservationType, item.preservationTemperatureType, item.specimenType)
    }

    val item = for {
      newItem <- study.updateSpecimenGroup(specimenGroupRepository, cmd)
      item <- update(newItem).success
    } yield newItem
    CommandHandler.logMethod(log, "updateSpecimenGroup", cmd, item)
    item
  }

  private def removeSpecimenGroup(
    cmd: RemoveSpecimenGroupCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroup] = {

    def removeItem(item: SpecimenGroup) = {
      specimenGroupRepository.remove(item)
      listeners sendEvent StudySpecimenGroupRemovedEvent(item.studyId, item.id)
    }

    val item = for {
      oldItem <- study.removeSpecimenGroup(specimenGroupRepository, cmd)
      removedItem <- removeItem(oldItem).success
    } yield oldItem
    CommandHandler.logMethod(log, "removeSpecimenGroup", cmd, item)
    item
  }

}

object SpecimenGroupDomainService {
  val log = LoggerFactory.getLogger(SpecimenGroupDomainService.getClass)
}
