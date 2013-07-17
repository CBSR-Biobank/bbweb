package domain.study

import domain._

/**
 * This is a value object.
 */
case class SpecimenGroupCollectionEventType(
  specimenGroupId: SpecimenGroupId,
  count: Int,
  amount: BigDecimal) extends IdentifiedValueObject[String] {
}
