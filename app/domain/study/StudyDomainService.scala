package domain.study

import domain._
import service.Repository

import scalaz._
import Scalaz._

class StudyDomainService(studyRepository: Repository[StudyId, Study],
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup]) {
  import StudyDomainService._

  def addStudy(name: String, description: String): DomainValidation[DisabledStudy] = {
    studyRepository.getValues.find(s => s.name.equals(name)) match {
      case Some(study) => DomainError("study with name already exists: %s" format name).fail
      case None => Study.add(name, description)
    }
  }

  def updateStudy(studyId: StudyId, expectedVersion: Option[Long], name: String,
    description: String): DomainValidation[DisabledStudy] = {
    updateDisabledStudy(studyId, expectedVersion) {
      study => DisabledStudy(studyId, study.version + 1, name, description).success
    }
  }

  def enableStudy(studyId: StudyId, expectedVersion: Option[Long]): DomainValidation[EnabledStudy] = {
    updateDisabledStudy(studyId, expectedVersion) { study =>
      val specimenGroupCount = specimenGroupRepository.getValues.filter(
        sg => sg.studyId.equals(studyId)).size
      study.enable(specimenGroupCount, 0)
    }
  }

  def disableStudy(studyId: StudyId, expectedVersion: Option[Long]): DomainValidation[DisabledStudy] = {
    updateEnabledStudy(studyId, expectedVersion) { study =>
      study.disable
    }
  }

  def updateDisabledStudy[T <: Study](id: StudyId,
    expectedVersion: Option[Long])(f: DisabledStudy => DomainValidation[T]): DomainValidation[T] =
    updateStudy(id, expectedVersion) { study =>
      study match {
        case study: DisabledStudy => f(study)
        case study: Study => notDisabledError(study.name).fail
      }
    }

  def updateEnabledStudy[T <: Study](id: StudyId,
    expectedVersion: Option[Long])(f: EnabledStudy => DomainValidation[T]): DomainValidation[T] =
    updateStudy(id, expectedVersion) { study =>
      study match {
        case study: EnabledStudy => f(study)
        case study: Study => notEnabledError(study.name).fail
      }
    }

  def updateStudy[T <: Study](id: StudyId,
    expectedVersion: Option[Long])(f: Study => DomainValidation[T]): DomainValidation[T] =
    Entity.update(studyRepository.getByKey(id), id, expectedVersion)(f)

}

object StudyDomainService {
  def noSuchStudy(studyId: StudyId) =
    DomainError("no study with id: %s" format studyId)

  def notDisabledError(name: String) =
    DomainError("study is not disabled: %s" format name)

  def notEnabledError(name: String) =
    DomainError("study is not enabled: %s" format name)
}