package service.study

import service._
import service.commands._
import service.events._
import domain._
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.study.{
  DisabledStudy,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupRepository,
  Study,
  StudyId
}
import domain.study.Study._

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
class SpecimenGroupService() extends CommandHandler {

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
          addSpecimenGroup(cmd, msg.study, msg.listeners, msg.id)
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
    cmd: AddSpecimenGroupCmd,
    study: DisabledStudy,
    listeners: MessageEmitter,
    id: Option[String]): DomainValidation[SpecimenGroup] = {

    def generateEvent(item: SpecimenGroup) {
      listeners sendEvent StudySpecimenGroupAddedEvent(item.studyId, item.id,
        item.name, item.description, item.units, item.anatomicalSourceType,
        item.preservationType, item.preservationTemperatureType, item.specimenType)
    }

    val item = for {
      sgId <- id.toSuccess(DomainError("specimen group ID is missing"))
      newItem <- SpecimenGroupRepository.add(
        SpecimenGroup(new SpecimenGroupId(sgId), version = -1, study.id, cmd.name, cmd.description,
          cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
          cmd.preservationTemperatureType, cmd.specimenType))
      event <- generateEvent(newItem).success
    } yield newItem
    logMethod(log, "addSpecimenGroup", cmd, item)
    item
  }

  private def updateSpecimenGroup(
    cmd: UpdateSpecimenGroupCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroup] = {

    def generateEvent(item: SpecimenGroup) = {
      listeners.sendEvent(StudySpecimenGroupUpdatedEvent(study.id,
        item.id, item.name, item.description, item.units, item.anatomicalSourceType,
        item.preservationType, item.preservationTemperatureType, item.specimenType))
    }

    val item = for {
      validStudy <- StudyValidation.validateSpecimenGroupId(study, cmd.id)
      newItem <- SpecimenGroupRepository.add(
        SpecimenGroup(new SpecimenGroupId(cmd.id), version = -1, study.id, cmd.name, cmd.description,
          cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
          cmd.preservationTemperatureType, cmd.specimenType))
      event <- generateEvent(newItem).success
    } yield newItem
    logMethod(log, "updateSpecimenGroup", cmd, item)
    item
  }

  private def removeSpecimenGroup(
    cmd: RemoveSpecimenGroupCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroup] = {

    val item = for {
      specimenGroup <- SpecimenGroupRepository.specimenGroupWithId(
        StudyId(cmd.studyId), SpecimenGroupId(cmd.id))
      oldItem <- SpecimenGroupRepository.remove(specimenGroup)
      event <- listeners.sendEvent(StudySpecimenGroupRemovedEvent(item.studyId, item.id))
    } yield oldItem

    logMethod(log, "removeSpecimenGroup", cmd, item)
    item
  }
}
