package domain.study

import infrastructure._
import domain._

import scalaz._
import Scalaz._

case class CollectionEventType(
  id: CollectionEventTypeId,
  version: Long = -1,
  studyId: StudyId,
  name: String,
  description: String,
  recurring: Boolean) extends ConcurrencySafeEntity[CollectionEventTypeId] {

  def addSpecimenGroup(
    sg: SpecimenGroup,
    count: Int,
    amount: BigDecimal): SpecimenGroupCollectionEventType =
    SpecimenGroupCollectionEventType(
      SpecimenGroupCollectionEventTypeIdentityService.nextIdentity,
      sg.id, this.id, count, amount)

  def addAnnotationType(
    item: CollectionEventAnnotationType,
    required: Boolean): CollectionEventTypeAnnotationType =
    CollectionEventTypeAnnotationType(
      CollectionEventTypeAnnotationTypeIdentityService.nextIdentity,
      this.id, item.id, required)
}

object CollectionEventType {

  def add(
    studyId: StudyId,
    name: String,
    description: String,
    recurring: Boolean): DomainValidation[CollectionEventType] =
    CollectionEventType(CollectionEventTypeIdentityService.nextIdentity, version = 0L,
      studyId, name, description, recurring).success
}
