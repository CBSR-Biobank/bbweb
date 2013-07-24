package domain.study

import domain._

/**
 * This is a value object.
 */
case class CollectionEventTypeSpecimenGroup(
  specimenGroupId: String,
  count: Int,
  amount: BigDecimal) {
}
