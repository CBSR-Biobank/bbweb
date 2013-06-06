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

import scalaz._
import scalaz.Scalaz._

class SpecimenGroupDomainService(
  studyRepository: ReadRepository[StudyId, Study],
  specimenGroupRepository: ReadWriteRepository[SpecimenGroupId, SpecimenGroup]) {

  def process = PartialFunction[Any, DomainValidation[SpecimenGroup]] {
    case _@ (study: DisabledStudy, cmd: AddSpecimenGroupCmd, listeners: MessageEmitter) =>
      addSpecimenGroup(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: UpdateSpecimenGroupCmd, listeners: MessageEmitter) =>
      updateSpecimenGroup(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: RemoveSpecimenGroupCmd, listeners: MessageEmitter) =>
      removeSpecimenGroup(study, cmd, listeners)
    case _ =>
      throw new Error("invalid command received")

  }

  private def addSpecimenGroup(
    study: DisabledStudy,
    cmd: AddSpecimenGroupCmd,
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
    study: DisabledStudy,
    cmd: UpdateSpecimenGroupCmd,
    listeners: MessageEmitter): DomainValidation[SpecimenGroup] = {
    val specimenGroupId = new SpecimenGroupId(cmd.specimenGroupId)
    Entity.update(specimenGroupRepository.getByKey(specimenGroupId), specimenGroupId,
      cmd.expectedVersion) { prevSg =>
        val sg = SpecimenGroup(specimenGroupId, study.id, prevSg.version + 1, cmd.name,
          cmd.description, cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
          cmd.preservationTemperatureType, cmd.specimenType)
        specimenGroupRepository.updateMap(sg)
        listeners sendEvent StudySpecimenGroupUpdatedEvent(sg.studyId,
          sg.id, sg.name, sg.description, sg.units, sg.anatomicalSourceType, sg.preservationType,
          sg.preservationTemperatureType, sg.specimenType)
        sg.success
      }
  }

  private def removeSpecimenGroup(
    study: DisabledStudy,
    cmd: RemoveSpecimenGroupCmd,
    listeners: MessageEmitter): DomainValidation[SpecimenGroup] = {
    val specimenGroupId = new SpecimenGroupId(cmd.specimenGroupId)
    specimenGroupRepository.getByKey(specimenGroupId) match {
      case None =>
        DomainError("specimen group does not exist: %s" format cmd.specimenGroupId).fail
      case Some(sg) =>
        specimenGroupRepository.remove(sg)
        listeners sendEvent StudySpecimenGroupRemovedEvent(sg.studyId, sg.id)
        sg.success
    }
  }

}