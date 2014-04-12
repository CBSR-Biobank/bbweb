package org.biobank.domain.study

import org.biobank.domain._

/**
 * This is a value object.
 */
case class CollectionEventTypeSpecimenGroup(
  specimenGroupId: String,
  maxCount: Int,
  amount: BigDecimal) {
}
