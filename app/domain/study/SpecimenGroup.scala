package domain.study

import domain.AnatomicalSourceId
import domain.PreservationId

case class SpecimenGroup(
  specimenGroupId: SpecimenGroupId,
  name: String,
  description: String,
  unit: String,
  anatomicalSourceId: AnatomicalSourceId,
  preservationId: PreservationId,
  specimenTypeId: SpecimenTypeId) {
}

object SpecimenGroup {

  // TODO: not sure yet if this is the right place for this method
  def nextIdentity: SpecimenGroupId =
    new SpecimenGroupId(java.util.UUID.randomUUID.toString.toUpperCase)

}