package org.biobank.domain.study

import org.biobank.domain._

trait ParticipantAnnotationTypeRepository
    extends StudyAnnotationTypeRepository[ParticipantAnnotationType]

class ParticipantAnnotationTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[AnnotationTypeId, ParticipantAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[ParticipantAnnotationType]
    with ParticipantAnnotationTypeRepository
