package domain.study

import domain._

import scalaz._
import Scalaz._

trait SpecimenLinkAnnotationTypeRepositoryComponent {

  val specimenLinkAnnotationTypeRepository: SpecimenLinkAnnotationTypeRepository

  trait SpecimenLinkAnnotationTypeRepository
    extends StudyAnnotationTypeRepository[SpecimenLinkAnnotationType] {
  }
}

trait SpecimenLinkAnnotationTypeRepositoryComponentImpl
  extends SpecimenLinkAnnotationTypeRepositoryComponent {

  override val specimenLinkAnnotationTypeRepository: SpecimenLinkAnnotationTypeRepository =
    new SpecimenLinkAnnotationTypeRepositoryImpl

  class SpecimenLinkAnnotationTypeRepositoryImpl
    extends ReadWriteRepository[AnnotationTypeId, SpecimenLinkAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[SpecimenLinkAnnotationType]
    with SpecimenLinkAnnotationTypeRepository {
  }
}