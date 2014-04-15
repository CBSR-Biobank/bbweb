package org.biobank

package object infrastructure {

  trait Identity { val id: String }


  // common utility classes used in commands and events
  case class CollectionEventTypeAnnotationType(
    annotationTypeId: String,
    required: Boolean)

  /** Used to define which types of specimens (i.e. which [[SpecimenGroup]]s) need to be collected with this
    * type of collection event. A single specimen group can be used in multiple collection event types.
    *
    * @param specimenGroupId The ID associated with the [[SpecimenGroup]].

    * @param maxCount The number of specimens required to be collected.

    * @param amount The amount of substance that is expected in each collected specimen, or None
    *        if there is no default amount. The unit on the amount is defined in the SpecimenGroup.
    *
    */
  case class CollectionEventTypeSpecimenGroup(
    specimenGroupId: String,
    maxCount: Int,
    amount: Option[BigDecimal])

}
