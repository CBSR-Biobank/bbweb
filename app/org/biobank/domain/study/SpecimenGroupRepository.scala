package org.biobank.domain.study

import org.biobank.domain._

import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._

trait SpecimenGroupRepository extends ReadWriteRepository[SpecimenGroupId, SpecimenGroup] {

  def allForStudy(studyId: StudyId): Set[SpecimenGroup]

  def withId(
    studyId: StudyId,
    specimenGroupId: SpecimenGroupId): DomainValidation[SpecimenGroup]

}

class SpecimenGroupRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenGroupId, SpecimenGroup](v => v.id)
    with SpecimenGroupRepository {

  val log = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: SpecimenGroupId = new SpecimenGroupId(nextIdentityAsString)

  def allForStudy(studyId: StudyId): Set[SpecimenGroup] = {
    getValues.filter(x => x.studyId.equals(studyId)).toSet
  }

  def withId(
    studyId: StudyId,
    specimenGroupId: SpecimenGroupId): DomainValidation[SpecimenGroup] = {
    getByKey(specimenGroupId).fold(
      err => DomainError(
        s"specimen group does not exist: { studyId: $studyId, specimenGroupId: $specimenGroupId }")
        .failNel,
      sg => if (sg.studyId.equals(studyId)) {
        sg.success
      } else {
        DomainError(
          s"study does not have specimen group: { studyId: $studyId, specimenGroupId: $specimenGroupId }")
          .failNel
      }
    )
  }
}
