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
    id: String,
    sg: SpecimenGroup,
    count: Int,
    amount: BigDecimal): SpecimenGroupCollectionEventType =
    SpecimenGroupCollectionEventType(id, sg.id, this.id, count, amount)

  def addAnnotationType(
    id: String,
    item: CollectionEventAnnotationType,
    required: Boolean): CollectionEventTypeAnnotationType =
    CollectionEventTypeAnnotationType(id, this.id, item.id, required)

  val toStringFormat = """{ id: %s, version: %d, studyId: %s, name: %s, description: %s,""" +
    """ recurring: %s }"""

  override def toString: String = {
    toStringFormat.format(id, version, studyId, name, description, recurring)
  }
}

