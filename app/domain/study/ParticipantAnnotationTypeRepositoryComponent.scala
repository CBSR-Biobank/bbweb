package domain.study

import domain._

import scalaz._
import Scalaz._

trait ParticipantAnnotationTypeRepositoryComponent {

  val participantAnnotationTypeRepository: ParticipantAnnotationTypeRepository

  trait ParticipantAnnotationTypeRepository
    extends StudyAnnotationTypeRepository[ParticipantAnnotationType] {

  }

}

trait ParticipantAnnotationTypeRepositoryComponentImpl
  extends ParticipantAnnotationTypeRepositoryComponent {

  override val participantAnnotationTypeRepository: ParticipantAnnotationTypeRepository =
    new ParticipantAnnotationTypeRepositoryImpl

  class ParticipantAnnotationTypeRepositoryImpl
    extends ReadWriteRepository[AnnotationTypeId, ParticipantAnnotationType](v => v.id)
    with StudyAnnotationTypeRepositoryImpl[ParticipantAnnotationType]
    with ParticipantAnnotationTypeRepository {

  }
}
