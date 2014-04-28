package org.biobank.domain.study

import org.biobank.domain._

import scalaz._
import Scalaz._

trait StudyRepositoryComponent {

  val studyRepository: StudyRepository

  trait StudyRepository extends ReadWriteRepository[StudyId, Study] {

    def nextIdentity: StudyId

    def allStudies(): Set[Study]

    def studyWithId(studyId: StudyId): DomainValidation[Study]
  }
}

trait StudyRepositoryComponentImpl extends StudyRepositoryComponent {

  override val studyRepository: StudyRepository = new StudyRepositoryImpl

  class StudyRepositoryImpl extends ReadWriteRepositoryRefImpl[StudyId, Study](v => v.id) with StudyRepository {

    def nextIdentity: StudyId = new StudyId(java.util.UUID.randomUUID.toString.toUpperCase)

    def allStudies(): Set[Study] = {
      getValues.toSet
    }

    def studyWithId(studyId: StudyId): DomainValidation[Study] = {
      getByKey(studyId)
    }

  }

}
