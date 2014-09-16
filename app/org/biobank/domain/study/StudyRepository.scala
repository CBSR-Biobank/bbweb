package org.biobank.domain.study

import org.biobank.domain._

import scalaz._
import Scalaz._

trait StudyRepositoryComponent {

  val studyRepository: StudyRepository

  trait StudyRepository extends ReadWriteRepository[StudyId, Study] {

    def allStudies(): Set[Study]

    def getDisabled(id: StudyId): DomainValidation[DisabledStudy]

    def getEnabled(id: StudyId): DomainValidation[EnabledStudy]

    def getRetired(id: StudyId): DomainValidation[RetiredStudy]

  }
}

trait StudyRepositoryComponentImpl extends StudyRepositoryComponent {

  override val studyRepository: StudyRepository = new StudyRepositoryImpl

  class StudyRepositoryImpl extends ReadWriteRepositoryRefImpl[StudyId, Study](v => v.id) with StudyRepository {

    def nextIdentity: StudyId = new StudyId(nextIdentityAsString)

    def allStudies(): Set[Study] = getValues.toSet

    def getDisabled(id: StudyId): DomainValidation[DisabledStudy] =
      getByKey(id).map(_.asInstanceOf[DisabledStudy])

    def getEnabled(id: StudyId): DomainValidation[EnabledStudy] =
      getByKey(id).map(_.asInstanceOf[EnabledStudy])

    def getRetired(id: StudyId): DomainValidation[RetiredStudy] =
      getByKey(id).map(_.asInstanceOf[RetiredStudy])

  }

}
