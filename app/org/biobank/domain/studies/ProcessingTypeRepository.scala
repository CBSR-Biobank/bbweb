package org.biobank.domain.studies

import org.biobank.domain._

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import org.biobank.TestData
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[ProcessingTypeRepositoryImpl])
trait ProcessingTypeRepository
    extends ReadWriteRepositoryWithSlug[ProcessingTypeId, ProcessingType] {

  def withId(studyId: StudyId, processingTypeId: ProcessingTypeId)
      : DomainValidation[ProcessingType]

  def processingTypeInUse(id: ProcessingTypeId): Boolean

  def allForStudy(studyId: StudyId): Set[ProcessingType]

}

@Singleton
class ProcessingTypeRepositoryImpl @Inject() (val testData: TestData)
    extends ReadWriteRepositoryRefImplWithSlug[ProcessingTypeId, ProcessingType](v => v.id)
    with ProcessingTypeRepository {
  import org.biobank.CommonValidations._

  override def init(): Unit = {
    super.init()
    testData.testProcessingTypes.foreach(put)
  }

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

  def processingTypeInUse(id: ProcessingTypeId): Boolean = {
    // check if this processing type is an input for other processing types
    val found = getValues.find { pt =>
        (pt.input.definitionType == ProcessingType.processedDefinition) &&
        (pt.input.entityId == id.id)
      }

    !found.isEmpty
  }

  def allForStudy(studyId: StudyId): Set[ProcessingType] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }
}
