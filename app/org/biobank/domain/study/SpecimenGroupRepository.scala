package org.biobank.domain.study

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import org.slf4j.LoggerFactory
import scalaz._
import Scalaz._

@ImplementedBy(classOf[SpecimenGroupRepositoryImpl])
trait SpecimenGroupRepository extends ReadWriteRepository[SpecimenGroupId, SpecimenGroup] {

  def allForStudy(studyId: StudyId): Set[SpecimenGroup]

  def withId(studyId: StudyId, specimenGroupId: SpecimenGroupId)
      : DomainValidation[SpecimenGroup]

}

@Singleton
class SpecimenGroupRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenGroupId, SpecimenGroup](v => v.id)
    with SpecimenGroupRepository {

  override val NotFoundError = "specimen group with id not found:"

  def nextIdentity: SpecimenGroupId = new SpecimenGroupId(nextIdentityAsString)

  def allForStudy(studyId: StudyId): Set[SpecimenGroup] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }

  def withId(studyId: StudyId, specimenGroupId: SpecimenGroupId)
      : DomainValidation[SpecimenGroup] = {
    getByKey(specimenGroupId).fold(
      err => DomainError(
        s"specimen group does not exist: { studyId: $studyId, specimenGroupId: $specimenGroupId }")
        .failureNel,
      sg => if (sg.studyId == studyId) {
        sg.success
      } else {
        DomainError(
          s"study does not have specimen group: { studyId: $studyId, specimenGroupId: $specimenGroupId }")
          .failureNel
      }
    )
  }
}
