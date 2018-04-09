package org.biobank.domain.studies

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import org.biobank.TestData
import org.biobank.domain._
import org.biobank.domain.annotations._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CollectionEventTypeRepositoryImpl])
trait CollectionEventTypeRepository
    extends ReadWriteRepositoryWithSlug[CollectionEventTypeId, CollectionEventType] {

  def withId(studyId: StudyId, ceventTypeId: CollectionEventTypeId)
      : DomainValidation[CollectionEventType]

  def allForStudy(studyId: StudyId): Set[CollectionEventType]

  def specimenDefinitionCanBeUpdated(studyId: StudyId, specimenDefinitionId: SpecimenDefinitionId): Boolean

  def annotationTypeInUse(annotationType: AnnotationType): Boolean

}

@Singleton
class CollectionEventTypeRepositoryImpl @Inject() (val testData: TestData)
    extends ReadWriteRepositoryRefImplWithSlug[CollectionEventTypeId, CollectionEventType](v => v.id)
    with CollectionEventTypeRepository {
  import org.biobank.CommonValidations._

  override def init(): Unit = {
    super.init()
    testData.testEventTypes.foreach(put)
  }

  def nextIdentity: CollectionEventTypeId = new CollectionEventTypeId(nextIdentityAsString)

  protected def notFound(id: CollectionEventTypeId): IdNotFound = IdNotFound(s"collection event type: $id")

  protected def slugNotFound(slug: Slug): EntityCriteriaNotFound =
    EntityCriteriaNotFound(s"collection event type slug: $slug")

  def withId(studyId: StudyId, ceventTypeId: CollectionEventTypeId)
      : DomainValidation[CollectionEventType] = {
    for {
      cet          <- getByKey(ceventTypeId)
      validStudyId <- {
        if (cet.studyId == studyId) {
          cet.successNel[String]
        } else {
          EntityCriteriaError(
            s"collection event type not in study:{ ceventTypeId: $ceventTypeId, studyId: $studyId }")
            .failureNel[CollectionEventType]
        }
      }
    } yield cet
  }

  def allForStudy(studyId: StudyId): Set[CollectionEventType] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }

  def specimenDefinitionCanBeUpdated(studyId: StudyId, specimenDefinitionId: SpecimenDefinitionId)
      : Boolean = {
    ???
  }

  def annotationTypeInUse(annotationType: AnnotationType): Boolean = ???

}
