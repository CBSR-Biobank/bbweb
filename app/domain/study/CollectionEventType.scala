package domain.study

import domain._

import scalaz._
import Scalaz._

case class CollectionEventType private[study] (
  id: CollectionEventTypeId,
  studyId: StudyId,
  version: Long = -1,
  name: String,
  description: String,
  recurring: Boolean) extends ConcurrencySafeEntity[CollectionEventTypeId] {

}

object CollectionEventType {

  def add(
    studyId: StudyId,
    name: String,
    description: String,
    recurring: Boolean): DomainValidation[CollectionEventType] =
    CollectionEventType(CollectionEventTypeIdentityService.nextIdentity, studyId, version = 0L,
      name, description, recurring).success
}