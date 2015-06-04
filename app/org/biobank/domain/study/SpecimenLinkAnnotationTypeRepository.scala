package org.biobank.domain.study

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[SpecimenLinkAnnotationTypeRepositoryImpl])
trait SpecimenLinkAnnotationTypeRepository
    extends StudyAnnotationTypeRepository[SpecimenLinkAnnotationType]

@Singleton
class SpecimenLinkAnnotationTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[AnnotationTypeId, SpecimenLinkAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[SpecimenLinkAnnotationType]
    with SpecimenLinkAnnotationTypeRepository

