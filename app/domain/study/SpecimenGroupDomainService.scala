package domain.study

import domain._
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import service.Repository

import infrastructure.commands._

import scalaz._
import Scalaz._

class SpecimenGroupDomainService(
  studyRepository: Repository[StudyId, Study],
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup]) {

  def addSpecimenGroup(cmd: AddSpecimenGroupCmd): DomainValidation[SpecimenGroup] = {
    val studyId = new StudyId(cmd.studyId)
    studyRepository.getByKey(studyId) match {
      case None => StudyDomainService.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyDomainService.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          val studySpecimenGroups = specimenGroupRepository.getMap.filter(
            sg => sg._2.studyId.equals(study.id))
          study.addSpecimenGroup(studySpecimenGroups, cmd)
      }
    }
  }

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): DomainValidation[SpecimenGroup] = {
    val studyId = new StudyId(cmd.studyId)
    studyRepository.getByKey(studyId) match {
      case None => StudyDomainService.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyDomainService.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          val specimenGroupId = new SpecimenGroupId(cmd.specimenGroupId)
          Entity.update(specimenGroupRepository.getByKey(specimenGroupId), specimenGroupId,
            cmd.expectedVersion) { sg =>
              SpecimenGroup(specimenGroupId, studyId, sg.version + 1, cmd.name, cmd.description,
                cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
                cmd.preservationTemperatureType, cmd.specimenType).success
            }
      }
    }
  }

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd): DomainValidation[SpecimenGroup] = {
    val studyId = new StudyId(cmd.studyId)
    studyRepository.getByKey(studyId) match {
      case None => StudyDomainService.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyDomainService.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          val specimenGroupId = new SpecimenGroupId(cmd.specimenGroupId)
          specimenGroupRepository.getByKey(specimenGroupId) match {
            case None =>
              DomainError("speciment group does not exist: %s" format cmd.specimenGroupId).fail
            case Some(sg) => sg.success
          }
      }
    }
  }

}