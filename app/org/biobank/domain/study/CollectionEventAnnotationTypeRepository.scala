package org.biobank.domain.study

import org.biobank.domain.{
  AnnotationTypeId,
  ReadWriteRepositoryRefImpl
}

trait CollectionEventAnnotationTypeRepository
    extends StudyAnnotationTypeRepository[CollectionEventAnnotationType] {
}

class CollectionEventAnnotationTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[AnnotationTypeId, CollectionEventAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[CollectionEventAnnotationType]
    with CollectionEventAnnotationTypeRepository {
}
