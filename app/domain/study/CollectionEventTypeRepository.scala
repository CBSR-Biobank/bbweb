package domain.study

import domain._

import scalaz._
import Scalaz._

object CollectionEventTypeRepository
  extends ReadWriteRepository[CollectionEventTypeId, CollectionEventType](v => v.id) {

  def allCollectionEventTypesForStudy(studyId: StudyId): Set[CollectionEventType] = {
    getValues.filter(x => x.studyId.equals(id)).toSet
  }

}