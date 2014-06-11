package org.biobank.domain.study

import org.biobank.domain._

import scalaz._
import Scalaz._

trait SpecimenLinkAnnotationTypeRepositoryComponent {

  val specimenLinkAnnotationTypeRepository: SpecimenLinkAnnotationTypeRepository

  trait SpecimenLinkAnnotationTypeRepository
    extends StudyAnnotationTypeRepository[SpecimenLinkAnnotationType]
}

trait SpecimenLinkAnnotationTypeRepositoryComponentImpl
  extends SpecimenLinkAnnotationTypeRepositoryComponent {

  override val specimenLinkAnnotationTypeRepository = new SpecimenLinkAnnotationTypeRepositoryImpl

  class SpecimenLinkAnnotationTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[AnnotationTypeId, SpecimenLinkAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[SpecimenLinkAnnotationType]
    with SpecimenLinkAnnotationTypeRepository
}
