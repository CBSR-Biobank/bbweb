package domain.study

import domain._
import service.commands._

import scalaz._
import scalaz.Scalaz._

case class SpecimenGroup(
  id: SpecimenGroupId,
  studyId: StudyId,
  version: Long = -1,
  name: String,
  description: String,
  units: String,
  anatomicalSourceId: AnatomicalSourceId,
  preservationId: PreservationId,
  specimenTypeId: SpecimenTypeId) extends ConcurrencySafeEntity[SpecimenGroupId] {
}

object SpecimenGroup {

  def add(studyId: StudyId, name: String, description: String, units: String,
    anatomicalSourceId: AnatomicalSourceId, preservationId: PreservationId,
    specimenTypeId: SpecimenTypeId): DomainValidation[SpecimenGroup] =
    SpecimenGroup(SpecimenGroupIdentityService.nextIdentity, studyId, version = 0L, name,
      description, units, anatomicalSourceId, preservationId, specimenTypeId).success

}
