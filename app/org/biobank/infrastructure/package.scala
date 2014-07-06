package org.biobank

package infrastructure {

  trait HasIdentity {

    /** An event that includes the ID of the object it references. */
    val id: String

   }

  /** Used to define annotation types associate annotation types to objects that use them.
    *
    */
  sealed trait AnnotationTypeData {

    /** @param annotationTypeId The ID of the corresponding  annotation type. */
    val annotationTypeId: String

    /** @param name A copy of the name of the corresponding annotation type. Read only. */
    val name: String

    /** When true, then a value for this annotation type is required when the collection event is entered.
      */
    val required: Boolean
  }

  /** Used to define annotation types to be used by a [[org.biobank.domain.study.CollectionEventType]]. */
  case class CollectionEventTypeAnnotationTypeData(
    annotationTypeId: String,
    name: String,
    required: Boolean)
      extends AnnotationTypeData

  /** Used to define annotation types to be used by a [[org.biobank.domain.study.SpecimenLinkType]]. */
  case class SpecimenLinkTypeAnnotationTypeData(
    annotationTypeId: String,
    name: String,
    required: Boolean)
      extends AnnotationTypeData

  /** Used to define which types of specimens (i.e. which [[org.biobank.domain.study.SpecimenGroup]]s) need to
    * be collected with this type of collection event. A single specimen group can be used in multiple
    * collection event types.
    *
    * @param specimenGroupId The ID associated with the [[org.biobank.domain.study.SpecimenGroup]].
    *
    * @param name A copy of the name of the corresponding specimen group. Read only.
    *
    * @param maxCount The number of specimens required to be collected.
    *
    * @param amount The amount of substance that is expected in each collected specimen, or None
    *        if there is no default amount. The unit on the amount is defined in the SpecimenGroup.
    *
    * @param units A copy of the units field from the specimen group. Read only.
    *
    */
  case class CollectionEventTypeSpecimenGroupData(
    specimenGroupId: String,
    name: String,
    maxCount: Int,
    amount: Option[BigDecimal],
    units: String)

}
