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
  description: Option[String],
  recurring: Boolean,
  specimenGroupData: Set[CollectionEventTypeSpecimenGroup],
  annotationTypeData: Set[CollectionEventTypeAnnotationType])
  extends ConcurrencySafeEntity[CollectionEventTypeId]
  with HasName with HasDescriptionOption {

  val toStringFormat = """CollectionEventType:{ id: %s, version: %d, studyId: %s, name: %s, description: %s,""" +
    """ recurring: %s, specimenGroupData: { %s },  annotationTypeData: { %s }}"""

  override def toString: String = {
    toStringFormat.format(id, version, studyId, name, description, recurring, specimenGroupData, annotationTypeData)
  }
}

