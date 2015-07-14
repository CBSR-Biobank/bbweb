package org.biobank.domain.study

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import org.biobank.domain._

@ImplementedBy(classOf[ParticipantAnnotationTypeRepositoryImpl])
trait ParticipantAnnotationTypeRepository
    extends StudyAnnotationTypeRepository[ParticipantAnnotationType]

@Singleton
class ParticipantAnnotationTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[AnnotationTypeId, ParticipantAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[ParticipantAnnotationType]
    with ParticipantAnnotationTypeRepository {

  override val NotFoundError = "participant annotation type with id not found:"

}
