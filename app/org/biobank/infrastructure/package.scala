package org.biobank

package object infrastructure {

  trait HasIdentity {

    /** An event that includes the ID of the object it references. */
    val id: String

   }


  /** Used to define annotation types to be used by the [[org.biobank.domain.study.CollectionEvent]].
    *
    * @param annotationTypeId The ID of the [[org.biobank.domain.study.CollectionEventType]] this
    *        annotation belongs to.
    *
    * @param required If true, then a value for this annotation type is required when the collection
    *        event is entered.
    */
  case class CollectionEventTypeAnnotationType(
    annotationTypeId: String,
    required: Boolean)

  /** Used to define which types of specimens (i.e. which [[org.biobank.domain.study.SpecimenGroup]]s) need to
    * be collected with this type of collection event. A single specimen group can be used in multiple
    * collection event types.
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
