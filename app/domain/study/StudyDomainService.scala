package domain.study

import domain._
import service.Repository
import infrastructure.commands._

import scalaz._
import Scalaz._

class StudyDomainService(studyRepository: Repository[StudyId, Study],
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup]) {
  import StudyDomainService._

  def addStudy(cmd: AddStudyCmd): DomainValidation[DisabledStudy] = {
    studyRepository.getValues.find(s => s.name.equals(cmd.name)) match {
      case Some(study) => DomainError("study with name already exists: %s" format cmd.name).fail
      case None => Study.add(cmd.name, cmd.description)
    }
  }

  def updateStudy(cmd: UpdateStudyCmd): DomainValidation[DisabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    updateDisabledStudy(studyId, cmd.expectedVersion) {
      study => DisabledStudy(studyId, study.version + 1, cmd.name, cmd.description).success
    }
  }

  def enableStudy(cmd: EnableStudyCmd): DomainValidation[EnabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    updateDisabledStudy(studyId, cmd.expectedVersion) { study =>
      val specimenGroupCount = specimenGroupRepository.getValues.filter(
        sg => sg.studyId.equals(studyId)).size
      study.enable(specimenGroupCount, 0)
    }
  }

  def disableStudy(cmd: DisableStudyCmd): DomainValidation[DisabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    updateEnabledStudy(studyId, cmd.expectedVersion) { study =>
      study.disable
    }
  }

  private def updateStudy[T <: Study](id: StudyId,
    expectedVersion: Option[Long])(f: Study => DomainValidation[T]): DomainValidation[T] =
    Entity.update(studyRepository.getByKey(id), id, expectedVersion)(f)

  private def updateDisabledStudy[T <: Study](id: StudyId,
    expectedVersion: Option[Long])(f: DisabledStudy => DomainValidation[T]): DomainValidation[T] =
    updateStudy(id, expectedVersion) { study =>
      study match {
        case study: DisabledStudy => f(study)
        case study: Study => notDisabledError(study.name).fail
      }
    }

  private def updateEnabledStudy[T <: Study](id: StudyId,
    expectedVersion: Option[Long])(f: EnabledStudy => DomainValidation[T]): DomainValidation[T] =
    updateStudy(id, expectedVersion) { study =>
      study match {
        case study: EnabledStudy => f(study)
        case study: Study => notEnabledError(study.name).fail
      }
    }

}

object StudyDomainService {
  def noSuchStudy(studyId: StudyId) =
    DomainError("no study with id: %s" format studyId)

  def notDisabledError(name: String) =
    DomainError("study is not disabled: %s" format name)

  def notEnabledError(name: String) =
    DomainError("study is not enabled: %s" format name)
}