package domain.study

import domain.AmatomicalSourceId
import domain.PreservationId

class SpecimenGroup private[study] (
  specimenGroupId: SpecimenGroupId,
  name: String,
  description: String,
  unit: String,
  anatomicalSourceId: AmatomicalSourceId,
  preservationId: PreservationId,
  specimenTypeId: SpecimenTypeId) {
}

object SpecimenGroup {

  // TODO: not sure yet if this is the right place for this method
  def nextIdentity: SpecimenGroupId =
    new SpecimenGroupId(java.util.UUID.randomUUID.toString.toUpperCase)

}