package org.biobank.domain.study

import org.biobank.domain._

import scalaz._
import Scalaz._

trait SpecimenLinkAnnotationTypeRepository
    extends StudyAnnotationTypeRepository[SpecimenLinkAnnotationType]

class SpecimenLinkAnnotationTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[AnnotationTypeId, SpecimenLinkAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[SpecimenLinkAnnotationType]
    with SpecimenLinkAnnotationTypeRepository

