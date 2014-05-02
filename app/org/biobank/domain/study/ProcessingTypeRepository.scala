package org.biobank.domain.study

import org.biobank.domain._

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait ProcessingTypeRepositoryComponent {

  val processingTypeRepository: ProcessingTypeRepository

  trait ProcessingTypeRepository extends ReadWriteRepository [ProcessingTypeId, ProcessingType] {

    def nextIdentity: ProcessingTypeId

    def withId(
      studyId: StudyId,
      processingTypeId: ProcessingTypeId): DomainValidation[ProcessingType]

    def allForStudy(studyId: StudyId): Set[ProcessingType]

  }
}

trait ProcessingTypeRepositoryComponentImpl extends ProcessingTypeRepositoryComponent {

  override val processingTypeRepository: ProcessingTypeRepository = new ProcessingTypeRepositoryImpl

  class ProcessingTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[ProcessingTypeId, ProcessingType](v => v.id)
    with ProcessingTypeRepository {

    val log = LoggerFactory.getLogger(this.getClass)

    def nextIdentity: ProcessingTypeId =
      new ProcessingTypeId(java.util.UUID.randomUUID.toString.toUpperCase)

    def withId(
      studyId: StudyId,
      processingTypeId: ProcessingTypeId): DomainValidation[ProcessingType] = {
      getByKey(processingTypeId) match {
        case Failure(err) =>
          DomainError(
            s"processing type does not exist: { studyId: $studyId, processingTypeId: $processingTypeId }")
	    .failNel
        case Success(cet) =>
          if (cet.studyId.equals(studyId))
            cet.success
          else DomainError(
            "study does not have processing type:{ studyId: $studyId, processingTypeId: $processingTypeId }")
              .failNel
      }
    }

    def allForStudy(studyId: StudyId): Set[ProcessingType] = {
      getValues.filter(x => x.studyId.equals(studyId)).toSet
    }
  }
}
