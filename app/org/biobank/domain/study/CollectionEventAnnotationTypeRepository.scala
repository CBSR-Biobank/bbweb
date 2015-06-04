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
}

@Singleton
class CollectionEventAnnotationTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[AnnotationTypeId, CollectionEventAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[CollectionEventAnnotationType]
    with CollectionEventAnnotationTypeRepository {
}
