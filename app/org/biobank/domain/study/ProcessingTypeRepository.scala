package org.biobank.domain.study

import org.biobank.domain._

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait ProcessingTypeRepository extends ReadWriteRepository [ProcessingTypeId, ProcessingType] {

  def withId(
    studyId: StudyId,
    processingTypeId: ProcessingTypeId): DomainValidation[ProcessingType]

  def allForStudy(studyId: StudyId): Set[ProcessingType]

}

class ProcessingTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[ProcessingTypeId, ProcessingType](v => v.id)
    with ProcessingTypeRepository {

  val log = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: ProcessingTypeId = new ProcessingTypeId(nextIdentityAsString)

  def withId(
    studyId: StudyId,
    processingTypeId: ProcessingTypeId): DomainValidation[ProcessingType] = {
    getByKey(processingTypeId).fold(
      err =>
      DomainError(
        s"processing type does not exist: { studyId: $studyId, processingTypeId: $processingTypeId }")
        .failNel,
      cet =>
      if (cet.studyId.equals(studyId)) {
        cet.success
      } else {
        DomainError(
          s"study does not have processing type:{ studyId: $studyId, processingTypeId: $processingTypeId }")
          .failNel
      }
    )
  }

  def allForStudy(studyId: StudyId): Set[ProcessingType] = {
    getValues.filter(x => x.studyId.equals(studyId)).toSet
  }
}
