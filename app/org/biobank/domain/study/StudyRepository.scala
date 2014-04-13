package org.biobank.domain.study

import org.biobank.domain._

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait StudyRepositoryComponent {

  val studyRepository: StudyRepository

  trait StudyRepository {

    def nextIdentity: StudyId

    def nameAvailable(name: String): DomainValidation[Boolean]

    def allStudies(): Set[Study]

    def studyWithId(studyId: StudyId): DomainValidation[Study]

    def add(study: DisabledStudy): DomainValidation[DisabledStudy]

    def update(study: Study): DomainValidation[Study]
  }
}

trait StudyRepositoryComponentImpl extends StudyRepositoryComponent {

  override val studyRepository: StudyRepository = new StudyRepositoryImpl

  class StudyRepositoryImpl extends ReadWriteRepository[StudyId, Study](v => v.id) with StudyRepository {

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

    def add(study: DisabledStudy): DomainValidation[DisabledStudy] = {
      getByKey(study.id) match {
        case Success(prevItem) =>
          DomainError("study with ID already exists: %s" format study.id).failNel
        case Failure(err) =>
          for {
            nameValid <- nameAvailable(study.name)
            item <- updateMap(study).success
          } yield study
      }
    }

    def update(study: Study): DomainValidation[Study] = {
      for {
        prevStudy <- studyWithId(study.id)
        validVersion <- prevStudy.requireVersion(Some(study.version))
        nameValid <- nameAvailable(study.name)
        updatedItem <- DisabledStudy.create(
          study.id, prevStudy.version + 1, study.name, study.description)
        repoItem <- updateMap(updatedItem).success
      } yield updatedItem
    }
  }

}
