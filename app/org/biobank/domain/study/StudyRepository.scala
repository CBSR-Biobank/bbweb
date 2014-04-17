package org.biobank.domain.study

import org.biobank.domain._

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait StudyRepositoryComponent {

  val studyRepository: StudyRepository

  trait StudyRepository extends ReadWriteRepository[StudyId, Study] {

    def nextIdentity: StudyId

    def nameAvailable(name: String): DomainValidation[Boolean]

    def allStudies(): Set[Study]

    def studyWithId(studyId: StudyId): DomainValidation[Study]
  }
}

trait StudyRepositoryComponentImpl extends StudyRepositoryComponent {

  override val studyRepository: StudyRepository = new StudyRepositoryImpl

  class StudyRepositoryImpl extends ReadWriteRepositoryRefImpl[StudyId, Study](v => v.id) with StudyRepository {

    val log = LoggerFactory.getLogger(this.getClass)

    def nextIdentity: StudyId = new StudyId(java.util.UUID.randomUUID.toString.toUpperCase)

    def allStudies(): Set[Study] = {
      getValues.toSet
    }

    def studyWithId(studyId: StudyId): DomainValidation[Study] = {
      getByKey(studyId)
    }

    def nameAvailable(name: String): DomainValidation[Boolean] = {
      val exists = getValues.exists { item =>
        item.name.equals(name)
      }

      if (exists) {
        DomainError(s"study with name already exists: $name").failNel
      } else {
        true.successNel
      }
    }

  }

}
