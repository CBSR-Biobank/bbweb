package org.biobank.domain.study

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz._
import scalaz.Scalaz._

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

  override val hashidsSalt = "biobank-processing-types"

  def nextIdentity: ProcessingTypeId = new ProcessingTypeId(nextIdentityAsString)

  def notFound(id: ProcessingTypeId) = IdNotFound(s"processing type id: $id")

  override def getByKey(id: ProcessingTypeId): DomainValidation[ProcessingType] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def withId(studyId: StudyId, processingTypeId: ProcessingTypeId)
      : DomainValidation[ProcessingType] = {
    getByKey(processingTypeId) match  {
      case Success(pt) => {
        if (pt.studyId == studyId) {
          pt.success
        } else {
          EntityCriteriaError(
            s"processing type not in study:{ processingTypeId: $processingTypeId, studyId: $studyId }")
            .failureNel
        }
      }
      case err => err
    }
  }

  def allForStudy(studyId: StudyId): Set[ProcessingType] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }
}
