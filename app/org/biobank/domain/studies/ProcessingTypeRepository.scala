package org.biobank.domain.studies

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[ProcessingTypeRepositoryImpl])
trait ProcessingTypeRepository
    extends ReadWriteRepositoryWithSlug[ProcessingTypeId, ProcessingType] {

  def withId(studyId: StudyId, processingTypeId: ProcessingTypeId)
      : DomainValidation[ProcessingType]

  def processingTypeInUse(processingType: ProcessingType): Boolean

  def allForStudy(studyId: StudyId): Set[ProcessingType]

}

@Singleton
class ProcessingTypeRepositoryImpl
    extends ReadWriteRepositoryRefImplWithSlug[ProcessingTypeId, ProcessingType](v => v.id)
    with ProcessingTypeRepository {
  import org.biobank.CommonValidations._

  def nextIdentity: ProcessingTypeId = new ProcessingTypeId(nextIdentityAsString)

  protected def notFound(id: ProcessingTypeId): IdNotFound = IdNotFound(s"processing type id: $id")

  protected def slugNotFound(slug: Slug): EntityCriteriaNotFound =
    EntityCriteriaNotFound(s"processing type slug: $slug")

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

  def processingTypeInUse(processingType: ProcessingType): Boolean = {
    // check if this processing type is an input for other processing types
    val found = getValues.find { pt =>
        (pt.specimenProcessing.input.definitionType == ProcessingType.processedDefinition) &&
        (pt.specimenProcessing.input.entityId == processingType.id)
      }

    !found.isEmpty
  }

  def allForStudy(studyId: StudyId): Set[ProcessingType] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }
}
