package org.biobank.domain.study

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._

@ImplementedBy(classOf[StudyRepositoryImpl])
trait StudyRepository extends ReadWriteRepository[StudyId, Study] {

  def allStudies(): Set[Study]

  def getDisabled(id: StudyId): DomainValidation[DisabledStudy]

  def getEnabled(id: StudyId): DomainValidation[EnabledStudy]

  def getRetired(id: StudyId): DomainValidation[RetiredStudy]

}

@Singleton
class StudyRepositoryImpl
    extends ReadWriteRepositoryRefImpl[StudyId, Study](v => v.id)
    with StudyRepository {

  override val NotFoundError = "study with id not found:"

  def nextIdentity: StudyId = new StudyId(nextIdentityAsString)

  def allStudies(): Set[Study] = getValues.toSet

  def getDisabled(id: StudyId): DomainValidation[DisabledStudy] = {
    getByKey(id).fold(
      err => DomainError(s"study with id does not exist: $id").failureNel,
      study => study match {
        case study: DisabledStudy => study.success
        case study => DomainError(s"study is not disabled: $study").failureNel
      }
    )
  }

  def getEnabled(id: StudyId): DomainValidation[EnabledStudy] = {
    getByKey(id).fold(
      err => DomainError(s"study with id does not exist: $id").failureNel,
      study => study match {
        case study: EnabledStudy => study.success
        case study => DomainError(s"study is not enabled: $study").failureNel
      }
    )
  }

  def getRetired(id: StudyId): DomainValidation[RetiredStudy] = {
    getByKey(id).fold(
      err => DomainError(s"study with id does not exist: $id").failureNel,
      study => study match {
        case study: RetiredStudy => study.success
        case study => DomainError(s"study is not retired: $study").failureNel
      }
    )
  }

}
