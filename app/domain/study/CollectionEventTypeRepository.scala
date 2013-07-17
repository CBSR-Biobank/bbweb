package domain.study

import domain._

import scalaz._
import Scalaz._

object CollectionEventTypeRepository
  extends ReadWriteRepository[CollectionEventTypeId, CollectionEventType](v => v.id) {

  def collectionEventTypeWithId(
    studyId: StudyId,
    ceventTypeId: CollectionEventTypeId): DomainValidation[CollectionEventType] = {
    getByKey(ceventTypeId) match {
      case Failure(x) =>
        DomainError(
          "collection event type does not exist: { studyId: %s, ceventTypeId: %s }".format(
            studyId, ceventTypeId)).fail
      case Success(cet) =>
        if (cet.studyId.equals(studyId)) cet.success
        else DomainError("study does not have collection event type").fail
    }
  }

  def allCollectionEventTypesForStudy(studyId: StudyId): Set[CollectionEventType] = {
    getValues.filter(x => x.studyId.equals(id)).toSet
  }

  private def nameAvailable(ceventType: CollectionEventType): DomainValidation[Boolean] = {
    val exists = getValues.exists { item =>
      item.studyId.equals(ceventType.studyId) &&
        item.name.equals(ceventType.name) &&
        !item.id.equals(ceventType.id)
    }

    if (exists)
      DomainError("specimen group with name already exists: %s" format ceventType.name).fail
    else
      true.success
  }

  def add(ceventType: CollectionEventType): DomainValidation[CollectionEventType] = {
    collectionEventTypeWithId(ceventType.studyId, ceventType.id) match {
      case Success(prevItem) =>
        DomainError("specimen group with ID already exists: %s" format ceventType.id).fail
      case Failure(x) =>
        for {
          nameValid <- nameAvailable(ceventType)
          item <- updateMap(ceventType).success
        } yield item
    }
  }

  def update(ceventType: CollectionEventType): DomainValidation[CollectionEventType] = {
    for {
      prevItem <- collectionEventTypeWithId(ceventType.studyId, ceventType.id)
      validVersion <- prevItem.requireVersion(Some(ceventType.version))
      nameValid <- nameAvailable(ceventType)
      updatedItem <- updateMap(ceventType).success
    } yield updatedItem
  }

  def remove(ceventType: CollectionEventType): DomainValidation[CollectionEventType] = {
    for {
      item <- collectionEventTypeWithId(ceventType.studyId, ceventType.id)
      validVersion <- item.requireVersion(Some(ceventType.version))
      removedItem <- removeFromMap(item).success
    } yield removedItem
  }

}
