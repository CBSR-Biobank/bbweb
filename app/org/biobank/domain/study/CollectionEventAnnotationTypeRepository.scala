package org.biobank.domain.study

import org.biobank.domain.{
  AnnotationTypeId,
  ReadWriteRepositoryRefImpl
}

import javax.inject.Singleton
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[CollectionEventAnnotationTypeRepositoryImpl])
trait CollectionEventAnnotationTypeRepository
    extends StudyAnnotationTypeRepository[CollectionEventAnnotationType] {

  def allForCollectionEventType(collectionEventType: CollectionEventType)
      : Set[CollectionEventAnnotationType]
}


@Singleton
class CollectionEventAnnotationTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[AnnotationTypeId, CollectionEventAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[CollectionEventAnnotationType]
    with CollectionEventAnnotationTypeRepository {

  def allForCollectionEventType(collectionEventType: CollectionEventType)
      : Set[CollectionEventAnnotationType] = {
    val annotationTypeIds = collectionEventType.annotationTypeData.map { atDataItem =>
      AnnotationTypeId(atDataItem.annotationTypeId)
    }

    getValues.filter { x => annotationTypeIds.contains(x.id) }.toSet
  }

}
