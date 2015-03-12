package org.biobank.domain.study

import org.biobank.domain._

trait StudyRepository extends ReadWriteRepository[StudyId, Study] {

  def allStudies(): Set[Study]

  def getDisabled(id: StudyId): DomainValidation[DisabledStudy]

  def getEnabled(id: StudyId): DomainValidation[EnabledStudy]

  def getRetired(id: StudyId): DomainValidation[RetiredStudy]

}

class StudyRepositoryImpl extends ReadWriteRepositoryRefImpl[StudyId, Study](v => v.id) with StudyRepository {

  def nextIdentity: StudyId = new StudyId(nextIdentityAsString)

  def allStudies(): Set[Study] = getValues.toSet

  def getDisabled(id: StudyId): DomainValidation[DisabledStudy] =
    getByKey(id).mapTo[DisabledStudy]

  def getEnabled(id: StudyId): DomainValidation[EnabledStudy] =
    getByKey(id).mapTo[EnabledStudy]

  def getRetired(id: StudyId): DomainValidation[RetiredStudy] =
    getByKey(id).mapTo[RetiredStudy]

}
