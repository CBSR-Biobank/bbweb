package org.biobank.domain.study

import org.biobank.domain._

import scalaz._
import Scalaz._

trait StudyRepositoryComponent {

  val studyRepository: StudyRepository

  trait StudyRepository extends ReadWriteRepository[StudyId, Study] {

    def allStudies(): Set[Study]
  }
}

trait StudyRepositoryComponentImpl extends StudyRepositoryComponent {

  override val studyRepository: StudyRepository = new StudyRepositoryImpl

  class StudyRepositoryImpl extends ReadWriteRepositoryRefImpl[StudyId, Study](v => v.id) with StudyRepository {

    def nextIdentity: StudyId = new StudyId(nextIdentityAsString)

    def allStudies(): Set[Study] = getValues.toSet

  }

}
