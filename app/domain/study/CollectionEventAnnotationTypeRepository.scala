package domain.study

import domain._

import scalaz._
import Scalaz._

object CollectionEventAnnotationTypeRepository
  extends ReadWriteRepository[AnnotationTypeId, CollectionEventAnnotationType](v => v.id) {

  def annotationTypeWithId(
    studyId: StudyId,
    annotationTypeId: AnnotationTypeId): DomainValidation[CollectionEventAnnotationType] = {
    getByKey(annotationTypeId) match {
      case Failure(x) =>
        DomainError(
          "collection event annotation type does not exist: { studyId: %s, annotationTypeId: %s }".format(
            studyId, annotationTypeId)).fail
      case Success(annotType) =>
        if (annotType.studyId.equals(studyId)) annotType.success
        else DomainError(
          "study does not have collection event annotation type: { studyId: %s, annotationTypeId: %s }".format(
            studyId, annotationTypeId)).fail
    }
  }

  def allCollectionEventAnnotationTypesForStudy(studyId: StudyId): Set[CollectionEventAnnotationType] = {
    getValues.filter(x => x.studyId.equals(id)).toSet
  }

  private def nameAvailable(annotationType: CollectionEventAnnotationType): DomainValidation[Boolean] = {
    val exists = getValues.exists { item =>
      item.studyId.equals(annotationType.studyId) &&
        item.name.equals(annotationType.name) &&
        !item.id.equals(annotationType.id)
    }

    if (exists)
      DomainError("collection event annotation type with name already exists: %s" format annotationType.name).fail
    else
      true.success
  }

  def add(annotationType: CollectionEventAnnotationType): DomainValidation[CollectionEventAnnotationType] = {
    annotationTypeWithId(annotationType.studyId, annotationType.id) match {
      case Success(prevItem) =>
        DomainError("collection event annotation type with ID already exists: %s" format annotationType.id).fail
      case Failure(x) =>
        for {
          nameValid <- nameAvailable(annotationType)
          item <- updateMap(annotationType).success
        } yield item
    }
  }

  def update(annotationType: CollectionEventAnnotationType): DomainValidation[CollectionEventAnnotationType] = {
    for {
      prevItem <- annotationTypeWithId(annotationType.studyId, annotationType.id)
      validVersion <- prevItem.requireVersion(Some(annotationType.version))
      nameValid <- nameAvailable(annotationType)
      updatedItem <- CollectionEventAnnotationType(
        annotationType.id, annotationType.version + 1, annotationType.studyId, annotationType.name,
        annotationType.description, annotationType.valueType, annotationType.maxValueCount,
        annotationType.options).success
      repoItem <- updateMap(updatedItem).success
    } yield updatedItem
  }

  def remove(annotationType: CollectionEventAnnotationType): DomainValidation[CollectionEventAnnotationType] = {
    for {
      item <- annotationTypeWithId(annotationType.studyId, annotationType.id)
      validVersion <- item.requireVersion(Some(annotationType.version))
      removedItem <- removeFromMap(item).success
    } yield removedItem

  }

}
