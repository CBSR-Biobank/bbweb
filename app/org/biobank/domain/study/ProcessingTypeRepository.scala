package org.biobank.domain.study

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[ProcessingTypeRepositoryImpl])
trait ProcessingTypeRepository extends ReadWriteRepository [ProcessingTypeId, ProcessingType] {

  def withId(studyId: StudyId,processingTypeId: ProcessingTypeId)
      : DomainValidation[ProcessingType]

  def allForStudy(studyId: StudyId): Set[ProcessingType]

}

@Singleton
class ProcessingTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[ProcessingTypeId, ProcessingType](v => v.id)
    with ProcessingTypeRepository {
  import org.biobank.CommonValidations._

  def nextIdentity: ProcessingTypeId = new ProcessingTypeId(nextIdentityAsString)

  def notFound(id: ProcessingTypeId) = IdNotFound(s"processing type id: $id")

  override def getByKey(id: ProcessingTypeId): DomainValidation[ProcessingType] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def withId(studyId: StudyId, processingTypeId: ProcessingTypeId)
      : DomainValidation[ProcessingType] = {
    for {
      pt    <- getByKey(processingTypeId)
      valid <- {
        if (pt.studyId == studyId) {
          pt.successNel[String]
        } else {
          EntityCriteriaError(
            s"processing type not in study:{ processingTypeId: $processingTypeId, studyId: $studyId }")
            .failureNel[ProcessingType]
        }
      }
    } yield pt
  }

  def allForStudy(studyId: StudyId): Set[ProcessingType] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }
}
