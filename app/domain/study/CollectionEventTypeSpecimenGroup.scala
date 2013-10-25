package domain.study

import domain._

/**
 * This is a value object.
 */
case class CollectionEventTypeSpecimenGroup(
  specimenGroupId: String,
  maxCount: Int,
  amount: BigDecimal) {
}
