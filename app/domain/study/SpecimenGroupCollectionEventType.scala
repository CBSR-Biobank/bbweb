package domain.study

import domain._

/**
 * This is a value object.
 */
case class SpecimenGroupCollectionEventType(
  identity: String,
  sgId: SpecimenGroupId,
  cetId: CollectionEventTypeId,
  c: Int,
  a: BigDecimal) extends IdentifiedValueObject[String] {

  val id = identity
  val specimenGroupId = sgId
  val collectionEventTypeId = cetId
  val count = c
  val amount = a
}

object SpecimenGroupCollectionEventTypeIdentityService {

  def nextIdentity: String =
    java.util.UUID.randomUUID.toString.toUpperCase

}