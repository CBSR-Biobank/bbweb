package domain.study

import domain._

import scalaz._
import Scalaz._

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

  override def equals(other: Any) =
    other match {
      case that: SpecimenGroupCollectionEventType =>
        (this.specimenGroupId.equals(that.specimenGroupId)
          && this.specimenGroupId.equals(that.collectionEventTypeId)
          && this.count.equals(that.count)
          && this.amount.equals(that.amount))
      case _ => false
    }
}

object SpecimenGroupCollectionEventTypeIdentityService {

  def nextIdentity: String =
    java.util.UUID.randomUUID.toString.toUpperCase

}