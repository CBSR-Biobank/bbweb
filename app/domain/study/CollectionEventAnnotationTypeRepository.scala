package domain.study

import domain._

import scalaz._
import Scalaz._

trait CollectionEventAnnotationTypeRepositoryComponent {

  val collectionEventAnnotationTypeRepository: CollectionEventAnnotationTypeRepository

  trait CollectionEventAnnotationTypeRepository
    extends StudyAnnotationTypeRepository[CollectionEventAnnotationType] {
  }
}

trait CollectionEventAnnotationTypeRepositoryComponentImpl
  extends CollectionEventAnnotationTypeRepositoryComponent {

  override val collectionEventAnnotationTypeRepository: CollectionEventAnnotationTypeRepository =
    new CollectionEventAnnotationTypeRepositoryImpl

  class CollectionEventAnnotationTypeRepositoryImpl
    extends ReadWriteRepository[AnnotationTypeId, CollectionEventAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[CollectionEventAnnotationType]
    with CollectionEventAnnotationTypeRepository {
  }
}