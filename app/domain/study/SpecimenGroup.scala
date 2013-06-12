package domain.study

import infrastructure._
import domain._
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._

import scalaz._
import Scalaz._

case class SpecimenGroup(
  id: SpecimenGroupId,
  studyId: StudyId,
  version: Long = -1,
  name: String,
  description: String,
  units: String,
  anatomicalSourceType: AnatomicalSourceType,
  preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType,
  specimenType: SpecimenType) extends ConcurrencySafeEntity[SpecimenGroupId] {
}

object SpecimenGroup {

  def add(studyId: StudyId,
    name: String,
    description: String,
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType): DomainValidation[SpecimenGroup] =
    SpecimenGroup(SpecimenGroupIdentityService.nextIdentity, studyId, version = 0L, name,
      description, units, anatomicalSourceType, preservationType, preservationTemperatureType,
      specimenType).success

}
