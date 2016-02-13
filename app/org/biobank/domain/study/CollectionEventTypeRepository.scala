package org.biobank.domain.study

import org.biobank.domain._

import com.google.inject.ImplementedBy
import org.slf4j.LoggerFactory

import javax.inject.Singleton
import scalaz._
import Scalaz._

@ImplementedBy(classOf[CollectionEventTypeRepositoryImpl])
trait CollectionEventTypeRepository
    extends ReadWriteRepository [CollectionEventTypeId, CollectionEventType] {

  def withId(studyId: StudyId, ceventTypeId: CollectionEventTypeId)
      : DomainValidation[CollectionEventType]

  def allForStudy(studyId: StudyId): Set[CollectionEventType]

  def specimenSpecCanBeUpdated(studyId: StudyId, specimenGroupId: SpecimenGroupId): Boolean

  def annotationTypeInUse(annotationType: AnnotationType): Boolean

}

@Singleton
class CollectionEventTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[CollectionEventTypeId, CollectionEventType](v => v.id)
    with CollectionEventTypeRepository {

  override val NotFoundError = "collection event type with id not found:"

  def nextIdentity: CollectionEventTypeId = new CollectionEventTypeId(nextIdentityAsString)

  def withId(studyId: StudyId, ceventTypeId: CollectionEventTypeId)
      : DomainValidation[CollectionEventType] = {
    getByKey(ceventTypeId).fold(
      err =>
      DomainError(
        s"collection event type does not exist: { studyId: $studyId, ceventTypeId: $ceventTypeId }")
        .failureNel,
      cet => {
        if (cet.studyId == studyId)
          cet.success
        else DomainError(
          s"study does not have collection event type:{ studyId: $studyId, ceventTypeId: $ceventTypeId }")
        .failureNel
      }
    )
  }

  def allForStudy(studyId: StudyId): Set[CollectionEventType] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }

  def specimenSpecCanBeUpdated(studyId: StudyId, specimenGroupId: SpecimenGroupId): Boolean = ???

  def annotationTypeInUse(annotationType: AnnotationType): Boolean = ???
}
