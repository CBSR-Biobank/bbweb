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

  def withId(
    studyId: StudyId,
    ceventTypeId: CollectionEventTypeId): DomainValidation[CollectionEventType]

  def allForStudy(studyId: StudyId): Set[CollectionEventType]

  def specimenGroupCanBeUpdated(studyId: StudyId, specimenGroupId: SpecimenGroupId): Boolean

  def annotationTypeInUse(annotationType: CollectionEventAnnotationType): Boolean

}

@Singleton
class CollectionEventTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[CollectionEventTypeId, CollectionEventType](v => v.id)
    with CollectionEventTypeRepository {

  val log = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: CollectionEventTypeId = new CollectionEventTypeId(nextIdentityAsString)

  def withId(
    studyId: StudyId,
    ceventTypeId: CollectionEventTypeId): DomainValidation[CollectionEventType] = {
    getByKey(ceventTypeId).fold(
      err =>
      DomainError(
        s"collection event type does not exist: { studyId: $studyId, ceventTypeId: $ceventTypeId }")
        .failureNel,
      cet =>
      if (cet.studyId == studyId)
        cet.success
      else DomainError(
        s"study does not have collection event type:{ studyId: $studyId, ceventTypeId: $ceventTypeId }")
        .failureNel
    )
  }

  def allForStudy(studyId: StudyId): Set[CollectionEventType] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }

  def specimenGroupCanBeUpdated(studyId: StudyId, specimenGroupId: SpecimenGroupId): Boolean = {
    val sgId = specimenGroupId.toString
    val studyCeventTypes = getValues.filter(cet => cet.studyId == studyId)
    studyCeventTypes.exists(cet =>
      cet.specimenGroupData.exists(sgd => sgd.specimenGroupId == sgId))
  }

  def annotationTypeInUse(annotationType: CollectionEventAnnotationType): Boolean = {
    getValues.exists(cet =>
      cet.annotationTypeData.exists(atd => atd.annotationTypeId == annotationType.id.id))
  }
}
