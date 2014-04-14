package org.biobank

package object infrastructure {

  trait Identity { val id: String }


  // common utility classes used in commands and events
  case class CollectionEventTypeAnnotationType(
    annotationTypeId: String,
    required: Boolean)

  case class CollectionEventTypeSpecimenGroup(
    specimenGroupId: String,
    maxCount: Int,
    amount: BigDecimal)

}
