package org.biobank.domain.study

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import org.biobank.TestData
import org.biobank.domain._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CollectionEventTypeRepositoryImpl])
trait CollectionEventTypeRepository
    extends ReadWriteRepositoryWithSlug[CollectionEventTypeId, CollectionEventType] {

  def withId(studyId: StudyId, ceventTypeId: CollectionEventTypeId)
      : DomainValidation[CollectionEventType]

  def allForStudy(studyId: StudyId): Set[CollectionEventType]

  def specimenSpecCanBeUpdated(studyId: StudyId, specimenGroupId: SpecimenGroupId): Boolean

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

  def notFound(id: CollectionEventTypeId): IdNotFound = IdNotFound(s"collection event type: $id")

  override def getByKey(id: CollectionEventTypeId): DomainValidation[CollectionEventType] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

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

  def specimenSpecCanBeUpdated(studyId: StudyId, specimenGroupId: SpecimenGroupId): Boolean = ???

  def annotationTypeInUse(annotationType: AnnotationType): Boolean = ???

}
