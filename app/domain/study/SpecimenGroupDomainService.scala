package domain.study

import domain._
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import service.Repository

import scalaz._
import Scalaz._

class SpecimenGroupDomainService(
  studyRepository: Repository[StudyId, Study],
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup]) {

  def addSpecimenGroup(studyId: StudyId, name: String, description: String, units: String,
    anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType): DomainValidation[SpecimenGroup] = {
    studyRepository.getByKey(studyId) match {
      case None => StudyDomainService.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyDomainService.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          // FIXME: lookup other IDs and verify them
          val studySpecimenGroups = specimenGroupRepository.getMap.filter(
            sg => sg._2.studyId.equals(study.id))
          study.addSpecimenGroup(studySpecimenGroups, studyId, name, description, units,
            anatomicalSourceType, preservationType, preservationTemperatureType, specimenType)
      }
    }
  }

  def updateSpecimenGroup(studyId: StudyId, specimenGroupId: SpecimenGroupId,
    expectedVersion: Option[Long], name: String, description: String, units: String,
    anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType): DomainValidation[SpecimenGroup] = {
    updateSpecimenGroup(studyId, specimenGroupId, expectedVersion) { sg =>
      SpecimenGroup(specimenGroupId, studyId, sg.version + 1, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).success
    }
  }

  def removeSpecimenGroup(studyId: StudyId, specimenGroupId: SpecimenGroupId,
    expectedVersion: Option[Long]): DomainValidation[SpecimenGroup] = {
    studyRepository.getByKey(studyId) match {
      case None => StudyDomainService.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyDomainService.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          specimenGroupRepository.getByKey(specimenGroupId) match {
            case None => StudyDomainService.noSuchStudy(studyId).fail
            case Some(sg) => sg.success
          }
      }
    }
  }

  private def updateSpecimenGroup(studyId: StudyId, specimenGroupId: SpecimenGroupId,
    expectedVersion: Option[Long])(f: SpecimenGroup => DomainValidation[SpecimenGroup]): DomainValidation[SpecimenGroup] = {
    studyRepository.getByKey(studyId) match {
      case None => StudyDomainService.noSuchStudy(studyId).fail
      case Some(sg) =>
        Entity.update(specimenGroupRepository.getByKey(specimenGroupId), specimenGroupId,
          expectedVersion)(f)
    }
  }

}